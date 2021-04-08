package dev.jianmu.application.service;

import dev.jianmu.parameter.aggregate.ParameterDefinition;
import dev.jianmu.parameter.repository.ParameterDefinitionRepository;
import dev.jianmu.workflow.aggregate.definition.Workflow;
import dev.jianmu.workflow.repository.WorkflowRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

/**
 * @program: workflow
 * @description: workflow门面类
 * @author: Ethan Liu
 * @create: 2021-01-22 13:35
 **/
@Service
public class WorkflowApplication {

    private final WorkflowRepository workflowRepository;
    private final ParameterDefinitionRepository parameterDefinitionRepository;

    @Inject
    public WorkflowApplication(WorkflowRepository workflowRepository, ParameterDefinitionRepository parameterDefinitionRepository) {
        this.workflowRepository = workflowRepository;
        this.parameterDefinitionRepository = parameterDefinitionRepository;
    }

    // 创建流程定义
    @Transactional
    public Workflow create(Workflow workflow) {

        Workflow newWorkflow = this.workflowRepository.findByRefAndVersion(workflow.getRef(), workflow.getVersion())
                .orElseGet(() ->
                        Workflow.Builder
                                .aWorkflow()
                                .name(workflow.getName())
                                .ref(workflow.getRef())
                                .description(workflow.getDescription())
                                .version(workflow.getVersion())
                                .nodes(workflow.getNodes())
                                .build()
                );

        // TODO 同步创建参数定义
        List<ParameterDefinition<?>> parameters = List.of();
        // 保存参数定义列表
        this.parameterDefinitionRepository.addList(parameters);

        return this.workflowRepository.add(newWorkflow);
    }

    // 删除流程定义版本与关联参数
    public void deleteByRefAndVersion(String ref, String version) {
        // TODO 同步删除相关参数定义
        this.parameterDefinitionRepository.deleteByBusinessId(ref);
        this.workflowRepository.deleteByRefAndVersion(ref, version);
    }

    public List<Workflow> findByRef(String ref) {
        return this.workflowRepository.findByRef(ref);
    }

    public Optional<Workflow> findByRefAndVersion(String ref, String version) {
        return this.workflowRepository.findByRefAndVersion(ref, version);
    }
}
