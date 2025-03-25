package com.example.user_onboarding.temporal.service;

import com.example.user_onboarding.temporal.UserOnboardingActivities;
import com.example.user_onboarding.temporal.UserOnboardingWorkflow;
import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.Workflow;
import io.temporal.workflow.WorkflowMethod;

import java.time.Duration;

public class UserOnboardingWorkflowImpl implements UserOnboardingWorkflow {
    private final UserOnboardingActivities activities = Workflow.newActivityStub(
            UserOnboardingActivities.class,
            ActivityOptions.newBuilder() //define configuration setting
                    .setStartToCloseTimeout(Duration.ofSeconds(10)) // Set timeout here
                    .build()
    );

    private boolean isVerified;
    private boolean isKycCompleted;

    @Override
    @WorkflowMethod
    public void startOnboarding(String name, String email) {
        activities.saveUser(name, email);
        activities.sendVerificationEmail(email);
        Workflow.await(() -> isVerified);
        activities.performKycCheck(email);
        Workflow.await(() -> isKycCompleted);
        activities.activateUser(email);
        activities.sendWelcomeEmail(email);
    }

    @Override
    public void verifyUser() {
        System.out.println("Received verification signal");
        this.isVerified = true;
    }

    @Override
    public void completeKyc() {
        System.out.println("Received KYC completion signal");
        this.isKycCompleted = true;
    }
}

