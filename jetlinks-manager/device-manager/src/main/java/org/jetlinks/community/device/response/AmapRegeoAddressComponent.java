/*
 * Decompiled with CFR 0.152.
 */
package org.jetlinks.community.device.response;

import java.util.List;
import org.jetlinks.community.device.response.AmapRegeoBuilding;
import org.jetlinks.community.device.response.AmapRegeoBusinessArea;
import org.jetlinks.community.device.response.AmapRegeoStreetNumber;

public class AmapRegeoAddressComponent {
    private String city;
    private String province;
    private String adcode;
    private String district;
    private String towncode;
    private String country;
    private String township;
    private String citycode;
    private AmapRegeoStreetNumber streetNumber;
    private AmapRegeoBuilding building;
    private List<AmapRegeoBusinessArea> businessAreas;
    private AmapRegeoBuilding neighborhood;

    public static AmapRegeoAddressComponentBuilder builder() {
        return new AmapRegeoAddressComponentBuilder();
    }

    public String getCity() {
        return this.city;
    }

    public String getProvince() {
        return this.province;
    }

    public String getAdcode() {
        return this.adcode;
    }

    public String getDistrict() {
        return this.district;
    }

    public String getTowncode() {
        return this.towncode;
    }

    public String getCountry() {
        return this.country;
    }

    public String getTownship() {
        return this.township;
    }

    public String getCitycode() {
        return this.citycode;
    }

    public AmapRegeoStreetNumber getStreetNumber() {
        return this.streetNumber;
    }

    public AmapRegeoBuilding getBuilding() {
        return this.building;
    }

    public List<AmapRegeoBusinessArea> getBusinessAreas() {
        return this.businessAreas;
    }

