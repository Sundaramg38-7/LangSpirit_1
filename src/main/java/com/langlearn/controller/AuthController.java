package com.langlearn.controller;

import com.langlearn.model.Setting;
import com.langlearn.model.User;
import com.langlearn.repository.SettingRepository;
import com.langlearn.repository.UserRepository;
import com.langlearn.service.ActivityService;
import com.langlearn.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuthController {
    private final UserRepository users;
    private final SettingRepository settings;
    private final AuthService auth;
    private final ActivityService activity;

    public AuthController(UserRepository users, SettingRepository settings, AuthService auth, ActivityService activity) {
        this.users = users; this.settings = settings; this.auth = auth; this.activity = activity;
    }

    @PostMapping("/auth/login")
    public Map<String, Object> login(@RequestBody Map<String, String> body) {
        User user = users.findByEmail(body.get("email"))
                .filter(u -> u.password.equals(body.get("password")))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));
        String token = auth.createSession(user);
        activity.log(user.name + " (" + user.role + ") logged in");
        return userResponse(user, token);
    }

    @PostMapping("/auth/register")
    public Map<String, Object> register(@RequestBody User body) {
        boolean allowed = settings.findById("allowRegistration").map(s -> "true".equals(s.val)).orElse(true);
        if (!allowed) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Registration is disabled by admin");
        if (body.name == null || body.name.isBlank() || body.email == null || body.password == null || body.password.length() < 4)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name, email and a password (min 4 chars) are required");
        if (users.findByEmail(body.email).isPresent())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        User user = users.save(new User(body.name, body.email, body.password, "LEARNER"));
        activity.log("New learner registered: " + user.name);
        return userResponse(user, auth.createSession(user));
    }

    @PostMapping("/auth/logout")
    public void logout(@RequestHeader(value = "X-Auth-Token", required = false) String token) {
        auth.logout(token);
    }

    /** Public settings used by the login page. */
    @GetMapping("/settings/public")
    public Map<String, String> publicSettings() {
        Map<String, String> map = new HashMap<>();
        for (Setting s : settings.findAll()) map.put(s.name, s.val);
        return map;
    }

    private Map<String, Object> userResponse(User u, String token) {
        Map<String, Object> map = new HashMap<>();
        map.put("token", token);
        map.put("id", u.id);
        map.put("name", u.name);
        map.put("role", u.role);
        return map;
    }
}
