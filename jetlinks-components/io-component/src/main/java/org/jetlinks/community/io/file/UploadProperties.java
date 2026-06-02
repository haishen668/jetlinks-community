/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.boot.context.properties.ConfigurationProperties
 */
package org.jetlinks.community.io.file;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix="hsweb.file.upload")
public class UploadProperties {
    private String staticFilePath = "./static";
    private String staticLocation = "/static";

    public String getStaticFilePath() {
        return this.staticFilePath;
    }

    public String getStaticLocation() {
        return this.staticLocation;
    }

    public void setStaticFilePath(String staticFilePath) {
        this.staticFilePath = staticFilePath;
    }

    public void setStaticLocation(String staticLocation) {
        this.staticLocation = staticLocation;
    }
}

