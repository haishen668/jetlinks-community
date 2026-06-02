/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.hswebframework.web.authorization.Authentication
 *  org.hswebframework.web.validator.ValidatorUtils
 *  org.jetlinks.community.io.excel.AbstractImporter
 *  org.jetlinks.community.io.excel.ImportHelper
 *  org.jetlinks.community.io.file.FileManager
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.springframework.web.reactive.function.client.WebClient
 *  reactor.core.publisher.Flux
 *  reactor.core.publisher.Mono
 */
package org.jetlinks.community.device.web.excel;

import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.validator.ValidatorUtils;
import org.jetlinks.community.device.entity.DeviceProductEntity;
import org.jetlinks.community.device.web.excel.CustomerDeviceExcelInfo;
import org.jetlinks.community.io.excel.AbstractImporter;
import org.jetlinks.community.io.excel.ImportHelper;
import org.jetlinks.community.io.file.FileManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class CustomerDeviceExcelImporter
extends AbstractImporter<CustomerDeviceExcelInfo> {
    private static final Logger log = LoggerFactory.getLogger(CustomerDeviceExcelImporter.class);
    private final DeviceProductEntity product;
    private final Authentication auth;

    public CustomerDeviceExcelImporter(FileManager fileManager, WebClient client, DeviceProductEntity product, Authentication auth) {
        super(fileManager, client);
        this.product = product;
        this.auth = auth;
    }

    protected Mono<Void> handleData(Flux<CustomerDeviceExcelInfo> data) {
        return data.doOnNext(x$0 -> ValidatorUtils.tryValidate((Object)x$0, (Class[])new Class[0])).map(deviceExcelInfo -> deviceExcelInfo.initDeviceInstance(this.product, this.auth)).then();
    }

    protected CustomerDeviceExcelInfo newInstance() {
        CustomerDeviceExcelInfo deviceExcelInfo = new CustomerDeviceExcelInfo();
        return deviceExcelInfo;
    }

    protected void customImport(ImportHelper<CustomerDeviceExcelInfo> helper) {
        helper.fallbackSingle(true);
    }

    public DeviceProductEntity getProduct() {
        return this.product;
    }
}

