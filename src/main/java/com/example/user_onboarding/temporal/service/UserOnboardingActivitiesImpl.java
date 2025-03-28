package com.example.user_onboarding.temporal.service;

import com.example.user_onboarding.exception.InvalidEmailExistsException;
import com.example.user_onboarding.exception.UserAlreadyExistsException;
import com.example.user_onboarding.model.KycInfo;
import com.example.user_onboarding.model.User;
import com.example.user_onboarding.repository.KycRepository;
import com.example.user_onboarding.repository.UserRepository;
import com.example.user_onboarding.service.KafkaProducerService;
import com.example.user_onboarding.temporal.UserOnboardingActivities;
import io.temporal.failure.ApplicationFailure;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.K;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserOnboardingActivitiesImpl implements UserOnboardingActivities {
    private final UserRepository userRepository;
    private final KycRepository kycRepository;
    private final KafkaProducerService kafkaProducerService;


    @Autowired
    public UserOnboardingActivitiesImpl(UserRepository userRepository, KycRepository kycRepository, KafkaProducerService kafkaProducerService) {
        this.userRepository = userRepository;
        this.kycRepository = kycRepository;
        this.kafkaProducerService = kafkaProducerService;
    }

    @Override
    public void saveUser(String name, String email) {
    // Check if user already exists
        if (userRepository.existsByEmail(email)) {
            User existingUser = userRepository.findByEmail(email);
            existingUser.setVerified(true);
            userRepository.save(existingUser);
            return;
        }
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setVerified(true);
        user.setKycCompleted(false);
        user.setActive(false);
        userRepository.save(user);
    }

    @Override
    public void sendVerificationEmail(String email) {
        if(userRepository.existsByEmail(email)){
            throw ApplicationFailure.newNonRetryableFailure(
                    "User Already Exists, Redirect to Home Page",
                    UserAlreadyExistsException.class.getName()
            );
        }

        if(!isValidEmail(email)){
            throw ApplicationFailure.newNonRetryableFailure(
                    "Invalid email format.",
                    InvalidEmailExistsException.class.getName()
            );
        }
        System.out.println("Sending verification mail to : " + email);
    }

    @Override
    public boolean performKycCheck(String email,String documentId) {

        System.out.println("Performing KYC check for : " + email);

        // first check if the user exist
        if(!userRepository.existsByEmail(email)){
            System.out.println("User does not exist");
            throw ApplicationFailure.newNonRetryableFailure(
                    "User does not exist",
                    "UserNotFound"
            );
        }

        //check if the kyc record exist
        Optional<KycInfo> kycRecord = kycRepository.findByEmail(email);
        if(kycRecord.isEmpty()){
            System.out.println("KYC record not found");

            KycInfo kycInfo = new KycInfo(email, documentId);
            kycRepository.save(kycInfo);

            //send kyc success event
            kafkaProducerService.sendKycCompletedEvent(email,documentId,true);
            return true;
        }
        String expectedKycId = kycRecord.get().getKycId();
        if (!expectedKycId.equals(documentId)) {
            System.out.println("KYC ID mismatch for email:"+email);
            // Send KYC failure event
            kafkaProducerService.sendKycCompletedEvent(email, documentId, false);
            throw ApplicationFailure.newFailure(
                    "KYC ID mismatch",
                    "KycIdMismatch"
            );
        }
        // Send KYC success event
        kafkaProducerService.sendKycCompletedEvent(email, documentId, true);
        return true;
    }

    @Override
    public void activateUser(String email) {
        User user = userRepository.findByEmail(email);
        if(user != null){
            user.setKycCompleted(true);
            user.setActive(true);
            userRepository.save(user);

            // Send user activated event
            kafkaProducerService.sendUserActivatedEvent(email);
        }
    }

    @Override
    public void sendWelcomeEmail(String email) {
        System.out.println("Send Welcome email to : " + email);
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
}

