/*
 * Copyright 2025 JetLinks https://www.jetlinks.cn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetlinks.community.device.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.hswebframework.ezorm.core.param.Term;
import org.hswebframework.ezorm.rdb.mapping.defaults.SaveResult;
import org.hswebframework.web.api.crud.entity.PagerResult;
import org.hswebframework.web.api.crud.entity.QueryNoPagingOperation;
import org.hswebframework.web.api.crud.entity.QueryOperation;
import org.hswebframework.web.api.crud.entity.QueryParamEntity;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.authorization.annotation.QueryAction;
import org.hswebframework.web.authorization.annotation.Resource;
import org.hswebframework.web.authorization.annotation.SaveAction;
import org.hswebframework.web.authorization.exception.UnAuthorizedException;
import org.jetlinks.community.auth.entity.UserDetail;
import org.jetlinks.community.auth.service.UserDetailService;
import org.jetlinks.community.device.entity.CustomerDevice;
import org.jetlinks.community.device.entity.DeviceInstanceEntity;
import org.jetlinks.community.device.entity.DevicePosition;
import org.jetlinks.community.device.service.LocalDeviceInstanceService;
import org.jetlinks.community.device.web.request.BatchUpdateDeviceRequest;
import org.jetlinks.community.device.web.response.DeviceDeployResult;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/customer/device")
@Authorize
@Resource(id = "customer-device", name = "客户设备实例")
@Tag(name = "客户设备接口")
public class CustomerDeviceController {

    private final UserDetailService userDetailService;

    private final LocalDeviceInstanceService service;

    public CustomerDeviceController(UserDetailService userDetailService,
                                    LocalDeviceInstanceService service) {
        this.userDetailService = userDetailService;
        this.service = service;
    }

    @PostMapping("/_query")
    @QueryAction
    @Operation(summary = "分页获取客户设备实例")
    public Mono<PagerResult<CustomerDevice>> queryCustomerDevice(@RequestBody Mono<QueryParamEntity> query) {
        return currentUserWithQuery(query)
            .flatMap(tuple -> service.queryCustomerDevice(prepareQuery(tuple.getT1(), tuple.getT2(), tuple.getT3())));
    }

    @PostMapping("/queryPosition")
    @QueryAction
    @Operation(summary = "客户设备实例位置统计")
    public Mono<List<DevicePosition>> queryDevicePosition() {
        return currentUserWithQuery(Mono.just(QueryParamEntity.of()))
            .flatMap(tuple -> service.queryDevicePosition(prepareQuery(tuple.getT1(), tuple.getT2(), tuple.getT3())));
    }

    @GetMapping("/getLocation")
    @QueryNoPagingOperation(summary = "获取客户端IP")
    public Mono<Map<String, String>> getClientLocation(ServerWebExchange exchange) {
        return Mono.just(Collections.singletonMap("ip", getIp(exchange)));
    }

    @GetMapping(value = "/syncState", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @SaveAction
    @QueryNoPagingOperation(summary = "同步设备状态")
    public Flux<Integer> syncState(@Parameter(hidden = true) QueryParamEntity query) {
        query.setPaging(false);
        return currentUserWithQuery(Mono.just(query))
            .flatMapMany(tuple -> service
                .queryCustomerDevices(prepareQuery(tuple.getT1(), tuple.getT2(), tuple.getT3()))
                .map(DeviceInstanceEntity::getId)
                .buffer(200)
                .publishOn(Schedulers.single())
                .concatMap(ids -> service.syncStateBatch(Flux.just(ids), true).map(List::size))
                .defaultIfEmpty(0));
    }

    @GetMapping(value = "/deployAll", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @SaveAction
    @QueryOperation(summary = "查询并批量激活设备")
    public Flux<DeviceDeployResult> deployAll(@Parameter(hidden = true) QueryParamEntity query) {
        query.setPaging(false);
        return currentUserWithQuery(Mono.just(query))
            .flatMapMany(tuple -> service
                .queryCustomerDevices(prepareQuery(tuple.getT1(), tuple.getT2(), tuple.getT3()))
                .as(service::deploy));
    }

    @PostMapping("/_update")
    @SaveAction
    @Operation(summary = "修改用户设备")
    public Mono<SaveResult> update(@RequestBody Mono<DeviceInstanceEntity> request) {
        return Authentication
            .currentReactive()
            .switchIfEmpty(Mono.error(UnAuthorizedException.NoStackTrace::new))
            .zipWith(request)
            .flatMap(tuple -> {
                DeviceInstanceEntity entity = tuple.getT2();
                entity.setModifierId(tuple.getT1().getUser().getId());
                entity.setModifierName(tuple.getT1().getUser().getName());
                return service.updateById(entity.getId(), entity).thenReturn(SaveResult.of(0, 1));
            });
    }

    @PatchMapping("/_add")
    @SaveAction
    @Operation(summary = "添加用户设备")
    public Mono<SaveResult> add(@RequestBody Mono<DeviceInstanceEntity> request) {
        return Authentication
            .currentReactive()
            .switchIfEmpty(Mono.error(UnAuthorizedException.NoStackTrace::new))
            .zipWith(request)
            .flatMap(tuple -> {
                DeviceInstanceEntity entity = tuple.getT2();
                entity.setModifierId(tuple.getT1().getUser().getId());
                entity.setModifierName(tuple.getT1().getUser().getName());
                return service.save(entity);
            });
    }

    @PatchMapping("/batchUpdate")
    @SaveAction
    @Operation(summary = "批量修改设备用户")
    public Mono<Integer> batchUpdate(@RequestBody Mono<BatchUpdateDeviceRequest> request) {
        return Authentication
            .currentReactive()
            .switchIfEmpty(Mono.error(UnAuthorizedException.NoStackTrace::new))
            .zipWith(request)
            .flatMap(tuple -> {
                tuple.getT2().setModifyUserId(tuple.getT1().getUser().getId());
                return service.batchUpdate(tuple.getT2());
            });
    }

    @PostMapping("/_count")
    @Operation(summary = "使用POST方式查询总数")
    public Mono<Integer> count(@RequestBody Mono<QueryParamEntity> query) {
        return currentUserWithQuery(query)
            .flatMap(tuple -> service.countCustomerDevice(prepareQuery(tuple.getT1(), tuple.getT2(), tuple.getT3())));
    }

    @GetMapping("/{productId}/import")
    @SaveAction
    @Operation(summary = "导入设备数据")
    public Flux<Object> doBatchImportByProduct(@PathVariable @Parameter(description = "产品ID") String productId) {
        return Flux.empty();
    }

    @GetMapping("/{productId}/template.{format}")
    @QueryAction
    @Operation(summary = "下载设备导入模板")
    public Mono<Void> downloadExportTemplate(@PathVariable @Parameter(description = "产品ID") String productId,
                                             ServerHttpResponse response,
                                             @PathVariable @Parameter(description = "文件格式,支持csv,xlsx") String format) {
        response.getHeaders().set(HttpHeaders.CONTENT_DISPOSITION,
                                  "attachment; filename=".concat(URLEncoder.encode("设备导入模板." + format, StandardCharsets.UTF_8)));
        return response.writeWith(Flux.empty());
    }

    @GetMapping("/export.{format}")
    @QueryAction
    @QueryNoPagingOperation(summary = "导出设备实例数据")
    public Mono<Void> export(ServerHttpResponse response,
                             @PathVariable @Parameter(description = "文件格式,支持csv,xlsx") String format) {
        response.getHeaders().set(HttpHeaders.CONTENT_DISPOSITION,
                                  "attachment; filename=".concat(URLEncoder.encode("设备实例." + format, StandardCharsets.UTF_8)));
        return response.writeWith(Flux.empty());
    }

    private QueryParamEntity prepareQuery(Authentication auth, UserDetail user, QueryParamEntity query) {
        if (!"admin".equals(auth.getUser().getUsername())) {
            appendCustomerTerm(query, user);
        }
        query.setOrderBy("create_time DESC");
        return query;
    }

    private void appendCustomerTerm(QueryParamEntity query, UserDetail user) {
        List<Term> terms = query.getTerms();
        if (terms == null) {
            terms = new ArrayList<>();
        }

        Term wrapper = new Term();
        wrapper.setType(Term.Type.and);
        wrapper.setTerms(createCustomerTerms(user));
        terms.add(wrapper);
        query.setTerms(terms);
    }

    private List<Term> createCustomerTerms(UserDetail user) {
        List<Term> subTerms = new ArrayList<>();

        Term treeTerm = new Term();
        treeTerm.setType(Term.Type.or);
        treeTerm.setTermType("like");
        treeTerm.setColumn("tree_path");
        treeTerm.setValue((user.getTreePath() == null ? "," : user.getTreePath()) + user.getId() + ",%");
        subTerms.add(treeTerm);

        Term selfTerm = new Term();
        selfTerm.setType(Term.Type.or);
        selfTerm.setTermType("eq");
        selfTerm.setColumn("user_id");
        selfTerm.setValue(user.getId());
        subTerms.add(selfTerm);

        return subTerms;
    }

    private Mono<Tuple3<Authentication, UserDetail, QueryParamEntity>> currentUserWithQuery(Mono<QueryParamEntity> query) {
        return Authentication
            .currentReactive()
            .switchIfEmpty(Mono.error(UnAuthorizedException.NoStackTrace::new))
            .flatMap(auth -> userDetailService
                .findUserDetail(auth.getUser().getId())
                .zipWith(query)
                .map(tuple -> Tuples.of(auth, tuple.getT1(), tuple.getT2())));
    }

    private String getIp(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        HttpHeaders headers = request.getHeaders();
        String ip = headers.getFirst("x-forwarded-for");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip) && ip.contains(",")) {
            ip = ip.split(",")[0];
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = headers.getFirst("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = headers.getFirst("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = headers.getFirst("X-Real-IP");
        }
        if ((ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) && request.getRemoteAddress() != null) {
            ip = request.getRemoteAddress().getAddress().getHostAddress();
        }
        return ip == null ? "" : ip.replaceAll(":", ".");
    }
}
