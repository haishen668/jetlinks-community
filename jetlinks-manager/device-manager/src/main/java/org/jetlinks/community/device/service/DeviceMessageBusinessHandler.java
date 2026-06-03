package org.jetlinks.community.device.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.MapUtils;
import org.hswebframework.ezorm.rdb.mapping.ReactiveRepository;
import org.jetlinks.community.PropertyConstants;
import org.jetlinks.community.buffer.PersistenceBuffer;
import org.jetlinks.community.configure.cluster.Cluster;
import org.jetlinks.community.device.entity.DeviceCardEntity;
import org.jetlinks.community.device.entity.DeviceInstanceEntity;
import org.jetlinks.community.device.entity.DeviceTagEntity;
import org.jetlinks.community.device.enums.DeviceFeature;
import org.jetlinks.community.device.enums.DeviceState;
import org.jetlinks.community.gateway.annotation.Subscribe;
import org.jetlinks.community.utils.ErrorUtils;
import org.jetlinks.core.device.DeviceConfigKey;
import org.jetlinks.core.device.DeviceOperator;
import org.jetlinks.core.device.DeviceRegistry;
import org.jetlinks.core.event.EventBus;
import org.jetlinks.core.event.Subscription;
import org.jetlinks.core.message.*;
import org.jetlinks.core.message.property.ReportPropertyMessage;
import org.jetlinks.core.metadata.DeviceMetadata;
import org.jetlinks.core.utils.FluxUtils;
import org.jetlinks.core.utils.Reactors;
import org.jetlinks.reactor.ql.utils.CastUtils;
import org.jetlinks.supports.official.JetLinksDeviceMetadataCodec;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.BufferOverflowStrategy;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

@Component
@AllArgsConstructor
@Slf4j
public class DeviceMessageBusinessHandler {

    private final LocalDeviceInstanceService deviceService;

    private final LocalDeviceProductService productService;

    private final DeviceRegistry registry;

    private final ReactiveRepository<DeviceTagEntity, String> tagRepository;

    private final ReactiveRepository<DeviceCardEntity, String> cardRepository;

    private final EventBus eventBus;

    private final ReactiveRedisOperations<String, String> redis;

    private final Disposable.Composite disposable = Disposables.composite();

    /**
     * 自动注册设备信息
     * <p>
     * 设备消息的header需要包含{@code deviceName},{@code productId}才会自动注册.、
     *
     * @param message 注册消息
     * @return 注册后的设备操作接口
     */
    private Mono<DeviceOperator> doAutoRegister(DeviceRegisterMessage message) {
        //自动注册
        return Mono
            .zip(
                //T1. 设备ID
                Mono.justOrEmpty(message.getDeviceId()),
                //T2. 设备名称
                Mono.justOrEmpty(message.getHeader("deviceName")).map(String::valueOf),
                //T3. 产品ID
                Mono.justOrEmpty(message.getHeader("productId").map(String::valueOf)),
                //T4. 产品
                Mono.justOrEmpty(message.getHeader("productId").map(String::valueOf))
                    .flatMap(productService::findById),
                //T5. 配置信息
                Mono.justOrEmpty(message.getHeader("configuration").map(Map.class::cast).orElse(new HashMap()))
            ).flatMap(tps -> {
                DeviceInstanceEntity instance = new DeviceInstanceEntity();
                instance.setId(tps.getT1());
                instance.setName(tps.getT2());
                instance.setProductId(tps.getT3());
                instance.setProductName(tps.getT4().getName());
                instance.setConfiguration(tps.getT5());
                instance.setRegistryTime(message.getTimestamp());
                instance.setCreateTimeNow();
                instance.setCreatorId(tps.getT4().getCreatorId());
                instance.setOrgId(tps.getT4().getOrgId());

                //设备自状态管理
                //网关注册设备子设备时,设置自状态管理。
                //在检查子设备状态时,将会发送ChildDeviceMessage<DeviceStateCheckMessage>到网关
                //网关需要回复ChildDeviceMessageReply<DeviceStateCheckMessageReply>
                @SuppressWarnings("all")
                boolean selfManageState = CastUtils
                    .castBoolean(tps.getT5().getOrDefault(DeviceConfigKey.selfManageState.getKey(), false));

                instance.setState(selfManageState ? DeviceState.offline : DeviceState.online);
                //合并配置
                instance.mergeConfiguration(tps.getT5());

                return deviceService
                    .save(instance)
                    .then(Mono.defer(() -> registry
                        .register(instance.toDeviceInfo()
                                          .addConfig("state", selfManageState
                                              ? org.jetlinks.core.device.DeviceState.offline
                                              : org.jetlinks.core.device.DeviceState.online))));
            });
    }


