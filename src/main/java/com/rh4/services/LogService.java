package com.rh4.services;

import com.rh4.entities.Log;
import com.rh4.repositories.LogRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class LogService {

    @Autowired
    private LogRepo logRepo;

    public void saveLog(String internId, String action, String details) {
        Log log = new Log(internId, action, details);
        logRepo.save(log);
    }

    public List<Log> getAllLogs() {
        List<Log> logs = logRepo.findAll();
        System.out.println("Logs fetched: " + logs.size());
        return logs;
    }
}