package com.example.user_onboarding.temporal.service;

import com.example.user_onboarding.exception.InvalidEmailExistsException;
import com.example.user_onboarding.exception.UserAlreadyExistsException;
import com.example.user_onboarding.temporal.UserOnboardingActivities;
import com.example.user_onboarding.temporal.UserOnboardingWorkflow;
import io.temporal.activity.Activity;
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
                    .setStartToCloseTimeout(Duration.ofSeconds(30))
                    .setRetryOptions(RetryOptions.newBuilder()
                            .setMaximumAttempts(3) // Retry up to 3 times
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
        this.pendingVerificationEmail = email;
        try {
            activities.sendVerificationEmail(email);
        } catch (ActivityFailure e) {
            if (e.getCause() instanceof ApplicationFailure) {
                ApplicationFailure failure = (ApplicationFailure) e.getCause();
                if (UserAlreadyExistsException.class.getName().equals(failure.getType())) {
                    System.out.println("User already exists: Redirecting to Home Page.");
                    return;
                } else if (InvalidEmailExistsException.class.getName().equals(failure.getType())) {
                    System.out.println("Invalid email: Stopping workflow.");
                    throw Workflow.wrap(failure);
                }
                throw Workflow.wrap(e);
            }
        }
        boolean verified = Workflow.await(Duration.ofMinutes(10), () -> isVerified);
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
                }
            } catch (ActivityFailure e) {
                if (e.getCause() instanceof ApplicationFailure failure) {
                    if ("KycNotFound".equals(failure.getType())) {
                        System.out.println("workflow has stopped");
                        throw Workflow.wrap(e);
                    }else if("KycIdMismatch".equals(failure.getType())){
                        System.out.println("KYC failed due to ID mismatch");
                        isKycCompleted = false;
                        Workflow.await(() -> isKycUpdated);
                        continue;
                    }
                    System.out.println("KYC check failed"+ e.getMessage());
                    // wait for updated document
                    isKycCompleted = false;
                    Workflow.await(() -> isKycUpdated);
                    continue;
                }

                // Break out of the loop if kyc is completed
                if(isKycCompleted){
                    break;
                }
            }

            activities.activateUser(email);
            activities.sendWelcomeEmail(email);
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

