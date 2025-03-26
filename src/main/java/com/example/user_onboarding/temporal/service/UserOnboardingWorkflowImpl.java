package com.example.user_onboarding.temporal.service;

import com.example.user_onboarding.temporal.UserOnboardingActivities;
import com.example.user_onboarding.temporal.UserOnboardingWorkflow;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.failure.ApplicationFailure;
import io.temporal.workflow.Workflow;
import io.temporal.workflow.WorkflowMethod;

import java.time.Duration;

public class UserOnboardingWorkflowImpl implements UserOnboardingWorkflow {
    private final UserOnboardingActivities activities = Workflow.newActivityStub(
            UserOnboardingActivities.class,
            ActivityOptions.newBuilder()
                    .setStartToCloseTimeout(Duration.ofSeconds(30)) // Activity must complete in 30s
                    .setRetryOptions(RetryOptions.newBuilder()
                            .setMaximumAttempts(3) // Retry up to 3 times
                            .setInitialInterval(Duration.ofSeconds(10)) // First retry after 10s
                            .setBackoffCoefficient(2.0) // Exponential backoff
                            .setMaximumInterval(Duration.ofMinutes(5)) // Max wait time between retries
                            .build())
                    .build()
    );

    private boolean isVerified = false ;
    private boolean isKycCompleted = false;
    private String pendingVerificationEmail = null;

    @Override
    @WorkflowMethod
    public void startOnboarding(String name, String email) {
        this.pendingVerificationEmail = email;
        activities.sendVerificationEmail(email);
        boolean verified = Workflow.await(Duration.ofMinutes(1), () -> isVerified);
        if (!verified) {
            throw ApplicationFailure.newNonRetryableFailure("Email verification failed", "VerificationTimeout");
        }
        activities.saveUser(name, email);
        while (!isKycCompleted) {
            boolean kycSuccess = activities.performKycCheck(email);
            if (kycSuccess) {
                isKycCompleted = true;
                break;
            }
            Workflow.sleep(Duration.ofMinutes(10));
        }

        activities.activateUser(email);
        activities.sendWelcomeEmail(email);
    }

    @Override
    public void verifyUser(String email) {
        if (pendingVerificationEmail != null && pendingVerificationEmail.equals(email)) {
            isVerified = true;  // Only verify if it matches stored email
        }
    }

    @Override
    public void completeKyc() {
        System.out.println("Received KYC completion signal");
        this.isKycCompleted = true;
    }
}

