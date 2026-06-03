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
package org.jetlinks.community.auth.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.hswebframework.ezorm.core.param.Term;
import org.hswebframework.web.api.crud.entity.PagerResult;
import org.hswebframework.web.api.crud.entity.QueryParamEntity;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.Dimension;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.authorization.annotation.QueryAction;
import org.hswebframework.web.authorization.annotation.Resource;
import org.hswebframework.web.authorization.annotation.SaveAction;
import org.hswebframework.web.authorization.exception.UnAuthorizedException;
import org.hswebframework.web.exception.BusinessException;
import org.hswebframework.web.system.authorization.api.entity.UserEntity;
import org.jetlinks.community.auth.entity.CustomerDetail;
import org.jetlinks.community.auth.entity.UserDetail;
import org.jetlinks.community.auth.service.UserDetailService;
import org.jetlinks.community.auth.service.request.SaveUserDetailRequest;
import org.jetlinks.community.auth.service.request.SaveUserRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/customer")
@Tag(name = "我的客户接口")
@Resource(id = "customer", name = "我的客户", group = "iot")
public class CustomerController {

    private final UserDetailService userDetailService;

    public CustomerController(UserDetailService userDetailService) {
        this.userDetailService = userDetailService;
    }

    @PostMapping("/_create")
    @SaveAction
    @Operation(summary = "创建用户")
    @Transactional
    public Mono<String> createUser(@RequestBody Mono<SaveUserRequest> body) {
        return Authentication
            .currentReactive()
            .switchIfEmpty(Mono.error(UnAuthorizedException.NoStackTrace::new))
            .zipWith(body)
            .flatMap(tuple -> {
                Authentication authentication = tuple.getT1();
                if ("admin".equals(authentication.getUser().getUsername())) {
                    throw new BusinessException("管理员请在用户管理里面添加！");
                }

                List<Dimension> roles = authentication.getDimensions("role");
                List<Dimension> orgs = authentication.getDimensions("org");
                Set<String> orgIdList = orgs.stream().map(Dimension::getId).collect(Collectors.toSet());
                Set<String> roleIdList = roles.stream().map(Dimension::getId).collect(Collectors.toSet());

                SaveUserRequest request = tuple.getT2();
                request.setOrgIdList(orgIdList);
                request.setRoleIdList(roleIdList);
                return userDetailService.saveUser(request);
            });
    }

    @PutMapping("/{userId}/_update")
    @SaveAction
    @Operation(summary = "修改用户")
    public Mono<String> updateUser(@PathVariable String userId,
                                   @RequestBody Mono<SaveUserRequest> body) {
        return body
            .doOnNext(request -> {
                if (request.getUser() != null) {
                    request.getUser().setId(userId);
                }
            })
            .flatMap(userDetailService::saveUser);
    }

    @GetMapping("/{userId}")
    @QueryAction
    @Operation(summary = "获取用户详情信息")
    public Mono<UserDetail> getUserDetail(@PathVariable String userId) {
        return userDetailService.findUserDetail(userId);
    }

    @PostMapping("/_query")
    @QueryAction
    @Operation(summary = "分页获取客户详情")
    public Mono<PagerResult<CustomerDetail>> querySubUserDetail(@RequestBody Mono<QueryParamEntity> query) {
        return Authentication
            .currentReactive()
            .switchIfEmpty(Mono.error(UnAuthorizedException.NoStackTrace::new))
            .flatMap(authentication -> userDetailService
                .findUserDetail(authentication.getUser().getId())
                .zipWith(query)
                .flatMap(tuple -> {
                    QueryParamEntity param = appendSubUserTerm(tuple.getT2(), tuple.getT1(), false);
                    param.setOrderBy("create_time DESC");
                    return userDetailService.queryCustomerDetail(param);
                }));
    }

    @PostMapping("/no-paging/_query")
    @QueryAction
    @Operation(summary = "获取下级客户详情")
    public Mono<List<UserEntity>> querySubUserList(@RequestBody Mono<QueryParamEntity> query) {
        return Authentication
            .currentReactive()
            .switchIfEmpty(Mono.error(UnAuthorizedException.NoStackTrace::new))
            .flatMap(authentication -> userDetailService
                .findUserDetail(authentication.getUser().getId())
                .zipWith(query)
                .flatMap(tuple -> userDetailService.queryCustomers(appendSubUserTerm(tuple.getT2(), tuple.getT1(), true))));
    }

    @GetMapping
    @Operation(summary = "获取当前登录用户详情")
    @Authorize(merge = false)
    public Mono<UserDetail> getCurrentLoginUserDetail() {
        return Authentication
            .currentReactive()
            .switchIfEmpty(Mono.error(UnAuthorizedException.NoStackTrace::new))
            .flatMap(authentication -> userDetailService
                .findUserDetail(authentication.getUser().getId())
                .switchIfEmpty(Mono.fromSupplier(() -> new UserDetail().with(authentication))));
    }

    @PutMapping
    @Operation(summary = "保存当前用户详情")
    @Authorize(merge = false)
    public Mono<Void> saveUserDetail(@RequestBody Mono<SaveUserDetailRequest> request) {
        return Authentication
            .currentReactive()
            .zipWith(request)
            .switchIfEmpty(Mono.error(UnAuthorizedException.NoStackTrace::new))
            .flatMap(tuple -> userDetailService.saveUserDetail(tuple.getT1().getUser().getId(), tuple.getT2()));
    }

    private QueryParamEntity appendSubUserTerm(QueryParamEntity param, UserDetail detail, boolean includeSelf) {
        List<Term> terms = param.getTerms();
        if (terms == null) {
            terms = new ArrayList<>();
        }

        Term wrapper = new Term();
        wrapper.setType(Term.Type.and);
        wrapper.setTerms(createSubUserTerms(detail, includeSelf));
        terms.add(wrapper);
        param.setTerms(terms);
        return param;
    }

    private List<Term> createSubUserTerms(UserDetail detail, boolean includeSelf) {
        List<Term> subTerms = new ArrayList<>();

        Term treeTerm = new Term();
        treeTerm.setType(Term.Type.or);
        treeTerm.setTermType("like");
        treeTerm.setColumn("tree_path");
        treeTerm.setValue((detail.getTreePath() == null ? "," : detail.getTreePath()) + detail.getId() + ",%");
        subTerms.add(treeTerm);

        if (includeSelf) {
            Term selfTerm = new Term();
            selfTerm.setType(Term.Type.or);
            selfTerm.setTermType("eq");
            selfTerm.setColumn("id");
            selfTerm.setValue(detail.getId());
            subTerms.add(selfTerm);
        }

        return subTerms;
    }
}
