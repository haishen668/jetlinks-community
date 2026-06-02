/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.alibaba.fastjson.JSON
 *  com.alibaba.fastjson.JSONObject
 *  javax.validation.constraints.NotBlank
 *  org.hswebframework.reactor.excel.CellDataType
 *  org.hswebframework.reactor.excel.ExcelHeader
 *  org.hswebframework.web.authorization.Authentication
 *  org.hswebframework.web.bean.FastBeanCopier
 *  org.hswebframework.web.validator.ValidatorUtils
 *  org.jetlinks.community.io.excel.annotation.ExcelHeader
 *  org.jetlinks.core.metadata.Jsonable
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.springframework.util.StringUtils
 */
package org.jetlinks.community.device.web.excel;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotBlank;
import org.hswebframework.reactor.excel.CellDataType;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.bean.FastBeanCopier;
import org.hswebframework.web.validator.ValidatorUtils;
import org.jetlinks.community.device.entity.DeviceCardEntity;
import org.jetlinks.community.device.entity.DeviceInstanceEntity;
import org.jetlinks.community.device.entity.DeviceProductEntity;
import org.jetlinks.community.io.excel.annotation.ExcelHeader;
import org.jetlinks.core.metadata.Jsonable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

public class CustomerDeviceExcelInfo
implements Jsonable {
    private static final Logger log = LoggerFactory.getLogger(CustomerDeviceExcelInfo.class);
    @ExcelHeader(value={"\u8bbe\u5907ID"})
    @NotBlank(message="\u8bbe\u5907ID\u4e0d\u80fd\u4e3a\u7a7a")
    private @NotBlank(message="\u8bbe\u5907ID\u4e0d\u80fd\u4e3a\u7a7a") String id;
    @ExcelHeader(value={"\u4ea7\u54c1\u540d\u79f0"})
    private String productName;
    @ExcelHeader(value={"\u8bbe\u5907\u578b\u53f7"})
    private String model;
    @ExcelHeader(value={"IMEI"})
    private String imei;
    @ExcelHeader(value={"MAC"})
    private String mac;
    @ExcelHeader(value={"\u5185\u7f6eICCID"})
    private String iccid;
    @ExcelHeader(value={"\u63d2\u62d4\u5361ICCID"})
    private String iccid1;
    @ExcelHeader(value={"\u5f52\u5c5e\u5ba2\u6237"})
    private String customer;
    @ExcelHeader(value={"\u8bf4\u660e"})
    private String describe;
    private long rowNumber;
    private String state;
    private DeviceInstanceEntity device;

    public void with(String key, Object value) {
        log.info("=====================================with=key:{},value:{}", (Object)key, value);
        FastBeanCopier.copy(Collections.singletonMap(key, value), (Object)this, (String[])new String[0]);
    }

    public static List<org.hswebframework.reactor.excel.ExcelHeader> getTemplateHeaderMapping() {
        ArrayList<org.hswebframework.reactor.excel.ExcelHeader> arr = new ArrayList<org.hswebframework.reactor.excel.ExcelHeader>(Arrays.asList(new org.hswebframework.reactor.excel.ExcelHeader("id", "\u8bbe\u5907ID", CellDataType.STRING), new org.hswebframework.reactor.excel.ExcelHeader("model", "\u8bbe\u5907\u578b\u53f7", CellDataType.STRING), new org.hswebframework.reactor.excel.ExcelHeader("imei", "IMEI", CellDataType.STRING), new org.hswebframework.reactor.excel.ExcelHeader("mac", "MAC", CellDataType.STRING), new org.hswebframework.reactor.excel.ExcelHeader("iccid", "\u5185\u7f6eICCID", CellDataType.STRING)));
        return arr;
    }

    public static List<org.hswebframework.reactor.excel.ExcelHeader> getExportHeaderMapping() {
        ArrayList<org.hswebframework.reactor.excel.ExcelHeader> arr = new ArrayList<org.hswebframework.reactor.excel.ExcelHeader>(Arrays.asList(new org.hswebframework.reactor.excel.ExcelHeader("id", "\u8bbe\u5907ID", CellDataType.STRING), new org.hswebframework.reactor.excel.ExcelHeader("productName", "\u4ea7\u54c1\u540d\u79f0", CellDataType.STRING), new org.hswebframework.reactor.excel.ExcelHeader("model", "\u8bbe\u5907\u578b\u53f7", CellDataType.STRING), new org.hswebframework.reactor.excel.ExcelHeader("imei", "IMEI", CellDataType.STRING), new org.hswebframework.reactor.excel.ExcelHeader("mac", "MAC", CellDataType.STRING), new org.hswebframework.reactor.excel.ExcelHeader("iccid", "\u5185\u7f6eICCID", CellDataType.STRING), new org.hswebframework.reactor.excel.ExcelHeader("iccid1", "\u63d2\u62d4\u5361ICCID", CellDataType.STRING), new org.hswebframework.reactor.excel.ExcelHeader("customer", "\u5f52\u5c5e\u5ba2\u6237", CellDataType.STRING), new org.hswebframework.reactor.excel.ExcelHeader("describe", "\u8bf4\u660e", CellDataType.STRING)));
        return arr;
    }

    public Map<String, Object> toMap() {
        log.info("=====================================toMap={}", JSON.toJSON((Object)this));
        return (Map)FastBeanCopier.copy((Object)this, new HashMap(), (String[])new String[0]);
    }

    public static Map<String, String> getImportHeaderMapping() {
        HashMap<String, String> mapping = new HashMap<String, String>();
        mapping.put("\u8bbe\u5907ID", "id");
        mapping.put("\u4ea7\u54c1\u540d\u79f0", "productName");
        mapping.put("\u8bbe\u5907\u578b\u53f7", "model");
        mapping.put("IMEI", "imei");
        mapping.put("MAC", "mac");
        mapping.put("\u5185\u7f6eICCID", "iccid");
        return mapping;
    }

    public CustomerDeviceExcelInfo initDeviceInstance(DeviceProductEntity product, Authentication auth) {
        DeviceInstanceEntity entity = (DeviceInstanceEntity)((Object)FastBeanCopier.copy((Object)this, (Object)((Object)new DeviceInstanceEntity()), (String[])new String[0]));
        entity.setProductId(product.getId());
        entity.setProductName(product.getName());
        entity.setCreateTimeNow();
        entity.setCreatorId(auth.getUser().getId());
        entity.setCreatorName(auth.getUser().getName());
        entity.setUserId(auth.getUser().getId());
        entity.setModifyTimeNow();
        entity.setModifierId(auth.getUser().getId());
        entity.setModifierName(auth.getUser().getName());
        ValidatorUtils.tryValidate((Object)((Object)entity), (Class[])new Class[0]);
        this.device = entity;
        return this;
    }

    public void fromJson(JSONObject json) {
        Jsonable.super.fromJson(json);
        log.info("=====================================fromJson={}", (Object)json);
    }

    public DeviceCardEntity getCard() {
        DeviceCardEntity entity = new DeviceCardEntity();
        if (StringUtils.isEmpty((Object)this.iccid)) {
            return entity;
        }
        entity.setIccid(this.iccid);
        entity.setUseState(1);
        entity.setSlot(2);
        entity.setDeviceId(this.id);
        return entity;
    }

    public String getId() {
        return this.id;
    }

    public String getProductName() {
        return this.productName;
    }

    public String getModel() {
        return this.model;
    }

    public String getImei() {
        return this.imei;
    }

    public String getMac() {
        return this.mac;
    }

    public String getIccid() {
        return this.iccid;
    }

    public String getIccid1() {
        return this.iccid1;
    }

    public String getCustomer() {
        return this.customer;
    }

    public String getDescribe() {
        return this.describe;
    }

    public long getRowNumber() {
        return this.rowNumber;
    }

    public String getState() {
        return this.state;
    }

    public DeviceInstanceEntity getDevice() {
        return this.device;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public void setIccid(String iccid) {
        this.iccid = iccid;
    }

    public void setIccid1(String iccid1) {
        this.iccid1 = iccid1;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }

    public void setRowNumber(long rowNumber) {
        this.rowNumber = rowNumber;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setDevice(DeviceInstanceEntity device) {
        this.device = device;
    }
}
