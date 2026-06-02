/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.swagger.v3.oas.annotations.media.Schema
 *  javax.persistence.Column
 *  javax.persistence.Table
 *  javax.validation.constraints.NotBlank
 *  org.hswebframework.ezorm.rdb.mapping.annotation.ColumnType
 *  org.hswebframework.ezorm.rdb.mapping.annotation.DefaultValue
 *  org.hswebframework.ezorm.rdb.mapping.annotation.EnumCodec
 *  org.hswebframework.ezorm.rdb.mapping.annotation.JsonCodec
 *  org.hswebframework.web.api.crud.entity.GenericEntity
 *  org.hswebframework.web.validator.CreateGroup
 */
package org.jetlinks.community.notify.manager.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import java.sql.JDBCType;
import java.util.Map;
import javax.persistence.Column;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import org.hswebframework.ezorm.rdb.mapping.annotation.ColumnType;
import org.hswebframework.ezorm.rdb.mapping.annotation.DefaultValue;
import org.hswebframework.ezorm.rdb.mapping.annotation.EnumCodec;
import org.hswebframework.ezorm.rdb.mapping.annotation.JsonCodec;
import org.hswebframework.web.api.crud.entity.GenericEntity;
import org.hswebframework.web.validator.CreateGroup;
import org.jetlinks.community.notify.manager.enums.NotifyChannelState;

@Table(name="notify_channel")
@Schema(description="\u901a\u77e5\u901a\u9053(\u914d\u7f6e)")
public class NotifyChannelEntity
extends GenericEntity<String> {
    @Column(nullable=false, length=32)
    @NotBlank(groups={CreateGroup.class})
    @Schema(description="\u540d\u79f0")
    private String name;
    @Column(nullable=false, length=32, updatable=false)
    @NotBlank(groups={CreateGroup.class})
    @Schema(description="\u4e3b\u9898\u63d0\u4f9b\u5546\u6807\u8bc6")
    private String topicProvider;
    @Column(nullable=false, length=32)
    @NotBlank(groups={CreateGroup.class})
    @Schema(description="\u4e3b\u9898\u63d0\u4f9b\u5546\u540d\u79f0")
    private String topicName;
    @Column(nullable=false, length=32, updatable=false)
    @NotBlank(groups={CreateGroup.class})
    @Schema(description="\u901a\u77e5\u7c7b\u578b")
    private String channelProvider;
    @Column
    @JsonCodec
    @ColumnType(jdbcType=JDBCType.LONGVARCHAR, javaType=String.class)
    @Schema(description="\u901a\u77e5\u914d\u7f6e")
    private Map<String, Object> channelConfiguration;
    @Column(length=32)
    @EnumCodec
    @ColumnType(javaType=String.class)
    @DefaultValue(value="enabled")
    @Schema(description="\u72b6\u6001")
    private NotifyChannelState state;

    public String getName() {
        return this.name;
    }

    public String getTopicProvider() {
        return this.topicProvider;
    }

    public String getTopicName() {
        return this.topicName;
    }

    public String getChannelProvider() {
        return this.channelProvider;
    }

    public Map<String, Object> getChannelConfiguration() {
        return this.channelConfiguration;
    }

    public NotifyChannelState getState() {
        return this.state;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTopicProvider(String topicProvider) {
        this.topicProvider = topicProvider;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public void setChannelProvider(String channelProvider) {
        this.channelProvider = channelProvider;
    }

    public void setChannelConfiguration(Map<String, Object> channelConfiguration) {
        this.channelConfiguration = channelConfiguration;
    }

    public void setState(NotifyChannelState state) {
        this.state = state;
    }
}

