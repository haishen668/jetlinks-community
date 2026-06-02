/*
 * Decompiled with CFR 0.152.
 */
package org.jetlinks.community.device.response;

public class BaiduLocationPoint {
    private String x = "";
    private String y = "";

    public static BaiduLocationPointBuilder builder() {
        return new BaiduLocationPointBuilder();
    }

    public String getX() {
        return this.x;
    }

    public String getY() {
        return this.y;
    }

    public void setX(String x) {
        this.x = x;
    }

    public void setY(String y) {
        this.y = y;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof BaiduLocationPoint)) {
            return false;
        }
        BaiduLocationPoint other = (BaiduLocationPoint)o;
        if (!other.canEqual(this)) {
            return false;
        }
        String this$x = this.getX();
        String other$x = other.getX();
        if (this$x == null ? other$x != null : !this$x.equals(other$x)) {
            return false;
        }
        String this$y = this.getY();
        String other$y = other.getY();
        return !(this$y == null ? other$y != null : !this$y.equals(other$y));
    }

    protected boolean canEqual(Object other) {
        return other instanceof BaiduLocationPoint;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        String $x = this.getX();
        result = result * 59 + ($x == null ? 43 : $x.hashCode());
        String $y = this.getY();
        result = result * 59 + ($y == null ? 43 : $y.hashCode());
        return result;
    }

    public String toString() {
        return "BaiduLocationPoint(x=" + this.getX() + ", y=" + this.getY() + ")";
    }

    public BaiduLocationPoint(String x, String y) {
        this.x = x;
        this.y = y;
    }

    public BaiduLocationPoint() {
    }

    public static class BaiduLocationPointBuilder {
        private String x;
        private String y;

        BaiduLocationPointBuilder() {
        }

        public BaiduLocationPointBuilder x(String x) {
            this.x = x;
            return this;
        }

        public BaiduLocationPointBuilder y(String y) {
            this.y = y;
            return this;
        }

        public BaiduLocationPoint build() {
            return new BaiduLocationPoint(this.x, this.y);
        }

        public String toString() {
            return "BaiduLocationPoint.BaiduLocationPointBuilder(x=" + this.x + ", y=" + this.y + ")";
        }
    }
}

