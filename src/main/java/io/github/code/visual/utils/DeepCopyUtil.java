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
package io.github.code.visual.utils;

import groovy.lang.Closure;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class DeepCopyUtil {

    public static Object deepCopy(Object original) {
        if (original == null) {
            return null;
        } else if (ClassUtils.isPrimitiveOrWrapper(original.getClass())|| original instanceof String) {
            return original;
        } else if (original instanceof Map) {
            return deepCopyMap((Map<String, Object>) original);
        } else if (original instanceof List) {
            return deepCopyList((List<Object>) original);
        } else if (original instanceof Set) {
            return deepCopySet((Set<Object>) original);
        } else if (original instanceof Queue) {
            return deepCopyQueue((Queue<Object>) original);
        } else if (original.getClass().isArray()) {
            return deepCopyArray(original);
        } else if (original instanceof Closure) {
            return original.getClass().getSimpleName();
        } else if (original instanceof java.io.Serializable) {
            return original;
        }
        return original;
    }

    private static Map<String, Object> deepCopyMap(Map<String, Object> original) {
        Map<String, Object> copy = new HashMap<>();
        for (Map.Entry<String, Object> entry : original.entrySet()) {
            copy.put(entry.getKey(), deepCopy(entry.getValue()));
        }
        return copy;
    }
    private static List<Object> deepCopyList(List<Object> original) {
        List<Object> copy = new ArrayList<>();
        for (Object item : original) {
            copy.add(deepCopy(item));
        }
        return copy;
    }

    private static Set<Object> deepCopySet(Set<Object> original) {
        Set<Object> copy = new HashSet<>();
        for (Object item : original) {
            copy.add(deepCopy(item));
        }
        return copy;
    }

    private static Queue<Object> deepCopyQueue(Queue<Object> original) {
        Queue<Object> copy = new LinkedList<>();
        for (Object item : original) {
            copy.add(deepCopy(item));
        }
        return copy;
    }

    private static Object deepCopyArray(Object original) {
        int length = Array.getLength(original);
        Object copy = Array.newInstance(original.getClass().getComponentType(), length);
        for (int i = 0; i < length; i++) {
            Array.set(copy, i, deepCopy(Array.get(original, i)));
        }
        return copy;
    }
}
