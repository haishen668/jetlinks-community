/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.swagger.v3.oas.annotations.media.Schema
 *  javax.persistence.Column
 *  javax.persistence.Table
 *  javax.validation.constraints.NotBlank
 *  javax.validation.constraints.NotNull
 *  org.apache.commons.collections.CollectionUtils
 *  org.hswebframework.ezorm.rdb.mapping.annotation.ColumnType
 *  org.hswebframework.ezorm.rdb.mapping.annotation.Comment
 *  org.hswebframework.ezorm.rdb.mapping.annotation.DefaultValue
 *  org.hswebframework.ezorm.rdb.mapping.annotation.EnumCodec
 *  org.hswebframework.ezorm.rdb.mapping.annotation.JsonCodec
 *  org.hswebframework.web.api.crud.entity.GenericEntity
 *  org.hswebframework.web.api.crud.entity.RecordCreationEntity
 *  org.hswebframework.web.api.crud.entity.RecordModifierEntity
 *  org.hswebframework.web.crud.annotation.EnableEntityEvent
 *  org.hswebframework.web.exception.BusinessException
 *  org.hswebframework.web.validator.CreateGroup
 *  org.jetlinks.community.TimerSpec$PeriodUnit
 *  org.jetlinks.rule.engine.api.model.RuleModel
 *  org.jetlinks.rule.engine.cluster.RuleInstance
 */
package org.jetlinks.community.rule.engine.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import java.sql.JDBCType;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import org.apache.commons.collections.CollectionUtils;
import org.hswebframework.ezorm.rdb.mapping.annotation.ColumnType;
import org.hswebframework.ezorm.rdb.mapping.annotation.Comment;
import org.hswebframework.ezorm.rdb.mapping.annotation.DefaultValue;
import org.hswebframework.ezorm.rdb.mapping.annotation.EnumCodec;
import org.hswebframework.ezorm.rdb.mapping.annotation.JsonCodec;
import org.hswebframework.web.api.crud.entity.GenericEntity;
import org.hswebframework.web.api.crud.entity.RecordCreationEntity;
import org.hswebframework.web.api.crud.entity.RecordModifierEntity;
import org.hswebframework.web.crud.annotation.EnableEntityEvent;
import org.hswebframework.web.exception.BusinessException;
import org.hswebframework.web.validator.CreateGroup;
import org.jetlinks.community.TimerSpec;
import org.jetlinks.community.rule.engine.enums.RuleInstanceState;
import org.jetlinks.community.rule.engine.scene.SceneAction;
import org.jetlinks.community.rule.engine.scene.SceneRule;
import org.jetlinks.community.rule.engine.scene.Trigger;
import org.jetlinks.rule.engine.api.model.RuleModel;
import org.jetlinks.rule.engine.cluster.RuleInstance;

