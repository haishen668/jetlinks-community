/*
 * Decompiled with CFR 0.152.
 */
package org.jetlinks.community.rule.engine.model;

import java.io.Serializable;

public class DeviceJobLog
implements Serializable {
    private Integer index;
    private Long createTime;
    private String params;
    private String messageId;
    private String functionId;
    private Boolean success;

    public static DeviceJobLog of(DeviceJobLog deviceJobLog, Integer index) {
        deviceJobLog.setIndex(index);
        return deviceJobLog;
    }

    public static DeviceJobLog of(Long createTime, String params, String messageId, String functionId, Boolean success) {
        DeviceJobLog obj = new DeviceJobLog();
        obj.createTime = createTime;
        obj.params = params;
        obj.messageId = messageId;
        obj.functionId = functionId;
        obj.success = success;
        return obj;
    }

    public Integer getIndex() {
        return this.index;
    }

    public Long getCreateTime() {
        return this.createTime;
    }

    public String getParams() {
        return this.params;
    }

    public String getMessageId() {
        return this.messageId;
    }

    public String getFunctionId() {
        return this.functionId;
    }

    public Boolean getSuccess() {
        return this.success;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public void setFunctionId(String functionId) {
        this.functionId = functionId;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }
}

