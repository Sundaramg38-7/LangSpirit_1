package com.langlearn.repository;

import com.langlearn.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findTop50ByOrderByCreatedAtDesc();
}
