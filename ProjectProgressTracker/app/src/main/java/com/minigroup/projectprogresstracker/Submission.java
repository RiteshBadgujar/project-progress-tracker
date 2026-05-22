package com.minigroup.projectprogresstracker;

import java.util.UUID;

public class Submission {
    private String submissionId;
    private String taskId;
    private String groupId; // Added to isolate data between different groups
    private String studentEmail;
    private String fileUri;
    private String fileName;
    private String timestamp;
    private String feedback;

    public Submission(String taskId, String groupId, String studentEmail, String fileUri, String fileName, String timestamp) {
        this.submissionId = UUID.randomUUID().toString();
        this.taskId = taskId;
        this.groupId = groupId; // Initialize groupId
        this.studentEmail = studentEmail;
        this.fileUri = fileUri;
        this.fileName = fileName;
        this.timestamp = timestamp;
        this.feedback = "";
    }

    // Getters
    public String getSubmissionId() { return submissionId; }
    public String getTaskId() { return taskId; }
    public String getGroupId() { return groupId; } // Added Getter
    public String getStudentEmail() { return studentEmail; }
    public String getFileUri() { return fileUri; }
    public String getFileName() { return fileName; }
    public String getTimestamp() { return timestamp; }
    public String getFeedback() { return feedback; }

    // Setters
    public void setFeedback(String feedback) { this.feedback = feedback; }
    public void setFileUri(String fileUri) { this.fileUri = fileUri; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    public void setTaskId(String taskId) { this.taskId = taskId; }
    public void setGroupId(String groupId) { this.groupId = groupId; } // Added Setter
    public void setStudentEmail(String studentEmail) { this.studentEmail = studentEmail; }

    public boolean isTextMessage() {
        return fileUri != null && fileUri.startsWith("text:");
    }
}