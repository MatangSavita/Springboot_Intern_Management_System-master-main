package com.rh4.repositories;

import com.rh4.entities.Log;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LogRepo extends JpaRepository<Log, Long> {
    List<Log> findByInternId(String internId);
}