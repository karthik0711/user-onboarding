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
    boolean performKycCheck(String email, String documentId);

    @ActivityMethod
    void activateUser(String email);

    @ActivityMethod
    void sendWelcomeEmail(String email);

}