    @Subscribe("/device/*/*/register")
    @Transactional(propagation = Propagation.NEVER)
    public Mono<Void> autoRegisterDevice(DeviceRegisterMessage message) {
        if (message.getHeader(Headers.force).orElse(false)) {
            return this
                .doAutoRegister(message)
                .then();
        }
        return registry
            .getDevice(message.getDeviceId())
            .flatMap(device -> {
                //注册消息中修改了配置信息
                @SuppressWarnings("all")
                Map<String, Object> config = message.getHeader("configuration").map(Map.class::cast).orElse(null);
                if (MapUtils.isNotEmpty(config)) {

                    return deviceService
                        .mergeConfiguration(device.getDeviceId(), config, update ->
                            //更新设备名称
                            update.set(DeviceInstanceEntity::getName,
                                       message.getHeader(PropertyConstants.deviceName).orElse(null)))
                        .thenReturn(device);
                }
                return Mono.just(device);
            })
            //注册中心中没有此设备则进行自动注册
            .switchIfEmpty(Mono.defer(() -> doAutoRegister(message)))
            .then();
    }

    /**
     * 通过订阅子设备注册消息,自动绑定子设备到网关设备
     *
     * @param message 子设备消息
     * @return void
     */
    @Subscribe("/device/*/*/message/children/*/register")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Mono<Void> autoBindChildrenDevice(ChildDeviceMessage message) {

        Message childMessage = message.getChildDeviceMessage();
        if (childMessage instanceof DeviceRegisterMessage) {
            String childId = ((DeviceRegisterMessage) childMessage).getDeviceId();
            if (message.getDeviceId().equals(childId)) {
                log.warn("子设备注册消息循环依赖:{}", message);
                return Mono.empty();
            }
            //网关设备添加到header中
            childMessage.addHeaderIfAbsent(DeviceConfigKey.parentGatewayId.getKey(), message.getDeviceId());

            return registry
                .getDevice(childId)
                .map(device -> device
                    .getState()
                    //更新数据库
                    .flatMap(state -> deviceService
                        .createUpdate()
                        .set(DeviceInstanceEntity::getParentId, message.getDeviceId())
                        //状态还有更好的更新方式?
                        .set(DeviceInstanceEntity::getState, DeviceState.of(state))
                        .where(DeviceInstanceEntity::getId, childId)
                        .execute())
                    //更新缓存
                    .then(device.setConfig(DeviceConfigKey.parentGatewayId, message.getDeviceId()))
                    .thenReturn(device))
                .defaultIfEmpty(Mono.defer(() -> doAutoRegister(((DeviceRegisterMessage) childMessage))))
                .flatMap(Function.identity())
                .then();
        }
        return Mono.empty();
    }

