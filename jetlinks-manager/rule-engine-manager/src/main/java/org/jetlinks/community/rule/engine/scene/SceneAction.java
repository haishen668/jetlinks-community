/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  io.swagger.v3.oas.annotations.media.Schema
 *  javax.validation.constraints.NotBlank
 *  javax.validation.constraints.NotNull
 *  org.apache.commons.collections4.MapUtils
 *  org.hswebframework.ezorm.core.param.Term
 *  org.hswebframework.web.bean.FastBeanCopier
 *  org.hswebframework.web.i18n.LocaleUtils
 *  org.jetlinks.community.reactorql.term.TermTypes
 *  org.jetlinks.community.relation.utils.VariableSource
 *  org.jetlinks.community.rule.engine.executor.DelayTaskExecutorProvider$DelayTaskExecutorConfig
 *  org.jetlinks.community.rule.engine.executor.DelayTaskExecutorProvider$PauseType
 *  org.jetlinks.community.rule.engine.executor.DeviceMessageSendTaskExecutorProvider$DeviceMessageSendConfig
 *  org.jetlinks.community.rule.engine.executor.device.DeviceSelectorProviders
 *  org.jetlinks.community.rule.engine.executor.device.DeviceSelectorSpec
 *  org.jetlinks.community.utils.ConverterUtils
 *  org.jetlinks.core.device.DeviceRegistry
 *  org.jetlinks.core.message.DeviceMessage
 *  org.jetlinks.core.message.MessageType
 *  org.jetlinks.core.message.function.FunctionInvokeMessage
 *  org.jetlinks.core.message.function.FunctionParameter
 *  org.jetlinks.core.message.property.ReadPropertyMessage
 *  org.jetlinks.core.message.property.WritePropertyMessage
 *  org.jetlinks.core.metadata.DataType
 *  org.jetlinks.core.metadata.DeviceMetadata
 *  org.jetlinks.core.metadata.FunctionMetadata
 *  org.jetlinks.core.metadata.PropertyMetadata
 *  org.jetlinks.core.metadata.types.BooleanType
 *  org.jetlinks.core.metadata.types.DateTimeType
 *  org.jetlinks.core.metadata.types.IntType
 *  org.jetlinks.core.metadata.types.ObjectType
 *  org.jetlinks.core.metadata.types.StringType
 *  org.jetlinks.rule.engine.api.model.RuleNodeModel
 *  reactor.core.publisher.Flux
 *  reactor.core.publisher.Mono
 */
package org.jetlinks.community.rule.engine.scene;