    public AmapRegeoBuilding getNeighborhood() {
        return this.neighborhood;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public void setAdcode(String adcode) {
        this.adcode = adcode;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public void setTowncode(String towncode) {
        this.towncode = towncode;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setTownship(String township) {
        this.township = township;
    }

    public void setCitycode(String citycode) {
        this.citycode = citycode;
    }

    public void setStreetNumber(AmapRegeoStreetNumber streetNumber) {
        this.streetNumber = streetNumber;
    }

    public void setBuilding(AmapRegeoBuilding building) {
        this.building = building;
    }

    public void setBusinessAreas(List<AmapRegeoBusinessArea> businessAreas) {
        this.businessAreas = businessAreas;
    }

    public void setNeighborhood(AmapRegeoBuilding neighborhood) {
        this.neighborhood = neighborhood;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof AmapRegeoAddressComponent)) {
            return false;
        }
        AmapRegeoAddressComponent other = (AmapRegeoAddressComponent)o;
        if (!other.canEqual(this)) {
            return false;
        }
        String this$city = this.getCity();
        String other$city = other.getCity();
        if (this$city == null ? other$city != null : !this$city.equals(other$city)) {
            return false;
        }
        String this$province = this.getProvince();
        String other$province = other.getProvince();
        if (this$province == null ? other$province != null : !this$province.equals(other$province)) {
            return false;
        }
        String this$adcode = this.getAdcode();
        String other$adcode = other.getAdcode();
        if (this$adcode == null ? other$adcode != null : !this$adcode.equals(other$adcode)) {
            return false;
        }
        String this$district = this.getDistrict();
        String other$district = other.getDistrict();
        if (this$district == null ? other$district != null : !this$district.equals(other$district)) {
            return false;
        }
        String this$towncode = this.getTowncode();
        String other$towncode = other.getTowncode();
        if (this$towncode == null ? other$towncode != null : !this$towncode.equals(other$towncode)) {
            return false;
        }
        String this$country = this.getCountry();
        String other$country = other.getCountry();
        if (this$country == null ? other$country != null : !this$country.equals(other$country)) {
            return false;
        }
        String this$township = this.getTownship();
        String other$township = other.getTownship();
        if (this$township == null ? other$township != null : !this$township.equals(other$township)) {
            return false;
        }
        String this$citycode = this.getCitycode();
        String other$citycode = other.getCitycode();
        if (this$citycode == null ? other$citycode != null : !this$citycode.equals(other$citycode)) {
            return false;
        }
        AmapRegeoStreetNumber this$streetNumber = this.getStreetNumber();
        AmapRegeoStreetNumber other$streetNumber = other.getStreetNumber();
        if (this$streetNumber == null ? other$streetNumber != null : !((Object)this$streetNumber).equals(other$streetNumber)) {
            return false;
        }
        AmapRegeoBuilding this$building = this.getBuilding();
        AmapRegeoBuilding other$building = other.getBuilding();
        if (this$building == null ? other$building != null : !((Object)this$building).equals(other$building)) {
            return false;
        }
        List<AmapRegeoBusinessArea> this$businessAreas = this.getBusinessAreas();
        List<AmapRegeoBusinessArea> other$businessAreas = other.getBusinessAreas();
        if (this$businessAreas == null ? other$businessAreas != null : !((Object)this$businessAreas).equals(other$businessAreas)) {
            return false;
        }
        AmapRegeoBuilding this$neighborhood = this.getNeighborhood();
        AmapRegeoBuilding other$neighborhood = other.getNeighborhood();
        return !(this$neighborhood == null ? other$neighborhood != null : !((Object)this$neighborhood).equals(other$neighborhood));
    }

    protected boolean canEqual(Object other) {
        return other instanceof AmapRegeoAddressComponent;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        String $city = this.getCity();
        result = result * 59 + ($city == null ? 43 : $city.hashCode());
        String $province = this.getProvince();
        result = result * 59 + ($province == null ? 43 : $province.hashCode());
        String $adcode = this.getAdcode();
        result = result * 59 + ($adcode == null ? 43 : $adcode.hashCode());
        String $district = this.getDistrict();
        result = result * 59 + ($district == null ? 43 : $district.hashCode());
        String $towncode = this.getTowncode();
        result = result * 59 + ($towncode == null ? 43 : $towncode.hashCode());
        String $country = this.getCountry();
        result = result * 59 + ($country == null ? 43 : $country.hashCode());
        String $township = this.getTownship();
        result = result * 59 + ($township == null ? 43 : $township.hashCode());
        String $citycode = this.getCitycode();
        result = result * 59 + ($citycode == null ? 43 : $citycode.hashCode());
        AmapRegeoStreetNumber $streetNumber = this.getStreetNumber();
        result = result * 59 + ($streetNumber == null ? 43 : ((Object)$streetNumber).hashCode());
        AmapRegeoBuilding $building = this.getBuilding();
        result = result * 59 + ($building == null ? 43 : ((Object)$building).hashCode());
        List<AmapRegeoBusinessArea> $businessAreas = this.getBusinessAreas();
        result = result * 59 + ($businessAreas == null ? 43 : ((Object)$businessAreas).hashCode());
        AmapRegeoBuilding $neighborhood = this.getNeighborhood();
        result = result * 59 + ($neighborhood == null ? 43 : ((Object)$neighborhood).hashCode());
        return result;
    }

    public String toString() {
        return "AmapRegeoAddressComponent(city=" + this.getCity() + ", province=" + this.getProvince() + ", adcode=" + this.getAdcode() + ", district=" + this.getDistrict() + ", towncode=" + this.getTowncode() + ", country=" + this.getCountry() + ", township=" + this.getTownship() + ", citycode=" + this.getCitycode() + ", streetNumber=" + this.getStreetNumber() + ", building=" + this.getBuilding() + ", businessAreas=" + this.getBusinessAreas() + ", neighborhood=" + this.getNeighborhood() + ")";
    }

    public AmapRegeoAddressComponent(String city, String province, String adcode, String district, String towncode, String country, String township, String citycode, AmapRegeoStreetNumber streetNumber, AmapRegeoBuilding building, List<AmapRegeoBusinessArea> businessAreas, AmapRegeoBuilding neighborhood) {
        this.city = city;
        this.province = province;
        this.adcode = adcode;
        this.district = district;
        this.towncode = towncode;
        this.country = country;
        this.township = township;
        this.citycode = citycode;
        this.streetNumber = streetNumber;
        this.building = building;
        this.businessAreas = businessAreas;
        this.neighborhood = neighborhood;
    }

    public AmapRegeoAddressComponent() {
    }

    public static class AmapRegeoAddressComponentBuilder {
        private String city;
        private String province;
        private String adcode;
        private String district;
        private String towncode;
        private String country;
        private String township;
        private String citycode;
        private AmapRegeoStreetNumber streetNumber;
        private AmapRegeoBuilding building;
        private List<AmapRegeoBusinessArea> businessAreas;
        private AmapRegeoBuilding neighborhood;

        AmapRegeoAddressComponentBuilder() {
        }

        public AmapRegeoAddressComponentBuilder city(String city) {
            this.city = city;
            return this;
        }

        public AmapRegeoAddressComponentBuilder province(String province) {
            this.province = province;
            return this;
        }

        public AmapRegeoAddressComponentBuilder adcode(String adcode) {
            this.adcode = adcode;
            return this;
        }

        public AmapRegeoAddressComponentBuilder district(String district) {
            this.district = district;
            return this;
        }

        public AmapRegeoAddressComponentBuilder towncode(String towncode) {
            this.towncode = towncode;
            return this;
        }

        public AmapRegeoAddressComponentBuilder country(String country) {
            this.country = country;
            return this;
        }

        public AmapRegeoAddressComponentBuilder township(String township) {
            this.township = township;
            return this;
        }

        public AmapRegeoAddressComponentBuilder citycode(String citycode) {
            this.citycode = citycode;
            return this;
        }

        public AmapRegeoAddressComponentBuilder streetNumber(AmapRegeoStreetNumber streetNumber) {
            this.streetNumber = streetNumber;
            return this;
        }

        public AmapRegeoAddressComponentBuilder building(AmapRegeoBuilding building) {
            this.building = building;
            return this;
        }

        public AmapRegeoAddressComponentBuilder businessAreas(List<AmapRegeoBusinessArea> businessAreas) {
            this.businessAreas = businessAreas;
            return this;
        }

        public AmapRegeoAddressComponentBuilder neighborhood(AmapRegeoBuilding neighborhood) {
            this.neighborhood = neighborhood;
            return this;
        }

        public AmapRegeoAddressComponent build() {
            return new AmapRegeoAddressComponent(this.city, this.province, this.adcode, this.district, this.towncode, this.country, this.township, this.citycode, this.streetNumber, this.building, this.businessAreas, this.neighborhood);
        }

        public String toString() {
            return "AmapRegeoAddressComponent.AmapRegeoAddressComponentBuilder(city=" + this.city + ", province=" + this.province + ", adcode=" + this.adcode + ", district=" + this.district + ", towncode=" + this.towncode + ", country=" + this.country + ", township=" + this.township + ", citycode=" + this.citycode + ", streetNumber=" + this.streetNumber + ", building=" + this.building + ", businessAreas=" + this.businessAreas + ", neighborhood=" + this.neighborhood + ")";
        }
    }
}

