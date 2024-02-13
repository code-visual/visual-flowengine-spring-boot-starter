package com.github.managetech.model;

import java.util.Date;
import java.util.Map;

@SuppressWarnings({"rawtypes"})
public class WorkflowTaskLog {
    private String scriptId;
    private String scriptName;

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
}
