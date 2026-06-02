/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.alibaba.fastjson.JSONObject
 *  org.hswebframework.ezorm.core.param.QueryParam
 *  org.hswebframework.ezorm.core.param.Term
 *  org.hswebframework.ezorm.rdb.mapping.ReactiveQuery
 *  org.hswebframework.ezorm.rdb.mapping.ReactiveUpdate
 *  org.hswebframework.web.api.crud.entity.GenericEntity
 *  org.hswebframework.web.api.crud.entity.PagerResult
 *  org.hswebframework.web.crud.events.EntityCreatedEvent
 *  org.hswebframework.web.crud.events.EntityDeletedEvent
 *  org.hswebframework.web.crud.events.EntityModifyEvent
 *  org.hswebframework.web.crud.events.EntitySavedEvent
 *  org.hswebframework.web.crud.service.GenericReactiveCrudService
 *  org.hswebframework.web.id.IDGenerator
 *  org.jetlinks.community.elastic.search.index.ElasticIndex
 *  org.jetlinks.community.elastic.search.service.ElasticSearchService
 *  org.jetlinks.community.rule.engine.entity.RuleEngineExecuteEventInfo
 *  org.jetlinks.community.rule.engine.event.handler.RuleEngineLoggerIndexProvider
 *  org.jetlinks.rule.engine.api.RuleEngine
 *  org.reactivestreams.Publisher
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.boot.CommandLineRunner
 *  org.springframework.context.event.EventListener
 *  org.springframework.stereotype.Service
 *  org.springframework.transaction.annotation.Transactional
 *  org.springframework.util.Assert
 *  org.springframework.util.StringUtils
 *  reactor.core.publisher.Flux
 *  reactor.core.publisher.Mono
 */
package org.jetlinks.community.rule.engine.service;

