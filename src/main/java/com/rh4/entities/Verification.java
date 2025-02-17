package com.rh4.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.util.Date;

@Entity
@Table(name = "verification")
public class Verification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-increment Long ID
    private Long id;

    @Column(name = "intern_id")
    private String internId;

    @Column(name = "companyName")
    private String companyName;

    @Column(name = "contact", unique = true)
    private String contact;

    @Column(name = "email_id")
    private String email;

    @Column(name = "status")
    private String status; // PENDING, APPROVED, REJECTED

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private Date requestDate;

    @Column(name = "verifiedDate")
    private Date verifiedDate;

    @Column(name = "admin_id")
    private String adminId;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "internName")
    private String internName;

    @Column(name = "internContact")
    private String internContact;

    public Verification() {
        super();
    }

    public Verification(Long id, String internId, String companyName, String contact, String email, String status, Date requestDate, Date verifiedDate, String adminId, String remarks, String internName, String internContact) {
        super();
        this.id = id;
        this.internId = internId;
        this.companyName = companyName;
        this.contact = contact;
        this.email = email;
        this.status = status;
        this.requestDate = requestDate;
        this.verifiedDate = verifiedDate;
        this.adminId = adminId;
        this.remarks = remarks;
        this.internName = internName;
        this.internContact = internContact;
    }

    public Long getId() { // Changed return type from String to Long
        return id;
    }

    public void setId(Long id) { // Changed parameter type from String to Long
        this.id = id;
    }

    public String getInternId() {
        return internId;
    }

    public void setInternId(String internId) {
        this.internId = internId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(Date requestDate) {
        this.requestDate = requestDate;
    }

    public Date getVerifiedDate() {
        return verifiedDate;
    }

    public void setVerifiedDate(Date verifiedDate) {
        this.verifiedDate = verifiedDate;
    }

    public String getAdminId() {
        return adminId;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getInternName() {
        return internName;
    }

    public void setInternName(String internName) {
        this.internName = internName;
    }

    public String getInternContact() {
        return internContact;
    }

    public void setInternContact(String internContact) {
        this.internContact = internContact;
    }
}