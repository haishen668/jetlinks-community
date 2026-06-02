/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.persistence.Column
 */
package org.jetlinks.community.rule.engine.entity;

import javax.persistence.Column;
import org.jetlinks.community.rule.engine.entity.AlarmHandleHistoryEntity;

public class AlarmHandleHistoryInfo
extends AlarmHandleHistoryEntity {
    @Column
    private String deviceName;
    @Column
    private String userName;

    public String getDeviceName() {
        return this.deviceName;
    }

    public String getUserName() {
        return this.userName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}

