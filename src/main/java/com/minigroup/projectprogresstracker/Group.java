package com.minigroup.projectprogresstracker;

import android.content.Context;
import java.util.ArrayList;
import java.util.Random;

public class Group {

    private String groupId;
    private String groupName;
    private String projectTitle;
    private String technology;
    private String guideName = "";
    private String guideEmail = "";
    private String classCode;
    private String leaderEmail;
    private ArrayList<String> studentEmails;

    private String githubLink;
    private int progress = 0;

    /**
     * 🔹 Default Constructor
     */
    public Group() {
        this.studentEmails = new ArrayList<>();
        this.guideName = "";
        this.guideEmail = "";
        this.progress = 0;
        this.githubLink = null;
        // Forces a clean 6-char ID
        this.groupId = generate6CharCode();
    }

    /**
     * 🔹 Fully Updated Constructor
     * Logic Fix: If the passed groupId is too long (like a UUID),
     * it automatically generates a fresh 6-character code instead.
     */
    public Group(String groupId, String groupName, String projectTitle, String technology,
                 String guideName, String guideEmail, String classCode, String leaderEmail) {

        // ✅ FIX: Check if incoming ID is null, empty, or a long UUID (> 8 chars)
        if (groupId == null || groupId.isEmpty() || groupId.length() > 8) {
            this.groupId = generate6CharCode();
        } else {
            this.groupId = groupId;
        }

        this.groupName = groupName;
        this.projectTitle = projectTitle;
        this.technology = technology;
        this.guideName = (guideName == null) ? "" : guideName;
        this.guideEmail = (guideEmail == null) ? "" : guideEmail;
        this.classCode = classCode;
        this.leaderEmail = leaderEmail;
        this.studentEmails = new ArrayList<>();
        this.progress = 0;
        this.githubLink = null;
    }

    /**
     * 🔹 Private helper to generate 6 character alphanumeric code
     * Removed 'I', '1', '0', 'O' to avoid user confusion.
     */
    private String generate6CharCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder code = new StringBuilder();
        Random rnd = new Random();
        while (code.length() < 6) {
            code.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return code.toString();
    }

    // --- Getters and Setters ---

    public ArrayList<User> getMembers(Context context) {
        ArrayList<User> memberUsers = new ArrayList<>();
        ArrayList<User> allUsers = UserStorage.getUsers(context);

        if (leaderEmail != null && !leaderEmail.isEmpty()) {
            for (User u : allUsers) {
                if (u.getEmail().equalsIgnoreCase(leaderEmail)) {
                    memberUsers.add(u);
                    break;
                }
            }
        }

        if (studentEmails != null) {
            for (String email : studentEmails) {
                if (email.equalsIgnoreCase(leaderEmail)) continue;
                for (User u : allUsers) {
                    if (u.getEmail().equalsIgnoreCase(email)) {
                        memberUsers.add(u);
                        break;
                    }
                }
            }
        }
        return memberUsers;
    }

    public String getGithubLink() { return githubLink; }
    public void setGithubLink(String githubLink) { this.githubLink = githubLink; }

    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }

    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) {
        // Also validate setter to prevent future UUID injection
        if (groupId != null && groupId.length() > 8) {
            this.groupId = generate6CharCode();
        } else {
            this.groupId = groupId;
        }
    }

    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }

    public String getProjectTitle() { return projectTitle; }
    public void setProjectTitle(String projectTitle) { this.projectTitle = projectTitle; }

    public String getTechnology() { return technology; }
    public void setTechnology(String technology) { this.technology = technology; }

    public String getGuideName() { return (guideName == null) ? "" : guideName; }
    public void setGuideName(String guideName) { this.guideName = (guideName == null) ? "" : guideName; }

    public String getGuideEmail() { return (guideEmail == null) ? "" : guideEmail; }
    public void setGuideEmail(String guideEmail) { this.guideEmail = (guideEmail == null) ? "" : guideEmail; }

    public String getTeacherEmail() { return getGuideEmail(); }
    public void setTeacherEmail(String teacherEmail) { setGuideEmail(teacherEmail); }

    public String getClassCode() { return classCode; }
    public void setClassCode(String classCode) { this.classCode = classCode; }

    public String getLeaderEmail() { return leaderEmail; }
    public void setLeaderEmail(String leaderEmail) { this.leaderEmail = leaderEmail; }

    public ArrayList<String> getStudentEmails() {
        if (studentEmails == null) studentEmails = new ArrayList<>();
        return studentEmails;
    }

    public void setStudentEmails(ArrayList<String> studentEmails) {
        this.studentEmails = studentEmails;
    }

    // --- Helper Methods ---

    public boolean isPendingRequest() {
        return !getGuideEmail().isEmpty() && getGuideName().isEmpty();
    }

    public boolean isAssigned() {
        return !getGuideEmail().isEmpty() && !getGuideName().isEmpty();
    }

    public String getMembersListString() {
        if (studentEmails == null || studentEmails.isEmpty()) {
            return "No members added";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < studentEmails.size(); i++) {
            String email = studentEmails.get(i);
            if (email != null && email.contains("@")) {
                String name = email.split("@")[0];
                sb.append(name);
            } else {
                sb.append(email);
            }

            if (i < studentEmails.size() - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    public void addStudent(String email) {
        if (this.studentEmails == null) {
            this.studentEmails = new ArrayList<>();
        }
        if (email != null && !this.studentEmails.contains(email)) {
            this.studentEmails.add(email);
        }
    }
}