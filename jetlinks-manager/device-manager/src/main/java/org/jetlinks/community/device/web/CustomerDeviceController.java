package org.jetlinks.community.device.web;

import com.alibaba.fastjson.JSON;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.hswebframework.ezorm.core.param.Term;
import org.hswebframework.ezorm.rdb.mapping.ReactiveRepository;
import org.hswebframework.ezorm.rdb.mapping.defaults.SaveResult;
import org.hswebframework.reactor.excel.ReactorExcel;
import org.hswebframework.web.api.crud.entity.PagerResult;
import org.hswebframework.web.api.crud.entity.QueryNoPagingOperation;
import org.hswebframework.web.api.crud.entity.QueryOperation;
import org.hswebframework.web.api.crud.entity.QueryParamEntity;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.authorization.annotation.QueryAction;
import org.hswebframework.web.authorization.annotation.Resource;
import org.hswebframework.web.authorization.annotation.SaveAction;
import org.hswebframework.web.authorization.exception.UnAuthorizedException;
import org.hswebframework.web.bean.FastBeanCopier;
import org.hswebframework.web.exception.BusinessException;
import org.jetlinks.community.auth.entity.UserDetail;
import org.jetlinks.community.auth.service.UserDetailService;
import org.jetlinks.community.auth.utils.TermParseUtil;
import org.jetlinks.community.device.entity.CustomerDevice;
import org.jetlinks.community.device.entity.DeviceCardEntity;
import org.jetlinks.community.device.entity.DeviceInstanceEntity;
import org.jetlinks.community.device.entity.DevicePosition;
import org.jetlinks.community.device.entity.DeviceProductEntity;
import org.jetlinks.community.device.enums.DeviceState;
import org.jetlinks.community.device.response.BaiduLocationPoint;
import org.jetlinks.community.device.response.DeviceDeployResult;
import org.jetlinks.community.device.response.ImportDeviceInstanceResult;
import org.jetlinks.community.device.service.LocalDeviceInstanceService;
import org.jetlinks.community.device.service.LocalDeviceProductService;
import org.jetlinks.community.device.web.excel.CustomerDeviceExcelInfo;
import org.jetlinks.community.device.web.excel.CustomerDeviceWrapper;
import org.jetlinks.community.device.web.excel.DeviceExcelInfo;
import org.jetlinks.community.device.web.request.BatchUpdateDeviceRequest;
import org.jetlinks.community.io.excel.ImportExportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.concurrent.Queues;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/customer/device")
@Authorize
@Resource(id = "customer-device", name = "客户设备实例")
@Tag(name = "客户设备接口")
public class CustomerDeviceController {

    private static final Logger log = LoggerFactory.getLogger(CustomerDeviceController.class);

    private final UserDetailService userDetailService;
    private final LocalDeviceInstanceService service;
    private final LocalDeviceProductService productService;
    private final ImportExportService importExportService;
    private final ReactiveRepository<DeviceCardEntity, String> cardRepository;
    private final TransactionalOperator transactionalOperator;
    private final DataBufferFactory bufferFactory = new DefaultDataBufferFactory();

    CustomerDeviceController(LocalDeviceInstanceService localDeviceInstanceService,
                             UserDetailService userDetailService,
                             LocalDeviceProductService productService,
                             ImportExportService importExportService,
                             @SuppressWarnings("all") ReactiveRepository<DeviceCardEntity, String> cardRepository,
                             TransactionalOperator transactionalOperator) {
        this.userDetailService = userDetailService;
        this.service = localDeviceInstanceService;
        this.productService = productService;
        this.importExportService = importExportService;
        this.cardRepository = cardRepository;
        this.transactionalOperator = transactionalOperator;
    }

    @PostMapping("/_query")
    @QueryAction
    @Operation(summary = "分页获取客户设备实例")
    public Mono<PagerResult<CustomerDevice>> queryCustomerDevice(@RequestBody Mono<QueryParamEntity> query) {
        return currentUserWithQuery(query)
            .flatMap(tuple -> {
                setQueryWhere(tuple.getT1(), tuple.getT2(), tuple.getT3());
                tuple.getT3().setOrderBy("create_time DESC");
                return service.queryCustomerDevice(tuple.getT3());
            });
    }

