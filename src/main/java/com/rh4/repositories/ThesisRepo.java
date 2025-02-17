package com.rh4.repositories;

import com.rh4.entities.Thesis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ThesisRepo  extends JpaRepository<Thesis, Long> {

}