package com.langlearn.service;

import com.langlearn.model.*;
import com.langlearn.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/** Inserts demo data the first time the app runs. */
@Component
public class DataSeeder implements CommandLineRunner {
    private final UserRepository users;
    private final LessonRepository lessons;
    private final SettingRepository settings;

    public DataSeeder(UserRepository users, LessonRepository lessons, SettingRepository settings) {
        this.users = users; this.lessons = lessons; this.settings = settings;
    }

    @Override
    public void run(String... args) {
        if (users.count() > 0) return;

        users.save(new User("Admin", "admin@demo.com", "admin123", "ADMIN"));
        User instructor = users.save(new User("Maria Instructor", "instructor@demo.com", "teach123", "INSTRUCTOR"));
        users.save(new User("Ravi Learner", "learner@demo.com", "learn123", "LEARNER"));

        settings.save(new Setting("platformName", "LinguaLearn"));
        settings.save(new Setting("allowRegistration", "true"));

        Lesson l = new Lesson();
        l.title = "Spanish Basics: Greetings";
        l.language = "Spanish";
        l.content = "Hola = Hello\nBuenos días = Good morning\nBuenas noches = Good night\nGracias = Thank you\nAdiós = Goodbye";
        l.status = "APPROVED";
        l.instructorId = instructor.id;
        l.instructorName = instructor.name;

        l.questions.add(q("What does 'Gracias' mean?", "Hello", "Thank you", "Goodbye", "Good night", 2));
        l.questions.add(q("How do you say 'Good morning'?", "Buenas noches", "Adiós", "Buenos días", "Hola", 3));
        lessons.save(l);
    }

    private Question q(String text, String a, String b, String c, String d, int correct) {
        Question q = new Question();
        q.questionText = text; q.option1 = a; q.option2 = b; q.option3 = c; q.option4 = d;
        q.correctOption = correct;
        return q;
    }
}
