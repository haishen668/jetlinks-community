/*
 * Decompiled with CFR 0.152.
 */
package org.jetlinks.community.device.entity;

public class DevicePosition {
    private String lng;
    private String lat;
    private Long num;

    public String getLng() {
        return this.lng;
    }

    public String getLat() {
        return this.lat;
    }

    public Long getNum() {
        return this.num;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public void setNum(Long num) {
        this.num = num;
    }
}

