package com.example.user_onboarding.event;

import java.io.Serializable;
import java.time.LocalDateTime;

public class KycEvent implements Serializable {
    private String email;
    private String documentId;
    private String eventType;
    private LocalDateTime timestamp;
    private boolean isSuccess;

    public KycEvent() {
    }

    public KycEvent(String email, String documentId, String eventType, boolean isSuccess) {
        this.email = email;
        this.documentId = documentId;
        this.eventType = eventType;
        this.isSuccess = isSuccess;
        this.timestamp = LocalDateTime.now();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }
}