package com.example.user_onboarding.controller;

import com.example.user_onboarding.temporal.UserOnboardingWorkflow;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowExecutionAlreadyStarted;
import io.temporal.client.WorkflowOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/onboarding")
public class UserOnboardingController {
    private final WorkflowClient workflowClient;

    @Autowired
    public UserOnboardingController(WorkflowClient workflowClient) {
        this.workflowClient = workflowClient;
    }

    @PostMapping("/start")
    public ResponseEntity<String> startOnboarding(@RequestParam String name, @RequestParam String email) {
        UserOnboardingWorkflow workflow = workflowClient.newWorkflowStub(
                UserOnboardingWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setTaskQueue("UserOnboardingTaskQueue")
                        .setWorkflowId(email)
                        .build()
        );
        try {
            WorkflowClient.start(workflow::startOnboarding, name, email);
        } catch (WorkflowExecutionAlreadyStarted e) {
            return ResponseEntity.ok("Onboarding already started for " + email);
        }
        return ResponseEntity.ok("Onboarding process started.");
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verifyUser(@RequestParam String email) {
        try {
            UserOnboardingWorkflow workflow = workflowClient.newWorkflowStub(
                    UserOnboardingWorkflow.class,
                    email
            );
            workflow.verifyUser();
            return ResponseEntity.ok("User verified: " + email);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok("Error verifying user: " + e.getMessage());
        }
    }

    @PostMapping("/completeKyc")
    public ResponseEntity<String> completeKyc(@RequestParam String email) {
        try {
            UserOnboardingWorkflow workflow = workflowClient.newWorkflowStub(
                    UserOnboardingWorkflow.class,
                    email
            );
            workflow.completeKyc();
            return ResponseEntity.ok("KYC completed for " + email);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok("Error completing KYC: " + e.getMessage());
        }
    }
}

