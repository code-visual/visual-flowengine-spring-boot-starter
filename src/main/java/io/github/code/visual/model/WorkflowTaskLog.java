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

import java.util.Date;
import java.util.Map;

@SuppressWarnings({"rawtypes"})
public class WorkflowTaskLog {
    private String scriptId;
    private String scriptName;
    private ScriptType scriptType;

    private Map beforeRunBinding;

    private Map afterRunBinding;

    private ScriptRunStatus scriptRunStatus;

    private Object scriptRunResult;

    private Date scriptRunTime;

    private String scriptRunError;

    public String getScriptName() {
        return scriptName;
    }

    public void setScriptName(String scriptName) {
        this.scriptName = scriptName;
    }

    public String getScriptId() {
        return scriptId;
    }

    public void setScriptId(String scriptId) {
        this.scriptId = scriptId;
    }

    public Map getBeforeRunBinding() {
        return beforeRunBinding;
    }

    public void setBeforeRunBinding(Map beforeRunBinding) {
        this.beforeRunBinding = beforeRunBinding;
    }

    public Map getAfterRunBinding() {
        return afterRunBinding;
    }

    public void setAfterRunBinding(Map afterRunBinding) {
        this.afterRunBinding = afterRunBinding;
    }

    public ScriptRunStatus getScriptRunStatus() {
        return scriptRunStatus;
    }

    public void setScriptRunStatus(ScriptRunStatus scriptRunStatus) {
        this.scriptRunStatus = scriptRunStatus;
    }

    public Object getScriptRunResult() {
        return scriptRunResult;
    }

    public void setScriptRunResult(Object scriptRunResult) {
        this.scriptRunResult = scriptRunResult;
    }

    public Date getScriptRunTime() {
        return scriptRunTime;
    }

    public void setScriptRunTime(Date scriptRunTime) {
        this.scriptRunTime = scriptRunTime;
    }

    public String getScriptRunError() {
        return scriptRunError;
    }

    public void setScriptRunError(String scriptRunError) {
        this.scriptRunError = scriptRunError;
    }

    public ScriptType getScriptType() {
        return scriptType;
    }

    public void setScriptType(ScriptType scriptType) {
        this.scriptType = scriptType;
    }
}
