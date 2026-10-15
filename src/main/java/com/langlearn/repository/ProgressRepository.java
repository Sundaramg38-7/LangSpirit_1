package com.langlearn.repository;

import com.langlearn.model.Progress;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProgressRepository extends JpaRepository<Progress, Long> {
    List<Progress> findByLearnerIdOrderByTakenAtDesc(Long learnerId);
    List<Progress> findAllByOrderByTakenAtDesc();
}
