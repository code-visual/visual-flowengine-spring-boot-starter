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
import java.util.List;

/**
 * @author Levi Li
 * @since 01/22/2024
 */
public class WorkflowMetadata implements java.io.Serializable {

    private Integer workflowId;
    private String workflowName;
    private List<WorkflowParameters> workflowParameters;
    private String workflowPurpose;
    private String remark;
    private Integer revision;
    private Date createdAt;
    private Date updatedAt;
    private ScriptMetadata scriptMetadata;

    @Override
    public String toString() {
        return "WorkflowMetadata{" +
                "workflowId=" + workflowId +
                ", workflowName='" + workflowName + '\'' +
                ", workflowParameters=" + workflowParameters +
                ", workflowPurpose='" + workflowPurpose + '\'' +
                ", remark='" + remark + '\'' +
                ", revision=" + revision +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", scriptMetadata=" + scriptMetadata +
                '}';
    }

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

    public List<WorkflowParameters> getWorkflowParameters() {
        return workflowParameters;
    }

    public void setWorkflowParameters(List<WorkflowParameters> workflowParameters) {
        this.workflowParameters = workflowParameters;
    }

    public String getWorkflowPurpose() {
        return workflowPurpose;
    }

    public void setWorkflowPurpose(String workflowPurpose) {
        this.workflowPurpose = workflowPurpose;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Integer getRevision() {
        return revision;
    }

    public void setRevision(Integer revision) {
        this.revision = revision;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public ScriptMetadata getScriptMetadata() {
        return scriptMetadata;
    }

    public void setScriptMetadata(ScriptMetadata scriptMetadata) {
        this.scriptMetadata = scriptMetadata;
    }

    // Backward compatibility aliases
    /** @deprecated Use {@link #getCreatedAt()} */
    @Deprecated
    public Date getCreateTime() {
        return createdAt;
    }

    /** @deprecated Use {@link #setCreatedAt(Date)} */
    @Deprecated
    public void setCreateTime(Date createTime) {
        this.createdAt = createTime;
    }

    /** @deprecated Use {@link #getUpdatedAt()} */
    @Deprecated
    public Date getUpdateTime() {
        return updatedAt;
    }

    /** @deprecated Use {@link #setUpdatedAt(Date)} */
    @Deprecated
    public void setUpdateTime(Date updateTime) {
        this.updatedAt = updateTime;
    }
}
