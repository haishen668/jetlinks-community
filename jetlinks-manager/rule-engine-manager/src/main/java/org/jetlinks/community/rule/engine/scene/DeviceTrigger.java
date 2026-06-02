/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Sets
 *  io.swagger.v3.oas.annotations.media.Schema
 *  javax.validation.constraints.NotBlank
 *  javax.validation.constraints.NotNull
 *  org.hswebframework.ezorm.core.param.Term
 *  org.hswebframework.ezorm.rdb.executor.PrepareSqlRequest
 *  org.hswebframework.ezorm.rdb.executor.SqlRequest
 *  org.hswebframework.ezorm.rdb.operator.builder.fragments.AbstractTermsFragmentBuilder
 *  org.hswebframework.ezorm.rdb.operator.builder.fragments.EmptySqlFragments
 *  org.hswebframework.ezorm.rdb.operator.builder.fragments.SqlFragments
 *  org.hswebframework.web.bean.FastBeanCopier
 *  org.hswebframework.web.i18n.LocaleUtils
 *  org.hswebframework.web.validator.ValidatorUtils
 *  org.jetlinks.community.TimerSpec
 *  org.jetlinks.community.reactorql.term.TermTypeSupport
 *  org.jetlinks.community.reactorql.term.TermTypes
 *  org.jetlinks.community.rule.engine.executor.DeviceMessageSendTaskExecutorProvider$DeviceMessageSendConfig
 *  org.jetlinks.community.rule.engine.executor.device.DeviceSelectorProviders
 *  org.jetlinks.community.rule.engine.executor.device.DeviceSelectorSpec
 *  org.jetlinks.community.rule.engine.executor.device.SelectorValue
 *  org.jetlinks.core.device.DeviceRegistry
 *  org.jetlinks.core.metadata.DataType
 *  org.jetlinks.core.metadata.DeviceMetadata
 *  org.jetlinks.core.metadata.types.StringType
 *  org.jetlinks.core.things.ThingMetadata
 *  org.jetlinks.core.utils.Reactors
 *  org.jetlinks.reactor.ql.DefaultReactorQLContext
 *  org.jetlinks.reactor.ql.ReactorQL
 *  org.jetlinks.reactor.ql.ReactorQLContext
 *  org.jetlinks.rule.engine.api.model.RuleModel
 *  org.jetlinks.rule.engine.api.model.RuleNodeModel
 *  org.springframework.util.Assert
 *  org.springframework.util.CollectionUtils
 *  org.springframework.util.StringUtils
 *  reactor.core.publisher.Flux
 *  reactor.core.publisher.Mono
 */
package org.jetlinks.community.rule.engine.scene;

