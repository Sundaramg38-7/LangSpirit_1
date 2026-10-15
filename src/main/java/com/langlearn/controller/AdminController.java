package com.langlearn.controller;

import com.langlearn.model.*;
import com.langlearn.repository.*;
import com.langlearn.service.ActivityService;
import com.langlearn.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final UserRepository users;
    private final LessonRepository lessons;
    private final SettingRepository settings;
    private final ActivityRepository activityRepo;
    private final AuthService auth;
    private final ActivityService activity;

    public AdminController(UserRepository users, LessonRepository lessons, SettingRepository settings,
                           ActivityRepository activityRepo, AuthService auth, ActivityService activity) {
        this.users = users; this.lessons = lessons; this.settings = settings;
        this.activityRepo = activityRepo; this.auth = auth; this.activity = activity;
    }

    // ---------- User Management ----------
    @GetMapping("/users")
    public List<User> listUsers(@RequestHeader(value = "X-Auth-Token", required = false) String token) {
        auth.require(token, "ADMIN");
        return users.findAll();
    }

    @PostMapping("/users")
    public Map<String, String> createUser(@RequestHeader(value = "X-Auth-Token", required = false) String token,
                                          @RequestBody User body) {
        User admin = auth.require(token, "ADMIN");
        validate(body);
        if (users.findByEmail(body.email).isPresent())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        users.save(new User(body.name, body.email, body.password, body.role));
        activity.log(admin.name + " created user " + body.email);
        return Map.of("message", "User created successfully");
    }

    @PutMapping("/users/{id}")
    public Map<String, String> updateUser(@RequestHeader(value = "X-Auth-Token", required = false) String token,
                                          @PathVariable Long id, @RequestBody User body) {
        User admin = auth.require(token, "ADMIN");
        User user = users.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (body.name != null && !body.name.isBlank()) user.name = body.name;
        if (body.email != null && !body.email.isBlank()) user.email = body.email;
        if (body.password != null && !body.password.isBlank()) user.password = body.password;
        if (body.role != null) user.role = body.role;
        users.save(user);
        activity.log(admin.name + " updated user " + user.email);
        return Map.of("message", "User updated successfully");
    }

    @DeleteMapping("/users/{id}")
    public Map<String, String> deleteUser(@RequestHeader(value = "X-Auth-Token", required = false) String token,
                                          @PathVariable Long id) {
        User admin = auth.require(token, "ADMIN");
        if (admin.id.equals(id))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot delete your own account");
        users.deleteById(id);
        activity.log(admin.name + " deleted user #" + id);
        return Map.of("message", "User deleted successfully");
    }

    // ---------- Content Management ----------
    @GetMapping("/lessons")
    public List<Lesson> allLessons(@RequestHeader(value = "X-Auth-Token", required = false) String token) {
        auth.require(token, "ADMIN");
        return lessons.findAll();
    }

    @PostMapping("/lessons/{id}/{decision}")
    public Map<String, String> decide(@RequestHeader(value = "X-Auth-Token", required = false) String token,
                                      @PathVariable Long id, @PathVariable String decision) {
        User admin = auth.require(token, "ADMIN");
        Lesson lesson = lessons.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lesson not found"));
        if (decision.equals("approve")) lesson.status = "APPROVED";
        else if (decision.equals("reject")) lesson.status = "REJECTED";
        else throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Use approve or reject");
        lessons.save(lesson);
        activity.log(admin.name + " " + decision + "d lesson '" + lesson.title + "'");
        return Map.of("message", "Lesson " + lesson.status.toLowerCase());
    }

    // ---------- System Settings ----------
    @GetMapping("/settings")
    public Map<String, String> getSettings(@RequestHeader(value = "X-Auth-Token", required = false) String token) {
        auth.require(token, "ADMIN");
        Map<String, String> map = new HashMap<>();
        for (Setting s : settings.findAll()) map.put(s.name, s.val);
        return map;
    }

    @PutMapping("/settings")
    public Map<String, String> updateSettings(@RequestHeader(value = "X-Auth-Token", required = false) String token,
                                              @RequestBody Map<String, String> body) {
        User admin = auth.require(token, "ADMIN");
        body.forEach((k, v) -> settings.save(new Setting(k, v)));
        activity.log(admin.name + " updated system settings");
        return Map.of("message", "Settings updated successfully");
    }

    // ---------- Activity Monitoring ----------
    @GetMapping("/activity")
    public List<ActivityLog> activityFeed(@RequestHeader(value = "X-Auth-Token", required = false) String token) {
        auth.require(token, "ADMIN");
        return activityRepo.findTop30ByOrderByTimeDesc();
    }

    private void validate(User u) {
        if (u.name == null || u.name.isBlank() || u.email == null || u.email.isBlank()
                || u.password == null || u.password.length() < 4)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name, email and password (min 4 chars) are required");
        if (!List.of("ADMIN", "INSTRUCTOR", "LEARNER").contains(u.role))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role must be ADMIN, INSTRUCTOR or LEARNER");
    }
}
