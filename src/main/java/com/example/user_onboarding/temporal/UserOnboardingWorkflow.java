package com.example.user_onboarding.temporal;

import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface UserOnboardingWorkflow {

    @WorkflowMethod
    void startOnboarding(String name, String email);

    @SignalMethod
    void verifyUser();

    @SignalMethod
    void completeKyc();
}
