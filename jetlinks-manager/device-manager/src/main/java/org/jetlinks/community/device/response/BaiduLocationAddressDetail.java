/*
 * Decompiled with CFR 0.152.
 */
package org.jetlinks.community.device.response;

public class BaiduLocationAddressDetail {
    private String adcode;
    private String city;
    private String city_code;
    private String district;
    private String province;
    private String street;
    private String street_number;

    public static BaiduLocationAddressDetailBuilder builder() {
        return new BaiduLocationAddressDetailBuilder();
    }

    public String getAdcode() {
        return this.adcode;
    }

    public String getCity() {
        return this.city;
    }

    public String getCity_code() {
        return this.city_code;
    }

    public String getDistrict() {
        return this.district;
    }

    public String getProvince() {
        return this.province;
    }

    public String getStreet() {
        return this.street;
    }

    public String getStreet_number() {
        return this.street_number;
    }

    public void setAdcode(String adcode) {
        this.adcode = adcode;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setCity_code(String city_code) {
        this.city_code = city_code;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public void setStreet_number(String street_number) {
        this.street_number = street_number;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof BaiduLocationAddressDetail)) {
            return false;
        }
        BaiduLocationAddressDetail other = (BaiduLocationAddressDetail)o;
        if (!other.canEqual(this)) {
            return false;
        }
        String this$adcode = this.getAdcode();
        String other$adcode = other.getAdcode();
        if (this$adcode == null ? other$adcode != null : !this$adcode.equals(other$adcode)) {
            return false;
        }
        String this$city = this.getCity();
        String other$city = other.getCity();
        if (this$city == null ? other$city != null : !this$city.equals(other$city)) {
            return false;
        }
        String this$city_code = this.getCity_code();
        String other$city_code = other.getCity_code();
        if (this$city_code == null ? other$city_code != null : !this$city_code.equals(other$city_code)) {
            return false;
        }
        String this$district = this.getDistrict();
        String other$district = other.getDistrict();
        if (this$district == null ? other$district != null : !this$district.equals(other$district)) {
            return false;
        }
        String this$province = this.getProvince();
        String other$province = other.getProvince();
        if (this$province == null ? other$province != null : !this$province.equals(other$province)) {
            return false;
        }
        String this$street = this.getStreet();
        String other$street = other.getStreet();
        if (this$street == null ? other$street != null : !this$street.equals(other$street)) {
            return false;
        }
        String this$street_number = this.getStreet_number();
        String other$street_number = other.getStreet_number();
        return !(this$street_number == null ? other$street_number != null : !this$street_number.equals(other$street_number));
    }

    protected boolean canEqual(Object other) {
        return other instanceof BaiduLocationAddressDetail;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        String $adcode = this.getAdcode();
        result = result * 59 + ($adcode == null ? 43 : $adcode.hashCode());
        String $city = this.getCity();
        result = result * 59 + ($city == null ? 43 : $city.hashCode());
        String $city_code = this.getCity_code();
        result = result * 59 + ($city_code == null ? 43 : $city_code.hashCode());
        String $district = this.getDistrict();
        result = result * 59 + ($district == null ? 43 : $district.hashCode());
        String $province = this.getProvince();
        result = result * 59 + ($province == null ? 43 : $province.hashCode());
        String $street = this.getStreet();
        result = result * 59 + ($street == null ? 43 : $street.hashCode());
        String $street_number = this.getStreet_number();
        result = result * 59 + ($street_number == null ? 43 : $street_number.hashCode());
        return result;
    }

    public String toString() {
        return "BaiduLocationAddressDetail(adcode=" + this.getAdcode() + ", city=" + this.getCity() + ", city_code=" + this.getCity_code() + ", district=" + this.getDistrict() + ", province=" + this.getProvince() + ", street=" + this.getStreet() + ", street_number=" + this.getStreet_number() + ")";
    }

    public BaiduLocationAddressDetail(String adcode, String city, String city_code, String district, String province, String street, String street_number) {
        this.adcode = adcode;
        this.city = city;
        this.city_code = city_code;
        this.district = district;
        this.province = province;
        this.street = street;
        this.street_number = street_number;
    }

    public BaiduLocationAddressDetail() {
    }

    public static class BaiduLocationAddressDetailBuilder {
        private String adcode;
        private String city;
        private String city_code;
        private String district;
        private String province;
        private String street;
        private String street_number;

        BaiduLocationAddressDetailBuilder() {
        }

        public BaiduLocationAddressDetailBuilder adcode(String adcode) {
            this.adcode = adcode;
            return this;
        }

        public BaiduLocationAddressDetailBuilder city(String city) {
            this.city = city;
            return this;
        }

        public BaiduLocationAddressDetailBuilder city_code(String city_code) {
            this.city_code = city_code;
            return this;
        }

        public BaiduLocationAddressDetailBuilder district(String district) {
            this.district = district;
            return this;
        }

        public BaiduLocationAddressDetailBuilder province(String province) {
            this.province = province;
            return this;
        }

        public BaiduLocationAddressDetailBuilder street(String street) {
            this.street = street;
            return this;
        }

        public BaiduLocationAddressDetailBuilder street_number(String street_number) {
            this.street_number = street_number;
            return this;
        }

        public BaiduLocationAddressDetail build() {
            return new BaiduLocationAddressDetail(this.adcode, this.city, this.city_code, this.district, this.province, this.street, this.street_number);
        }

        public String toString() {
            return "BaiduLocationAddressDetail.BaiduLocationAddressDetailBuilder(adcode=" + this.adcode + ", city=" + this.city + ", city_code=" + this.city_code + ", district=" + this.district + ", province=" + this.province + ", street=" + this.street + ", street_number=" + this.street_number + ")";
        }
    }
}

