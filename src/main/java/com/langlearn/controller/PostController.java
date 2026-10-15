package com.langlearn.controller;

import com.langlearn.model.Post;
import com.langlearn.model.User;
import com.langlearn.repository.PostRepository;
import com.langlearn.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

/** Discussion board - lets learners interact with each other. */
@RestController
@RequestMapping("/api/posts")
public class PostController {
    private final PostRepository posts;
    private final AuthService auth;

    public PostController(PostRepository posts, AuthService auth) {
        this.posts = posts; this.auth = auth;
    }

    @GetMapping
    public List<Post> list(@RequestHeader(value = "X-Auth-Token", required = false) String token) {
        auth.require(token);
        return posts.findTop50ByOrderByCreatedAtDesc();
    }

    @PostMapping
    public Map<String, String> add(@RequestHeader(value = "X-Auth-Token", required = false) String token,
                                   @RequestBody Map<String, String> body) {
        User user = auth.require(token);
        String message = body.get("message");
        if (message == null || message.isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Message cannot be empty");
        Post p = new Post();
        p.authorName = user.name;
        p.authorRole = user.role;
        p.message = message;
        posts.save(p);
        return Map.of("message", "Posted");
    }
}
