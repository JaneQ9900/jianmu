package dev.jianmu.infrastructure;

import dev.jianmu.parameter.service.ParameterDomainService;
import dev.jianmu.parameter.service.ReferenceDomainService;
import dev.jianmu.task.service.InstanceDomainService;
import dev.jianmu.task.service.TaskDefinitionDomainService;
import dev.jianmu.trigger.service.TriggerDomainService;
import dev.jianmu.workflow.service.WorkflowInstanceDomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @class: AppConfig
 * @description: 自定义Bean配置类
 * @author: Ethan Liu
 * @create: 2021-03-17 16:49
 **/
@Configuration
public class AppConfig implements AsyncConfigurer, WebMvcConfigurer {
    private static final Logger logger = LoggerFactory.getLogger(AppConfig.class);

    @Bean
    public WorkflowInstanceDomainService createWorkflowInstanceDomainService() {
        return new WorkflowInstanceDomainService();
    }

    @Bean
    public InstanceDomainService createInstanceDomainService() {
        return new InstanceDomainService();
    }

    @Bean
    public ParameterDomainService createParameterDomainService() {
        return new ParameterDomainService();
    }

    @Bean
    public ReferenceDomainService createReferenceDomainService() {
        return new ReferenceDomainService();
    }

    @Bean
    public TaskDefinitionDomainService createTaskDefinitionDomainService() {
        return new TaskDefinitionDomainService();
    }

    @Bean
    public TriggerDomainService createTriggerDomainService() {
        return new TriggerDomainService();
    }

    @Bean
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        //核心线程数10：线程池创建时候初始化的线程数
        executor.setCorePoolSize(10);
        //最大线程数20：线程池最大的线程数，只有在缓冲队列满了之后才会申请超过核心线程数的线程
        executor.setMaxPoolSize(20);
        //缓冲队列200：用来缓冲执行任务的队列
        executor.setQueueCapacity(200);
        //允许线程的空闲时间60秒：当超过了核心线程出之外的线程在空闲时间到达之后会被销毁
        executor.setKeepAliveSeconds(60);
        //线程池对拒绝任务的处理策略：这里采用了CallerRunsPolicy策略，
        // 当线程池没有处理能力的时候，该策略会直接在 execute 方法的调用线程中运行被拒绝的任务；
        // 如果执行程序已关闭，则会丢弃该任务
        executor.setThreadNamePrefix("asyncTask-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new SpringAsyncExceptionHandler();
    }

    static class SpringAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {
        @Override
        public void handleUncaughtException(Throwable throwable, Method method, Object... obj) {
            logger.error("Async方法执行异常", throwable);
        }
    }

    @Bean
    public ThreadPoolTaskExecutor mvcTaskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(10);
        taskExecutor.setMaxPoolSize(10);
        return taskExecutor;
    }

    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        configurer.setTaskExecutor(mvcTaskExecutor());
        configurer.setDefaultTimeout(60000L);
    }
}