    public void setQueryWhere(Authentication auth, UserDetail user, QueryParamEntity query) {
        String whereQuery;
        if (!"admin".equals(auth.getUser().getUsername())) {
            String treePath = user.getTreePath() == null ? "," : user.getTreePath();
            whereQuery = "( tree_path like '" + treePath + user.getId() + ",%' or user_id='" + user.getId() + "')";
        } else {
            whereQuery = " id notnull  ";
        }

        List<Term> terms = query.getTerms();
        if (terms != null && !terms.isEmpty() && !terms.get(0).getTerms().isEmpty()) {
            whereQuery = whereQuery + TermParseUtil.parseToSql(terms.get(0).getTerms());
        }
        if (!StringUtils.isEmpty(whereQuery)) {
            query.setWhere(whereQuery);
        }
        query.setOrderBy("create_time DESC");
    }

    @PostMapping("/queryPosition")
    @QueryAction
    @Operation(summary = "客户设备实例位置统计")
    public Mono<List<DevicePosition>> queryDevicePosition() {
        return Authentication
            .currentReactive()
            .switchIfEmpty(Mono.error(UnAuthorizedException::new))
            .flatMap(auth -> userDetailService
                .findUserDetail(auth.getUser().getId())
                .flatMap(user -> {
                    String whereQuery = "1=1 ";
                    if (!"admin".equals(auth.getUser().getUsername())) {
                        String treePath = user.getTreePath() == null ? "," : user.getTreePath();
                        whereQuery = "( tree_path like '" + treePath + user.getId() + ",%' or user_id='" + user.getId() + "')";
                    }
                    return service.queryDevicePosition(whereQuery);
                }));
    }

    @GetMapping("/getLocation")
    @QueryNoPagingOperation(summary = "获取客户端IP")
    public Mono<BaiduLocationPoint> getClientLocation(ServerWebExchange serverWebExchange) {
        return service.getClientLocation(getIp(serverWebExchange));
    }

    @GetMapping(value = "/syncState", produces = "text/event-stream")
    @SaveAction
    @QueryNoPagingOperation(summary = "同步设备状态")
    public Flux<Integer> syncState(@Parameter(hidden = true) QueryParamEntity query) {
        query.setPaging(false);
        return Authentication
            .currentReactive()
            .switchIfEmpty(Mono.error(UnAuthorizedException::new))
            .flatMapMany(auth -> userDetailService
                .findUserDetail(auth.getUser().getId())
                .flatMapMany(user -> {
                    setQueryWhere(auth, user, query);
                    return service
                        .queryCustomerDevices(query)
                        .map(DeviceInstanceEntity::getId)
                        .buffer(200)
                        .publishOn(Schedulers.single())
                        .concatMap(ids -> service.syncStateBatch(Flux.just(ids), true).map(List::size))
                        .defaultIfEmpty(0);
                }));
    }

    @GetMapping(value = "/deployAll", produces = "text/event-stream")
    @SaveAction
    @QueryOperation(summary = "查询并批量激活设备")
    public Flux<DeviceDeployResult> deployAll(@Parameter(hidden = true) QueryParamEntity query) {
        query.setPaging(false);
        return Authentication
            .currentReactive()
            .switchIfEmpty(Mono.error(UnAuthorizedException::new))
            .flatMapMany(auth -> userDetailService
                .findUserDetail(auth.getUser().getId())
                .flatMapMany(user -> {
                    setQueryWhere(auth, user, query);
                    return service.queryCustomerDevices(query).as(service::deploy);
                }));
    }

    @PostMapping("/_update")
    @SaveAction
    @Operation(summary = "修改用户设备")
    public Mono<SaveResult> update(@RequestBody Mono<DeviceInstanceEntity> request) {
        return Authentication
            .currentReactive()
            .switchIfEmpty(Mono.error(UnAuthorizedException::new))
            .zipWith(request)
            .flatMap(tuple -> {
                DeviceInstanceEntity entity = tuple.getT2();
                entity.setModifierId(tuple.getT1().getUser().getId());
                entity.setModifierName(tuple.getT1().getUser().getName());
                return service
                    .findById(entity.getId())
                    .flatMap(instance -> service.updateById(entity.getId(), entity).thenReturn(SaveResult.of(0, 1)));
            });
    }

    @PatchMapping("/_add")
    @SaveAction
    @Operation(summary = "添加用户设备")
    public Mono<SaveResult> add(@RequestBody Mono<DeviceInstanceEntity> request) {
        return Authentication
            .currentReactive()
            .switchIfEmpty(Mono.error(UnAuthorizedException::new))
            .zipWith(request)
            .flatMap(tuple -> {
                DeviceInstanceEntity entity = tuple.getT2();
                entity.setModifierId(tuple.getT1().getUser().getId());
                entity.setModifierName(tuple.getT1().getUser().getName());
                return service.save(entity);
            });
    }

