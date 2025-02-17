package com.rh4.repositories;

import com.rh4.entities.Verification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VerificationRepo extends JpaRepository<Verification, String> {
    List<Verification> findByStatus(String status);

    long countByStatus(String status);
}