import com.google.common.collect.Sets;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import org.hswebframework.ezorm.core.param.Term;
import org.hswebframework.ezorm.rdb.executor.PrepareSqlRequest;
import org.hswebframework.ezorm.rdb.executor.SqlRequest;
import org.hswebframework.ezorm.rdb.operator.builder.fragments.AbstractTermsFragmentBuilder;
import org.hswebframework.ezorm.rdb.operator.builder.fragments.EmptySqlFragments;
import org.hswebframework.ezorm.rdb.operator.builder.fragments.SqlFragments;
import org.hswebframework.web.bean.FastBeanCopier;
import org.hswebframework.web.i18n.LocaleUtils;
import org.hswebframework.web.validator.ValidatorUtils;
import org.jetlinks.community.TimerSpec;
import org.jetlinks.community.reactorql.term.TermTypeSupport;
import org.jetlinks.community.reactorql.term.TermTypes;
import org.jetlinks.community.rule.engine.executor.DeviceMessageSendTaskExecutorProvider;
import org.jetlinks.community.rule.engine.executor.device.DeviceSelectorProviders;
import org.jetlinks.community.rule.engine.executor.device.DeviceSelectorSpec;
import org.jetlinks.community.rule.engine.executor.device.SelectorValue;
import org.jetlinks.community.rule.engine.scene.DeviceOperation;
import org.jetlinks.community.rule.engine.scene.SceneUtils;
import org.jetlinks.community.rule.engine.scene.Variable;
import org.jetlinks.community.rule.engine.scene.term.TermColumn;
import org.jetlinks.community.rule.engine.scene.value.TermValue;
import org.jetlinks.core.device.DeviceRegistry;
import org.jetlinks.core.metadata.DataType;
import org.jetlinks.core.metadata.DeviceMetadata;
import org.jetlinks.core.metadata.types.StringType;
import org.jetlinks.core.things.ThingMetadata;
import org.jetlinks.core.things.ThingsRegistry;
import org.jetlinks.core.utils.Reactors;
import org.jetlinks.reactor.ql.DefaultReactorQLContext;
import org.jetlinks.reactor.ql.ReactorQL;
import org.jetlinks.reactor.ql.ReactorQLContext;
import org.jetlinks.rule.engine.api.model.RuleModel;
import org.jetlinks.rule.engine.api.model.RuleNodeModel;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class DeviceTrigger
extends DeviceSelectorSpec
implements SceneTriggerProvider.TriggerConfig, Serializable {
    private static final long serialVersionUID = 1L;
    @NotBlank(message="error.scene_rule_trigger_device_product_cannot_be_null")
    @Schema(description="\u4ea7\u54c1ID")
    private @NotBlank(message="error.scene_rule_trigger_device_product_cannot_be_null") String productId;
    @Schema(description="\u64cd\u4f5c\u65b9\u5f0f")
    @NotNull(message="error.scene_rule_trigger_device_operation_cannot_be_null")
    private @NotNull(message="error.scene_rule_trigger_device_operation_cannot_be_null") DeviceOperation operation;
    public static final TermBuilder termBuilder = new TermBuilder();

    public SqlRequest createSql(List<Term> terms) {
        return this.createSql(terms, true);
    }

    public SqlRequest createSql(List<Term> terms, boolean hasWhere) {
        Map<String, Term> termsMap = SceneUtils.expandTerm(terms);
        LinkedHashSet<String> selectColumns = Sets.newLinkedHashSetWithExpectedSize((int)(10 + termsMap.size()));
        selectColumns.add("now() \"_now\"");
        selectColumns.add("this.timestamp \"timestamp\"");
        selectColumns.add("this.deviceId \"deviceId\"");
        selectColumns.add("this.headers.deviceName \"deviceName\"");
        selectColumns.add("this.headers.productId \"productId\"");
        selectColumns.add("this.headers.productName \"productName\"");
        selectColumns.add("'device' \"sourceType\"");
        selectColumns.add("this.deviceId \"sourceId\"");
        selectColumns.add("this.deviceName \"sourceName\"");
        selectColumns.add("this.headers._uid \"_uid\"");
        selectColumns.add("this.headers.bindings \"_bindings\"");
        selectColumns.add("this.headers.traceparent \"traceparent\"");
        switch (this.operation.getOperator()) {
            case readProperty: 
            case writeProperty: {
                selectColumns.add("this.success \"success\"");
            }
            case reportProperty: {
                selectColumns.add("this.properties \"properties\"");
                break;
            }
            case reportEvent: {
                selectColumns.add("this.data \"data\"");
                break;
            }
            case invokeFunction: {
                selectColumns.add("this.success \"success\"");
                selectColumns.add("this['output'] \"output\"");
            }
        }
        for (Term value : termsMap.values()) {
            String selectColumn;
            String column = value.getColumn();
            if (!StringUtils.hasText((String)value.getColumn()) || (selectColumn = DeviceTrigger.createSelectColumn(column)) == null) continue;
            String alias = DeviceTrigger.createColumnAlias(value.getColumn());
            List<TermValue> termValues = TermValue.of(value);
            selectColumns.add(selectColumn + " " + alias);
            for (TermValue termValue : termValues) {
                String property;
                if (termValue == null || termValue.getSource() != TermValue.Source.metric || null == (property = DeviceTrigger.parseProperty(column))) continue;
                selectColumns.add(String.format("property.metric('device',deviceId,'%s','%s') %s_metric_%s", property, termValue.getMetric(), property, termValue.getMetric()));
            }
        }
        StringBuilder builder = new StringBuilder();
        builder.append("select * from (\n");
        builder.append("\tselect\n");
        int idx = 0;
        for (String selectColumn : selectColumns) {
            if (idx++ > 0) {
                builder.append(",\n");
            }
            builder.append("\t").append(selectColumn);
        }
        builder.append("\t\nfrom ").append(this.createFromTable());
        builder.append("\n) t \n");
        if (hasWhere) {
            SqlFragments fragments = terms == null ? EmptySqlFragments.INSTANCE : termBuilder.createTermFragments(this, terms);
            if (!fragments.isEmpty()) {
                SqlRequest request = fragments.toRequest();
                builder.append("where ").append(request.getSql());
            }
            return PrepareSqlRequest.of((String)builder.toString(), (Object[])fragments.getParameters().toArray());
        }
        return PrepareSqlRequest.of((String)builder.toString(), (Object[])new Object[0]);
    }

    String createFilterDescription(List<Term> terms) {
        SqlFragments fragments = CollectionUtils.isEmpty(terms) ? EmptySqlFragments.INSTANCE : termBuilder.createTermFragments(this, terms);
        return fragments.isEmpty() ? "true" : fragments.toRequest().toNativeSql();
    }

    Function<Map<String, Object>, Mono<Boolean>> createFilter(List<Term> terms) {
            SqlFragments fragments = CollectionUtils.isEmpty(terms) ? EmptySqlFragments.INSTANCE : termBuilder.createTermFragments(this, terms);
        if (!fragments.isEmpty()) {
            SqlRequest request = fragments.toRequest();
            String sql = "select 1 from t where " + request.getSql();
            final ReactorQL ql = ReactorQL.builder().sql(new String[]{sql}).build();
            final List<Object> args = Arrays.asList(request.getParameters());
            final String sqlString = request.toNativeSql();
            return new Function<Map<String, Object>, Mono<Boolean>>(){

                @Override
                public Mono<Boolean> apply(Map<String, Object> map) {
                    DefaultReactorQLContext context = new DefaultReactorQLContext(t -> Flux.just((Object)map), args);
                    return ql.start((ReactorQLContext)context).hasElements();
                }

                public String toString() {
                    return sqlString;
                }
            };
        }
        return ignore -> Reactors.ALWAYS_TRUE;
    }

    private String createFromTable() {
        String topic = null;
        switch (this.operation.getOperator()) {
            case reportProperty: {
                topic = "/device/" + this.productId + "/%s/message/property/report";
                break;
            }
            case reportEvent: {
                topic = "/device/" + this.productId + "/%s/message/event/" + this.operation.getEventId();
                break;
            }
            case online: {
                topic = "/device/" + this.productId + "/%s/online";
                break;
            }
            case offline: {
                topic = "/device/" + this.productId + "/%s/offline";
            }
        }
        if (null == topic) {
            return "dual";
        }
        String selector = this.getSelector();
        if (!StringUtils.hasText((String)selector)) {
            selector = "all";
        }
        switch (selector) {
            case "all": {
                topic = String.format(topic, "*");
                break;
            }
            case "fixed": {
                String scope = this.getSelectorValues().stream().map(SelectorValue::getValue).map(String::valueOf).collect(Collectors.joining(","));
                topic = String.format(topic, scope);
                break;
            }
            default: {
                String scope = this.getSelectorValues().stream().map(SelectorValue::getValue).map(String::valueOf).collect(Collectors.joining(","));
                topic = "/" + selector + "/" + scope + String.format(topic, "*");
            }
        }
        return "\"" + topic + "\"";
    }

    static String createTermColumn(String tableName, String column) {
        String[] arr = column.split("[.]");
        column = arr.length > 3 && arr[0].equals("properties") ? tableName + "['" + DeviceTrigger.createColumnAlias(column, false) + "." + String.join((CharSequence)".", Arrays.copyOfRange(arr, 2, arr.length - 1)) + "']" : tableName + "['" + DeviceTrigger.createColumnAlias(column, false) + "']";
        return column;
    }

    static Term refactorTermValue(String tableName, Term term) {
        if (term.getColumn() == null) {
            return term;
        }
        String[] arr = term.getColumn().split("[.]");
        List<TermValue> values = TermValue.of(term);
        if (values.size() == 0) {
            return term;
        }
        Function<TermValue, Object> parser = value -> {
            if (value.getSource() == TermValue.Source.variable || value.getSource() == TermValue.Source.upper) {
                term.getOptions().add("native");
                return tableName + "['" + value.getValue() + "']";
            }
            if (value.getSource() == TermValue.Source.metric) {
                term.getOptions().add("native");
                return tableName + "['" + arr[1] + "_metric_" + value.getMetric() + "']";
            }
            return value.getValue();
        };
        Object val = values.size() == 1 ? parser.apply(values.get(0)) : values.stream().map(parser).collect(Collectors.toList());
        if (!term.getOptions().contains("native")) {
            String column = arr.length > 3 && arr[0].equals("properties") ? tableName + "['" + DeviceTrigger.createColumnAlias(term.getColumn(), false) + "." + String.join((CharSequence)".", Arrays.copyOfRange(arr, 2, arr.length - 1)) + "']" : (!DeviceTrigger.isBranchTerm(arr[0]) ? tableName + "['" + DeviceTrigger.createColumnAlias(term.getColumn(), false) + "']" : term.getColumn());
            term.setColumn(column);
        }
        term.setValue(val);
        return term;
    }

    private static boolean isBranchTerm(String column) {
        return column.startsWith("branch_") && column.contains("_group_") && column.contains("_action_");
    }

    static String parseProperty(String column) {
        String[] arr = column.split("[.]");
        if ("properties".equals(arr[0])) {
            return arr[1];
        }
        return null;
    }

    static String createSelectColumn(String column) {
        if (!column.contains(".")) {
            return null;
        }
        String[] arr = column.split("[.]");
        if ("properties".equals(arr[0])) {
            try {
                DeviceOperation.PropertyValueType valueType = DeviceOperation.PropertyValueType.valueOf(arr[arr.length - 1]);
                String property = arr[1];
                switch (valueType) {
                    case current: {
                        return "this['properties." + property + "']";
                    }
                    case recent: {
                        return "coalesce(this['properties." + property + "']" + ",device.property.recent(deviceId,'" + property + "',timestamp))";
                    }
                    case last: {
                        return "device.property.recent(deviceId,'" + property + "',timestamp)";
                    }
                }
            }
            catch (IllegalArgumentException illegalArgumentException) {
                // empty catch block
            }
        }
        return "this['" + String.join((CharSequence)".", Arrays.copyOfRange(arr, 1, arr.length)) + "']";
    }

    static String createColumnAlias(String column, boolean wrapColumn) {
        String alias;
        if (!column.contains(".")) {
            return wrapColumn ? DeviceTrigger.wrapColumnName(column) : column;
        }
        String[] arr = column.split("[.]");
        if ("properties".equals(arr[0])) {
            String property = arr[1];
            alias = property + "_" + arr[arr.length - 1];
        } else {
            alias = arr.length > 1 ? String.join((CharSequence)"_", Arrays.copyOfRange(arr, 1, arr.length)) : column.replace(".", "_");
        }
        return wrapColumn ? DeviceTrigger.wrapColumnName(alias) : alias;
    }

    static String createColumnAlias(String column) {
        return DeviceTrigger.createColumnAlias(column, true);
    }

    static String wrapColumnName(String column) {
        if (column.startsWith("\"") && column.endsWith("\"")) {
            return column;
        }
        return "\"" + column.replace("\"", "\\\"") + "\"";
    }

    public List<Variable> createDefaultVariable() {
        return Arrays.asList(Variable.of("deviceId", "\u8bbe\u5907ID").withOption("productId", this.productId).withTermType(TermTypes.lookup((DataType)StringType.GLOBAL)).withColumn("deviceId"), Variable.of("deviceName", "\u8bbe\u5907\u540d\u79f0").withTermType(TermTypes.lookup((DataType)StringType.GLOBAL)).withColumn("deviceName"), Variable.of("productId", "\u4ea7\u54c1ID").withTermType(TermTypes.lookup((DataType)StringType.GLOBAL)).withColumn("productId"), Variable.of("productName", "\u4ea7\u54c1\u540d\u79f0").withTermType(TermTypes.lookup((DataType)StringType.GLOBAL)).withColumn("productName"));
    }

    public Flux<TermColumn> parseTermColumns(DeviceRegistry registry) {
        if (!StringUtils.hasText((String)this.productId)) {
            return Flux.empty();
        }
        return (Flux)this.getDeviceMetadata((ThingsRegistry)registry, this.productId).cast(DeviceMetadata.class).as(this::parseTermColumns);
    }

    public Flux<TermColumn> parseTermColumns(Mono<DeviceMetadata> metadataMono) {
        if (this.operation == null) {
            return Flux.empty();
        }
        return Mono.zip((Mono)LocaleUtils.currentReactive(), metadataMono, (locale, metadata) -> (List<TermColumn>)LocaleUtils.doWith((Object)metadata, (Locale)locale, (m, l) -> this.operation.parseTermColumns((ThingMetadata)m))).flatMapIterable(Function.identity());
    }

    void applyModel(RuleModel model, RuleNodeModel sceneNode) {
        switch (this.operation.getOperator()) {
            case reportProperty: 
            case reportEvent: 
            case online: 
            case offline: {
                return;
            }
        }
        RuleNodeModel deviceNode = new RuleNodeModel();
        deviceNode.setId("scene:device:message");
        deviceNode.setName("\u4e0b\u53d1\u8bbe\u5907\u6307\u4ee4");
        deviceNode.setExecutor("device-message-sender");
        DeviceMessageSendTaskExecutorProvider.DeviceMessageSendConfig config = new DeviceMessageSendTaskExecutorProvider.DeviceMessageSendConfig();
        config.setProductId(this.productId);
        config.setMessage(this.operation.toMessageTemplate());
        if (DeviceSelectorProviders.isFixed((DeviceSelectorSpec)this)) {
            config.setSelectorSpec((DeviceSelectorSpec)FastBeanCopier.copy((Object)this, (Object)new DeviceSelectorSpec(), (String[])new String[0]));
        } else {
            config.setSelectorSpec(DeviceSelectorProviders.composite((DeviceSelectorSpec[])new DeviceSelectorSpec[]{DeviceSelectorProviders.product((String)this.productId), (DeviceSelectorSpec)FastBeanCopier.copy((Object)this, (Object)new DeviceSelectorSpec(), (String[])new String[0])}));
        }
        config.validate();
        deviceNode.setConfiguration(config.toMap());
        model.getNodes().add(deviceNode);
        TimerSpec timer = this.operation.getTimer();
        Assert.notNull((Object)timer, (String)"timer can not be null");
        RuleNodeModel timerNode = new RuleNodeModel();
        timerNode.setId("scene:device:timer");
        timerNode.setName("\u5b9a\u65f6\u4e0b\u53d1\u6307\u4ee4");
        timerNode.setExecutor("timer");
        timerNode.setConfiguration((Map)FastBeanCopier.copy((Object)timer, new HashMap(), (String[])new String[0]));
        model.getNodes().add(timerNode);
        model.link(timerNode, deviceNode);
        model.link(deviceNode, sceneNode);
    }

    public void validate() {
        ValidatorUtils.tryValidate((Object)this, (Class[])new Class[0]);
        this.operation.validate();
    }

    public String getProductId() {
        return this.productId;
    }

    public DeviceOperation getOperation() {
        return this.operation;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public void setOperation(DeviceOperation operation) {
        this.operation = operation;
    }

    static class TermBuilder
    extends AbstractTermsFragmentBuilder<DeviceTrigger> {
        TermBuilder() {
        }

        public SqlFragments createTermFragments(DeviceTrigger parameter, List<Term> terms) {
            return super.createTermFragments(parameter, terms);
        }

        protected SqlFragments createTermFragments(DeviceTrigger trigger, Term term) {
            if (!StringUtils.hasText((String)term.getColumn())) {
                return EmptySqlFragments.INSTANCE;
            }
            String termType = StringUtils.hasText((String)term.getTermType()) ? term.getTermType() : "is";
            TermTypeSupport support = (TermTypeSupport)TermTypes.lookupSupport((String)termType).orElseThrow(() -> new UnsupportedOperationException("unsupported termType " + termType));
            Term copy = DeviceTrigger.refactorTermValue("t", term.clone());
            return support.createSql(copy.getColumn(), copy.getValue(), term);
        }
    }
}
