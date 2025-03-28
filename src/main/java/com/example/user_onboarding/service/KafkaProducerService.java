package com.example.user_onboarding.service;

import com.example.user_onboarding.config.KafkaConfig;
import com.example.user_onboarding.event.KycEvent;
import com.example.user_onboarding.event.UserEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class KafkaProducerService {
    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerService.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    public KafkaProducerService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendUserRegisteredEvent(String name, String email) {
        UserEvent event = new UserEvent(email, name, "USER_REGISTERED");
        send(KafkaConfig.TOPIC_USER_REGISTERED, email, event);
    }

    public void sendUserVerifiedEvent(String email) {
        UserEvent event = new UserEvent(email, null, "USER_VERIFIED");
        send(KafkaConfig.TOPIC_USER_VERIFIED, email, event);
    }

    public void sendKycInitiatedEvent(String email, String documentId) {
        KycEvent event = new KycEvent(email, documentId, "KYC_INITIATED", false);
        send(KafkaConfig.TOPIC_KYC_INITIATED, email, event);
    }

    public void sendKycCompletedEvent(String email, String documentId, boolean isSuccess) {
        KycEvent event = new KycEvent(email, documentId, "KYC_COMPLETED", isSuccess);
        send(KafkaConfig.TOPIC_KYC_COMPLETED, email, event);
    }

    public void sendUserActivatedEvent(String email) {
        UserEvent event = new UserEvent(email, null, "USER_ACTIVATED");
        send(KafkaConfig.TOPIC_USER_ACTIVATED, email, event);
    }

    private void send(String topic, String key, Object event) {
        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, key, event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                logger.info("Sent message=[{}] with offset=[{}] to topic=[{}]",
                        event, result.getRecordMetadata().offset(), topic);
            } else {
                logger.error("Unable to send message=[{}] to topic=[{}] due to : {}",
                        event, topic, ex.getMessage());
            }
        });
    }
}