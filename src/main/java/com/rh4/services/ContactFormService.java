package com.rh4.services;


import com.rh4.entities.ContactForm;
import com.rh4.repositories.ContactRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ContactFormService {

    @Autowired
    private ContactRepo contactRepo;

    public void saveContactForm(String name, String email, String subject, String message) {
        ContactForm contactForm = new ContactForm(name, email, subject, message, LocalDateTime.now());
        contactForm.save(contactForm);
    }
}
