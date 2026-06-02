package org.jetlinks.community.auth.service;

import org.hswebframework.ezorm.rdb.executor.reactive.ReactiveSqlExecutor;
import org.hswebframework.web.api.crud.entity.PagerResult;
import org.hswebframework.web.api.crud.entity.QueryParamEntity;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.ReactiveAuthenticationManager;
import org.hswebframework.web.bean.FastBeanCopier;
import org.hswebframework.web.crud.query.QueryHelper;
import org.hswebframework.web.crud.service.GenericReactiveCrudService;
import org.hswebframework.web.i18n.LocaleUtils;
import org.hswebframework.web.system.authorization.api.entity.UserEntity;
import org.hswebframework.web.system.authorization.api.event.UserDeletedEvent;
import org.hswebframework.web.system.authorization.api.service.reactive.ReactiveUserService;
import org.hswebframework.web.validator.ValidatorUtils;
import org.jetlinks.community.auth.entity.CustomerDetail;
import org.jetlinks.community.auth.entity.UserDetail;
import org.jetlinks.community.auth.entity.UserDetailEntity;
import org.jetlinks.community.auth.enums.DefaultUserEntityType;
import org.jetlinks.community.auth.enums.UserEntityTypes;
import org.jetlinks.community.auth.service.request.SaveUserDetailRequest;
import org.jetlinks.community.auth.service.request.SaveUserRequest;
import org.reactivestreams.Publisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class UserDetailService extends GenericReactiveCrudService<UserDetailEntity, String> {

    private final ReactiveUserService userService;
    private final RoleService roleService;
    private final OrganizationService organizationService;
    private final ReactiveAuthenticationManager authenticationManager;
    private final QueryHelper queryHelper;
    private final ReactiveSqlExecutor sqlExecutor;

    private final static UserDetailEntity emptyDetail = new UserDetailEntity();

    public UserDetailService(ReactiveUserService userService,
                             RoleService roleService,
                             OrganizationService organizationService,
                             ReactiveAuthenticationManager authenticationManager,
                             QueryHelper queryHelper,
                             ReactiveSqlExecutor sqlExecutor) {
        this.userService = userService;
        this.roleService = roleService;
        this.organizationService = organizationService;
        this.authenticationManager = authenticationManager;
        this.queryHelper = queryHelper;
        this.sqlExecutor = sqlExecutor;
        UserEntityTypes.register(Arrays.asList(DefaultUserEntityType.values()));
    }

    public Mono<UserDetail> findUserDetail(String userId) {
        return Mono
            .zip(
                userService.findById(userId),
                this.findById(userId).defaultIfEmpty(emptyDetail),
                authenticationManager
                    .getByUserId(userId)
                    .map(Authentication::getDimensions)
                    .defaultIfEmpty(Collections.emptyList())
            )
            .map(tuple -> UserDetail
                .of(tuple.getT1())
                .with(tuple.getT2())
                .withDimension(tuple.getT3()));
    }

    public Mono<Void> saveUserDetail(String userId, SaveUserDetailRequest request) {
        ValidatorUtils.tryValidate(request);
        UserDetailEntity entity = FastBeanCopier.copy(request, new UserDetailEntity());
        entity.setId(userId);

        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setName(request.getName());

        return this
            .save(entity)
            .then(userService.saveUser(Mono.just(userEntity)))
            .as(LocaleUtils::transform)
            .then();
    }

    public Mono<PagerResult<CustomerDetail>> queryCustomerDetail(QueryParamEntity query) {
        return this.queryHelper
            .select(
                "select * from s_user t  inner join s_user_detail userDetail on userDetail.id = t.id  inner join s_user creator on creator.id = t.creator_id ",
                CustomerDetail::new)
            .where(query)
            .fetchPaged();
    }

    public Mono<List<UserEntity>> querySubUser(QueryParamEntity query) {
        return this.userService.findUser(query).collectList();
    }

    public Mono<List<UserEntity>> queryCustomers(QueryParamEntity query) {
        return this.queryHelper
            .select(
                " SELECT t1.id,t1.name FROM `s_user` t INNER JOIN `s_user_detail` t1  ON t1.`id`= t.id  ",
                UserEntity::new)
            .where(query)
            .fetch()
            .collectList();
    }

    public Mono<PagerResult<UserDetail>> queryUserDetail(QueryParamEntity query) {
        return Mono
            .zip(
                userService.countUser(query),
                userService.findUser(query).collectList())
            .flatMap(tuple -> {
                List<UserEntity> userList = tuple.getT2();
                if (userList.isEmpty()) {
                    return Mono.just(PagerResult.of(tuple.getT1(), Collections.emptyList(), query));
                }

                List<String> userIds = userList
                    .stream()
                    .map(UserEntity::getId)
                    .collect(Collectors.toList());

                return this.createQuery()
                    .in(UserDetailEntity::getId, userIds)
                    .fetch()
                    .collectMap(UserDetailEntity::getId)
                    .flatMap(userDetailMap -> {
                        List<UserDetail> details = userList
                            .stream()
                            .map(user -> {
                                UserDetail detail = UserDetail.of(user);
                                UserDetailEntity entity = userDetailMap.get(user.getId());
                                return entity == null ? detail : detail.with(entity);
                            })
                            .collect(Collectors.toList());

                        List<String> creatorIds = details
                            .stream()
                            .map(UserDetail::getCreatorId)
                            .filter(StringUtils::hasText)
                            .distinct()
                            .collect(Collectors.toList());

                        Mono<List<UserDetail>> withCreators = fetchCreatorDetails(creatorIds, details);
                        return withCreators.map(list -> PagerResult.of(tuple.getT1(), list, query));
                    });
            });
    }

    private Mono<List<UserDetail>> fetchCreatorDetails(Collection<String> creatorIds, List<UserDetail> details) {
        if (creatorIds.isEmpty()) {
            return Mono.just(details);
        }
        return this.createQuery()
            .in(UserDetailEntity::getId, creatorIds)
            .fetch()
            .collectMap(UserDetailEntity::getId)
            .map(creatorMap -> {
                details.forEach(detail -> detail.setCreateUser(creatorMap.get(detail.getCreatorId())));
                return details;
            });
    }

    @Transactional
    public Mono<String> saveUser(SaveUserRequest request) {
        request.validate();
        UserDetail detail = request.getUser();
        boolean isUpdate = StringUtils.hasText(detail.getId());
        UserEntity entity = request.getUser().toUserEntity();

        return userService
            .saveUser(Mono.just(entity))
            .then(Mono.fromSupplier(entity::getId))
            .flatMap(userId -> {
                detail.setId(userId);
                return updateTreePath(userId, detail, entity, isUpdate)
                    .then(Mono.fromSupplier(detail::toDetailEntity).flatMap(this::save))
                    .then(roleService.bindUser(Collections.singleton(userId), request.getRoleIdList(), isUpdate))
                    .then(organizationService.bindUser(Collections.singleton(userId), request.getOrgIdList(), isUpdate))
                    .thenReturn(userId);
            })
            .as(LocaleUtils::transform);
    }

    private Mono<Void> updateTreePath(String userId, UserDetail detail, UserEntity requestedEntity, boolean isUpdate) {
        return this.userService
            .findById(userId)
            .flatMap(savedUser -> {
                String creatorId = isUpdate ? requestedEntity.getCreatorId() : savedUser.getCreatorId();
                String creatorLookupId = creatorId == null ? "" : creatorId;

                return this.findById(creatorLookupId)
                    .switchIfEmpty(Mono.fromSupplier(() -> {
                        if (!isUpdate) {
                            detail.setTreePath(",");
                        }
                        return detail.toDetailEntity();
                    }))
                    .flatMap(creator -> {
                        boolean creatorChanged = isUpdate
                            && StringUtils.hasText(requestedEntity.getCreatorId())
                            && !Objects.equals(savedUser.getCreatorId(), requestedEntity.getCreatorId());

                        String oldTreePath = String.valueOf(detail.getTreePath()) + userId + ",";
                        if (!isUpdate || creatorChanged) {
                            String creatorTreePath = creator.getTreePath() == null ? "," : creator.getTreePath();
                            detail.setTreePath(creatorTreePath + creatorId + ",");
                        }

                        if (!creatorChanged) {
                            return Mono.just(creator);
                        }

                        String newTreePath = detail.getTreePath() + userId + ",";
                        return this.sqlExecutor
                            .update("update s_user set creator_id='" + creatorId + "' where id= '" + userId + "'")
                            .then(this.sqlExecutor.update("update s_user_detail set tree_path=replace(tree_path,'" + oldTreePath + "','" + newTreePath + "') where tree_path like '" + oldTreePath + "%'"))
                            .thenReturn(creator);
                    });
            })
            .then();
    }

    @EventListener
    public void handleUserDeletedEvent(UserDeletedEvent event) {
        event.async((Publisher<?>) this.deleteById(event.getUser().getId()));
    }
}
