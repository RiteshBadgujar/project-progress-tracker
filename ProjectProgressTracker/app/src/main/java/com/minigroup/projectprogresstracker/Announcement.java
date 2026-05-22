package com.minigroup.projectprogresstracker;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Announcement {

    private String title;
    private String message;
    private String classCode;
    private long timestamp;

    // 🔹 Default constructor (Required for GSON/JSON parsing)
    public Announcement() {
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 🔹 TWO-ARGUMENT CONSTRUCTOR (Fixes AdminDashboardActivity error)
     * This allows your old code to work while defaulting the title.
     */
    public Announcement(String message, String classCode) {
        this.title = "Announcement"; // Default title
        this.message = message;
        this.classCode = classCode;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 🔹 THREE-ARGUMENT CONSTRUCTOR
     * Use this when you want to specify a custom title.
     */
    public Announcement(String title, String message, String classCode) {
        this.title = title;
        this.message = message;
        this.classCode = classCode;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 🔹 UI DATE FORMATTER
     * Matches your XML: "Oct 24, 2026 • 10:30 AM"
     */
    public String getDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    // 🔹 Getters and Setters
    public String getTitle() {
        return (title == null || title.isEmpty()) ? "Announcement" : title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getClassCode() {
        return classCode;
    }

    public void setClassCode(String classCode) {
        this.classCode = classCode;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}