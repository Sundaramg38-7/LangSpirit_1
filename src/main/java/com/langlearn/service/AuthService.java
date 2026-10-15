package com.langlearn.service;

import com.langlearn.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Very simple token based login (kept in memory). */
@Service
public class AuthService {
    private final Map<String, User> sessions = new ConcurrentHashMap<>();

    public String createSession(User user) {
        String token = UUID.randomUUID().toString();
        sessions.put(token, user);
        return token;
    }

    public void logout(String token) {
        if (token != null) sessions.remove(token);
    }

    /** Returns the logged-in user or throws 401/403. */
    public User require(String token, String... roles) {
        User user = token == null ? null : sessions.get(token);
        if (user == null)
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please login first");
        if (roles.length > 0 && !Arrays.asList(roles).contains(user.role))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have access to this action");
        return user;
    }
}