    /**
     * 通过订阅子设备注销消息,自动解绑子设备
     *
     * @param message 子设备消息
     * @return void
     */
    @Subscribe("/device/*/*/message/children/*/unregister")
    public Mono<Void> autoUnbindChildrenDevice(ChildDeviceMessage message) {
        Message childMessage = message.getChildDeviceMessage();
        if (childMessage instanceof DeviceUnRegisterMessage) {
            String childId = ((DeviceUnRegisterMessage) childMessage).getDeviceId();
            return registry
                .getDevice(childId)
                .flatMap(dev -> dev
                    .removeConfig(DeviceConfigKey.parentGatewayId.getKey())
                    .then(dev.checkState()))
                .flatMap(state -> deviceService
                    .createUpdate()
                    .setNull(DeviceInstanceEntity::getParentId)
                    .set(DeviceInstanceEntity::getState, DeviceState.of(state))
                    .where(DeviceInstanceEntity::getId, childId)
                    .execute()
                    .then());
        }
        return Mono.empty();
    }

    @Subscribe("/device/*/*/unregister")
    @Transactional(propagation = Propagation.NEVER)
    public Mono<Void> unRegisterDevice(DeviceUnRegisterMessage message) {
        //注销设备
        return deviceService
            .unregisterDevice(message.getDeviceId())
            .then();
    }

    @Subscribe("/device/*/*/message/tags/update")
    public Mono<Void> updateDeviceTag(UpdateTagMessage message) {
        Map<String, Object> tags = message.getTags();
        String deviceId = message.getDeviceId();

        return registry
            .getDevice(deviceId)
            .flatMap(DeviceOperator::getMetadata)
            .flatMapMany(metadata -> Flux
                .fromIterable(tags.entrySet())
                .map(e -> {
                    DeviceTagEntity tagEntity = metadata
                        .getTag(e.getKey())
                        .map(tagMeta -> DeviceTagEntity.of(tagMeta, e.getValue()))
                        .orElseGet(() -> {
                            DeviceTagEntity entity = new DeviceTagEntity();
                            entity.setKey(e.getKey());
                            entity.setType("string");
                            entity.setName(e.getKey());
                            entity.setCreateTime(new Date());
                            entity.setDescription("设备上报");
                            entity.setValue(String.valueOf(e.getValue()));
                            return entity;
                        });
                    tagEntity.setDeviceId(deviceId);
                    tagEntity.setId(DeviceTagEntity.createTagId(deviceId, tagEntity.getKey()));
                    return tagEntity;
                }))
            .as(tagRepository::save)
            .then();
    }

    @Subscribe({"/device/*/*/message/property/report", "device/*/*/message/property/report"})
    public Mono<Void> upgradeDeviceProperty(DeviceMessage message) {
        if (!(message instanceof ReportPropertyMessage)) {
            return Mono.empty();
        }
        String key = message.getDeviceId() + "_prop";
        long timestamp = ((ReportPropertyMessage) message).getTimestamp();

        Mono<Void> update = deviceService
            .findById(message.getDeviceId())
            .flatMap(device -> {
                JSONObject json = message.toJson();
                JSONObject propJson = json.getJSONObject("properties");
                if (propJson == null) {
                    return Mono.empty();
                }
                log.info("device property report: {}", propJson.toJSONString());

                DeviceInstanceEntity deviceEntity = JSONObject.parseObject(propJson.toJSONString(), DeviceInstanceEntity.class);
                Mono<Void> cardUpdate = syncDeviceCard(device.getId(), propJson);

                if (!hasText(deviceEntity.getMac())) {
                    deviceEntity.setMac(device.getMac());
                }
                if (!hasText(deviceEntity.getImei())) {
                    deviceEntity.setImei(device.getImei());
                }
                if (!hasText(deviceEntity.getOperator())) {
                    deviceEntity.setOperator(device.getOperator());
                }

                Mono<String> locationUpdate = resolveReportLocation(device, deviceEntity, propJson);
                applyRewebAndPingConfig(device, deviceEntity, propJson);
                deviceEntity.setModifyTime(System.currentTimeMillis());

                return locationUpdate
                    .doOnNext(deviceEntity::setLocation)
                    .then(syncPingConfigState(device, deviceEntity, propJson))
                    .then(deviceService.updateById(device.getId(), deviceEntity))
                    .then(cardUpdate);
            });

        return redis
            .opsForValue()
            .get(key)
            .defaultIfEmpty("")
            .flatMap(deviceMsg -> {
                if (!hasText(deviceMsg)) {
                    return update;
                }
                JSONObject jsonObj = JSON.parseObject(deviceMsg);
                Long lastTimestamp = jsonObj.getLong("timestamp");
                if (lastTimestamp == null || timestamp - lastTimestamp >= 60000L || Objects.equals(lastTimestamp, timestamp)) {
                    return update;
                }
                return Mono.empty();
            });
    }

