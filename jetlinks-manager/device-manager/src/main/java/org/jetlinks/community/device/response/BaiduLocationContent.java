/*
 * Decompiled with CFR 0.152.
 */
package org.jetlinks.community.device.response;

import org.jetlinks.community.device.response.BaiduLocationAddressDetail;
import org.jetlinks.community.device.response.BaiduLocationPoint;

public class BaiduLocationContent {
    private String address;
    private BaiduLocationAddressDetail address_detail;
    private BaiduLocationPoint point;

    public static BaiduLocationContentBuilder builder() {
        return new BaiduLocationContentBuilder();
    }

    public String getAddress() {
        return this.address;
    }

    public BaiduLocationAddressDetail getAddress_detail() {
        return this.address_detail;
    }

    public BaiduLocationPoint getPoint() {
        return this.point;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setAddress_detail(BaiduLocationAddressDetail address_detail) {
        this.address_detail = address_detail;
    }

    public void setPoint(BaiduLocationPoint point) {
        this.point = point;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof BaiduLocationContent)) {
            return false;
        }
        BaiduLocationContent other = (BaiduLocationContent)o;
        if (!other.canEqual(this)) {
            return false;
        }
        String this$address = this.getAddress();
        String other$address = other.getAddress();
        if (this$address == null ? other$address != null : !this$address.equals(other$address)) {
            return false;
        }
        BaiduLocationAddressDetail this$address_detail = this.getAddress_detail();
        BaiduLocationAddressDetail other$address_detail = other.getAddress_detail();
        if (this$address_detail == null ? other$address_detail != null : !((Object)this$address_detail).equals(other$address_detail)) {
            return false;
        }
        BaiduLocationPoint this$point = this.getPoint();
        BaiduLocationPoint other$point = other.getPoint();
        return !(this$point == null ? other$point != null : !((Object)this$point).equals(other$point));
    }

    protected boolean canEqual(Object other) {
        return other instanceof BaiduLocationContent;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        String $address = this.getAddress();
        result = result * 59 + ($address == null ? 43 : $address.hashCode());
        BaiduLocationAddressDetail $address_detail = this.getAddress_detail();
        result = result * 59 + ($address_detail == null ? 43 : ((Object)$address_detail).hashCode());
        BaiduLocationPoint $point = this.getPoint();
        result = result * 59 + ($point == null ? 43 : ((Object)$point).hashCode());
        return result;
    }

    public String toString() {
        return "BaiduLocationContent(address=" + this.getAddress() + ", address_detail=" + this.getAddress_detail() + ", point=" + this.getPoint() + ")";
    }

    public BaiduLocationContent(String address, BaiduLocationAddressDetail address_detail, BaiduLocationPoint point) {
        this.address = address;
        this.address_detail = address_detail;
        this.point = point;
    }

    public BaiduLocationContent() {
    }

    public static class BaiduLocationContentBuilder {
        private String address;
        private BaiduLocationAddressDetail address_detail;
        private BaiduLocationPoint point;

        BaiduLocationContentBuilder() {
        }

        public BaiduLocationContentBuilder address(String address) {
            this.address = address;
            return this;
        }

        public BaiduLocationContentBuilder address_detail(BaiduLocationAddressDetail address_detail) {
            this.address_detail = address_detail;
            return this;
        }

        public BaiduLocationContentBuilder point(BaiduLocationPoint point) {
            this.point = point;
            return this;
        }

        public BaiduLocationContent build() {
            return new BaiduLocationContent(this.address, this.address_detail, this.point);
        }

        public String toString() {
            return "BaiduLocationContent.BaiduLocationContentBuilder(address=" + this.address + ", address_detail=" + this.address_detail + ", point=" + this.point + ")";
        }
    }
}

