package com.langlearn.controller;

import com.langlearn.model.*;
import com.langlearn.repository.*;
import com.langlearn.service.ActivityService;
import com.langlearn.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/instructor")
public class InstructorController {
    private final LessonRepository lessons;
    private final UserRepository users;
    private final ProgressRepository progressRepo;
    private final FeedbackRepository feedbackRepo;
    private final AuthService auth;
    private final ActivityService activity;

    public InstructorController(LessonRepository lessons, UserRepository users, ProgressRepository progressRepo,
                                FeedbackRepository feedbackRepo, AuthService auth, ActivityService activity) {
        this.lessons = lessons; this.users = users; this.progressRepo = progressRepo;
        this.feedbackRepo = feedbackRepo; this.auth = auth; this.activity = activity;
    }

    // ---------- Lesson Creation ----------
    @PostMapping("/lessons")
    public Map<String, String> createLesson(@RequestHeader(value = "X-Auth-Token", required = false) String token,
                                            @RequestBody Lesson body) {
        User instructor = auth.require(token, "INSTRUCTOR");
        if (body.title == null || body.title.isBlank() || body.language == null || body.language.isBlank()
                || body.content == null || body.content.isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Title, language and content are required");
        body.id = null;
        body.questions.forEach(q -> q.id = null);
        body.status = "PENDING";
        body.instructorId = instructor.id;
        body.instructorName = instructor.name;
        lessons.save(body);
        activity.log(instructor.name + " submitted lesson '" + body.title + "' for approval");
        return Map.of("message", "Lesson created successfully and sent for admin approval");
    }

    @GetMapping("/lessons")
    public List<Lesson> myLessons(@RequestHeader(value = "X-Auth-Token", required = false) String token) {
        User instructor = auth.require(token, "INSTRUCTOR");
        return lessons.findByInstructorId(instructor.id);
    }

    @DeleteMapping("/lessons/{id}")
    public Map<String, String> deleteLesson(@RequestHeader(value = "X-Auth-Token", required = false) String token,
                                            @PathVariable Long id) {
        User instructor = auth.require(token, "INSTRUCTOR");
        Lesson lesson = lessons.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lesson not found"));
        if (!instructor.id.equals(lesson.instructorId))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only delete your own lessons");
        lessons.delete(lesson);
        activity.log(instructor.name + " deleted lesson '" + lesson.title + "'");
        return Map.of("message", "Lesson deleted");
    }

    // ---------- Track Learner Progress ----------
    @GetMapping("/progress")
    public List<Progress> allProgress(@RequestHeader(value = "X-Auth-Token", required = false) String token) {
        auth.require(token, "INSTRUCTOR");
        return progressRepo.findAllByOrderByTakenAtDesc();
    }

    @GetMapping("/learners")
    public List<User> learners(@RequestHeader(value = "X-Auth-Token", required = false) String token) {
        auth.require(token, "INSTRUCTOR");
        return users.findByRole("LEARNER");
    }

    // ---------- Provide Feedback ----------
    @PostMapping("/feedback")
    public Map<String, String> giveFeedback(@RequestHeader(value = "X-Auth-Token", required = false) String token,
                                            @RequestBody Map<String, String> body) {
        User instructor = auth.require(token, "INSTRUCTOR");
        String message = body.get("message");
        if (message == null || message.isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Feedback message is required");
        User learner = users.findById(Long.parseLong(body.get("learnerId")))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Learner not found"));
        Feedback f = new Feedback();
        f.learnerId = learner.id;
        f.learnerName = learner.name;
        f.instructorName = instructor.name;
        f.message = message;
        feedbackRepo.save(f);
        activity.log(instructor.name + " gave feedback to " + learner.name);
        return Map.of("message", "Feedback submitted successfully");
    }
}