    @PatchMapping("/batchUpdate")
    @SaveAction
    @Operation(summary = "批量修改设备用户")
    public Mono<Integer> batchUpdate(@RequestBody Mono<BatchUpdateDeviceRequest> request) {
        return Authentication
            .currentReactive()
            .switchIfEmpty(Mono.error(UnAuthorizedException::new))
            .zipWith(request)
            .flatMap(tuple -> {
                tuple.getT2().setModifyUserId(tuple.getT1().getUser().getId());
                return service.batchUpdate(tuple.getT2());
            });
    }

    @PostMapping("/_count")
    @Operation(summary = "使用POST方式查询总数")
    public Mono<Integer> count(@RequestBody Mono<QueryParamEntity> query) {
        return currentUserWithQuery(query)
            .flatMap(tuple -> {
                setQueryWhere(tuple.getT1(), tuple.getT2(), tuple.getT3());
                return service.countCustomerDevice(tuple.getT3());
            });
    }

    @GetMapping(value = "/{productId}/import", produces = "text/event-stream")
    @SaveAction
    @Operation(summary = "导入设备数据")
    public Flux<ImportDeviceInstanceResult> doBatchImportByProduct(@PathVariable @Parameter(description = "产品ID") String productId,
                                                                    @RequestParam(defaultValue = "true") @Parameter(description = "自动启用") boolean autoDeploy,
                                                                    @RequestParam(required = false) @Parameter(description = "文件地址,支持csv,xlsx文件格式") String fileUrl,
                                                                    @RequestParam(required = false) @Parameter(description = "文件Id") String fileId,
                                                                    @RequestParam(defaultValue = "32") @Parameter int speed) {
        return Authentication
            .currentReactive()
            .flatMapMany(auth -> productService
                .findById(productId)
                .flatMapMany(product -> importExportService
                    .readData(fileUrl, fileId, new CustomerDeviceWrapper())
                    .cast(CustomerDeviceExcelInfo.class)
                    .doOnNext(info -> info.setProductName(product.getName()))
                    .flatMap(info -> Mono
                        .just(info)
                        .zipWith(service.findById(info.getId()).switchIfEmpty(Mono.fromSupplier(DeviceInstanceEntity::new)))
                        .map(tuple -> toImportDevice(productId, tuple.getT1(), tuple.getT2()))))
                .as(flux -> handleImportDevice(flux, autoDeploy, speed)));
    }

    private Tuple2<DeviceInstanceEntity, DeviceCardEntity> toImportDevice(String productId,
                                                                          CustomerDeviceExcelInfo info,
                                                                          DeviceInstanceEntity existing) {
        log.info("设备导入:{}", JSON.toJSON(info));
        DeviceInstanceEntity entity = FastBeanCopier.copy(info, new DeviceInstanceEntity());
        if (StringUtils.isEmpty(entity.getId())) {
            throw new BusinessException("第" + (info.getRowNumber() + 1L) + "行:设备ID不能为空");
        }
        if (existing.getId() != null) {
            throw new BusinessException("第" + (info.getRowNumber() + 1L) + "行:设备已经存在");
        }
        entity.setName("设备" + entity.getId());
        entity.setProductId(productId);
        entity.setState(DeviceState.notActive);
        return Tuples.of(entity, info.getCard());
    }

    private Flux<ImportDeviceInstanceResult> handleImportDevice(Flux<Tuple2<DeviceInstanceEntity, DeviceCardEntity>> flux,
                                                                boolean autoDeploy,
                                                                int speed) {
        return flux
            .buffer(100)
            .flatMap(batch -> {
                List<DeviceInstanceEntity> devices = batch.stream().map(Tuple2::getT1).collect(Collectors.toList());
                List<DeviceCardEntity> cards = batch
                    .stream()
                    .map(Tuple2::getT2)
                    .filter(card -> !StringUtils.isEmpty(card.getIccid()))
                    .collect(Collectors.toList());

                Mono<SaveResult> saveDevices = service
                    .save(Flux.fromIterable(devices))
                    .flatMap(result -> autoDeploy
                        ? service.importDeploy(Flux.fromIterable(devices)).then(Mono.just(result))
                        : Mono.just(result));

                Mono<SaveResult> saveCards = cards.isEmpty()
                    ? Mono.just(SaveResult.of(0, 0))
                    : cardRepository.save(cards).defaultIfEmpty(SaveResult.of(0, 0));

                return Mono
                    .zip(saveDevices, saveCards)
                    .as(transactionalOperator::transactional)
                    .map(result -> ImportDeviceInstanceResult.success(result.getT1()))
                    .onErrorResume(err -> Mono.just(ImportDeviceInstanceResult.error(err)));
            }, Math.min(speed, Queues.XS_BUFFER_SIZE));
    }

