package com.github.managetech.model;

import java.util.List;

public class ScriptMetadata {

    private String scriptId;
    private String scriptName;
    private String scriptContent;
    private ScriptType scriptType;
    private List<ScriptMetadata> children;

    public String getScriptId() {
        return scriptId;
    }

    public void setScriptId(String scriptId) {
        this.scriptId = scriptId;
    }

    public String getScriptContent() {
        return scriptContent;
    }

    public void setScriptContent(String scriptContent) {
        this.scriptContent = scriptContent;
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
