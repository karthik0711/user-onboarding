package com.example.user_onboarding.controller;

import com.example.user_onboarding.repository.UserRepository;
import com.example.user_onboarding.temporal.UserOnboardingClient;
import com.example.user_onboarding.temporal.UserOnboardingWorkflow;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowExecutionAlreadyStarted;
import io.temporal.client.WorkflowOptions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/onboarding")
public class UserOnboardingController {
    private static final Logger log = LogManager.getLogger(UserOnboardingController.class);
    private final UserRepository userRepository;
    private final UserOnboardingClient userOnboardingClient;

    @Autowired
    public UserOnboardingController(UserRepository userRepository, UserOnboardingClient userOnboardingClient) {
        this.userRepository = userRepository;
        this.userOnboardingClient = userOnboardingClient;
    }

    @PostMapping("/start")
    public ResponseEntity<String> startOnboarding(@RequestParam String name, @RequestParam String email) {
        if (userRepository.existsByEmail(email)) {
            return ResponseEntity.ok("User already exists. Redirecting to onboarding page...");
        }

        UserOnboardingWorkflow workflow = userOnboardingClient.createWorkflowStub(email);
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
            UserOnboardingWorkflow workflow = userOnboardingClient.getExistingWorkflowStub(email);
            workflow.verifyUser(email);
            return ResponseEntity.ok("User verified: " + email);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok("Error verifying user: " + e.getMessage());
        }
    }

    @PostMapping("/completeKyc")
    public ResponseEntity<String> completeKyc(@RequestParam String email) {
        try {
            UserOnboardingWorkflow workflow = userOnboardingClient.getExistingWorkflowStub(email);
            workflow.completeKyc();
            return ResponseEntity.ok("KYC completed for " + email);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok("Error completing KYC: " + e.getMessage());
        }
    }
}

