package com.rh4.services;

import com.rh4.entities.Verification;
import com.rh4.repositories.VerificationRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class VerificationService {

    @Autowired
    private VerificationRepo verificationRepo;

    public List<Verification> getPendingRequests() {
        return verificationRepo.findByStatus("PENDING");
    }

    public List<Verification> getApprovedVerifications() {
        return verificationRepo.findByStatus("APPROVED");
    }

    public List<Verification> getRejectedVerifications() {
        return verificationRepo.findByStatus("REJECTED");
    }

    public Optional<Verification> getVerificationById(Long id) {
        return verificationRepo.findById(String.valueOf(id));
    }

    public Verification approveVerification(Long id, String adminId, String remarks) {
        Optional<Verification> verification = verificationRepo.findById(String.valueOf(id));
        if (verification.isPresent()) {
            Verification v = verification.get();
            v.setStatus("APPROVED");
            v.setAdminId(adminId);
            v.setVerifiedDate(new Date());
            v.setRemarks(remarks != null ? remarks : "");
            return verificationRepo.save(v);
        }
        return null;
    }

    // Reject a verification request
    public Verification rejectVerification(Long id, String adminId, String remarks) {
        Optional<Verification> verification = verificationRepo.findById(String.valueOf(id));
        if (verification.isPresent()) {
            Verification v = verification.get();
            v.setStatus("REJECTED");
            v.setAdminId(adminId);
            v.setVerifiedDate(new Date());
            v.setRemarks(remarks != null ? remarks : "");
            return verificationRepo.save(v);
        }
        return null;
    }

    // Create a new verification request
    public void createVerificationRequest(Verification verification) {
        verification.setStatus("PENDING");
        verificationRepo.save(verification);
    }

    public long countPendingVerificationRequests() {
        return verificationRepo.countByStatus("Pending");
    }
}