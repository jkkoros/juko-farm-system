package com.example.farmmanagement.service;

import com.example.farmmanagement.model.ActivityLog;
import com.example.farmmanagement.repository.ActivityLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ActivityLogService {

    @Autowired
    private ActivityLogRepository logRepo;

    public void log(String username, String action, String description) {
        ActivityLog log = new ActivityLog();
        log.setUsername(username);
        log.setAction(action);
        log.setDescription(description);
        logRepo.save(log);
    }

    public List<ActivityLog> getRecentLogs() {
        return logRepo.findTop50ByOrderByTimestampDesc();
    }
}
