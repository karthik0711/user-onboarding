package com.example.user_onboarding.temporal.service;

import com.example.user_onboarding.model.KycInfo;
import com.example.user_onboarding.model.User;
import com.example.user_onboarding.repository.KycRepository;
import com.example.user_onboarding.repository.UserRepository;
import com.example.user_onboarding.temporal.UserOnboardingActivities;
import io.temporal.failure.ApplicationFailure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserOnboardingActivitiesImpl implements UserOnboardingActivities {
    private final UserRepository userRepository;
    private final KycRepository kycRepository;


    @Autowired
    public UserOnboardingActivitiesImpl(UserRepository userRepository, KycRepository kycRepository) {
        this.userRepository = userRepository;
        this.kycRepository = kycRepository;
    }

    @Override
    public void saveUser(String name, String email) {
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
        System.out.println("Sending verification mail to : " + email);
    }

    @Override
    public boolean performKycCheck(String email,String documentId) {
        Optional<KycInfo> kycRecord = kycRepository.findByEmail(email);

        if (kycRecord.isEmpty()) {
            throw ApplicationFailure.newNonRetryableFailure("Email not found", "KYC_ERROR");
        }

        String expectedKycId = kycRecord.get().getKycId();
        if (!expectedKycId.equals(documentId)) {
            throw ApplicationFailure.newFailure("ID mismatch", "KYC_ERROR");
        }
        return true;
    }

    @Override
    public void activateUser(String email) {
        User user = userRepository.findByEmail(email);
        if(user != null){
            user.setKycCompleted(true);
            user.setActive(true);
            userRepository.save(user);
        }
    }

    @Override
    public void sendWelcomeEmail(String email) {
        System.out.println("Send Welcome email to : " + email);
    }
}

