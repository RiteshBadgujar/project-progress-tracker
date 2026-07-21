# 📚 Project Progress Tracker

A role-based Android application designed to simplify academic project management in colleges and universities. The system enables administrators, teachers, and students to collaborate efficiently by managing project groups, assigning tasks, tracking progress, submitting work, and monitoring project activities.

---

## 📖 Overview

Project Progress Tracker is an Android application developed using **Java**, **Android Studio**, and **SQLite**. It provides a centralized platform for managing student projects through dedicated dashboards for different user roles.

The application helps educational institutions streamline project management by organizing classes, student groups, project tasks, submissions, announcements, notifications, and progress reports.

---

## ✨ Features

### 🔐 Authentication
- User Registration
- Secure Login
- Forgot Password
- Session Management

### 👨‍💼 Admin Module
- Manage Users
- Create & Manage Classes
- Monitor Project Progress
- Manage Announcements
- View Reports

### 👨‍🏫 Teacher Module
- View Assigned Groups
- Assign Tasks
- Review Student Progress
- Manage Weekly Reports
- Monitor Submissions

### 👨‍🎓 Student Module
- Join Groups
- View Assigned Tasks
- Submit Project Progress
- Track Submission History
- Receive Notifications

### 👥 Group Management
- Create Groups
- Join Groups
- Group Workspace

### 📋 Task Management
- Create Tasks
- Track Deadlines
- Update Task Status
- View Task History

### 📂 Submission Management
- Submit Project Work
- View Submission History
- Track Progress

### 📢 Communication
- Announcements
- Notifications
- Weekly Reports

---

# 🛠️ Technology Stack

| Category | Technology |
|----------|------------|
| Language | Java |
| UI | XML |
| IDE | Android Studio |
| Database | SQLite |
| Architecture | Repository Pattern |
| Version Control | Git & GitHub |

---

# 📂 Project Structure

```
ProjectProgressTracker
│
├── app
├── activities
├── adapters
├── database
├── models
├── repository
├── storage
├── utils
└── res
```

---

# 🗄️ Database

SQLite is used as the local database.

### Main Tables

- Users
- Classes
- Groups
- Tasks
- Submissions
- Announcements
- Notifications
- Weekly Reports

---

# 👥 User Roles

## Administrator
- Manage users
- Manage classes
- Monitor projects
- Publish announcements
- View reports

## Teacher
- Assign project tasks
- Review submissions
- Track student progress
- Manage groups

## Student
- Join project groups
- View assigned tasks
- Submit project work
- Track project progress

---

# 🔄 Application Workflow

```
Splash Screen
      │
      ▼
Login / Register
      │
      ▼
Authentication
      │
 ┌────┼────┐
 │    │    │
 ▼    ▼    ▼
Admin Teacher Student
 │      │      │
 ▼      ▼      ▼
Manage  Assign  Submit
Users   Tasks   Progress
 │      │      │
 ▼      ▼      ▼
Reports Weekly Notifications
```

---

# 🚀 Getting Started

### Clone the Repository

```bash
git clone https://github.com/RiteshBadgujar/project-progress-tracker.git
```

### Open in Android Studio

1. Clone the repository.
2. Open the project in Android Studio.
3. Sync Gradle.
4. Build the project.
5. Run the application on an Android emulator or physical device.

---

# 📋 Requirements

- Android Studio
- Java JDK 17+
- Android SDK
- Gradle
- SQLite (Built-in)

---

# 🔒 Security

- User Authentication
- Session Management
- Role-Based Access Control
- Local SQLite Storage
- Input Validation

---

# 🎯 Learning Outcomes

- Android Application Development
- Java Programming
- SQLite Database Integration
- Repository Pattern
- CRUD Operations
- RecyclerView
- Material Design Components
- Session Management

---

# 🚀 Future Enhancements

- Firebase Authentication
- Cloud Database Integration
- Push Notifications
- File Upload Support
- Calendar Integration
- GitHub Repository Integration
- Analytics Dashboard
- PDF Report Generation
- Dark Mode

---

# 👨‍💻 Developer

**Riteshkumar Badgujar**

🎓 Master of Computer Applications (MCA)

🏫 MET's Institute of Management Bhujbal Knowledge City, Nashik

🔗 GitHub: https://github.com/RiteshBadgujar

---

# 📄 License

This project is developed for educational and portfolio purposes.

---

## ⭐ Support

If you found this project useful, please consider giving it a ⭐ Star on GitHub.