/*
 * Decompiled with CFR 0.152.
 */
package org.jetlinks.community.device.response;

public class AmapRegeoBuilding {
    private Object name;
    private Object type;

    public static AmapRegeoBuildingBuilder builder() {
        return new AmapRegeoBuildingBuilder();
    }

    public Object getName() {
        return this.name;
    }

    public Object getType() {
        return this.type;
    }

    public void setName(Object name) {
        this.name = name;
    }

    public void setType(Object type) {
        this.type = type;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof AmapRegeoBuilding)) {
            return false;
        }
        AmapRegeoBuilding other = (AmapRegeoBuilding)o;
        if (!other.canEqual(this)) {
            return false;
        }
        Object this$name = this.getName();
        Object other$name = other.getName();
        if (this$name == null ? other$name != null : !this$name.equals(other$name)) {
            return false;
        }
        Object this$type = this.getType();
        Object other$type = other.getType();
        return !(this$type == null ? other$type != null : !this$type.equals(other$type));
    }

    protected boolean canEqual(Object other) {
        return other instanceof AmapRegeoBuilding;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        Object $name = this.getName();
        result = result * 59 + ($name == null ? 43 : $name.hashCode());
        Object $type = this.getType();
        result = result * 59 + ($type == null ? 43 : $type.hashCode());
        return result;
    }

    public String toString() {
        return "AmapRegeoBuilding(name=" + this.getName() + ", type=" + this.getType() + ")";
    }

    public AmapRegeoBuilding(Object name, Object type) {
        this.name = name;
        this.type = type;
    }

    public AmapRegeoBuilding() {
    }

    public static class AmapRegeoBuildingBuilder {
        private Object name;
        private Object type;

        AmapRegeoBuildingBuilder() {
        }

        public AmapRegeoBuildingBuilder name(Object name) {
            this.name = name;
            return this;
        }

        public AmapRegeoBuildingBuilder type(Object type) {
            this.type = type;
            return this;
        }

        public AmapRegeoBuilding build() {
            return new AmapRegeoBuilding(this.name, this.type);
        }

        public String toString() {
            return "AmapRegeoBuilding.AmapRegeoBuildingBuilder(name=" + this.name + ", type=" + this.type + ")";
        }
    }
}

