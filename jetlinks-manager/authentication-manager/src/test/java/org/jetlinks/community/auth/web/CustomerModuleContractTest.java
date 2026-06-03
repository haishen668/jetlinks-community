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

import org.hswebframework.web.system.authorization.api.entity.UserEntity;
import org.jetlinks.community.auth.entity.UserDetailEntity;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.persistence.Column;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

class CustomerModuleContractTest {

    @Test
    void shouldExposeOldCustomerRootRoute() throws ClassNotFoundException {
        Class<?> controller = Class.forName("org.jetlinks.community.auth.web.CustomerController");
        RequestMapping mapping = controller.getAnnotation(RequestMapping.class);

        Assert.assertNotNull(mapping);
        Assert.assertArrayEquals(new String[]{"/customer"}, mapping.value());
    }

    @Test
    void shouldExposeOldCustomerQueryRoutes() throws ClassNotFoundException {
        Class<?> controller = Class.forName("org.jetlinks.community.auth.web.CustomerController");

        Assert.assertTrue(hasPostMapping(controller, "/_query"));
        Assert.assertTrue(hasPostMapping(controller, "/no-paging/_query"));
        Assert.assertTrue(hasGetMapping(controller, ""));
    }

    @Test
    void shouldPersistCustomerTreePath() throws NoSuchFieldException {
        Field field = UserDetailEntity.class.getDeclaredField("treePath");
        Column column = field.getAnnotation(Column.class);

        Assert.assertNotNull(column);
        Assert.assertEquals("tree_path", column.name());
        Assert.assertEquals(2048, column.length());
    }

    @Test
    void shouldExposeCustomerDetailModel() throws ClassNotFoundException {
        Class<?> detailType = Class.forName("org.jetlinks.community.auth.entity.CustomerDetail");

        Assert.assertTrue(UserEntity.class.isAssignableFrom(detailType));
    }

    private boolean hasPostMapping(Class<?> type, String path) {
        return Arrays.stream(type.getDeclaredMethods())
            .map(method -> method.getAnnotation(PostMapping.class))
            .filter(Objects::nonNull)
            .anyMatch(mapping -> Arrays.asList(mapping.value()).contains(path));
    }

    private boolean hasGetMapping(Class<?> type, String path) {
        return Arrays.stream(type.getDeclaredMethods())
            .map(method -> method.getAnnotation(GetMapping.class))
            .filter(Objects::nonNull)
            .anyMatch(mapping -> {
                if (path.isEmpty() && mapping.value().length == 0) {
                    return true;
                }
                return Arrays.asList(mapping.value()).contains(path);
            });
    }
}
