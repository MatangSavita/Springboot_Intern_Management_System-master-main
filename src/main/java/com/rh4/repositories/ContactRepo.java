package com.rh4.repositories;

import com.rh4.entities.ContactForm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContactRepo extends JpaRepository<ContactForm, Long> {

}
