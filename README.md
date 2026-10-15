# Online Language Learning Platform

A web-based language learning platform built with **Java Servlets, JDBC and a JavaScript frontend**.
Users can take interactive lessons and quizzes, track their progress, get feedback from instructors and talk to other learners. Administrators manage users, lesson content and system settings.

## Tech Stack

| Layer    | Technology |
|----------|------------|
| Frontend | HTML, CSS, JavaScript (`src/main/webapp`) |
| Backend  | Java 17, Servlets (javax.servlet 4), Gson (JSON) |
| Database | JDBC with H2 (file based, no installation). MySQL optional |
| Server   | Jetty (Maven plugin). The WAR also runs on Tomcat 9 |
| Build    | Maven |

## User Roles and Features

**Admin**
- User management: create, edit and delete users and roles
- Content management: approve or reject lessons submitted by instructors
- System settings: platform name, enable or disable registration
- Activity monitoring: live log of user actions

**Instructor**
- Create lessons with quiz questions
- Provide feedback to learners
- Track learner progress (quiz scores)

**Learner**
- Register, login and browse approved lessons
- Take quizzes (auto graded)
- View own progress and instructor feedback
- Discussion board to interact with other learners

## How to Run

**Requirements:** JDK 17 or newer, Maven, internet connection (first run downloads dependencies).

1. Open a terminal in the project folder (the folder that contains `pom.xml`).
2. Run:
   ```
   mvn jetty:run
   ```
3. When the terminal shows `Started Jetty Server`, open **http://localhost:8080**
4. Press `Ctrl + C` in the terminal to stop the server.

## Demo Accounts

| Role       | Email                 | Password |
|------------|-----------------------|----------|
| Admin      | admin@demo.com        | admin123 |
| Instructor | instructor@demo.com   | teach123 |
| Learner    | learner@demo.com      | learn123 |

## Project Structure

```
language-platform-web
 ├── pom.xml
 ├── README.md
 └── src/main
     ├── java/com/langlearn
     │   ├── model/       User (abstract), Admin, Instructor, Learner, UserFactory,
     │   │                Lesson, Question, Progress, Feedback, Post, ActivityLog
     │   ├── dao/         Dao (interface), BaseDao<T>, Database, UserDao, LessonDao,
     │   │                ProgressDao, FeedbackDao, PostDao, ActivityDao, SettingDao
     │   ├── exception/   AppException
     │   ├── service/     ActivityLogger (background thread), SessionStore
     │   ├── listener/    AppInitListener (startup: tables + demo data)
     │   └── servlet/     BaseServlet, AuthServlet, AdminServlet, InstructorServlet,
     │                    LearnerServlet, PostServlet, PublicSettingsServlet
     ├── resources/       db.properties (database configuration)
     └── webapp/          index.html, style.css, app.js, WEB-INF/web.xml
```

## Java Concepts Used

| Concept | Where |
|---------|-------|
| Inheritance, abstraction, polymorphism | `User` (abstract) with `Admin`, `Instructor`, `Learner`; `getDashboardTitle()` and `canManageUsers()` behave differently per role |
| Interface | `Dao<T>` |
| Exception handling | Custom `AppException` with an HTTP status code, handled centrally in `BaseServlet` |
| Generics and collections | `BaseDao<T>`, `List`, `Map`, `Optional`, `BlockingQueue` |
| Multithreading and synchronization | `ActivityLogger` (background thread with a queue), `synchronized` methods in `SessionStore` and `ActivityLogger` |
| Encapsulation | Private fields with getters and setters in model classes |
| JDBC | `Database` (connection, table creation), `BaseDao` (`PreparedStatement`, `ResultSet`), one DAO class per table, transaction (commit and rollback) in `LessonDao.save()` |
| Servlets | `@WebServlet` classes using `doGet`, `doPost`, `doPut`, `doDelete`; `ServletContextListener` for startup |

## Database Tables

`app_users`, `lessons`, `questions`, `progress`, `feedback`, `posts`, `activity_log`, `settings`

Tables and demo data are created automatically on first start. Data is stored in the `data/` folder. To reset everything, stop the server and delete the `data/` folder.

To use MySQL instead of H2, add the `mysql-connector-j` dependency to `pom.xml` and change the values in `src/main/resources/db.properties` (instructions are in the file).

## REST API (used by the frontend)

| Method | URL | Who |
|--------|-----|-----|
| POST | `/api/auth/login`, `/api/auth/register`, `/api/auth/logout` | everyone |
| GET | `/api/settings/public` | everyone |
| GET, POST, PUT, DELETE | `/api/admin/users` | Admin |
| GET, POST | `/api/admin/lessons`, `/api/admin/lessons/{id}/approve` or `reject` | Admin |
| GET, PUT | `/api/admin/settings` | Admin |
| GET | `/api/admin/activity` | Admin |
| GET, POST, DELETE | `/api/instructor/lessons` | Instructor |
| GET | `/api/instructor/progress`, `/api/instructor/learners` | Instructor |
| POST | `/api/instructor/feedback` | Instructor |
| GET | `/api/learner/lessons`, `/api/learner/lessons/{id}` | Learner |
| POST | `/api/learner/lessons/{id}/submit` | Learner |
| GET | `/api/learner/progress`, `/api/learner/feedback` | Learner |
| GET, POST | `/api/posts` | all logged-in users |

## Troubleshooting

- **`No plugin found for prefix 'jetty'`**: the terminal is not in the folder that contains `pom.xml`. Use `cd` to go to that folder and run `dir` to confirm `pom.xml` is listed.
- **`Address already in use`**: another server is using port 8080. Stop it with `Ctrl + C` and run again.
- **Page not loading**: wait for `Started Jetty Server` in the terminal before opening the browser, and use `http://` (not `https://`).

## Limitations and Future Scope

- Passwords are stored as plain text and sessions are kept in memory. Future work: BCrypt password hashing and JWT authentication.
- Possible additions: lesson editing, audio and video lessons, certificates, leaderboard.
