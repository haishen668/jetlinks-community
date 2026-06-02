/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.swagger.v3.oas.annotations.media.Schema
 *  javax.persistence.Column
 *  javax.persistence.Index
 *  javax.persistence.Table
 *  javax.validation.constraints.NotBlank
 *  org.hswebframework.ezorm.rdb.mapping.annotation.Comment
 *  org.hswebframework.ezorm.rdb.mapping.annotation.DefaultValue
 *  org.hswebframework.web.api.crud.entity.GenericEntity
 *  org.hswebframework.web.crud.annotation.EnableEntityEvent
 *  org.hswebframework.web.validator.CreateGroup
 */
package org.jetlinks.community.device.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import org.hswebframework.ezorm.rdb.mapping.annotation.Comment;
import org.hswebframework.ezorm.rdb.mapping.annotation.DefaultValue;
import org.hswebframework.web.api.crud.entity.GenericEntity;
import org.hswebframework.web.crud.annotation.EnableEntityEvent;
import org.hswebframework.web.validator.CreateGroup;

@Table(name="dev_device_card", indexes={@Index(name="dev_dev_card_uk", columnList="device_id,iccid")})
@EnableEntityEvent
public class DeviceCardEntity
extends GenericEntity<String> {
    @Comment(value="\u8bbe\u5907ID")
    @Column(length=64, nullable=false, updatable=false)
    @NotBlank(message="[deviceId]\u4e0d\u80fd\u4e3a\u7a7a", groups={CreateGroup.class})
    @Schema(description="\u8bbe\u5907ID")
    private @NotBlank(message="[deviceId]\u4e0d\u80fd\u4e3a\u7a7a", groups={CreateGroup.class}) String deviceId;
    @Comment(value="ICCID")
    @Column(name="iccid", length=64, nullable=false)
    @NotBlank(message="[iccid]\u4e0d\u80fd\u4e3a\u7a7a", groups={CreateGroup.class})
    @Schema(description="ICCID")
    private @NotBlank(message="[iccid]\u4e0d\u80fd\u4e3a\u7a7a", groups={CreateGroup.class}) String iccid;
    @Comment(value="\u8fd0\u8425\u5546(1\u79fb\u52a8 2\u8054\u901a 3\u7535\u4fe1)")
    @Column(name="operator")
    @Schema(description="\u8fd0\u8425\u5546(1\u79fb\u52a8 2\u8054\u901a 3\u7535\u4fe1)")
    private Integer operator;
    @Comment(value="\u4e1a\u52a1\u53f7\u7801")
    @Column(name="msisdn")
    @Schema(description="\u4e1a\u52a1\u53f7\u7801")
    private String msisdn;
    @Comment(value="IMSI")
    @Column(name="imsi")
    @Schema(description="IMSI")
    private String imsi;
    @Comment(value="\u5361\u69fd(1\u62d4\u63d2 2\u5185\u7f6e)")
    @Column(name="slot")
    @Schema(description="\u5361\u69fd(1\u62d4\u63d2 2\u5185\u7f6e)")
    private Integer slot;
    @Comment(value="\u4f7f\u7528\u72b6\u6001(0\u672a\u4f7f\u7528 1\u5df2\u4f7f\u7528)")
    @Column(name="use_state")
    @Schema(description="\u4f7f\u7528\u72b6\u6001(0\u672a\u4f7f\u7528 1\u5df2\u4f7f\u7528)")
    private Integer useState;
    @Comment(value="\u5361\u72b6\u6001")
    @Column(name="status")
    @Schema(description="\u5361\u72b6\u6001")
    private Integer status;
    @Comment(value="\u5df2\u4f7f\u7528\u6d41\u91cf")
    @Column(name="used_flow")
    @Schema(description="\u5df2\u4f7f\u7528\u6d41\u91cf")
    private BigDecimal usedFlow;
    @Column(name="create_time", updatable=false)
    @DefaultValue(generator="current_time")
    @Schema(description="\u521b\u5efa\u65f6\u95f4(\u53ea\u8bfb)")
    private Date createTime;
    @Column(name="modify_time")
    @DefaultValue(generator="current_time")
    @Schema(description="\u4fee\u6539\u65f6\u95f4")
    private Date modifyTime;

    public String getDeviceId() {
        return this.deviceId;
    }

    public String getIccid() {
        return this.iccid;
    }

    public Integer getOperator() {
        return this.operator;
    }

    public String getMsisdn() {
        return this.msisdn;
    }

    public String getImsi() {
        return this.imsi;
    }

    public Integer getSlot() {
        return this.slot;
    }

    public Integer getUseState() {
        return this.useState;
    }

    public Integer getStatus() {
        return this.status;
    }

    public BigDecimal getUsedFlow() {
        return this.usedFlow;
    }

    public Date getCreateTime() {
        return this.createTime;
    }

    public Date getModifyTime() {
        return this.modifyTime;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public void setIccid(String iccid) {
        this.iccid = iccid;
    }

    public void setOperator(Integer operator) {
        this.operator = operator;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    public void setImsi(String imsi) {
        this.imsi = imsi;
    }

    public void setSlot(Integer slot) {
        this.slot = slot;
    }

    public void setUseState(Integer useState) {
        this.useState = useState;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public void setUsedFlow(BigDecimal usedFlow) {
        this.usedFlow = usedFlow;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public void setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
    }
}

