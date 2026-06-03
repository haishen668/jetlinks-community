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

import org.jetlinks.community.device.entity.DeviceInstanceEntity;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

class CustomerDeviceModuleContractTest {

    @Test
    void shouldExposeOldCustomerDeviceRootRoute() throws ClassNotFoundException {
        Class<?> controller = Class.forName("org.jetlinks.community.device.web.CustomerDeviceController");
        RequestMapping mapping = controller.getAnnotation(RequestMapping.class);

        Assert.assertNotNull(mapping);
        Assert.assertArrayEquals(new String[]{"/customer/device"}, mapping.value());
    }

    @Test
    void shouldExposeOldCustomerDeviceRoutes() throws ClassNotFoundException {
        Class<?> controller = Class.forName("org.jetlinks.community.device.web.CustomerDeviceController");

        Assert.assertTrue(hasPostMapping(controller, "/_query"));
        Assert.assertTrue(hasPostMapping(controller, "/queryPosition"));
        Assert.assertTrue(hasPostMapping(controller, "/_count"));
        Assert.assertTrue(hasPostMapping(controller, "/_update"));
        Assert.assertTrue(hasPatchMapping(controller, "/_add"));
        Assert.assertTrue(hasPatchMapping(controller, "/batchUpdate"));
        Assert.assertTrue(hasGetMapping(controller, "/getLocation"));
        Assert.assertTrue(hasGetMapping(controller, "/syncState"));
        Assert.assertTrue(hasGetMapping(controller, "/deployAll"));
        Assert.assertTrue(hasGetMapping(controller, "/{productId}/template.{format}"));
        Assert.assertTrue(hasGetMapping(controller, "/export.{format}"));
    }

    @Test
    void shouldExposeCustomerDeviceModel() throws ClassNotFoundException {
        Class<?> type = Class.forName("org.jetlinks.community.device.entity.CustomerDevice");

        Assert.assertTrue(DeviceInstanceEntity.class.isAssignableFrom(type));
    }

    @Test
    void shouldExposeBatchUpdateRequest() throws ClassNotFoundException {
        Class<?> type = Class.forName("org.jetlinks.community.device.web.request.BatchUpdateDeviceRequest");

        Assert.assertNotNull(type);
    }

    private boolean hasPostMapping(Class<?> type, String path) {
        return Arrays.stream(type.getDeclaredMethods())
            .map(method -> method.getAnnotation(PostMapping.class))
            .filter(Objects::nonNull)
            .anyMatch(mapping -> Arrays.asList(mapping.value()).contains(path));
    }

    private boolean hasPatchMapping(Class<?> type, String path) {
        return Arrays.stream(type.getDeclaredMethods())
            .map(method -> method.getAnnotation(PatchMapping.class))
            .filter(Objects::nonNull)
            .anyMatch(mapping -> Arrays.asList(mapping.value()).contains(path));
    }

    private boolean hasGetMapping(Class<?> type, String path) {
        return Arrays.stream(type.getDeclaredMethods())
            .map(method -> method.getAnnotation(GetMapping.class))
            .filter(Objects::nonNull)
            .anyMatch(mapping -> Arrays.asList(mapping.value()).contains(path));
    }
}
