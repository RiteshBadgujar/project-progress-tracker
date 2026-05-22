package com.minigroup.projectprogresstracker;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class Task {
    private String taskId;
    private String taskName;
    private String classCode;
    private String description;
    private String deadline;
    private boolean isCompleted;
    private String assignedDate;
    private String adminEmail = "admin@system.com"; // Default for UI display

    // Primary Constructor
    public Task(String taskId, String taskName, String description, String deadline) {
        this.taskId = taskId;
        this.taskName = taskName;
        this.description = description;
        this.deadline = deadline;
        this.isCompleted = false;
        this.assignedDate = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(new Date());
    }

    // Secondary Constructor (Matches AdminDashboardActivity call)
    public Task(String taskName, String classCode, String deadline) {
        this.taskId = UUID.randomUUID().toString().substring(0, 8);
        this.taskName = taskName;
        this.classCode = classCode;
        this.description = "Class Task";
        this.deadline = deadline;
        this.isCompleted = false;
        this.assignedDate = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(new Date());
    }

    // --- UI and Adapter Compatibility Methods ---

    public String getTaskTitle() { return taskName; }

    /**
     * Matches the call in GroupWorkspaceActivity
     */
    public String getAssignDate() { return assignedDate; }

    public String getCurrentStatus() {
        return isCompleted ? "COMPLETED" : "PENDING";
    }

    public String getClassCode() { return classCode; }

    public String getAdminEmail() { return adminEmail; }

    // --- Original Getters and Setters ---

    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }

    public String getTaskName() { return taskName; }
    public void setTaskName(String taskName) { this.taskName = taskName; }

    public void setClassCode(String classCode) { this.classCode = classCode; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDeadline() { return deadline; }
    public void setDeadline(String deadline) { this.deadline = deadline; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }

    public String getAssignedDate() { return assignedDate; }
    public void setAssignedDate(String assignedDate) { this.assignedDate = assignedDate; }

    public void setAdminEmail(String adminEmail) { this.adminEmail = adminEmail; }
}