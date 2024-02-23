/*
 * Copyright (c) 2023-2024, levi li (levi.lideng@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.code.visual.config;

import org.kohsuke.groovy.sandbox.GroovyInterceptor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class GroovyNotSupportInterceptor extends GroovyInterceptor {


    private final List<String> defaultMethodBlacklist = Arrays.asList("getClass", "class", "wait", "notify",
            "notifyAll", "invokeMethod", "finalize", "execute");


    static final Map<Class<?>, String> STATIC_METHOD_BLACK_LIST = createBlackList();

    private static Map<Class<?>, String> createBlackList() {
        Map<Class<?>, String> map = new HashMap<>();
        map.put(System.class, "exit,gc");
        map.put(Class.class, "forName");
        map.put(Runtime.class, "");
        return map;
    }

    /**
     * 静态方法拦截
     */
    @Override
    public Object onStaticCall(GroovyInterceptor.Invoker invoker, Class receiver,
                               String method, Object... args) throws Throwable {

        String staticMethodBlackList = STATIC_METHOD_BLACK_LIST.get(receiver);
        if (staticMethodBlackList != null) {

            if (staticMethodBlackList.isEmpty()) {
                throw new SecurityException("Not support method: " + method);
            }
            String[] split = staticMethodBlackList.split(",");
            boolean allMatch = Arrays.stream(split).anyMatch(method::equalsIgnoreCase);
            if (allMatch) {
                throw new SecurityException("Not support method: " + method);
            }
        }
        return super.onStaticCall(invoker, receiver, method, args);
    }

    /**
     * 普通方法拦截
     */
    @Override
    public Object onMethodCall(GroovyInterceptor.Invoker invoker, Object receiver, String method, Object... args)
            throws Throwable {
        if (defaultMethodBlacklist.contains(method)) {
            // 方法列表黑名单
            throw new SecurityException("Not support method: " + method);
        }
        return super.onMethodCall(invoker, receiver, method, args);
    }

}