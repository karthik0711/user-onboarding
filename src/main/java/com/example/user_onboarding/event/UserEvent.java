package com.example.user_onboarding.event;

import java.io.Serializable;
import java.time.LocalDateTime;

public class UserEvent implements Serializable {
    private String email;
    private String name;
    private String eventType;
    private LocalDateTime timestamp;

    public UserEvent() {
    }

    public UserEvent(String email, String name, String eventType) {
        this.email = email;
        this.name = name;
        this.eventType = eventType;
        this.timestamp = LocalDateTime.now();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
}