package com.example.user_onboarding.temporal;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserOnboardingClient {
    private final WorkflowClient workflowClient;

    @Autowired
    public UserOnboardingClient(WorkflowClient workflowClient) {
        this.workflowClient = workflowClient;
    }

    public UserOnboardingWorkflow createWorkflowStub(String email) {
        WorkflowOptions options = WorkflowOptions.newBuilder()
                .setTaskQueue("UserOnboardingTaskQueue")
                .setWorkflowId(email)
                .build();

        UserOnboardingWorkflow workflow = workflowClient.newWorkflowStub(UserOnboardingWorkflow.class, options);
        return workflow;
    }

    public UserOnboardingWorkflow getExistingWorkflowStub(String email) {
        return workflowClient.newWorkflowStub(
                UserOnboardingWorkflow.class,
                email
        );
    }
}
