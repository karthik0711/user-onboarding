package com.example.user_onboarding.temporal.service;

import com.example.user_onboarding.exception.InvalidEmailExistsException;
import com.example.user_onboarding.exception.UserAlreadyExistsException;
import com.example.user_onboarding.temporal.UserOnboardingActivities;
import com.example.user_onboarding.temporal.UserOnboardingWorkflow;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.failure.ActivityFailure;
import io.temporal.failure.ApplicationFailure;
import io.temporal.workflow.Workflow;
import io.temporal.workflow.WorkflowMethod;

import java.time.Duration;

public class UserOnboardingWorkflowImpl implements UserOnboardingWorkflow {
    private final UserOnboardingActivities activities = Workflow.newActivityStub(
            UserOnboardingActivities.class,
            ActivityOptions.newBuilder()
                    .setStartToCloseTimeout(Duration.ofMinutes(10))
                    .setRetryOptions(RetryOptions.newBuilder()
                            .setMaximumAttempts(1)
                            .setDoNotRetry(UserAlreadyExistsException.class.getName())
                            .build())
                    .build()
    );

    private boolean isVerified;
    private boolean isKycCompleted;

    @Override
    @WorkflowMethod
    public void startOnboarding(String name, String email) {
        try{
            activities.saveUser(name, email);
            activities.sendVerificationEmail(email);
            Workflow.await(() -> isVerified);
            activities.performKycCheck(email);
            Workflow.await(() -> isKycCompleted);
            activities.activateUser(email);
            activities.sendWelcomeEmail(email);
        }
        catch (ActivityFailure e) {
            if (e.getCause() instanceof ApplicationFailure) {
                ApplicationFailure failure = (ApplicationFailure) e.getCause();
                if (UserAlreadyExistsException.class.getName().equals(failure.getType())) {
                    System.out.println("User already exists: Redirecting to Home Page.");
                    return;
                }
                else if (InvalidEmailExistsException.class.getName().equals(failure.getType())) {
                    System.out.println("Invalid email: Stopping workflow.");
                    throw Workflow.wrap(failure);
                }
            }
        }
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

