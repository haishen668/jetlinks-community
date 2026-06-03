package org.jetlinks.community.standalone.web;

import org.apache.commons.codec.digest.DigestUtils;
import org.jetlinks.community.device.entity.ProtocolSupportEntity;
import org.jetlinks.community.device.response.DeviceDeployResult;
import org.jetlinks.community.device.service.LocalDeviceInstanceService;
import org.jetlinks.community.device.service.LocalProtocolSupportService;
import org.jetlinks.core.Value;
import org.jetlinks.core.Values;
import org.jetlinks.core.ProtocolSupport;
import org.jetlinks.core.device.DeviceOperator;
import org.jetlinks.core.device.DeviceRegistry;
import org.jetlinks.supports.protocol.management.ProtocolSupportDefinition;
import org.jetlinks.supports.protocol.management.ProtocolSupportLoader;
import org.jetlinks.supports.protocol.management.ProtocolSupportManager;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;
import java.util.Map;

@Profile("wj")
@RestController
@RequestMapping("/_debug/mqtt-auth")
public class LocalMqttAuthDebugController {

    private final DeviceRegistry registry;

    private final LocalDeviceInstanceService deviceService;

    private final LocalProtocolSupportService protocolService;

    private final ProtocolSupportLoader protocolSupportLoader;

    private final ProtocolSupportManager protocolSupportManager;

    public LocalMqttAuthDebugController(DeviceRegistry registry,
                                        LocalDeviceInstanceService deviceService,
                                        LocalProtocolSupportService protocolService,
                                        ProtocolSupportLoader protocolSupportLoader,
                                        ProtocolSupportManager protocolSupportManager) {
        this.registry = registry;
        this.deviceService = deviceService;
        this.protocolService = protocolService;
        this.protocolSupportLoader = protocolSupportLoader;
        this.protocolSupportManager = protocolSupportManager;
    }

    @GetMapping
    public Mono<Map<String, Object>> inspect(@RequestParam String deviceId,
                                             @RequestParam(required = false) String username,
                                             @RequestParam(required = false) String password) {
        return registry
            .getDevice(deviceId)
            .flatMap(device -> device
                .getConfigs("secureId", "secureKey", "productId", "protocol")
                .map(values -> toResult(device, values, username, password)))
            .defaultIfEmpty(notFound(deviceId));
    }

    @PostMapping("/deploy-device")
    public Mono<Map<String, Object>> deployDevice(@RequestParam String id) {
        return deviceService
            .deploy(id)
            .map(result -> toDeployResult(id, result));
    }

    @PostMapping("/reload-protocol")
    public Mono<Map<String, Object>> reloadProtocol(@RequestParam String id) {
        return protocolService
            .findById(id)
            .flatMap(this::reloadProtocol)
            .defaultIfEmpty(protocolNotFound(id));
    }

    private Mono<Map<String, Object>> reloadProtocol(ProtocolSupportEntity entity) {
        ProtocolSupportDefinition definition = entity.toDeployDefinition();
        return protocolSupportLoader
            .load(definition)
            .doOnNext(ProtocolSupport::dispose)
            .then(protocolSupportManager.save(definition))
            .map(saved -> {
                Map<String, Object> result = new LinkedHashMap<>();
                result.put("id", entity.getId());
                result.put("saved", saved);
                result.put("state", entity.getState());
                result.put("provider", entity.getType());
                return result;
            });
    }

    private Map<String, Object> toDeployResult(String id, DeviceDeployResult deployResult) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", id);
        result.put("total", deployResult.getTotal());
        result.put("success", deployResult.isSuccess());
        result.put("message", deployResult.getMessage());
        result.put("operation", deployResult.getOperation());
        return result;
    }

    private Map<String, Object> toResult(DeviceOperator device,
                                         Values values,
                                         String username,
                                         String password) {
        String secureId = values.getValue("secureId")
                                .map(Value::asString)
                                .orElse(null);
        String secureKey = values.getValue("secureKey")
                                 .map(Value::asString)
                                 .orElse(null);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("deviceId", device.getDeviceId());
        result.put("hasSecureId", secureId != null);
        result.put("secureIdPreview", preview(secureId));
        result.put("secureKeyLength", secureKey == null ? -1 : secureKey.length());
        result.put("hasProductId", values.getValue("productId").isPresent());
        result.put("hasProtocol", values.getValue("protocol").isPresent());

        if (username != null && password != null && secureKey != null) {
            String[] parts = username.split("[|]");
            String usernameSecureId = parts.length > 0 ? parts[0] : "";
            String expected = DigestUtils.md5Hex(username + "|" + secureKey);
            result.put("usernameParts", parts.length);
            result.put("secureIdMatches", usernameSecureId.equals(secureId));
            result.put("passwordMatches", expected.equals(password));
            result.put("passwordLength", password.length());
            result.put("expectedLength", expected.length());
        }
        return result;
    }

    private Map<String, Object> notFound(String deviceId) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("deviceId", deviceId);
        result.put("found", false);
        return result;
    }

    private Map<String, Object> protocolNotFound(String id) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", id);
        result.put("found", false);
        return result;
    }

    private String preview(String value) {
        if (value == null) {
            return null;
        }
        if (value.length() <= 2) {
            return "**";
        }
        return value.substring(0, 2) + "***" + value.substring(value.length() - 1);
    }
}
