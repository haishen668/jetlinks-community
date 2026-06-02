/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.alibaba.fastjson.JSON
 *  com.alibaba.fastjson.JSONObject
 *  org.hswebframework.reactor.excel.CellDataType
 *  org.hswebframework.reactor.excel.ExcelHeader
 *  org.hswebframework.web.bean.FastBeanCopier
 *  org.jetlinks.community.io.excel.annotation.ExcelHeader
 *  org.jetlinks.core.metadata.Jsonable
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package org.jetlinks.community.rule.engine.web.excel;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.hswebframework.reactor.excel.CellDataType;
import org.hswebframework.reactor.excel.ExcelHeader;
import org.hswebframework.web.bean.FastBeanCopier;
import org.jetlinks.core.metadata.Jsonable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlarmHandleExcelInfo
implements Jsonable {
    private static final Logger log = LoggerFactory.getLogger(AlarmHandleExcelInfo.class);
    @org.jetlinks.community.io.excel.annotation.ExcelHeader(value={"\u5904\u7406\u65f6\u95f4"})
    private String handleTime;
    @org.jetlinks.community.io.excel.annotation.ExcelHeader(value={"\u5904\u7406\u7c7b\u578b"})
    private String handleType;
    @org.jetlinks.community.io.excel.annotation.ExcelHeader(value={"\u5904\u7406\u8bf4\u660e"})
    private String description;
    @org.jetlinks.community.io.excel.annotation.ExcelHeader(value={"\u544a\u8b66\u65f6\u95f4"})
    private String alarmTime;
    @org.jetlinks.community.io.excel.annotation.ExcelHeader(value={"\u7ed1\u5b9a\u7528\u6237"})
    private String userName;
    @org.jetlinks.community.io.excel.annotation.ExcelHeader(value={"\u70b9\u4f4d\u540d\u79f0"})
    private String deviceName;

    public static List<ExcelHeader> getExportHeaderMapping() {
        ArrayList<ExcelHeader> arr = new ArrayList<ExcelHeader>(Arrays.asList(new ExcelHeader("deviceName", "\u70b9\u4f4d\u540d\u79f0", CellDataType.STRING), new ExcelHeader("userName", "\u7ed1\u5b9a\u7528\u6237", CellDataType.STRING), new ExcelHeader("handleTime", "\u5904\u7406\u65f6\u95f4", CellDataType.STRING), new ExcelHeader("handleType", "\u5904\u7406\u7c7b\u578b", CellDataType.STRING), new ExcelHeader("description", "\u5904\u7406\u8bf4\u660e", CellDataType.STRING), new ExcelHeader("alarmTime", "\u544a\u8b66\u65f6\u95f4", CellDataType.STRING)));
        return arr;
    }

    public Map<String, Object> toMap() {
        log.info("=====================================toMap={}", JSON.toJSON((Object)this));
        return (Map)FastBeanCopier.copy((Object)this, new HashMap(), (String[])new String[0]);
    }

    public void fromJson(JSONObject json) {
        Jsonable.super.fromJson(json);
        log.info("=====================================fromJson={}", (Object)json);
    }

    public String getHandleTime() {
        return this.handleTime;
    }

    public String getHandleType() {
        return this.handleType;
    }

    public String getDescription() {
        return this.description;
    }

    public String getAlarmTime() {
        return this.alarmTime;
    }

    public String getUserName() {
        return this.userName;
    }

    public String getDeviceName() {
        return this.deviceName;
    }

    public void setHandleTime(String handleTime) {
        this.handleTime = handleTime;
    }

    public void setHandleType(String handleType) {
        this.handleType = handleType;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setAlarmTime(String alarmTime) {
        this.alarmTime = alarmTime;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
}