    @GetMapping("/{productId}/template.{format}")
    @QueryAction
    @Operation(summary = "下载设备导入模板")
    public Mono<Void> downloadExportTemplate(@PathVariable @Parameter(description = "产品ID") String productId,
                                             ServerHttpResponse response,
                                             @PathVariable @Parameter(description = "文件格式,支持csv,xlsx") String format) throws IOException {
        response.getHeaders().set("Content-Disposition", "attachment; filename=".concat(URLEncoder.encode("设备导入模板." + format, StandardCharsets.UTF_8.displayName())));
        Flux<DataBuffer> data = ReactorExcel
            .writer(format)
            .headers((Collection) CustomerDeviceExcelInfo.getTemplateHeaderMapping())
            .converter(info -> ((DeviceExcelInfo) info).toMap())
            .writeBuffer(Flux.<DeviceExcelInfo>empty())
            .doOnError(err -> log.error(String.valueOf(err)))
            .map(bytes -> bufferFactory.wrap((byte[]) bytes));
        return response.writeWith(data);
    }

    @GetMapping("/export.{format}")
    @QueryAction
    @QueryNoPagingOperation(summary = "导出设备实例数据", description = "此操作不支持导出设备标签和配置信息")
    public Mono<Void> export(ServerHttpResponse response,
                             @RequestParam(required = false) @Parameter String queryParam,
                             @PathVariable @Parameter(description = "文件格式,支持csv,xlsx") String format) throws IOException {
        response.getHeaders().set("Content-Disposition", "attachment; filename=".concat(URLEncoder.encode("设备实例." + format, StandardCharsets.UTF_8.displayName())));
        QueryParamEntity parameter = JSON.parseObject(Optional.ofNullable(queryParam).orElse("{}"), QueryParamEntity.class);
        return Authentication
            .currentReactive()
            .switchIfEmpty(Mono.error(UnAuthorizedException::new))
            .flatMap(auth -> userDetailService
                .findUserDetail(auth.getUser().getId())
                .flatMap(user -> {
                    setQueryWhere(auth, user, parameter);
                    parameter.setOrderBy("create_time DESC");
                    Flux<CustomerDeviceExcelInfo> exportData = service
                        .exportCustomerDevices(parameter)
                        .map(this::toExportInfo);
                    Flux<DataBuffer> data = ReactorExcel
                        .writer(format)
                        .headers((Collection) CustomerDeviceExcelInfo.getExportHeaderMapping())
                        .converter(info -> ((CustomerDeviceExcelInfo) info).toMap())
                        .writeBuffer(exportData, 524288)
                        .doOnError(err -> log.error(String.valueOf(err)))
                        .map(bytes -> bufferFactory.wrap((byte[]) bytes));
                    return response.writeWith(data);
                }));
    }

    private CustomerDeviceExcelInfo toExportInfo(CustomerDevice entity) {
        CustomerDeviceExcelInfo exportEntity = FastBeanCopier.copy(entity, new CustomerDeviceExcelInfo(), "state");
        exportEntity.setState(entity.getState() == null ? "" : entity.getState().getText());
        if (entity.getUserDetail() != null) {
            exportEntity.setCustomer(entity.getUserDetail().getName());
        }
        if (entity.getDeviceCard() != null) {
            exportEntity.setIccid(entity.getDeviceCard().getIccid());
        }
        if (entity.getDeviceCard1() != null) {
            exportEntity.setIccid1(entity.getDeviceCard1().getIccid());
        }
        return exportEntity;
    }

    private Mono<reactor.util.function.Tuple3<Authentication, UserDetail, QueryParamEntity>> currentUserWithQuery(Mono<QueryParamEntity> query) {
        return Authentication
            .currentReactive()
            .switchIfEmpty(Mono.error(UnAuthorizedException::new))
            .flatMap(auth -> userDetailService
                .findUserDetail(auth.getUser().getId())
                .zipWith(query)
                .map(tuple -> reactor.util.function.Tuples.of(auth, tuple.getT1(), tuple.getT2())));
    }

    public String getIp(ServerWebExchange serverWebExchange) {
        ServerHttpRequest request = serverWebExchange.getRequest();
        HttpHeaders headers = request.getHeaders();
        String ip = headers.getFirst("x-forwarded-for");
        if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip) && ip.indexOf(",") != -1) {
            ip = ip.split(",")[0];
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = headers.getFirst("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = headers.getFirst("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = headers.getFirst("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = headers.getFirst("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = headers.getFirst("X-Real-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddress().getAddress().getHostAddress();
        }
        return ip.replaceAll(":", ".");
    }
}
