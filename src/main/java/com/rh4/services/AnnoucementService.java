package com.rh4.services;

import com.rh4.entities.Announcement;
import com.rh4.repositories.AnnouncementRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnnoucementService {

    @Autowired
    private AnnouncementRepo announcementRepo;

    public List<Announcement> getAllAnnouncements() {
        return announcementRepo.findAll();
    }
    public void createAnnouncement(Announcement announcement) {
        announcementRepo.save(announcement);
    }
    public List<Announcement> getInternAnnouncements() {
        return announcementRepo.findByType("announcements");
    }

    public Announcement getAnnouncementById(int announcementId) {
        return announcementRepo.findById(announcementId);
    }

    public void saveAnnouncement(Announcement announcement) {
        announcementRepo.save(announcement);
    }

    public void deleteAnnouncement(int id) {
        announcementRepo.deleteById(id);
    }
}
