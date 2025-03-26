package com.example.user_onboarding.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "kyc_info")
public class KycInfo {

    @Id
    private String email;

    @Column(nullable = false)
    private String kycId;

    public KycInfo() {}

    public KycInfo(String email, String kycId) {
        this.email = email;
        this.kycId = kycId;
    }

    public String getEmail() {
        return email;
    }

    public String getKycId() {
        return kycId;
    }
}

