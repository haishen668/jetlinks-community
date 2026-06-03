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
package org.jetlinks.community.device.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.codec.digest.DigestUtils;
import org.hswebframework.ezorm.rdb.mapping.annotation.Comment;
import org.hswebframework.ezorm.rdb.mapping.annotation.DefaultValue;
import org.hswebframework.web.api.crud.entity.GenericEntity;
import org.hswebframework.web.api.crud.entity.RecordCreationEntity;
import org.hswebframework.web.api.crud.entity.RecordModifierEntity;
import org.hswebframework.web.crud.annotation.EnableEntityEvent;
import org.hswebframework.web.crud.generator.Generators;
import org.springframework.util.StringUtils;

import javax.persistence.Column;
import javax.persistence.Index;
import javax.persistence.Table;
import java.math.BigDecimal;

@Getter
@Setter
@Table(name = "dev_device_card", indexes = {
    @Index(name = "idx_dev_card_device_id", columnList = "device_id"),
    @Index(name = "idx_dev_card_device_iccid", columnList = "device_id,iccid")
})
@Comment("设备SIM卡状态表")
@EnableEntityEvent
public class DeviceCardEntity extends GenericEntity<String> implements RecordCreationEntity, RecordModifierEntity {

    @Override
    public String getId() {
        if (!StringUtils.hasText(super.getId()) && StringUtils.hasText(deviceId) && StringUtils.hasText(iccid)) {
            super.setId(createId(deviceId, iccid));
        }
        return super.getId();
    }

    @Column(name = "device_id", length = 64, nullable = false)
    @Schema(description = "设备ID")
    private String deviceId;

    @Column(length = 64, nullable = false)
    @Schema(description = "ICCID")
    private String iccid;

    @Column
    @Schema(description = "运营商")
    private Integer operator;

    @Column
    @Schema(description = "MSISDN")
    private String msisdn;

    @Column
    @Schema(description = "IMSI")
    private String imsi;

    @Column
    @Schema(description = "卡槽")
    private Integer slot;

    @Column(name = "use_state")
    @Schema(description = "使用状态 0未使用 1当前使用")
    private Integer useState;

    @Column
    @Schema(description = "卡状态")
    private Integer status;

    @Column(name = "used_flow")
    @Schema(description = "已用流量")
    private BigDecimal usedFlow;

    @Column(updatable = false)
    @DefaultValue(generator = Generators.CURRENT_TIME)
    @Schema(description = "创建时间")
    private Long createTime;

    @Column
    @DefaultValue(generator = Generators.CURRENT_TIME)
    @Schema(description = "修改时间")
    private Long modifyTime;

    @Column(length = 64)
    @Schema(description = "创建人ID")
    private String creatorId;

    @Column(length = 64)
    @Schema(description = "修改人ID")
    private String modifierId;

    public static String createId(String deviceId, String iccid) {
        return DigestUtils.md5Hex(deviceId + ":" + iccid);
    }
}
