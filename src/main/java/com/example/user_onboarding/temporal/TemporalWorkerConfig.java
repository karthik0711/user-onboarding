package com.example.user_onboarding.temporal;

import com.example.user_onboarding.temporal.service.UserOnboardingActivitiesImpl;
import com.example.user_onboarding.temporal.service.UserOnboardingWorkflowImpl;
import io.temporal.client.WorkflowClient;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TemporalWorkerConfig {

    @Bean
    public WorkflowServiceStubs workflowServiceStubs() {
        return WorkflowServiceStubs.newInstance();
    }

    @Bean
    public WorkflowClient workflowClient(WorkflowServiceStubs service) {
        return WorkflowClient.newInstance(service);
    }

    @Bean
    public WorkerFactory workerFactory(WorkflowClient client) {
        return WorkerFactory.newInstance(client);
    }

    @Bean
    public Worker worker(WorkerFactory factory, UserOnboardingActivitiesImpl activities) {
        Worker worker = factory.newWorker("UserOnboardingTaskQueue");
        worker.registerWorkflowImplementationTypes(UserOnboardingWorkflowImpl.class);
        worker.registerActivitiesImplementations(activities);
        factory.start();
        return worker;
    }
}

