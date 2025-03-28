package com.example.user_onboarding.controller;

import com.example.user_onboarding.repository.UserRepository;
import com.example.user_onboarding.temporal.UserOnboardingClient;
import com.example.user_onboarding.temporal.UserOnboardingWorkflow;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowExecutionAlreadyStarted;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/onboarding")
public class UserOnboardingController {
    private final UserOnboardingClient userOnboardingClient;

    @Autowired
    public UserOnboardingController( UserOnboardingClient userOnboardingClient) {
        this.userOnboardingClient = userOnboardingClient;
    }

    @PostMapping("/start")
    public ResponseEntity<String> startOnboarding(@RequestParam String name, @RequestParam String email) {
        UserOnboardingWorkflow workflow = userOnboardingClient.createWorkflowStub(email);
        try {
            WorkflowClient.start(workflow::startOnboarding, name, email);
        } catch (WorkflowExecutionAlreadyStarted e) {
            return ResponseEntity.ok("workflow already started for " + email);
        }
        return ResponseEntity.ok("workflow started successfully");
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verifyUser(@RequestParam String email) {
        try {
            UserOnboardingWorkflow workflow = userOnboardingClient.getExistingWorkflowStub(email);
            workflow.verifyUser(email);
            return ResponseEntity.ok("User verified: " + email);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok("Error verifying user: " + e.getMessage());
        }
    }

    @PostMapping("/completeKyc")
    public ResponseEntity<String> completeKyc(@RequestParam String email, @RequestParam String documentId) {
        try {
            UserOnboardingWorkflow workflow = userOnboardingClient.getExistingWorkflowStub(email);
            workflow.completeKyc(documentId);
            return ResponseEntity.ok("KYC initiated for " + email);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}

