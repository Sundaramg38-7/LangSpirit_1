# Online Language Learning Platform

A basic full-stack college project (Admin / Instructor / Learner roles).

| Layer     | Technology                                   |
|-----------|----------------------------------------------|
| Frontend  | HTML, CSS, JavaScript (in `src/main/resources/static`) |
| Backend   | **Java 17**, Spring Boot 3 (REST API)        |
| Database  | H2 (file based) via Spring Data JPA — MySQL optional |

## How to run
1. Install **JDK 17+** and **Maven** (or open the folder in IntelliJ / Eclipse / VS Code and run `LangLearnApplication`).
2. In this folder run:  `mvn spring-boot:run`
3. Open **http://localhost:8080**
4. Database viewer (optional): http://localhost:8080/h2-console  
   JDBC URL `jdbc:h2:file:./data/langdb`, user `sa`, empty password.

## Demo logins
| Role       | Email               | Password  |
|------------|---------------------|-----------|
| Admin      | admin@demo.com      | admin123  |
| Instructor | instructor@demo.com | teach123  |
| Learner    | learner@demo.com    | learn123  |

## Features (mapped to the project document)
**Admin** – User management (create / edit / delete), approve or reject lessons, system settings (platform name, allow registration), activity monitoring.
**Instructor** – Create lessons with quiz questions, give feedback to learners, track learner progress.
**Learner** – Browse approved lessons, take quizzes (auto-graded), view own progress, read instructor feedback, discussion board to interact with other learners.

## Project structure
```
src/main/java/com/langlearn
 ├── model/        entities = database tables (User, Lesson, Question, Progress, Feedback, Post, ActivityLog, Setting)
 ├── repository/   Spring Data JPA interfaces (database access)
 ├── service/      AuthService (login tokens), ActivityService, DataSeeder (demo data)
 └── controller/   REST APIs: Auth, Admin, Instructor, Learner, Post
src/main/resources
 ├── application.properties   database + server config
 └── static/                  index.html, style.css, app.js (frontend)
```

## Main REST endpoints
| Method | URL | Who |
|---|---|---|
| POST | /api/auth/login, /api/auth/register | everyone |
| GET/POST/PUT/DELETE | /api/admin/users | Admin |
| GET, POST | /api/admin/lessons, /api/admin/lessons/{id}/approve or reject | Admin |
| GET/PUT | /api/admin/settings, GET /api/admin/activity | Admin |
| GET/POST/DELETE | /api/instructor/lessons | Instructor |
| GET | /api/instructor/progress, /api/instructor/learners | Instructor |
| POST | /api/instructor/feedback | Instructor |
| GET | /api/learner/lessons, /api/learner/lessons/{id} | Learner |
| POST | /api/learner/lessons/{id}/submit | Learner |
| GET | /api/learner/progress, /api/learner/feedback | Learner |
| GET/POST | /api/posts | all logged-in users |

## Ideas to extend (for viva / next semester)
- Hash passwords with BCrypt and use Spring Security + JWT.
- Move to MySQL (see comments in `application.properties`).
- Add lesson editing, audio/video lessons, certificates, leaderboard.
"# LangSpirit_1" 
