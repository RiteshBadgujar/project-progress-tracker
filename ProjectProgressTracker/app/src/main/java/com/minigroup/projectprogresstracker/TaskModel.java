package com.minigroup.projectprogresstracker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

@SuppressWarnings("unused") // Public API methods for TaskModel
public class TaskModel {

    private String taskId;
    private String taskName;
    private String classCode;
    private String groupId;
    private String deadline;
    private long createdAt;
    private boolean completed;

    // --- NEW FIELDS FOR HISTORY & TRACKING ---
    private String currentStatus; // "TODO", "SUBMITTED", "REJECTED", "COMPLETED"
    private ArrayList<Submission> history; // Changed to Submission
    private String assignedBy; // To distinguish between Admin and Teacher

    // Required for GSON/Firebase
    public TaskModel() {
        this.taskId = UUID.randomUUID().toString();
        this.createdAt = System.currentTimeMillis();
        this.completed = false;
        // Initialize fields
        this.currentStatus = "TODO";
        this.history = new ArrayList<>();
        this.assignedBy = "Admin"; // Default assigner
    }

    /**
     * Constructor for Class-wide Tasks
     */
    public TaskModel(String taskName, String classCode, String deadline) {
        this();
        this.taskName = taskName;
        this.classCode = classCode;
        this.groupId = null;
        this.deadline = deadline;
    }

    /**
     * Constructor for Group-specific Tasks
     */
    public TaskModel(String taskName, String classCode, String groupId, String deadline) {
        this();
        this.taskName = taskName;
        this.classCode = classCode;
        this.groupId = groupId;
        this.deadline = deadline;
    }

    // --- HELPER METHODS ---

    /**
     * Formats the createdAt timestamp into a readable string for the UI.
     * Resolves the "cannot find symbol: method getAssignedDate()" error.
     */
    public String getAssignedDate() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            return sdf.format(new Date(this.createdAt));
        } catch (Exception e) {
            return "N/A";
        }
    }

    /**
     * Adds a submission and syncs the status and completion boolean
     */
    public void addSubmission(Submission entry) { // Changed to Submission
        if (this.history == null) this.history = new ArrayList<>();
        this.history.add(entry);

        // Use feedback marker or check for file presence to determine status
        // For now, we assume adding a submission moves it to SUBMITTED
        this.currentStatus = "SUBMITTED";

        // Sync with your existing 'completed' boolean - only true if explicitly completed
        // Note: completed is false until explicitly set via setCompleted() or toggleCompleted()
        // this.completed = "COMPLETED".equalsIgnoreCase(this.currentStatus);
    }

    public Submission getLatestSubmission() { // Changed to Submission
        if (history != null && !history.isEmpty()) {
            return history.get(history.size() - 1);
        }
        return null;
    }

    // --- GETTERS ---
    public String getTaskId() { return taskId; }
    public String getTaskTitle() { return taskName; }
    public String getTaskName() { return taskName; }
    public String getClassCode() { return classCode; }
    public String getGroupId() { return groupId; }
    public String getDeadline() { return deadline; }
    public long getCreatedAt() { return createdAt; }
    public boolean isCompleted() { return completed; }
    public String getCurrentStatus() { return currentStatus; }
    public ArrayList<Submission> getHistory() { return history; } // Changed to Submission
    public String getAssignedBy() { return assignedBy; }

    // --- SETTERS ---
    public void setTaskId(String taskId) { this.taskId = taskId; }
    public void setTaskName(String taskName) { this.taskName = taskName; }
    public void setClassCode(String classCode) { this.classCode = classCode; }
    public void setGroupId(String groupId) { this.groupId = groupId; }
    public void setDeadline(String deadline) { this.deadline = deadline; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public void setAssignedBy(String assignedBy) { this.assignedBy = assignedBy; }

    public void setCompleted(boolean completed) {
        this.completed = completed;
        this.currentStatus = completed ? "COMPLETED" : "TODO";
    }

    public void setCurrentStatus(String currentStatus) { this.currentStatus = currentStatus; }
    public void setHistory(ArrayList<Submission> history) { this.history = history; } // Changed to Submission

    public void toggleCompleted() {
        this.completed = !this.completed;
        this.currentStatus = this.completed ? "COMPLETED" : "TODO";
    }

    public boolean isClassWide() {
        return this.groupId == null || this.groupId.isEmpty();
    }

    public void setAssignedDate(@SuppressWarnings("unused") String string) {
        // This method is kept for backward compatibility but is not currently used
        // The assigned date is stored as createdAt timestamp internally
    }
}