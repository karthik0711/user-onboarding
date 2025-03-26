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
            ActivityOptions.newBuilder() //define configuration setting
                    .setStartToCloseTimeout(Duration.ofSeconds(10))// Set timeout here
                    .setRetryOptions(RetryOptions.newBuilder()
                            .setMaximumAttempts(3)
                            .build())
                    .build()
    );

    private boolean isVerified = false ;
    private boolean isKycCompleted = false;
    private String pendingVerificationEmail = null;
    private String documentId = null;
    private boolean isKycUpdated = false;

    @Override
    @WorkflowMethod
    public void startOnboarding(String name, String email) {
        try{
            this.pendingVerificationEmail = email;
            activities.sendVerificationEmail(email);
            boolean verified = Workflow.await(Duration.ofMinutes(1), () -> isVerified);
            if (!verified) {
                throw ApplicationFailure.newNonRetryableFailure("Email verification failed", "VerificationTimeout");
            }
            activities.saveUser(name, email);
            Workflow.await(() -> this.documentId != null);

            while (!isKycCompleted) {
                try {
                    boolean kycSuccess = activities.performKycCheck(email, documentId);
                    if (kycSuccess) {
                        System.out.println("KYC completed successfully");
                        isKycCompleted = true;
                        break;
                    }
                } catch (ActivityFailure e) {
                    System.out.println("KYC failed after 3 attempts. Waiting for new document...");
                }

                isKycUpdated = false;
                Workflow.await(() -> isKycUpdated);
            }

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
    public void verifyUser(String email) {
        if (pendingVerificationEmail != null && pendingVerificationEmail.equals(email)) {
            isVerified = true;
        }
    }

    @Override
    public void completeKyc(String documentId) {
        this.documentId = documentId;
        this.isKycUpdated = true;
    }
}

