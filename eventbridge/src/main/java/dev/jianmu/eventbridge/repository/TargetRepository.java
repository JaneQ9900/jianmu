package dev.jianmu.eventbridge.repository;

import dev.jianmu.eventbridge.aggregate.Target;

import java.util.Optional;

/**
 * @class: TargetRepository
 * @description: TargetRepository
 * @author: Ethan Liu
 * @create: 2021-08-11 16:06
 **/
public interface TargetRepository {
    Optional<Target> findById(String id);

    Optional<Target> findByDestinationId(String destinationId);

    void save(Target target);

    void deleteById(String id);
}