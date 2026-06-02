/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.hswebframework.reactor.excel.Cell
 *  org.hswebframework.reactor.excel.converter.RowWrapper
 */
package org.jetlinks.community.device.web.excel;

import java.util.Map;
import org.hswebframework.reactor.excel.Cell;
import org.hswebframework.reactor.excel.converter.RowWrapper;
import org.jetlinks.community.device.web.excel.CustomerDeviceExcelInfo;

public class CustomerDeviceWrapper
extends RowWrapper<CustomerDeviceExcelInfo> {
    static Map<String, String> headerMapping = CustomerDeviceExcelInfo.getImportHeaderMapping();
    public static CustomerDeviceWrapper empty = new CustomerDeviceWrapper();

    protected CustomerDeviceExcelInfo newInstance() {
        return new CustomerDeviceExcelInfo();
    }

    protected CustomerDeviceExcelInfo wrap(CustomerDeviceExcelInfo deviceExcelInfo, Cell header, Cell cell) {
        String headerText = header.valueAsText().orElse("null");
        deviceExcelInfo.setRowNumber(cell.getRowIndex());
        deviceExcelInfo.with(headerMapping.getOrDefault(headerText, headerText), cell.value().orElse(null));
        return deviceExcelInfo;
    }
}

