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
package io.github.code.visual.model;

import java.util.List;

public class ScriptMetadata implements java.io.Serializable{

    private String scriptId;
    private String scriptName;
    private String scriptText;
    private ScriptType scriptType;
    private String scriptDesc;
    private List<ScriptMetadata> children;

    @Override
    public String toString() {
        return "ScriptMetadata{" +
                "scriptId='" + scriptId + '\'' +
                ", scriptName='" + scriptName + '\'' +
                ", scriptText='" + scriptText + '\'' +
                ", scriptType=" + scriptType +
                ", scriptDesc='" + scriptDesc + '\'' +
                ", children=" + children +
                '}';
    }

    public String getScriptDesc() {
        return scriptDesc;
    }

    public void setScriptDesc(String scriptDesc) {
        this.scriptDesc = scriptDesc;
    }

    public String getScriptId() {
        return scriptId;
    }

    public void setScriptId(String scriptId) {
        this.scriptId = scriptId;
    }

    public String getScriptText() {
        return scriptText;
    }

    public void setScriptText(String scriptText) {
        this.scriptText = scriptText;
    }

    public String getScriptName() {
        return scriptName;
    }

    public void setScriptName(String scriptName) {
        this.scriptName = scriptName;
    }

    public ScriptType getScriptType() {
        return scriptType;
    }

    public void setScriptType(ScriptType scriptType) {
        this.scriptType = scriptType;
    }

    public List<ScriptMetadata> getChildren() {
        return children;
    }

    public void setChildren(List<ScriptMetadata> children) {
        this.children = children;
    }
}
