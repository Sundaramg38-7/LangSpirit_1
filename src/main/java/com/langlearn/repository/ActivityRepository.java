package com.langlearn.repository;

import com.langlearn.model.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ActivityRepository extends JpaRepository<ActivityLog, Long> {
    List<ActivityLog> findTop30ByOrderByTimeDesc();
}
