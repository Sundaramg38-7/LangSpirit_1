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
@RequestMapping("/api/learner")
public class LearnerController {
    private final LessonRepository lessons;
    private final ProgressRepository progressRepo;
    private final FeedbackRepository feedbackRepo;
    private final AuthService auth;
    private final ActivityService activity;

    public LearnerController(LessonRepository lessons, ProgressRepository progressRepo,
                             FeedbackRepository feedbackRepo, AuthService auth, ActivityService activity) {
        this.lessons = lessons; this.progressRepo = progressRepo;
        this.feedbackRepo = feedbackRepo; this.auth = auth; this.activity = activity;
    }

    @GetMapping("/lessons")
    public List<Lesson> approvedLessons(@RequestHeader(value = "X-Auth-Token", required = false) String token) {
        auth.require(token, "LEARNER");
        return lessons.findByStatus("APPROVED");
    }

    @GetMapping("/lessons/{id}")
    public Lesson lesson(@RequestHeader(value = "X-Auth-Token", required = false) String token, @PathVariable Long id) {
        auth.require(token, "LEARNER");
        return lessons.findById(id)
                .filter(l -> "APPROVED".equals(l.status))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lesson not found"));
    }

    /** Body: { "answers": [2, 3, ...] } - one chosen option (1-4) per question, in order. */
    @PostMapping("/lessons/{id}/submit")
    public Map<String, Object> submitQuiz(@RequestHeader(value = "X-Auth-Token", required = false) String token,
                                          @PathVariable Long id, @RequestBody Map<String, List<Integer>> body) {
        User learner = auth.require(token, "LEARNER");
        Lesson lesson = lessons.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lesson not found"));
        List<Integer> answers = body.get("answers");
        int score = 0;
        for (int i = 0; i < lesson.questions.size(); i++) {
            if (answers != null && i < answers.size() && answers.get(i) != null
                    && answers.get(i) == lesson.questions.get(i).correctOption) score++;
        }
        Progress p = new Progress();
        p.learnerId = learner.id;
        p.learnerName = learner.name;
        p.lessonId = lesson.id;
        p.lessonTitle = lesson.title;
        p.score = score;
        p.total = lesson.questions.size();
        progressRepo.save(p);
        activity.log(learner.name + " scored " + score + "/" + p.total + " in '" + lesson.title + "'");

        Map<String, Object> result = new HashMap<>();
        result.put("score", score);
        result.put("total", p.total);
        return result;
    }

    @GetMapping("/progress")
    public List<Progress> myProgress(@RequestHeader(value = "X-Auth-Token", required = false) String token) {
        User learner = auth.require(token, "LEARNER");
        return progressRepo.findByLearnerIdOrderByTakenAtDesc(learner.id);
    }

    @GetMapping("/feedback")
    public List<Feedback> myFeedback(@RequestHeader(value = "X-Auth-Token", required = false) String token) {
        User learner = auth.require(token, "LEARNER");
        return feedbackRepo.findByLearnerIdOrderByCreatedAtDesc(learner.id);
    }
}
