package com.example.user_onboarding.repository;

import com.example.user_onboarding.model.KycInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KycRepository extends JpaRepository<KycInfo, String> {
    Optional<KycInfo> findByEmail(String email);
}

