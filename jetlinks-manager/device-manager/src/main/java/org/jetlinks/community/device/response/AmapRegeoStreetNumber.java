/*
 * Decompiled with CFR 0.152.
 */
package org.jetlinks.community.device.response;

public class AmapRegeoStreetNumber {
    private String number;
    private String location;
    private String direction;
    private String distance;
    private String street;

    public static AmapRegeoStreetNumberBuilder builder() {
        return new AmapRegeoStreetNumberBuilder();
    }

    public String getNumber() {
        return this.number;
    }

    public String getLocation() {
        return this.location;
    }

    public String getDirection() {
        return this.direction;
    }

    public String getDistance() {
        return this.distance;
    }

    public String getStreet() {
        return this.street;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof AmapRegeoStreetNumber)) {
            return false;
        }
        AmapRegeoStreetNumber other = (AmapRegeoStreetNumber)o;
        if (!other.canEqual(this)) {
            return false;
        }
        String this$number = this.getNumber();
        String other$number = other.getNumber();
        if (this$number == null ? other$number != null : !this$number.equals(other$number)) {
            return false;
        }
        String this$location = this.getLocation();
        String other$location = other.getLocation();
        if (this$location == null ? other$location != null : !this$location.equals(other$location)) {
            return false;
        }
        String this$direction = this.getDirection();
        String other$direction = other.getDirection();
        if (this$direction == null ? other$direction != null : !this$direction.equals(other$direction)) {
            return false;
        }
        String this$distance = this.getDistance();
        String other$distance = other.getDistance();
        if (this$distance == null ? other$distance != null : !this$distance.equals(other$distance)) {
            return false;
        }
        String this$street = this.getStreet();
        String other$street = other.getStreet();
        return !(this$street == null ? other$street != null : !this$street.equals(other$street));
    }

    protected boolean canEqual(Object other) {
        return other instanceof AmapRegeoStreetNumber;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        String $number = this.getNumber();
        result = result * 59 + ($number == null ? 43 : $number.hashCode());
        String $location = this.getLocation();
        result = result * 59 + ($location == null ? 43 : $location.hashCode());
        String $direction = this.getDirection();
        result = result * 59 + ($direction == null ? 43 : $direction.hashCode());
        String $distance = this.getDistance();
        result = result * 59 + ($distance == null ? 43 : $distance.hashCode());
        String $street = this.getStreet();
        result = result * 59 + ($street == null ? 43 : $street.hashCode());
        return result;
    }

    public String toString() {
        return "AmapRegeoStreetNumber(number=" + this.getNumber() + ", location=" + this.getLocation() + ", direction=" + this.getDirection() + ", distance=" + this.getDistance() + ", street=" + this.getStreet() + ")";
    }

    public AmapRegeoStreetNumber(String number, String location, String direction, String distance, String street) {
        this.number = number;
        this.location = location;
        this.direction = direction;
        this.distance = distance;
        this.street = street;
    }

    public AmapRegeoStreetNumber() {
    }

    public static class AmapRegeoStreetNumberBuilder {
        private String number;
        private String location;
        private String direction;
        private String distance;
        private String street;

        AmapRegeoStreetNumberBuilder() {
        }

        public AmapRegeoStreetNumberBuilder number(String number) {
            this.number = number;
            return this;
        }

        public AmapRegeoStreetNumberBuilder location(String location) {
            this.location = location;
            return this;
        }

        public AmapRegeoStreetNumberBuilder direction(String direction) {
            this.direction = direction;
            return this;
        }

        public AmapRegeoStreetNumberBuilder distance(String distance) {
            this.distance = distance;
            return this;
        }

        public AmapRegeoStreetNumberBuilder street(String street) {
            this.street = street;
            return this;
        }

        public AmapRegeoStreetNumber build() {
            return new AmapRegeoStreetNumber(this.number, this.location, this.direction, this.distance, this.street);
        }

        public String toString() {
            return "AmapRegeoStreetNumber.AmapRegeoStreetNumberBuilder(number=" + this.number + ", location=" + this.location + ", direction=" + this.direction + ", distance=" + this.distance + ", street=" + this.street + ")";
        }
    }
}