    private Mono<Void> syncDeviceCard(String deviceId, JSONObject propJson) {
        String iccid = propJson.getString("iccid");
        if (!hasText(iccid) || iccid.length() <= 10) {
            return Mono.empty();
        }

        DeviceCardEntity cardEntity = new DeviceCardEntity();
        cardEntity.setIccid(iccid);
        cardEntity.setSlot(propJson.getInteger("slot"));
        cardEntity.setDeviceId(deviceId);
        cardEntity.setUseState(1);

        return cardRepository
            .createQuery()
            .where(DeviceCardEntity::getDeviceId, deviceId)
            .and(DeviceCardEntity::getIccid, cardEntity.getIccid())
            .fetchOne()
            .flatMap(card -> cardRepository.updateById(card.getId(), cardEntity))
            .switchIfEmpty(Mono.defer(() -> cardRepository.insert(cardEntity)))
            .then(cardRepository
                      .createUpdate()
                      .set(DeviceCardEntity::getUseState, 0)
                      .where(DeviceCardEntity::getDeviceId, deviceId)
                      .not(DeviceCardEntity::getIccid, cardEntity.getIccid())
                      .execute())
            .then();
    }

    private Mono<String> resolveReportLocation(DeviceInstanceEntity device,
                                               DeviceInstanceEntity deviceEntity,
                                               JSONObject propJson) {
        String gpsloc = propJson.getString("gpsloc");
        if (!hasText(gpsloc)) {
            return Mono.empty();
        }
        String[] arr = gpsloc.split(",");
        if (arr.length <= 3 || !hasText(arr[1]) || !hasText(arr[2])) {
            return Mono.empty();
        }

        deviceEntity.setLng(arr[2]);
        deviceEntity.setLat(arr[1]);
        if (!Objects.equals(device.getLat(), arr[1])
            || !Objects.equals(device.getLng(), arr[2])
            || !hasText(device.getLocation())) {
            return deviceService.getAmapLocation(arr[2] + "," + arr[1]);
        }
        return Mono.empty();
    }

    private void applyRewebAndPingConfig(DeviceInstanceEntity device,
                                         DeviceInstanceEntity deviceEntity,
                                         JSONObject propJson) {
        String webpwd = propJson.getString("webpwd");
        if (hasText(webpwd)) {
            deviceEntity.setPasswd(webpwd);
        }

        String switchState = propJson.getString("switch_state");
        String pingAddr = propJson.getString("ping_addr");
        Integer pingRetry = propJson.getInteger("ping_retry");

        if (hasText(switchState)) {
            deviceEntity.setSwitchState(switchState);
        }
        if (hasText(pingAddr)) {
            deviceEntity.setPingAddr(pingAddr);
        }
        if (pingRetry != null) {
            deviceEntity.setPingRetry(pingRetry);
        }
    }