@Table(name="dev_device_job")
@EnableEntityEvent
public class DeviceJob
extends GenericEntity<String>
implements RecordCreationEntity,
RecordModifierEntity {
    @Comment(value="\u8bbe\u5907ID")
    @Column(length=64, nullable=false, updatable=false)
    @NotBlank(message="[deviceId]\u4e0d\u80fd\u4e3a\u7a7a", groups={CreateGroup.class})
    @Schema(description="\u8bbe\u5907ID")
    private @NotBlank(message="[deviceId]\u4e0d\u80fd\u4e3a\u7a7a", groups={CreateGroup.class}) String deviceId;
    @Column
    @JsonCodec
    @ColumnType(javaType=String.class, jdbcType=JDBCType.LONGVARCHAR)
    @Schema(description="\u89e6\u53d1\u5668")
    @NotNull
    private Trigger trigger;
    @Column
    @Schema(description="\u6267\u884c\u6a21\u5f0f,\u4e00\u6b21\u8fd8\u662f\u5468\u671f\u6267\u884c")
    @NotNull
    private String mod;
    @Column
    @Schema(description="\u6267\u884c\u5468\u671f:week,\u5468\u671f:month")
    @NotNull
    private String period;
    @Column
    @Schema(description="\u6267\u884c\u7684\u65f6\u95f4.\u4e3a\u7a7a\u5219\u8868\u793a\u6bcf\u5929,\u6267\u884c\u5468\u671f\u4e3a[week]\u5219\u4e3a1-7,\u6267\u884c\u5468\u671f\u4e3a[month]\u65f6\u5219\u4e3a1-31")
    @NotNull
    private String periodWhen;
    @Schema(description="\u6267\u884c\u65f6\u95f4\u8303\u56f4\u4ece.\u683c\u5f0f:[hh:mm],\u6216\u8005[hh:mm:ss]")
    private String periodFrom;
    @Schema(description="\u6267\u884c\u65f6\u95f4\u8303\u56f4\u6b62.\u683c\u5f0f:[hh:mm],\u6216\u8005[hh:mm:ss]")
    private String periodTo;
    @Schema(description="\u5468\u671f\u503c\uff0c\u5982:\u6bcf[every][unit]\u6267\u884c\u4e00\u6b21")
    private int periodEvery;
    @Schema(description="\u5468\u671f\u6267\u884c\u5355\u4f4d")
    private TimerSpec.PeriodUnit periodUnit;
    @Column
    @Schema(description="\u5355\u6b21\u6267\u884c\u65f6\u95f4")
    private String onceTime;
    @Column
    @JsonCodec
    @ColumnType(javaType=String.class, jdbcType=JDBCType.LONGVARCHAR)
    @Schema(description="\u6267\u884c\u52a8\u4f5c")
    private List<SceneAction> actions;
    @Column(length=64, updatable=false)
    @Schema(description="\u521b\u5efa\u4eba")
    private String creatorId;
    @Column(updatable=false)
    @Schema(description="\u521b\u5efa\u65f6\u95f4")
    @DefaultValue(generator="current_time")
    private Long createTime;
    @Column(length=64)
    @Schema(description="\u4fee\u6539\u4eba")
    private String modifierId;
    @Column
    @Schema(description="\u4fee\u6539\u65f6\u95f4")
    @DefaultValue(generator="current_time")
    private Long modifyTime;
    @Column
    @Schema(description="\u542f\u52a8\u65f6\u95f4")
    private Long startTime;
    @Schema(description="\u72b6\u6001")
    @Column(length=32, nullable=false)
    @EnumCodec
    @ColumnType(javaType=String.class)
    @NotBlank
    @DefaultValue(value="disable")
    private RuleInstanceState state;
    @Column
    @Schema(description="\u8bf4\u660e")
    private String description;
    private Integer index;

    public static DeviceJob of(DeviceJob deviceJob, Integer index) {
        deviceJob.setIndex(index);
        return deviceJob;
    }

    public RuleInstance toRule() {
        SceneRule rule = (SceneRule)this.copyTo(new SceneRule(), new String[0]);
        rule.setName(this.getDeviceId() + "\u5b9a\u65f6\u4efb\u52a1_" + (String)this.getId());
        RuleInstance instance = new RuleInstance();
        instance.setId((String)this.getId());
        RuleModel model = rule.toModel();
        model.addConfiguration("creatorId", (Object)this.modifierId);
        model.addConfiguration("name", (Object)(this.getDeviceId() + "_" + (String)this.getId()));
        instance.setModel(model);
        return instance;
    }

    public void validate() {
        this.getTrigger().validate();
        if (CollectionUtils.isEmpty(this.getActions())) {
            throw new BusinessException("\u6267\u884c\u52a8\u4f5c\u4e0d\u80fd\u4e3a\u7a7a");
        }
    }

    public String getDeviceId() {
        return this.deviceId;
    }

    public Trigger getTrigger() {
        return this.trigger;
    }

    public String getMod() {
        return this.mod;
    }

    public String getPeriod() {
        return this.period;
    }

    public String getPeriodWhen() {
        return this.periodWhen;
    }

    public String getPeriodFrom() {
        return this.periodFrom;
    }

    public String getPeriodTo() {
        return this.periodTo;
    }

    public int getPeriodEvery() {
        return this.periodEvery;
    }

    public TimerSpec.PeriodUnit getPeriodUnit() {
        return this.periodUnit;
    }

    public String getOnceTime() {
        return this.onceTime;
    }

    public List<SceneAction> getActions() {
        return this.actions;
    }

    public String getCreatorId() {
        return this.creatorId;
    }

    public Long getCreateTime() {
        return this.createTime;
    }

    public String getModifierId() {
        return this.modifierId;
    }

    public Long getModifyTime() {
        return this.modifyTime;
    }

    public Long getStartTime() {
        return this.startTime;
    }

    public RuleInstanceState getState() {
        return this.state;
    }

    public String getDescription() {
        return this.description;
    }

    public Integer getIndex() {
        return this.index;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public void setTrigger(Trigger trigger) {
        this.trigger = trigger;
    }

    public void setMod(String mod) {
        this.mod = mod;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public void setPeriodWhen(String periodWhen) {
        this.periodWhen = periodWhen;
    }

    public void setPeriodFrom(String periodFrom) {
        this.periodFrom = periodFrom;
    }

    public void setPeriodTo(String periodTo) {
        this.periodTo = periodTo;
    }

    public void setPeriodEvery(int periodEvery) {
        this.periodEvery = periodEvery;
    }

    public void setPeriodUnit(TimerSpec.PeriodUnit periodUnit) {
        this.periodUnit = periodUnit;
    }

    public void setOnceTime(String onceTime) {
        this.onceTime = onceTime;
    }

    public void setActions(List<SceneAction> actions) {
        this.actions = actions;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public void setModifierId(String modifierId) {
        this.modifierId = modifierId;
    }

    public void setModifyTime(Long modifyTime) {
        this.modifyTime = modifyTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public void setState(RuleInstanceState state) {
        this.state = state;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }
}

