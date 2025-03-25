package com.example.user_onboarding.temporal;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface UserOnboardingActivities {

    @ActivityMethod
    void saveUser(String name, String email);

    @ActivityMethod
    void sendVerificationEmail(String email);

    @ActivityMethod
    void performKycCheck(String email);

    @ActivityMethod
    void activateUser(String email);

    @ActivityMethod
    void sendWelcomeEmail(String email);

}

