package com.langlearn.repository;

import com.langlearn.model.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findByLearnerIdOrderByCreatedAtDesc(Long learnerId);
}
