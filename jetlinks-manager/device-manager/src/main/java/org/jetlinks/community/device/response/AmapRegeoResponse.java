/*
 * Decompiled with CFR 0.152.
 */
package org.jetlinks.community.device.response;

import org.jetlinks.community.device.response.AmapRegeoCode;

public class AmapRegeoResponse {
    private String info;
    private String infocode;
    private String status;
    private AmapRegeoCode regeocode;

    public static AmapRegeoResponseBuilder builder() {
        return new AmapRegeoResponseBuilder();
    }

    public String getInfo() {
        return this.info;
    }

    public String getInfocode() {
        return this.infocode;
    }

    public String getStatus() {
        return this.status;
    }

    public AmapRegeoCode getRegeocode() {
        return this.regeocode;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public void setInfocode(String infocode) {
        this.infocode = infocode;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setRegeocode(AmapRegeoCode regeocode) {
        this.regeocode = regeocode;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof AmapRegeoResponse)) {
            return false;
        }
        AmapRegeoResponse other = (AmapRegeoResponse)o;
        if (!other.canEqual(this)) {
            return false;
        }
        String this$info = this.getInfo();
        String other$info = other.getInfo();
        if (this$info == null ? other$info != null : !this$info.equals(other$info)) {
            return false;
        }
        String this$infocode = this.getInfocode();
        String other$infocode = other.getInfocode();
        if (this$infocode == null ? other$infocode != null : !this$infocode.equals(other$infocode)) {
            return false;
        }
        String this$status = this.getStatus();
        String other$status = other.getStatus();
        if (this$status == null ? other$status != null : !this$status.equals(other$status)) {
            return false;
        }
        AmapRegeoCode this$regeocode = this.getRegeocode();
        AmapRegeoCode other$regeocode = other.getRegeocode();
        return !(this$regeocode == null ? other$regeocode != null : !((Object)this$regeocode).equals(other$regeocode));
    }

    protected boolean canEqual(Object other) {
        return other instanceof AmapRegeoResponse;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        String $info = this.getInfo();
        result = result * 59 + ($info == null ? 43 : $info.hashCode());
        String $infocode = this.getInfocode();
        result = result * 59 + ($infocode == null ? 43 : $infocode.hashCode());
        String $status = this.getStatus();
        result = result * 59 + ($status == null ? 43 : $status.hashCode());
        AmapRegeoCode $regeocode = this.getRegeocode();
        result = result * 59 + ($regeocode == null ? 43 : ((Object)$regeocode).hashCode());
        return result;
    }

    public String toString() {
        return "AmapRegeoResponse(info=" + this.getInfo() + ", infocode=" + this.getInfocode() + ", status=" + this.getStatus() + ", regeocode=" + this.getRegeocode() + ")";
    }

    public AmapRegeoResponse(String info, String infocode, String status, AmapRegeoCode regeocode) {
        this.info = info;
        this.infocode = infocode;
        this.status = status;
        this.regeocode = regeocode;
    }

    public AmapRegeoResponse() {
    }

    public static class AmapRegeoResponseBuilder {
        private String info;
        private String infocode;
        private String status;
        private AmapRegeoCode regeocode;

        AmapRegeoResponseBuilder() {
        }

        public AmapRegeoResponseBuilder info(String info) {
            this.info = info;
            return this;
        }

        public AmapRegeoResponseBuilder infocode(String infocode) {
            this.infocode = infocode;
            return this;
        }

        public AmapRegeoResponseBuilder status(String status) {
            this.status = status;
            return this;
        }

        public AmapRegeoResponseBuilder regeocode(AmapRegeoCode regeocode) {
            this.regeocode = regeocode;
            return this;
        }

        public AmapRegeoResponse build() {
            return new AmapRegeoResponse(this.info, this.infocode, this.status, this.regeocode);
        }

        public String toString() {
            return "AmapRegeoResponse.AmapRegeoResponseBuilder(info=" + this.info + ", infocode=" + this.infocode + ", status=" + this.status + ", regeocode=" + this.regeocode + ")";
        }
    }
}

