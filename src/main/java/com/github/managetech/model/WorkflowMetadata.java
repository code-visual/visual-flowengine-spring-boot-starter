package com.github.managetech.model;

import java.util.Date;

/**
 * @author Levi Li
 * @since 01/22/2024
 */
public class WorkflowMetadata {

    private Integer workflowId;
    private String workflowName;
    private String workflowDescription;
    private String workflowStatus;
    private String workflowAuthor;
    private Date createTime;
    private Date updateTime;
    private ScriptMetadata scriptMetadata;

    public Integer getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(Integer workflowId) {
        this.workflowId = workflowId;
    }

    public String getWorkflowName() {
        return workflowName;
    }

    public void setWorkflowName(String workflowName) {
        this.workflowName = workflowName;
    }

    public String getWorkflowDescription() {
        return workflowDescription;
    }

    public void setWorkflowDescription(String workflowDescription) {
        this.workflowDescription = workflowDescription;
    }

    public String getWorkflowStatus() {
        return workflowStatus;
    }

    public void setWorkflowStatus(String workflowStatus) {
        this.workflowStatus = workflowStatus;
    }

    public String getWorkflowAuthor() {
        return workflowAuthor;
    }

    public void setWorkflowAuthor(String workflowAuthor) {
        this.workflowAuthor = workflowAuthor;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public ScriptMetadata getScriptMetadata() {
        return scriptMetadata;
    }

    public void setScriptMetadata(ScriptMetadata scriptMetadata) {
        this.scriptMetadata = scriptMetadata;
    }
}
