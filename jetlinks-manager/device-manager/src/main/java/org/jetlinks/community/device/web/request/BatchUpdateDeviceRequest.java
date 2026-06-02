/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.swagger.v3.oas.annotations.media.Schema
 */
package org.jetlinks.community.device.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public class BatchUpdateDeviceRequest {
    @Schema(description="\u8bbe\u5907ID\u5217\u8868")
    private List<String> ids;
    @Schema(description="\u8bf4\u660e")
    private String describe;
    @Schema(description="\u6240\u5c5e\u4ebaID")
    private String userId;
    @Schema(description="\u4fee\u6539\u4ebaID")
    private String modifyUserId;

    public List<String> getIds() {
        return this.ids;
    }

    public String getDescribe() {
        return this.describe;
    }

    public String getUserId() {
        return this.userId;
    }

    public String getModifyUserId() {
        return this.modifyUserId;
    }

    public void setIds(List<String> ids) {
        this.ids = ids;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setModifyUserId(String modifyUserId) {
        this.modifyUserId = modifyUserId;
    }
}

