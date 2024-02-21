package io.github.code.visual.model;

import java.util.Date;
import java.util.List;

/**
 * @author Levi Li
 * @since 01/22/2024
 */
public class WorkflowMetadata {

    private Integer workflowId;
    private String workflowName;
    private List<WorkflowParameters> workflowParameters;
    private String workflowPurpose;
    private String remark;
    private Date createTime;
    private Date updateTime;
    private ScriptMetadata scriptMetadata;

    public List<WorkflowParameters> getWorkflowParameters() {
        return workflowParameters;
    }

    public void setWorkflowParameters(List<WorkflowParameters> workflowParameters) {
        this.workflowParameters = workflowParameters;
    }

    public Integer getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(Integer workflowId) {
        this.workflowId = workflowId;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getWorkflowName() {
        return workflowName;
    }

    public void setWorkflowName(String workflowName) {
        this.workflowName = workflowName;
    }


    public String getWorkflowPurpose() {
        return workflowPurpose;
    }

    public void setWorkflowPurpose(String workflowPurpose) {
        this.workflowPurpose = workflowPurpose;
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
