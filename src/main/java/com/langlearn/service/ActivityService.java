package com.langlearn.service;

import com.langlearn.model.ActivityLog;
import com.langlearn.repository.ActivityRepository;
import org.springframework.stereotype.Service;

@Service
public class ActivityService {
    private final ActivityRepository repo;

    public ActivityService(ActivityRepository repo) { this.repo = repo; }

    public void log(String message) { repo.save(new ActivityLog(message)); }
}
