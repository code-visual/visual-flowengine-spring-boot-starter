package com.github.managetech.engine;

import com.github.managetech.model.ScriptMetadata;
import com.github.managetech.model.ScriptType;
import com.github.managetech.model.WorkflowMetadata;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author Levi Li
 * @since 01/24/2024
 */
@Repository
public class TempWorkflowMetadataRepositoryImpl implements WorkflowMetadataRepository{

    private final Map<String, WorkflowMetadata> workflowMetadataMap = new ConcurrentHashMap<>();
    @Override
    public WorkflowMetadata create(WorkflowMetadata workflowMetadata) {
        ScriptMetadata metadata = new ScriptMetadata();
        metadata.setScriptId(1);
        //这里应该可以看到workflow 有哪些传入参数的描述
        metadata.setScriptContent("");
        metadata.setScriptName("Start");
        metadata.setScriptType(ScriptType.Start);
        metadata.setChildren(new ArrayList<>());

        workflowMetadata.setScriptMetadata(metadata);
        workflowMetadata.setCreateTime(new Date());
        return workflowMetadataMap.put(workflowMetadata.getWorkflowName(), workflowMetadata);
    }

    @Override
    public Object deleteByWorkflowName(String workflowName) {
        return workflowMetadataMap.remove(workflowName);
    }

    @Override
    public WorkflowMetadata findByWorkflowName(String workflowName) {
        return workflowMetadataMap.get(workflowName);
    }

    @Override
    public List<String> getMenuWorkflowList() {
        return workflowMetadataMap.values().stream().map(WorkflowMetadata::getWorkflowName).collect(Collectors.toList());
    }
}
