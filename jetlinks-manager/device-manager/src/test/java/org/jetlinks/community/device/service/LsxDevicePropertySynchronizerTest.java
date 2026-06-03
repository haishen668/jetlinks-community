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

import org.apache.commons.codec.digest.DigestUtils;
import org.jetlinks.community.device.entity.DeviceCardEntity;
import org.jetlinks.community.device.entity.DeviceDetail;
import org.jetlinks.community.device.entity.DeviceInstanceEntity;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import javax.persistence.Column;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

class LsxDevicePropertySynchronizerTest {

    @Test
    void shouldMapReportedPropertiesToDeviceProfileAndCurrentCard() {
        DeviceInstanceEntity current = DeviceInstanceEntity.of();
        current.setId("869624060285951");
        current.setName("manner+城创中心店");
        current.setProductId("device_test01");
        current.setProductName("鼎桥 Max3");

        LsxDevicePropertySynchronizer.LsxDevicePropertyUpdate update =
            LsxDevicePropertySynchronizer.applyReportedProperties(current, realDevicePayload());

        DeviceInstanceEntity device = update.getDevice();
        Assert.assertEquals("11.0.0.183(H72SP1C00)", device.getFirmwareVersion());
        Assert.assertEquals("D0:A0:D6:8C:B4:C8", device.getMac());
        Assert.assertEquals("869624060285951", device.getImei());
        Assert.assertEquals(Integer.valueOf(0), device.getT24gNum());
        Assert.assertEquals(Integer.valueOf(0), device.getT5gNum());
        Assert.assertEquals("-83", device.getRsrp());
        Assert.assertEquals("-4", device.getRsrq());
        Assert.assertEquals("30", device.getSinr());
        Assert.assertEquals("5G", device.getNetwork());
        Assert.assertEquals("testpaddword", device.getPasswd());
        Assert.assertEquals("CT", device.getOperator());
        Assert.assertEquals("0", device.getSwitchState());
        Assert.assertEquals("1", device.getSyncFlag());
        Assert.assertEquals("127.0.0.1\n", device.getPingAddr());
        Assert.assertEquals(Integer.valueOf(5), device.getPingRetry());

        Assert.assertTrue(update.getCurrentCard().isPresent());
        DeviceCardEntity card = update.getCurrentCard().get();
        Assert.assertEquals("869624060285951", card.getDeviceId());
        Assert.assertEquals("89861123242042925346", card.getIccid());
        Assert.assertEquals(Integer.valueOf(0), card.getSlot());
        Assert.assertEquals(Integer.valueOf(1), card.getUseState());
    }

    @Test
    void shouldExposeLsxProfileInDeviceDetail() {
        DeviceInstanceEntity current = DeviceInstanceEntity.of();
        current.setId("869624060285951");

        LsxDevicePropertySynchronizer.LsxDevicePropertyUpdate update =
            LsxDevicePropertySynchronizer.applyReportedProperties(current, realDevicePayload());

        DeviceCardEntity card = update.getCurrentCard().orElseThrow(AssertionError::new);
        DeviceDetail detail = new DeviceDetail()
            .with(update.getDevice())
            .withCards(Collections.singletonList(card));

        Assert.assertEquals("D0:A0:D6:8C:B4:C8", detail.getMac());
        Assert.assertEquals("869624060285951", detail.getImei());
        Assert.assertEquals("11.0.0.183(H72SP1C00)", detail.getFirmwareVersion());
        Assert.assertEquals(DigestUtils.md5Hex("D0:A0:D6:8C:B4:C8"), detail.getSubDomain());
        Assert.assertEquals(1, detail.getCards().size());
        Assert.assertEquals("89861123242042925346", detail.getCards().get(0).getIccid());
    }

    @Test
    void shouldExposeCustomerOwnerUserIdColumn() throws NoSuchFieldException {
        Field field = DeviceInstanceEntity.class.getDeclaredField("userId");
        Column column = field.getAnnotation(Column.class);

        Assert.assertNotNull(column);
        Assert.assertEquals("user_id", column.name());
    }

    private Map<String, Object> realDevicePayload() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("iccid", "89861123242042925346");
        payload.put("mac", "D0:A0:D6:8C:B4:C8");
        payload.put("imei", "869624060285951");
        payload.put("ip", "192.168.110.218");
        payload.put("version", "11.0.0.183(H72SP1C00)");
        payload.put("t24g_num", 0);
        payload.put("t5g_num", 0);
        payload.put("rsrp", -83);
        payload.put("rsrq", -4);
        payload.put("sinr", 30);
        payload.put("network", "5G");
        payload.put("slot", 0);
        payload.put("gpsloc", "5CC300");
        payload.put("webpwd", "testpaddword");
        payload.put("operator", "CT");
        payload.put("switch_state", 0);
        payload.put("ping_addr", "127.0.0.1\n");
        payload.put("ping_retry", 5);
        return payload;
    }
}
