/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetlinks.community.auth.entity.UserDetail
 */
package org.jetlinks.community.device.entity;

import org.jetlinks.community.auth.entity.UserDetail;
import org.jetlinks.community.device.entity.DeviceCardEntity;
import org.jetlinks.community.device.entity.DeviceInstanceEntity;

public class CustomerDevice
extends DeviceInstanceEntity {
    private Integer index;
    private UserDetail userDetail;
    private DeviceCardEntity deviceCard;
    private DeviceCardEntity deviceCard1;

    public static CustomerDevice of(CustomerDevice customerDevice, int index) {
        customerDevice.setIndex(index);
        return customerDevice;
    }

    public Integer getIndex() {
        return this.index;
    }

    public UserDetail getUserDetail() {
        return this.userDetail;
    }

    public DeviceCardEntity getDeviceCard() {
        return this.deviceCard;
    }

    public DeviceCardEntity getDeviceCard1() {
        return this.deviceCard1;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public void setUserDetail(UserDetail userDetail) {
        this.userDetail = userDetail;
    }

    public void setDeviceCard(DeviceCardEntity deviceCard) {
        this.deviceCard = deviceCard;
    }

    public void setDeviceCard1(DeviceCardEntity deviceCard1) {
        this.deviceCard1 = deviceCard1;
    }
}

