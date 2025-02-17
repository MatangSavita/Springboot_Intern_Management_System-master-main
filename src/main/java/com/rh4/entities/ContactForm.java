package com.rh4.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "contact_form")
public class ContactForm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    private String subject;
    private String message;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    // Constructors
    public ContactForm() {}

    public ContactForm(String name, String email, String subject, String message, LocalDateTime submittedAt) {
        this.name = name;
        this.email = email;
        this.subject = subject;
        this.message = message;
        this.submittedAt = submittedAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public void save(ContactForm contactForm) {

    }
}
