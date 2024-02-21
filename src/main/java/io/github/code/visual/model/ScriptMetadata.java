package io.github.code.visual.model;

import java.util.List;

public class ScriptMetadata {

    private String scriptId;
    private String scriptName;
    private String scriptText;
    private ScriptType scriptType;
    private String scriptDesc;
    private List<ScriptMetadata> children;


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
