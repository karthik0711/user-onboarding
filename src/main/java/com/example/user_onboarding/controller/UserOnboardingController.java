package com.example.user_onboarding.controller;

import com.example.user_onboarding.repository.UserRepository;
import com.example.user_onboarding.service.KafkaProducerService;
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
    private final KafkaProducerService kafkaProducerService;

    @Autowired
    public UserOnboardingController(UserOnboardingClient userOnboardingClient,KafkaProducerService kafkaProducerService) {
        this.userOnboardingClient = userOnboardingClient;
        this.kafkaProducerService = kafkaProducerService;
    }

    @PostMapping("/start")
    public ResponseEntity<String> startOnboarding(@RequestParam String name, @RequestParam String email) {
        UserOnboardingWorkflow workflow = userOnboardingClient.createWorkflowStub(email);
        try {
            WorkflowClient.start(workflow::startOnboarding, name, email);

            // publish user registered event to kafka
            kafkaProducerService.sendUserRegisteredEvent(name, email);

            return ResponseEntity.ok("Onboarding process started.");
        } catch (WorkflowExecutionAlreadyStarted e) {
            return ResponseEntity.ok("workflow already started for " + email);
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verifyUser(@RequestParam String email) {
        try {
//            UserOnboardingWorkflow workflow = userOnboardingClient.getExistingWorkflowStub(email);
//            workflow.verifyUser(email);

            // publish user verified event to kafka
            kafkaProducerService.sendUserVerifiedEvent(email);

            return ResponseEntity.ok("User verified: " + email);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok("Error verifying user: " + e.getMessage());
        }
    }

    @PostMapping("/completeKyc")
    public ResponseEntity<String> completeKyc(@RequestParam String email, @RequestParam String documentId) {
        try {
//            UserOnboardingWorkflow workflow = userOnboardingClient.getExistingWorkflowStub(email);
//            workflow.completeKyc(documentId);

            // publish kyc initiated event to kafka
            kafkaProducerService.sendKycInitiatedEvent(email, documentId);

            return ResponseEntity.ok("KYC initiated for " + email);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error completing KYC: " + e.getMessage());
        }
    }
}

