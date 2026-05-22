package com.minigroup.projectprogresstracker;

import java.io.Serializable;

/**
 * WeeklyReport Model
 * This class holds the data for project updates submitted by students.
 * It strictly separates the Internal ID (database) from the Alphanumeric Code (PDF/UI).
 */
public class WeeklyReport implements Serializable {

    private String groupId;        // The internal unique database key
    private String displayGroupId; // The 6-character alphanumeric code (e.g., ABC123)
    private String senderEmail;    // Submitter's email
    private String reportText;     // Summary of progress
    private String date;           // Submission date
    private String time;           // Submission time
    private String pdfUri;         // Path to the saved PDF file

    /**
     * No-args constructor for Firebase/Gson serialization
     */
    public WeeklyReport() {
    }

    /**
     * Optimized Full Constructor
     */
    public WeeklyReport(String groupId, String displayGroupId, String senderEmail,
                        String reportText, String date, String time, String pdfUri) {
        this.groupId = groupId;
        // Logic: if the display code is missing, use the raw ID as a safety fallback
        this.displayGroupId = (displayGroupId != null && !displayGroupId.isEmpty()) ? displayGroupId : groupId;
        this.senderEmail = senderEmail;
        this.reportText = reportText;
        this.date = date;
        this.time = time;
        this.pdfUri = pdfUri;
    }

    // --- Getters and Setters ---

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    /**
     * Returns the 6-character group code.
     * If not available, returns the raw groupId to avoid empty UI elements.
     */
    public String getDisplayGroupId() {
        if (displayGroupId == null || displayGroupId.isEmpty()) {
            return groupId;
        }
        return displayGroupId;
    }

    public void setDisplayGroupId(String displayGroupId) {
        this.displayGroupId = displayGroupId;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }

    public String getReportText() {
        return reportText;
    }

    public void setReportText(String reportText) {
        this.reportText = reportText;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getPdfUri() {
        return pdfUri;
    }

    public void setPdfUri(String pdfUri) {
        this.pdfUri = pdfUri;
    }
}