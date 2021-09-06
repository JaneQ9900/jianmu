package dev.jianmu.application.service;

import dev.jianmu.parameter.aggregate.Parameter;
import dev.jianmu.parameter.aggregate.SecretParameter;
import dev.jianmu.parameter.repository.ParameterRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @class: ParameterApplication
 * @description: 参数门面类
 * @author: Ethan Liu
 * @create: 2021-04-07 16:53
 **/
@Service
public class ParameterApplication {

    private final ParameterRepository parameterRepository;

    @Inject
    public ParameterApplication(
            ParameterRepository parameterRepository
    ) {
        this.parameterRepository = parameterRepository;
    }

    public List<Parameter> findParameters(Set<String> ids) {
        var parameters = this.parameterRepository.findByIds(ids);
        return parameters.stream().filter(parameter -> (!(parameter instanceof SecretParameter))).collect(Collectors.toList());
    }
}