import com.alibaba.fastjson.JSONObject;
import java.util.ArrayList;
import java.util.Collection;
import org.hswebframework.ezorm.core.param.QueryParam;
import org.hswebframework.ezorm.core.param.Term;
import org.hswebframework.web.api.crud.entity.PagerResult;
import org.hswebframework.web.crud.events.EntityCreatedEvent;
import org.hswebframework.web.crud.events.EntityDeletedEvent;
import org.hswebframework.web.crud.events.EntityModifyEvent;
import org.hswebframework.web.crud.events.EntitySavedEvent;
import org.hswebframework.web.crud.service.GenericReactiveCrudService;
import org.hswebframework.web.id.IDGenerator;
import org.jetlinks.community.elastic.search.index.ElasticIndex;
import org.jetlinks.community.elastic.search.service.ElasticSearchService;
import org.jetlinks.community.rule.engine.entity.DeviceJob;
import org.jetlinks.community.rule.engine.entity.RuleEngineExecuteEventInfo;
import org.jetlinks.community.rule.engine.enums.RuleInstanceState;
import org.jetlinks.community.rule.engine.event.handler.RuleEngineLoggerIndexProvider;
import org.jetlinks.community.rule.engine.model.DeviceJobLog;
import org.jetlinks.rule.engine.api.RuleEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class DeviceJobService
extends GenericReactiveCrudService<DeviceJob, String>
implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DeviceJobService.class);
    private final RuleEngine ruleEngine;
    @Autowired
    private ElasticSearchService elasticSearchService;

    public Mono<PagerResult<DeviceJobLog>> queryExecuteEvent(QueryParam queryParam) {
        queryParam.getTerms().add(Term.of((String)"nodeId", (String)"eq", (Object)"action_1", (String[])new String[0]));
        queryParam.getTerms().add(Term.of((String)"event", (String)"eq", (Object)"result", (String[])new String[0]));
        return this.elasticSearchService.queryPager((ElasticIndex)RuleEngineLoggerIndexProvider.RULE_EVENT_LOG, queryParam, RuleEngineExecuteEventInfo.class).flatMap(pager -> {
            PagerResult<DeviceJobLog> result = PagerResult.of((int)pager.getTotal(), new ArrayList<>(), (QueryParam)queryParam);
            result.setPageIndex(pager.getPageIndex());
            result.setPageSize(pager.getPageSize());
            pager.getData().forEach(data -> {
                String ruleData = data.getRuleData();
                JSONObject json = JSONObject.parseObject((String)ruleData);
                JSONObject msgData = json.getJSONObject("headers").getJSONObject("rd:action_1");
                JSONObject dtData = json.getJSONObject("headers").getJSONObject("rd:scene");
                String messageId = msgData.getString("messageId");
                String functionId = msgData.getString("functionId");
                Boolean sucess = msgData.getBoolean("success");
                Long createTime = dtData.getLong("timestamp");
                result.getData().add(DeviceJobLog.of(createTime, "", messageId, functionId, sucess));
            });
            return Mono.just(result);
        });
    }

    @Transactional(rollbackFor={Throwable.class})
    public Mono<DeviceJob> createDeviceJob(DeviceJob job) {
        if (!StringUtils.hasText((String)((String)job.getId()))) {
            job.setId(IDGenerator.SNOW_FLAKE_STRING.generate());
        }
        job.setState(RuleInstanceState.disable);
        return this.insert(job).thenReturn(job);
    }

    @Transactional(rollbackFor={Throwable.class})
    public Mono<DeviceJob> updateDeviceJob(String id, DeviceJob job) {
        job.setId(id);
        return this.updateById(id, job).thenReturn(job);
    }

    @Transactional(rollbackFor={Throwable.class})
    public Mono<Void> enable(String id) {
        Assert.hasText((String)id, (String)"id can not be empty");
        long now = System.currentTimeMillis();
        return this.createUpdate()
                   .set("state", RuleInstanceState.started)
                   .set("modifyTime", now)
                   .set("startTime", now)
                   .where("id", id)
                   .execute()
                   .then();
    }

    @Transactional
    public Mono<Void> disabled(String id) {
        Assert.hasText((String)id, (String)"id can not be empty");
        return this.createUpdate()
                   .set("state", RuleInstanceState.disable)
                   .where("id", id)
                   .execute()
                   .then();
    }

    @EventListener
    public void handleSceneSaved(EntitySavedEvent<DeviceJob> event) {
        event.async(this.handleEvent(event.getEntity()));
    }

    @EventListener
    public void handleSceneSaved(EntityModifyEvent<DeviceJob> event) {
        event.async(this.handleEvent(event.getAfter()));
    }

    @EventListener
    public void handleSceneSaved(EntityCreatedEvent<DeviceJob> event) {
        event.async(this.handleEvent(event.getEntity()));
    }

    @EventListener
    public void handleSceneDelete(EntityDeletedEvent<DeviceJob> event) {
        for (DeviceJob entity : event.getEntity()) {
            entity.setState(RuleInstanceState.disable);
        }
        event.async(this.handleEvent(event.getEntity()));
    }

    private Mono<Void> handleEvent(Collection<DeviceJob> entities) {
        return Flux.fromIterable(entities).flatMap(job -> {
            if (job.getState() == RuleInstanceState.disable) {
                return this.ruleEngine.shutdown((String)job.getId());
            }
            if (job.getState() == RuleInstanceState.started) {
                job.validate();
                return this.ruleEngine.startRule((String)job.getId(), job.toRule().getModel());
            }
            return Mono.empty();
        }).then();
    }

    public void run(String ... args) throws Exception {
        this.createQuery()
            .where("state", RuleInstanceState.started)
            .fetch()
            .flatMap(e -> Mono.defer(() -> this.ruleEngine.startRule(e.getId(), e.toRule().getModel()).then()).onErrorResume(err -> {
            log.warn("\u542f\u52a8\u8bbe\u5907\u5b9a\u65f6\u4efb\u52a1[{}]\u5931\u8d25", (Object)(e.getDeviceId() + "_" + (String)e.getId()), err);
            return Mono.empty();
        })).subscribe();
    }

    public DeviceJobService(RuleEngine ruleEngine, ElasticSearchService elasticSearchService) {
        this.ruleEngine = ruleEngine;
        this.elasticSearchService = elasticSearchService;
    }
}
