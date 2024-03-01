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
public class WorkflowTaskLog implements java.io.Serializable{
    private String scriptId;
    private String scriptName;
    private ScriptType scriptType;

    private String beforeRunBinding;

    private String afterRunBinding;

    private ScriptRunStatus scriptRunStatus;

    private Object scriptRunResult;

    private Date scriptRunTime;

    private String scriptRunError;

    @Override
    public String toString() {
        return "WorkflowTaskLog{" +
                "scriptId='" + scriptId + '\'' +
                ", scriptName='" + scriptName + '\'' +
                ", scriptType=" + scriptType +
                ", beforeRunBinding='" + beforeRunBinding + '\'' +
                ", afterRunBinding='" + afterRunBinding + '\'' +
                ", scriptRunStatus=" + scriptRunStatus +
                ", scriptRunResult=" + scriptRunResult +
                ", scriptRunTime=" + scriptRunTime +
                ", scriptRunError='" + scriptRunError + '\'' +
                '}';
    }

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

    public String getBeforeRunBinding() {
        return beforeRunBinding;
    }

    public void setBeforeRunBinding(String beforeRunBinding) {
        this.beforeRunBinding = beforeRunBinding;
    }

    public String getAfterRunBinding() {
        return afterRunBinding;
    }

    public void setAfterRunBinding(String afterRunBinding) {
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
