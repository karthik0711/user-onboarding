package com.example.user_onboarding.temporal.service;

import com.example.user_onboarding.model.User;
import com.example.user_onboarding.repository.UserRepository;
import com.example.user_onboarding.temporal.UserOnboardingActivities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserOnboardingActivitiesImpl implements UserOnboardingActivities {
    private final UserRepository userRepository;

    @Autowired
    public UserOnboardingActivitiesImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
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
    public void performKycCheck(String email) {
        User user = userRepository.findByEmail(email);
        if(user != null){
            user.setVerified(true);
            userRepository.save(user);
        }
        System.out.println("Performing KVC check for : " + email);
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

