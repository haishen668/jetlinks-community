/*
 * Decompiled with CFR 0.152.
 */
package org.jetlinks.community.device.response;

import org.jetlinks.community.device.response.AmapRegeoAddressComponent;

public class AmapRegeoCode {
    private String formatted_address;
    private AmapRegeoAddressComponent addressComponent;

    public static AmapRegeoCodeBuilder builder() {
        return new AmapRegeoCodeBuilder();
    }

    public String getFormatted_address() {
        return this.formatted_address;
    }

    public AmapRegeoAddressComponent getAddressComponent() {
        return this.addressComponent;
    }

    public void setFormatted_address(String formatted_address) {
        this.formatted_address = formatted_address;
    }

    public void setAddressComponent(AmapRegeoAddressComponent addressComponent) {
        this.addressComponent = addressComponent;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof AmapRegeoCode)) {
            return false;
        }
        AmapRegeoCode other = (AmapRegeoCode)o;
        if (!other.canEqual(this)) {
            return false;
        }
        String this$formatted_address = this.getFormatted_address();
        String other$formatted_address = other.getFormatted_address();
        if (this$formatted_address == null ? other$formatted_address != null : !this$formatted_address.equals(other$formatted_address)) {
            return false;
        }
        AmapRegeoAddressComponent this$addressComponent = this.getAddressComponent();
        AmapRegeoAddressComponent other$addressComponent = other.getAddressComponent();
        return !(this$addressComponent == null ? other$addressComponent != null : !((Object)this$addressComponent).equals(other$addressComponent));
    }

    protected boolean canEqual(Object other) {
        return other instanceof AmapRegeoCode;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        String $formatted_address = this.getFormatted_address();
        result = result * 59 + ($formatted_address == null ? 43 : $formatted_address.hashCode());
        AmapRegeoAddressComponent $addressComponent = this.getAddressComponent();
        result = result * 59 + ($addressComponent == null ? 43 : ((Object)$addressComponent).hashCode());
        return result;
    }

    public String toString() {
        return "AmapRegeoCode(formatted_address=" + this.getFormatted_address() + ", addressComponent=" + this.getAddressComponent() + ")";
    }

    public AmapRegeoCode(String formatted_address, AmapRegeoAddressComponent addressComponent) {
        this.formatted_address = formatted_address;
        this.addressComponent = addressComponent;
    }

    public AmapRegeoCode() {
    }

    public static class AmapRegeoCodeBuilder {
        private String formatted_address;
        private AmapRegeoAddressComponent addressComponent;

        AmapRegeoCodeBuilder() {
        }

        public AmapRegeoCodeBuilder formatted_address(String formatted_address) {
            this.formatted_address = formatted_address;
            return this;
        }

        public AmapRegeoCodeBuilder addressComponent(AmapRegeoAddressComponent addressComponent) {
            this.addressComponent = addressComponent;
            return this;
        }

        public AmapRegeoCode build() {
            return new AmapRegeoCode(this.formatted_address, this.addressComponent);
        }

        public String toString() {
            return "AmapRegeoCode.AmapRegeoCodeBuilder(formatted_address=" + this.formatted_address + ", addressComponent=" + this.addressComponent + ")";
        }
    }
}

