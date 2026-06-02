/*
 * Decompiled with CFR 0.152.
 */
package org.jetlinks.community.device.response;

public class AmapRegeoBusinessArea {
    private String location;
    private String name;
    private String id;

    public static AmapRegeoBusinessAreaBuilder builder() {
        return new AmapRegeoBusinessAreaBuilder();
    }

    public String getLocation() {
        return this.location;
    }

    public String getName() {
        return this.name;
    }

    public String getId() {
        return this.id;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof AmapRegeoBusinessArea)) {
            return false;
        }
        AmapRegeoBusinessArea other = (AmapRegeoBusinessArea)o;
        if (!other.canEqual(this)) {
            return false;
        }
        String this$location = this.getLocation();
        String other$location = other.getLocation();
        if (this$location == null ? other$location != null : !this$location.equals(other$location)) {
            return false;
        }
        String this$name = this.getName();
        String other$name = other.getName();
        if (this$name == null ? other$name != null : !this$name.equals(other$name)) {
            return false;
        }
        String this$id = this.getId();
        String other$id = other.getId();
        return !(this$id == null ? other$id != null : !this$id.equals(other$id));
    }

    protected boolean canEqual(Object other) {
        return other instanceof AmapRegeoBusinessArea;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        String $location = this.getLocation();
        result = result * 59 + ($location == null ? 43 : $location.hashCode());
        String $name = this.getName();
        result = result * 59 + ($name == null ? 43 : $name.hashCode());
        String $id = this.getId();
        result = result * 59 + ($id == null ? 43 : $id.hashCode());
        return result;
    }

    public String toString() {
        return "AmapRegeoBusinessArea(location=" + this.getLocation() + ", name=" + this.getName() + ", id=" + this.getId() + ")";
    }

    public AmapRegeoBusinessArea(String location, String name, String id) {
        this.location = location;
        this.name = name;
        this.id = id;
    }

    public AmapRegeoBusinessArea() {
    }

    public static class AmapRegeoBusinessAreaBuilder {
        private String location;
        private String name;
        private String id;

        AmapRegeoBusinessAreaBuilder() {
        }

        public AmapRegeoBusinessAreaBuilder location(String location) {
            this.location = location;
            return this;
        }

        public AmapRegeoBusinessAreaBuilder name(String name) {
            this.name = name;
            return this;
        }

        public AmapRegeoBusinessAreaBuilder id(String id) {
            this.id = id;
            return this;
        }

        public AmapRegeoBusinessArea build() {
            return new AmapRegeoBusinessArea(this.location, this.name, this.id);
        }

        public String toString() {
            return "AmapRegeoBusinessArea.AmapRegeoBusinessAreaBuilder(location=" + this.location + ", name=" + this.name + ", id=" + this.id + ")";
        }
    }
}

