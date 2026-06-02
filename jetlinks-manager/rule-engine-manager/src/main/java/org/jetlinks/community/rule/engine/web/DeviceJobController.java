/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.swagger.v3.oas.annotations.Operation
 *  io.swagger.v3.oas.annotations.tags.Tag
 *  org.hswebframework.ezorm.core.param.QueryParam
 *  org.hswebframework.ezorm.core.param.Term
 *  org.hswebframework.web.api.crud.entity.PagerResult
 *  org.hswebframework.web.api.crud.entity.QueryParamEntity
 *  org.hswebframework.web.authorization.annotation.DeleteAction
 *  org.hswebframework.web.authorization.annotation.QueryAction
 *  org.hswebframework.web.authorization.annotation.Resource
 *  org.hswebframework.web.authorization.annotation.SaveAction
 *  org.hswebframework.web.crud.web.reactive.ReactiveServiceQueryController
 *  org.springframework.web.bind.annotation.DeleteMapping
 *  org.springframework.web.bind.annotation.PathVariable
 *  org.springframework.web.bind.annotation.PostMapping
 *  org.springframework.web.bind.annotation.PutMapping
 *  org.springframework.web.bind.annotation.RequestBody
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.RestController
 *  reactor.core.publisher.Flux
 *  reactor.core.publisher.Mono
 */
package org.jetlinks.community.rule.engine.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.hswebframework.ezorm.core.param.QueryParam;
import org.hswebframework.ezorm.core.param.Term;
import org.hswebframework.web.api.crud.entity.PagerResult;
import org.hswebframework.web.api.crud.entity.QueryParamEntity;
import org.hswebframework.web.authorization.annotation.DeleteAction;
import org.hswebframework.web.authorization.annotation.QueryAction;
import org.hswebframework.web.authorization.annotation.Resource;
import org.hswebframework.web.authorization.annotation.SaveAction;
import org.hswebframework.web.crud.web.reactive.ReactiveServiceQueryController;
import org.jetlinks.community.rule.engine.entity.DeviceJob;
import org.jetlinks.community.rule.engine.model.DeviceJobLog;
import org.jetlinks.community.rule.engine.service.DeviceJobService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(value={"/deviceJob"})
@Tag(name="\u8bbe\u5907\u5b9a\u65f6\u4efb\u52a1\u7ba1\u7406")
@Resource(id="device-job", name="\u8bbe\u5907\u5b9a\u65f6\u4efb\u52a1\u7ba1\u7406")
public class DeviceJobController
implements ReactiveServiceQueryController<DeviceJob, String> {
    private final DeviceJobService service;

    @PostMapping(value={"/{deviceId}/_query"})
    @Operation(summary="\u8bbe\u5907\u5b9a\u65f6\u4efb\u52a1\u67e5\u8be2")
    @QueryAction
    public Mono<PagerResult<DeviceJob>> queryPager(@PathVariable String deviceId, @RequestBody Mono<QueryParamEntity> query) {
        return query.flatMap(queryParam -> {
            queryParam.getTerms().add(Term.of((String)"deviceId", (String)"eq", (Object)deviceId, (String[])new String[0]));
            return this.service.queryPager(queryParam).flatMap(page -> Flux.fromIterable(page.getData()).index().map(device -> DeviceJob.of(device.getT2(), page.getPageSize() * page.getPageIndex() + device.getT1().intValue() + 1)).collectList().flatMap(list -> {
                page.setData(list);
                return Mono.just(page);
            }));
        });
    }

    @PostMapping
    @Operation(summary="\u521b\u5efa\u8bbe\u5907\u5b9a\u65f6\u4efb\u52a1")
    @SaveAction
    public Mono<DeviceJob> create(@RequestBody Mono<DeviceJob> deviceJobMono) {
        return deviceJobMono.flatMap(this.service::createDeviceJob);
    }

    @PutMapping(value={"/{id}"})
    @Operation(summary="\u66f4\u65b0\u8bbe\u5907\u5b9a\u65f6\u4efb\u52a1")
    @SaveAction
    public Mono<Void> update(@PathVariable String id, @RequestBody Mono<DeviceJob> deviceJobMono) {
        return deviceJobMono.flatMap(deviceJob -> this.service.updateDeviceJob(id, deviceJob)).then();
    }

    @PutMapping(value={"/{id}/_disable"})
    @Operation(summary="\u7981\u7528\u8bbe\u5907\u5b9a\u65f6\u4efb\u52a1")
    @SaveAction
    public Mono<Void> disable(@PathVariable String id) {
        return this.service.disabled(id);
    }

    @PutMapping(value={"/{id}/_enable"})
    @Operation(summary="\u542f\u7528\u8bbe\u5907\u5b9a\u65f6\u4efb\u52a1")
    @SaveAction
    public Mono<Void> enabled(@PathVariable String id) {
        return this.service.enable(id);
    }

    @DeleteMapping(value={"/{id}"})
    @Operation(summary="\u5220\u9664\u8bbe\u5907\u5b9a\u65f6\u4efb\u52a1")
    @DeleteAction
    public Mono<Void> remove(@PathVariable String id) {
        return this.service.deleteById(id).then();
    }

    @PostMapping(value={"/{id}/execute/_query"})
    @Operation(summary="\u8bbe\u5907\u5b9a\u65f6\u4efb\u52a1-\u6267\u884c\u8bb0\u5f55")
    @QueryAction
    public Mono<PagerResult<DeviceJobLog>> queryPager(@PathVariable String id, @RequestBody QueryParamEntity queryParam) {
        queryParam.getTerms().add(Term.of((String)"instanceId", (String)"eq", (Object)id, (String[])new String[0]));
        return this.service.queryExecuteEvent((QueryParam)queryParam).flatMap(page -> Flux.fromIterable(page.getData()).index().map(device -> DeviceJobLog.of(device.getT2(), page.getPageSize() * page.getPageIndex() + device.getT1().intValue() + 1)).collectList().flatMap(list -> {
            page.setData(list);
            return Mono.just(page);
        }));
    }

    public DeviceJobController(DeviceJobService service) {
        this.service = service;
    }

    public DeviceJobService getService() {
        return this.service;
    }
}
