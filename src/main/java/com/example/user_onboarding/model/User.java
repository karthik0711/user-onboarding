package com.example.user_onboarding.model;

import jakarta.persistence.*;
import org.checkerframework.common.aliasing.qual.Unique;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;

    @Unique
    private String email;

    private boolean isVerified; //indicate user verify its email or not
    private boolean isKycCompleted; //indicate that completed your kyc process
    private boolean isActive; //indicate is user activated or ready to use or not

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }

    public boolean isKycCompleted() {
        return isKycCompleted;
    }

    public void setKycCompleted(boolean kycCompleted) {
        isKycCompleted = kycCompleted;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
