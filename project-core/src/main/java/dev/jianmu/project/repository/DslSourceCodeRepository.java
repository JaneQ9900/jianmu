package dev.jianmu.project.repository;


import dev.jianmu.project.aggregate.DslSourceCode;

import java.util.Optional;

/**
 * @class: DslSourceCodeRepository
 * @description: DslSourceCodeRepository
 * @author: Ethan Liu
 * @create: 2021-04-23 22:52
 **/
public interface DslSourceCodeRepository {
    void add(DslSourceCode dslSourceCode);

    void deleteByProjectId(String projectId);

    Optional<DslSourceCode> findByRefAndVersion(String ref, String version);
}