    private Mono<Void> syncPingConfigState(DeviceInstanceEntity device,
                                           DeviceInstanceEntity deviceEntity,
                                           JSONObject propJson) {
        String switchState = propJson.getString("switch_state");
        String pingAddr = propJson.getString("ping_addr");
        Integer pingRetry = propJson.getInteger("ping_retry");
        if (!hasText(switchState) || (!hasText(pingAddr) && pingRetry == null)) {
            return Mono.empty();
        }

        return deviceService
            .findById(device.getId())
            .doOnNext(current -> {
                if (Objects.equals(switchState, "0") && Objects.equals(current.getSwitchState(), "0")) {
                    deviceEntity.setSyncFlag("1");
                } else if (Objects.equals(switchState, "1")
                    && Objects.equals(current.getSwitchState(), "1")
                    && Objects.equals(pingAddr, current.getPingAddr())
                    && Objects.equals(pingRetry, current.getPingRetry())) {
                    deviceEntity.setSyncFlag("1");
                }
            })
            .then();
    }

    private boolean hasText(String text) {
        return text != null && !text.trim().isEmpty();
    }

    @Subscribe("/device/*/*/metadata/derived")
    public Mono<Void> updateMetadata(DerivedMetadataMessage message) {
        if (message.isAll()) {
            return updateMedata(message.getDeviceId(), message.getMetadata());
        }
        return Mono
            .zip(
                //原始物模型
                registry
                    .getDevice(message.getDeviceId())
                    .flatMap(DeviceOperator::getMetadata),
                //新的物模型
                JetLinksDeviceMetadataCodec
                    .getInstance()
                    .decode(message.getMetadata()),
                //合并在一起
                DeviceMetadata::merge
            )
            //重新编码为字符串
            .flatMap(JetLinksDeviceMetadataCodec.getInstance()::encode)
            //更新物模型
            .flatMap(metadata -> updateMedata(message.getDeviceId(), metadata));
    }

    private Mono<Void> updateMedata(String deviceId, String metadata) {
        return deviceService
            .createUpdate()
            .set(DeviceInstanceEntity::getDeriveMetadata, metadata)
            .where(DeviceInstanceEntity::getId, deviceId)
            .execute()
            .then(registry.getDevice(deviceId))
            .flatMap(device -> device.updateMetadata(metadata))
            .then();
    }



    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    public static class StateBuf implements Externalizable {
        //有效期一小时
        static long expires = Duration.ofHours(1).toMillis();

        private String id;
        private long time;

        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeUTF(id);
            out.writeLong(time);
        }

        @Override
        public void readExternal(ObjectInput in) throws IOException {
            id = in.readUTF();
            time = in.readLong();
        }

        public boolean isEffective() {
            return System.currentTimeMillis() - time < expires;
        }
    }

    @PreDestroy
    public void shutdown() {
        disposable.dispose();
    }

    @PostConstruct
    public void init() {

        Subscription subscription = Subscription
            .builder()
            .subscriberId("device-state-synchronizer")
            .topics("/device/*/*/online", "/device/*/*/offline")
            .justLocal()//只订阅本地
            .build();

        //缓冲同步设备上线信息,在突发大量上下线的情况,减少数据库的压力
        PersistenceBuffer<StateBuf> buffer =
            new PersistenceBuffer<>(
                "./data/device-state-buffer",
                "device-state.queue",
                StateBuf::new,
                flux -> deviceService
                    .syncStateBatch(flux
                                        .filter(StateBuf::isEffective)
                                        .map(StateBuf::getId)
                                        .distinct()
                                        .collectList()
                                        .flux(), false)
                    .then(Reactors.ALWAYS_FALSE))
                .name("device-state-synchronizer")
                .parallelism(1)
                .bufferTimeout(Duration.ofSeconds(1))
                .retryWhenError(e -> ErrorUtils
                    .hasException(e,
                                  IOException.class,
                                  QueryTimeoutException.class))
                .bufferSize(1000);

        buffer.start();

        disposable.add(eventBus
                           .subscribe(subscription, DeviceMessage.class)
                           .subscribe(msg -> buffer.write(new StateBuf(msg.getDeviceId(), msg.getTimestamp()))));

        disposable.add(buffer);

    }

}