import com.google.common.collect.Lists;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import org.apache.commons.collections4.MapUtils;
import org.hswebframework.ezorm.core.param.Term;
import org.hswebframework.web.bean.FastBeanCopier;
import org.hswebframework.web.i18n.LocaleUtils;
import org.jetlinks.community.reactorql.term.TermTypes;
import org.jetlinks.community.relation.utils.VariableSource;
import org.jetlinks.community.rule.engine.alarm.AlarmTaskExecutorProvider;
import org.jetlinks.community.rule.engine.executor.DelayTaskExecutorProvider;
import org.jetlinks.community.rule.engine.executor.DeviceMessageSendTaskExecutorProvider;
import org.jetlinks.community.rule.engine.executor.device.DeviceSelectorProviders;
import org.jetlinks.community.rule.engine.executor.device.DeviceSelectorSpec;
import org.jetlinks.community.rule.engine.scene.SceneRule;
import org.jetlinks.community.rule.engine.scene.Variable;
import org.jetlinks.community.utils.ConverterUtils;
import org.jetlinks.core.device.DeviceRegistry;
import org.jetlinks.core.message.DeviceMessage;
import org.jetlinks.core.message.MessageType;
import org.jetlinks.core.message.function.FunctionInvokeMessage;
import org.jetlinks.core.message.function.FunctionParameter;
import org.jetlinks.core.message.property.ReadPropertyMessage;
import org.jetlinks.core.message.property.WritePropertyMessage;
import org.jetlinks.core.metadata.DataType;
import org.jetlinks.core.metadata.DeviceMetadata;
import org.jetlinks.core.metadata.FunctionMetadata;
import org.jetlinks.core.metadata.PropertyMetadata;
import org.jetlinks.core.metadata.types.BooleanType;
import org.jetlinks.core.metadata.types.DateTimeType;
import org.jetlinks.core.metadata.types.IntType;
import org.jetlinks.core.metadata.types.ObjectType;
import org.jetlinks.core.metadata.types.StringType;
import org.jetlinks.core.things.ThingsRegistry;
import org.jetlinks.rule.engine.api.model.RuleNodeModel;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class SceneAction
implements Serializable {
    @Schema(description="\u6267\u884c\u5668\u7c7b\u578b")
    @NotNull
    private Executor executor;
    @Schema(description="\u6267\u884c\u5668\u7c7b\u578b\u4e3a[notify]\u65f6\u4e0d\u80fd\u4e3a\u7a7a")
    private Notify notify;
    @Schema(description="\u6267\u884c\u5668\u7c7b\u578b\u4e3a[delay]\u65f6\u4e0d\u80fd\u4e3a\u7a7a")
    private Delay delay;
    @Schema(description="\u6267\u884c\u5668\u7c7b\u578b\u4e3a[device]\u65f6\u4e0d\u80fd\u4e3a\u7a7a")
    private Device device;
    @Schema(description="\u6267\u884c\u5668\u7c7b\u578b\u4e3a[alarm]\u65f6\u4e0d\u80fd\u4e3a\u7a7a")
    private Alarm alarm;
    @Schema(description="\u8f93\u51fa\u8fc7\u6ee4\u6761\u4ef6,\u4e32\u884c\u6267\u884c\u52a8\u4f5c\u65f6,\u6ee1\u8db3\u6761\u4ef6\u624d\u4f1a\u8fdb\u5165\u4e0b\u4e00\u4e2a\u8282\u70b9")
    private List<Term> terms;
    @Schema(description="\u62d3\u5c55\u4fe1\u606f")
    private Map<String, Object> options;

    public static List<String> parseColumnFromOptions(Map<String, Object> options) {
        Object columns;
        if (MapUtils.isEmpty(options) || (columns = options.get("columns")) == null) {
            return Collections.emptyList();
        }
        return ConverterUtils.convertToList((Object)columns, String::valueOf);
    }

    private List<String> parseActionTerms() {
        if (this.executor == Executor.device && this.device != null) {
            return this.device.parseColumns();
        }
        if (this.executor == Executor.notify && this.notify != null) {
            return this.notify.parseColumns();
        }
        return Collections.emptyList();
    }

    public List<String> createContextColumns() {
        ArrayList<String> termList = new ArrayList<String>();
        termList.addAll(SceneAction.parseColumnFromOptions(this.options));
        termList.addAll(this.parseActionTerms());
        return termList;
    }

    public Flux<Variable> createVariables(DeviceRegistry registry, Integer branchIndex, Integer group, int index) {
        if (this.executor == Executor.device && this.device != null) {
            return (Flux)this.device.getDeviceMetadata((ThingsRegistry)registry, this.device.productId).map(metadata -> this.createVariable(branchIndex, group, index, this.device.createVariables((DeviceMetadata)metadata))).flux().as(LocaleUtils::transform);
        }
        if (this.executor == Executor.alarm && this.alarm != null) {
            return (Flux)Mono.fromSupplier(() -> this.createVariable(branchIndex, group, index, this.alarm.createVariables())).flux().as(LocaleUtils::transform);
        }
        return Flux.empty();
    }

    public Flux<Variable> createVariables(Integer group, int index) {
        return createVariables(null, null, group, index);
    }

    public Flux<Variable> createVariables(Integer branchIndex, Integer group, int index) {
        return createVariables(null, branchIndex, group, index);
    }

    private Variable createVariable(Integer branchIndex, Integer group, int actionIndex, List<Variable> children) {
        String varId = "action_" + actionIndex;
        if (branchIndex != null) {
            varId = SceneRule.createBranchActionId(branchIndex, group, actionIndex);
        }
        String name = LocaleUtils.resolveMessage((String)"message.action_var_index", (String)String.format("\u52a8\u4f5c[%s]", actionIndex), (Object[])new Object[]{actionIndex});
        String fullName = LocaleUtils.resolveMessage((String)"message.action_var_index_full", (String)String.format("\u52a8\u4f5c[%s]\u8f93\u51fa", actionIndex), (Object[])new Object[]{actionIndex});
        String description = LocaleUtils.resolveMessage((String)"message.action_var_output_description", (String)String.format("\u52a8\u4f5c[%s]\u6267\u884c\u7684\u8f93\u51fa\u7ed3\u679c", actionIndex), (Object[])new Object[]{actionIndex});
        Variable variable = Variable.of(varId, name);
        variable.setFullName(fullName);
        variable.setDescription(description);
        variable.setChildren(children);
        return variable;
    }

    private String getActionDescription() {
        if (this.executor == null) {
            return null;
        }
        return LocaleUtils.resolveMessage((String)("message.scene_action_" + this.executor.name()), (String)"", (Object[])new Object[0]);
    }

    public static SceneAction notify(String notifyType, String notifierId, String templateId, Consumer<Notify> consumer) {
        SceneAction action = new SceneAction();
        action.executor = Executor.notify;
        action.notify = new Notify();
        action.notify.notifierId = notifierId;
        action.notify.notifyType = notifyType;
        action.notify.templateId = templateId;
        consumer.accept(action.notify);
        return action;
    }

    public void applyNode(RuleNodeModel node) {
        switch (this.executor) {
            case delay: {
                DelayTaskExecutorProvider.DelayTaskExecutorConfig config = new DelayTaskExecutorProvider.DelayTaskExecutorConfig();
                config.setPauseType(DelayTaskExecutorProvider.PauseType.delay);
                config.setTimeout(this.delay.time);
                config.setTimeoutUnits(((Delay)this.delay).unit.chronoUnit);
                node.setExecutor("delay");
                node.setConfiguration((Map)FastBeanCopier.copy((Object)config, new HashMap(), (String[])new String[0]));
                return;
            }
            case notify: {
                node.setExecutor("notifier");
                HashMap<String, Object> config = new HashMap<String, Object>();
                config.put("notifyType", this.notify.getNotifyType());
                config.put("notifierId", this.notify.notifierId);
                config.put("templateId", this.notify.templateId);
                config.put("variables", this.notify.variables);
                node.setConfiguration(config);
                return;
            }
            case alarm: {
                node.setExecutor("alarm");
                node.setConfiguration((Map)FastBeanCopier.copy((Object)this.alarm, new HashMap(), (String[])new String[0]));
                return;
            }
            case device: {
                DeviceMessageSendTaskExecutorProvider.DeviceMessageSendConfig config = new DeviceMessageSendTaskExecutorProvider.DeviceMessageSendConfig();
                config.setMessage(this.device.message);
                if (DeviceSelectorProviders.isFixed((DeviceSelectorSpec)this.device)) {
                    config.setSelectorSpec((DeviceSelectorSpec)FastBeanCopier.copy((Object)((Object)this.device), (Object)new DeviceSelectorSpec(), (String[])new String[0]));
                } else {
                    config.setSelectorSpec(DeviceSelectorProviders.composite((DeviceSelectorSpec[])new DeviceSelectorSpec[]{DeviceSelectorProviders.product((String)this.device.productId), (DeviceSelectorSpec)FastBeanCopier.copy((Object)((Object)this.device), (Object)new DeviceSelectorSpec(), (String[])new String[0])}));
                }
                config.setFrom("fixed");
                config.setStateOperator("direct");
                config.setProductId(this.device.productId);
                node.setExecutor("device-message-sender");
                node.setConfiguration(config.toMap());
                return;
            }
        }
        throw new UnsupportedOperationException("unsupported executor:" + (Object)((Object)this.executor));
    }

    public static Variable toVariable(String prefix, PropertyMetadata metadata, String i18nKey, String msgPattern) {
        return SceneAction.toVariable(prefix.concat(".").concat(metadata.getId()), metadata.getName(), metadata.getValueType(), i18nKey, msgPattern, null);
    }

    public static Variable toVariable(String id, String metadataName, DataType dataType, String i18nKey, String msgPattern, String parentName) {
        String fullName = parentName == null ? metadataName : parentName + "." + metadataName;
        Variable variable = Variable.of(id, LocaleUtils.resolveMessage((String)i18nKey, (String)String.format(msgPattern, fullName), (Object[])new Object[]{fullName}));
        variable.setType(dataType.getType());
        variable.setTermTypes(TermTypes.lookup((DataType)dataType));
        variable.setColumn(id);
        if (dataType instanceof ObjectType) {
            ArrayList<Variable> children = new ArrayList<Variable>();
            for (PropertyMetadata property : ((ObjectType)dataType).getProperties()) {
                children.add(SceneAction.toVariable(id + "." + property.getId(), property.getName(), property.getValueType(), i18nKey, msgPattern, fullName));
            }
            variable.setChildren(children);
        }
        return variable;
    }

    public Executor getExecutor() {
        return this.executor;
    }

    public Notify getNotify() {
        return this.notify;
    }

    public Delay getDelay() {
        return this.delay;
    }

    public Device getDevice() {
        return this.device;
    }

    public Alarm getAlarm() {
        return this.alarm;
    }

    public List<Term> getTerms() {
        return this.terms;
    }

    public Map<String, Object> getOptions() {
        return this.options;
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    public void setNotify(Notify notify) {
        this.notify = notify;
    }

    public void setDelay(Delay delay) {
        this.delay = delay;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public void setAlarm(Alarm alarm) {
        this.alarm = alarm;
    }

    public void setTerms(List<Term> terms) {
        this.terms = terms;
    }

    public void setOptions(Map<String, Object> options) {
        this.options = options;
    }

    public static enum Executor {
        notify,
        delay,
        device,
        alarm;

    }

    public static enum DelayUnit {
        seconds(ChronoUnit.SECONDS),
        minutes(ChronoUnit.MINUTES),
        hours(ChronoUnit.HOURS);

        final ChronoUnit chronoUnit;

        public ChronoUnit getChronoUnit() {
            return this.chronoUnit;
        }

        private DelayUnit(ChronoUnit chronoUnit) {
            this.chronoUnit = chronoUnit;
        }
    }

    public static class Alarm
    extends AlarmTaskExecutorProvider.Config {
        public List<Variable> createVariables() {
            ArrayList<Variable> variables = new ArrayList<Variable>();
            variables.add(Variable.of("alarmName", LocaleUtils.resolveMessage((String)"message.alarm_config_name", (String)"\u544a\u8b66\u914d\u7f6e\u540d\u79f0", (Object[])new Object[0])).withType((DataType)StringType.GLOBAL));
            variables.add(Variable.of("level", LocaleUtils.resolveMessage((String)"message.alarm_level", (String)"\u544a\u8b66\u7ea7\u522b", (Object[])new Object[0])).withType((DataType)IntType.GLOBAL));
            variables.add(Variable.of("firstAlarm", LocaleUtils.resolveMessage((String)"message.first_alarm", (String)"\u662f\u5426\u9996\u6b21\u544a\u8b66", (Object[])new Object[0])).withDescription(LocaleUtils.resolveMessage((String)"message.first_alarm_description", (String)"\u662f\u5426\u4e3a\u9996\u6b21\u544a\u8b66\u6216\u8005\u89e3\u9664\u540e\u7684\u7b2c\u4e00\u6b21\u544a\u8b66", (Object[])new Object[0])).withType((DataType)BooleanType.GLOBAL));
            variables.add(Variable.of("alarmTime", LocaleUtils.resolveMessage((String)"message.alarm_time", (String)"\u9996\u6b21\u544a\u8b66\u65f6\u95f4", (Object[])new Object[0])).withDescription(LocaleUtils.resolveMessage((String)"message.alarm_time_description", (String)"\u9996\u6b21\u544a\u8b66\u6216\u8005\u89e3\u9664\u544a\u8b66\u540e\u7684\u7b2c\u4e00\u6b21\u544a\u8b66\u65f6\u95f4", (Object[])new Object[0])).withType((DataType)DateTimeType.GLOBAL));
            variables.add(Variable.of("lastAlarmTime", LocaleUtils.resolveMessage((String)"message.last_alarm_time", (String)"\u4e0a\u4e00\u6b21\u544a\u8b66\u65f6\u95f4", (Object[])new Object[0])).withDescription(LocaleUtils.resolveMessage((String)"message.last_alarm_time_description", (String)"\u4e0a\u4e00\u6b21\u89e6\u53d1\u544a\u8b66\u7684\u65f6\u95f4", (Object[])new Object[0])).withType((DataType)DateTimeType.GLOBAL));
            return variables;
        }
    }

    public static class Notify
    implements Serializable {
        @Schema(description="\u901a\u77e5\u7c7b\u578b")
        @NotBlank(message="error.scene_rule_actions_notify_type_cannot_be_empty")
        private @NotBlank(message="error.scene_rule_actions_notify_type_cannot_be_empty") String notifyType;
        @Schema(description="\u901a\u77e5\u914d\u7f6eID")
        @NotBlank(message="error.scene_rule_actions_notify_id_cannot_be_empty")
        private @NotBlank(message="error.scene_rule_actions_notify_id_cannot_be_empty") String notifierId;
        @Schema(description="\u901a\u77e5\u6a21\u7248ID")
        @NotBlank(message="error.scene_rule_actions_notify_template_cannot_be_blank")
        private @NotBlank(message="error.scene_rule_actions_notify_template_cannot_be_blank") String templateId;
        @Schema(description="\u901a\u77e5\u53d8\u91cf")
        @NotBlank(message="error.scene_rule_actions_notify_variables_cannot_be_blank")
        private @NotBlank(message="error.scene_rule_actions_notify_variables_cannot_be_blank") Map<String, Object> variables;

        public List<String> parseColumns() {
            if (MapUtils.isEmpty(this.variables)) {
                return Collections.emptyList();
            }
            return this.variables.values().stream().flatMap(val -> SceneAction.parseColumnFromOptions(VariableSource.of((Object)val).getOptions()).stream()).collect(Collectors.toList());
        }

        public String getNotifyType() {
            return this.notifyType;
        }

        public String getNotifierId() {
            return this.notifierId;
        }

        public String getTemplateId() {
            return this.templateId;
        }

        public Map<String, Object> getVariables() {
            return this.variables;
        }

        public void setNotifyType(String notifyType) {
            this.notifyType = notifyType;
        }

        public void setNotifierId(String notifierId) {
            this.notifierId = notifierId;
        }

        public void setTemplateId(String templateId) {
            this.templateId = templateId;
        }

        public void setVariables(Map<String, Object> variables) {
            this.variables = variables;
        }
    }

    public static class Delay
    implements Serializable {
        @Schema(description="\u5ef6\u8fdf\u65f6\u95f4")
        private int time;
        @Schema(description="\u65f6\u95f4\u5355\u4f4d")
        private DelayUnit unit;

        public int getTime() {
            return this.time;
        }

        public DelayUnit getUnit() {
            return this.unit;
        }

        public void setTime(int time) {
            this.time = time;
        }

        public void setUnit(DelayUnit unit) {
            this.unit = unit;
        }

        public Delay(int time, DelayUnit unit) {
            this.time = time;
            this.unit = unit;
        }

        public Delay() {
        }
    }

    public static class Device
    extends DeviceSelectorSpec {
        @Schema(description="\u4ea7\u54c1ID")
        private String productId;
        @Schema(description="\u8bbe\u5907\u6307\u4ee4")
        private Map<String, Object> message;

        public List<Variable> createVariables(DeviceMetadata metadata) {
            String functionId;
            FunctionMetadata metadata_;
            DeviceMessage message = MessageType.convertMessage(this.message).filter(DeviceMessage.class::isInstance).map(DeviceMessage.class::cast).orElse(null);
            if (message == null) {
                return Collections.emptyList();
            }
            ArrayList<Variable> variables = new ArrayList<Variable>();
            variables.add(Variable.of("success", LocaleUtils.resolveMessage((String)"message.action_execute_success", (String)"\u6267\u884c\u662f\u5426\u6210\u529f", (Object[])new Object[0])).withType("boolean").withTermType(TermTypes.lookup((DataType)BooleanType.GLOBAL)));
            variables.add(Variable.of("deviceId", LocaleUtils.resolveMessage((String)"message.device_id", (String)"\u8bbe\u5907ID", (Object[])new Object[0])).withType("boolean").withOption("productId", this.productId).withTermType(TermTypes.lookup((DataType)StringType.GLOBAL)));
            if (message instanceof ReadPropertyMessage) {
                List<String> properties = ((ReadPropertyMessage)message).getProperties();
                for (String property : properties) {
                    PropertyMetadata metadata_2 = metadata.getPropertyOrNull(property);
                    if (null == metadata_2) continue;
                    variables.add(SceneAction.toVariable("properties", metadata_2, "message.action_var_read_property", "\u8bfb\u53d6\u5c5e\u6027[%s]\u8fd4\u56de\u503c"));
                }
            } else if (message instanceof WritePropertyMessage) {
                Map<String, Object> properties = ((WritePropertyMessage)message).getProperties();
                for (String property : properties.keySet()) {
                    PropertyMetadata metadata_3 = metadata.getPropertyOrNull(property);
                    if (null == metadata_3) continue;
                    variables.add(SceneAction.toVariable("properties", metadata_3, "message.action_var_write_property", "\u8bbe\u7f6e\u5c5e\u6027[%s]\u8fd4\u56de\u503c"));
                }
            } else if (message instanceof FunctionInvokeMessage && null != (metadata_ = metadata.getFunctionOrNull(functionId = ((FunctionInvokeMessage)message).getFunctionId())) && metadata_.getOutput() != null) {
                variables.add(SceneAction.toVariable("output", metadata_.getName(), metadata_.getOutput(), "message.action_var_function", "\u529f\u80fd\u8c03\u7528[%s]\u8fd4\u56de\u503c", null));
            }
            return variables;
        }

        public List<String> parseColumns() {
            List<Object> readyToParse;
            if (MapUtils.isEmpty(this.message)) {
                return Collections.emptyList();
            }
            DeviceMessage msg = MessageType.convertMessage(this.message).filter(DeviceMessage.class::isInstance).map(DeviceMessage.class::cast).orElse(null);
            if (msg instanceof WritePropertyMessage) {
                readyToParse = new ArrayList<>(((WritePropertyMessage)msg).getProperties().values());
            } else if (msg instanceof FunctionInvokeMessage) {
                readyToParse = new ArrayList<>(Lists.transform((List<FunctionParameter>)((FunctionInvokeMessage)msg).getInputs(), FunctionParameter::getValue));
            } else {
                return Collections.emptyList();
            }
            return readyToParse.stream().flatMap(val -> SceneAction.parseColumnFromOptions(VariableSource.of((Object)val).getOptions()).stream()).collect(Collectors.toList());
        }

        public String getProductId() {
            return this.productId;
        }

        public Map<String, Object> getMessage() {
            return this.message;
        }

        public void setProductId(String productId) {
            this.productId = productId;
        }

        public void setMessage(Map<String, Object> message) {
            this.message = message;
        }
    }
}
