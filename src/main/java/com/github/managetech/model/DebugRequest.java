package com.github.managetech.model;

import java.util.Map;

public class DebugRequest {


    private ScriptMetadata scriptMetadata;
    private Map<String, Object> inputValues;

    public ScriptMetadata getScriptMetadata() {
        return scriptMetadata;
    }

    public void setScriptMetadata(ScriptMetadata scriptMetadata) {
        this.scriptMetadata = scriptMetadata;
    }

    public Map<String, Object> getInputValues() {
        return inputValues;
    }

    public void setInputValues(Map<String, Object> inputValues) {
        this.inputValues = inputValues;
    }
}
