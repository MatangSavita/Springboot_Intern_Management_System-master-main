package com.rh4.services;

import com.rh4.entities.Thesis;
import com.rh4.repositories.ThesisRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ThesisService {

    @Autowired
    private ThesisRepo thesisRepo;

    public List<Thesis> getAllTheses() {
        return thesisRepo.findAll();
    }

    public void saveThesis(Thesis thesis) {
        thesisRepo.save(thesis);
    }

    public Optional<Thesis> getThesisById(Long id) {
        return thesisRepo.findById(id);
    }

    public Thesis updateThesis(Thesis thesis) {
        return thesisRepo.save(thesis);
    }

    public void deleteThesis(String id) {
        thesisRepo.deleteById(Long.valueOf(id));
    }
}