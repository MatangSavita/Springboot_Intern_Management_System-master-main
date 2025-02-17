package com.rh4.repositories;

import com.rh4.entities.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnnouncementRepo extends JpaRepository<Announcement, Integer> {
    List<Announcement> findByType(String type);

    Announcement findById(int announcementId);
}
