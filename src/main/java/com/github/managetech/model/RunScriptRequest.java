package com.github.managetech.model;

import java.util.Map;

public class RunScriptRequest {
    private Map<String, Object> inputValues;
    private String code;

    public Map<String, Object> getInputValues() {
        return inputValues;
    }

    public void setInputValues(Map<String, Object> inputValues) {
        this.inputValues = inputValues;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}

