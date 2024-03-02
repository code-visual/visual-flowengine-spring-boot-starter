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
package io.github.code.visual.groovy;

import groovy.lang.Binding;
import io.github.code.visual.model.ScriptType;

import java.util.Map;

public class CustomBinding extends Binding {

    private ScriptType scriptType;


    public CustomBinding() {
        super();
    }

    public CustomBinding(Map variables) {
        super(variables);
    }


    public void setVariable(String name, Object value) {

        if (ScriptType.Condition == scriptType) {
            throw new RuntimeException("Error Condition script can not set variable");
        }
        super.setVariable(name, value);
    }


    public ScriptType getScriptType() {
        return scriptType;
    }

    public void setScriptType(ScriptType scriptType) {
        this.scriptType = scriptType;
    }
}
