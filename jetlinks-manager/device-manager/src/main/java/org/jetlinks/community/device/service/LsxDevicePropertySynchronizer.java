/*
 * Copyright 2025 JetLinks https://www.jetlinks.cn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetlinks.community.device.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetlinks.community.device.entity.DeviceCardEntity;
import org.jetlinks.community.device.entity.DeviceInstanceEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Optional;

@Component
public class LsxDevicePropertySynchronizer {

    public static LsxDevicePropertyUpdate applyReportedProperties(DeviceInstanceEntity current,
                                                                  Map<String, Object> properties) {
        DeviceInstanceEntity device = DeviceInstanceEntity.of();
        if (current != null) {
            device.setId(current.getId());
            device.setName(current.getName());
            device.setProductId(current.getProductId());
            device.setProductName(current.getProductName());
            device.setState(current.getState());
            device.setMac(current.getMac());
            device.setImei(current.getImei());
            device.setOperator(current.getOperator());
            device.setSwitchState(current.getSwitchState());
            device.setPingAddr(current.getPingAddr());
            device.setPingRetry(current.getPingRetry());
        }

        setText(properties, "mac", device::setMac);
        setText(properties, "imei", device::setImei);
        setText(properties, "ip", device::setAdress);
        setText(properties, "version", device::setFirmwareVersion);
        setInteger(properties, "t24g_num", device::setT24gNum);
        setInteger(properties, "t5g_num", device::setT5gNum);
        setText(properties, "rsrp", device::setRsrp);
        setText(properties, "rsrq", device::setRsrq);
        setText(properties, "sinr", device::setSinr);
        setText(properties, "network", device::setNetwork);
        setText(properties, "operator", device::setOperator);
        setText(properties, "webpwd", device::setPasswd);
        setText(properties, "switch_state", device::setSwitchState);
        setText(properties, "ping_addr", device::setPingAddr);
        setInteger(properties, "ping_retry", device::setPingRetry);
        applyLocation(device, properties);

        if (StringUtils.hasText(device.getSwitchState())) {
            device.setSyncFlag("1");
        }
        device.setModifyTime(System.currentTimeMillis());

        return new LsxDevicePropertyUpdate(device, createCurrentCard(device.getId(), properties));
    }

    private static Optional<DeviceCardEntity> createCurrentCard(String deviceId, Map<String, Object> properties) {
        String iccid = asText(properties.get("iccid"));
        if (!StringUtils.hasText(deviceId) || !StringUtils.hasText(iccid) || iccid.length() <= 10) {
            return Optional.empty();
        }
        DeviceCardEntity card = new DeviceCardEntity();
        card.setDeviceId(deviceId);
        card.setIccid(iccid);
        card.setSlot(asInteger(properties.get("slot")));
        card.setUseState(1);
        card.setModifyTime(System.currentTimeMillis());
        return Optional.of(card);
    }

    private static void applyLocation(DeviceInstanceEntity device, Map<String, Object> properties) {
        String gpsloc = asText(properties.get("gpsloc"));
        if (!StringUtils.hasText(gpsloc)) {
            return;
        }
        String[] arr = gpsloc.split(",");
        if (arr.length > 3 && StringUtils.hasText(arr[1]) && StringUtils.hasText(arr[2])) {
            device.setLat(arr[1]);
            device.setLng(arr[2]);
        }
    }

    private static void setText(Map<String, Object> properties,
                                String key,
                                java.util.function.Consumer<String> setter) {
        String value = asText(properties.get(key));
        if (StringUtils.hasText(value)) {
            setter.accept(value);
        }
    }

    private static void setInteger(Map<String, Object> properties,
                                   String key,
                                   java.util.function.Consumer<Integer> setter) {
        Integer value = asInteger(properties.get(key));
        if (value != null) {
            setter.accept(value);
        }
    }

    private static String asText(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private static Integer asInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        String text = String.valueOf(value);
        if (!StringUtils.hasText(text)) {
            return null;
        }
        return Integer.parseInt(text);
    }

    @Getter
    @AllArgsConstructor
    public static class LsxDevicePropertyUpdate {
        private final DeviceInstanceEntity device;
        private final Optional<DeviceCardEntity> currentCard;
    }
}
