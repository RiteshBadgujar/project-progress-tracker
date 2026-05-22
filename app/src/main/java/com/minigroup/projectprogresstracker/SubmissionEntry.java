package com.minigroup.projectprogresstracker;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SubmissionEntry {
    private String studentEmail;    // Who uploaded it
    private String fileUri;         // Path to the file
    private String fileName;        // Name of the file (e.g., "SRS_Final.pdf")
    private long timestamp;         // Exact time of action
    private String teacherFeedback; // Comments from the Guide
    private String teacherEmail;    // Which Guide gave the feedback
    private String status;          // "PENDING", "APPROVED", or "REJECTED"

    // Constructor for Student Uploads
    public SubmissionEntry(String studentEmail, String fileUri, String fileName) {
        this.studentEmail = studentEmail;
        this.fileUri = fileUri;
        this.fileName = fileName;
        this.timestamp = System.currentTimeMillis(); // Auto-capture time
        this.status = "PENDING";
        this.teacherFeedback = ""; // Starts empty
    }

    // Helper method to get a readable Date string for the UI
    public String getFormattedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    // --- Getters and Setters ---

    public String getStudentEmail() { return studentEmail; }

    public String getFileUri() { return fileUri; }

    public String getFileName() { return fileName; }

    public long getTimestamp() { return timestamp; }

    public String getTeacherFeedback() { return teacherFeedback; }

    public void setTeacherFeedback(String teacherFeedback) {
        this.teacherFeedback = teacherFeedback;
    }

    public String getTeacherEmail() { return teacherEmail; }

    public void setTeacherEmail(String teacherEmail) {
        this.teacherEmail = teacherEmail;
    }

    public String getStatus() { return status; }

    public void setStatus(String status) {
        this.status = status;
    }
}