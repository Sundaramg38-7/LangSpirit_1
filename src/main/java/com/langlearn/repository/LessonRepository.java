package com.langlearn.repository;

import com.langlearn.model.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LessonRepository extends JpaRepository<Lesson, Long> {
    List<Lesson> findByStatus(String status);
    List<Lesson> findByInstructorId(Long instructorId);
}
