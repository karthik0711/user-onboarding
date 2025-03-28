package com.example.user_onboarding.service;

import com.example.user_onboarding.config.KafkaConfig;
import com.example.user_onboarding.event.KycEvent;
import com.example.user_onboarding.event.UserEvent;
import com.example.user_onboarding.model.User;
import com.example.user_onboarding.repository.UserRepository;
import com.example.user_onboarding.temporal.UserOnboardingClient;
import com.example.user_onboarding.temporal.UserOnboardingWorkflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {
    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerService.class);

    private final UserRepository userRepository;
    private final UserOnboardingClient userOnboardingClient;
    private final EmailService emailService;

    @Autowired
    public KafkaConsumerService(UserRepository userRepository,
                                UserOnboardingClient userOnboardingClient,
                                EmailService emailService) {
        this.userRepository = userRepository;
        this.userOnboardingClient = userOnboardingClient;
        this.emailService = emailService;
    }

    @KafkaListener(topics = KafkaConfig.TOPIC_USER_REGISTERED, groupId = "${spring.kafka.consumer.group-id}")
    public void consumeUserRegistered(UserEvent event) {
        logger.info("Received user registered event for email: {}", event.getEmail());

        // Send verification email via the emailService
        emailService.sendVerificationEmail(event.getEmail());
    }

    @KafkaListener(topics = KafkaConfig.TOPIC_USER_VERIFIED, groupId = "${spring.kafka.consumer.group-id}")
    public void consumeUserVerified(UserEvent event) {
        logger.info("Received user verified event for email: {}", event.getEmail());

        try {
            // Signal the Temporal workflow that the user is verified
            UserOnboardingWorkflow workflow = userOnboardingClient.getExistingWorkflowStub(event.getEmail());
            workflow.verifyUser(event.getEmail());
        } catch (Exception e) {
            logger.error("Error signaling workflow for verification: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = KafkaConfig.TOPIC_KYC_INITIATED, groupId = "${spring.kafka.consumer.group-id}")
    public void consumeKycInitiated(KycEvent event) {
        logger.info("Received KYC initiated event for email: {}, documentId: {}",
                event.getEmail(), event.getDocumentId());

        try {
            // Signal the Temporal workflow with the KYC document ID
            UserOnboardingWorkflow workflow = userOnboardingClient.getExistingWorkflowStub(event.getEmail());
            workflow.completeKyc(event.getDocumentId());
        } catch (Exception e) {
            logger.error("Error signaling workflow for KYC: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = KafkaConfig.TOPIC_KYC_COMPLETED, groupId = "${spring.kafka.consumer.group-id}")
    public void consumeKycCompleted(KycEvent event) {
        logger.info("Received KYC completed event for email: {}, success: {}",
                event.getEmail(), event.isSuccess());

        if (event.isSuccess()) {
            // Update user record to mark KYC as completed
            User user = userRepository.findByEmail(event.getEmail());
            if (user != null) {
                user.setKycCompleted(true);
                userRepository.save(user);
                logger.info("User KYC status updated for email: {}", event.getEmail());
            }
        }
    }

    @KafkaListener(topics = KafkaConfig.TOPIC_USER_ACTIVATED, groupId = "${spring.kafka.consumer.group-id}")
    public void consumeUserActivated(UserEvent event) {
        logger.info("Received user activated event for email: {}", event.getEmail());

        // Send welcome email
        emailService.sendWelcomeEmail(event.getEmail());

        logger.info("User onboarding completed for email: {}", event.getEmail());
    }
}