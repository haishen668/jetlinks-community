/*
 * Decompiled with CFR 0.152.
 */
package org.jetlinks.community.device.response;

import org.jetlinks.community.device.response.BaiduLocationContent;

public class BaiduLbsResponse {
    private String address;
    private BaiduLocationContent content;
    private Integer status;

    public static BaiduLbsResponseBuilder builder() {
        return new BaiduLbsResponseBuilder();
    }

    public String getAddress() {
        return this.address;
    }

    public BaiduLocationContent getContent() {
        return this.content;
    }

    public Integer getStatus() {
        return this.status;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setContent(BaiduLocationContent content) {
        this.content = content;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof BaiduLbsResponse)) {
            return false;
        }
        BaiduLbsResponse other = (BaiduLbsResponse)o;
        if (!other.canEqual(this)) {
            return false;
        }
        Integer this$status = this.getStatus();
        Integer other$status = other.getStatus();
        if (this$status == null ? other$status != null : !((Object)this$status).equals(other$status)) {
            return false;
        }
        String this$address = this.getAddress();
        String other$address = other.getAddress();
        if (this$address == null ? other$address != null : !this$address.equals(other$address)) {
            return false;
        }
        BaiduLocationContent this$content = this.getContent();
        BaiduLocationContent other$content = other.getContent();
        return !(this$content == null ? other$content != null : !((Object)this$content).equals(other$content));
    }

    protected boolean canEqual(Object other) {
        return other instanceof BaiduLbsResponse;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        Integer $status = this.getStatus();
        result = result * 59 + ($status == null ? 43 : ((Object)$status).hashCode());
        String $address = this.getAddress();
        result = result * 59 + ($address == null ? 43 : $address.hashCode());
        BaiduLocationContent $content = this.getContent();
        result = result * 59 + ($content == null ? 43 : ((Object)$content).hashCode());
        return result;
    }

    public String toString() {
        return "BaiduLbsResponse(address=" + this.getAddress() + ", content=" + this.getContent() + ", status=" + this.getStatus() + ")";
    }

    public BaiduLbsResponse(String address, BaiduLocationContent content, Integer status) {
        this.address = address;
        this.content = content;
        this.status = status;
    }

    public BaiduLbsResponse() {
    }

    public static class BaiduLbsResponseBuilder {
        private String address;
        private BaiduLocationContent content;
        private Integer status;

        BaiduLbsResponseBuilder() {
        }

        public BaiduLbsResponseBuilder address(String address) {
            this.address = address;
            return this;
        }

        public BaiduLbsResponseBuilder content(BaiduLocationContent content) {
            this.content = content;
            return this;
        }

        public BaiduLbsResponseBuilder status(Integer status) {
            this.status = status;
            return this;
        }

        public BaiduLbsResponse build() {
            return new BaiduLbsResponse(this.address, this.content, this.status);
        }

        public String toString() {
            return "BaiduLbsResponse.BaiduLbsResponseBuilder(address=" + this.address + ", content=" + this.content + ", status=" + this.status + ")";
        }
    }
}

