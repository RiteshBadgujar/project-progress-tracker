package com.minigroup.projectprogresstracker;

import androidx.annotation.NonNull;

public class ClassModel {

    private String className;
    private String classCode;
    private String collegeName;
    private String description;

    // 🔹 Full Constructor
    public ClassModel(String className, String classCode,
                      String collegeName, String description) {
        this.className = className;
        this.classCode = classCode;
        this.collegeName = collegeName;
        this.description = description;
    }

    // 🔹 Default Constructor (Required for GSON/Firebase)
    public ClassModel() {}

    // 🔹 Getters
    public String getClassName() {
        return (className != null) ? className : "Untitled Class";
    }

    public String getClassCode() {
        return (classCode != null) ? classCode : "000000";
    }

    public String getCollegeName() {
        return (collegeName != null) ? collegeName : "Unknown College";
    }

    public String getDescription() {
        return (description != null) ? description : "No description available.";
    }

    // 🔹 Setters
    public void setClassName(String className) { this.className = className; }
    public void setClassCode(String classCode) { this.classCode = classCode; }
    public void setCollegeName(String collegeName) { this.collegeName = collegeName; }
    public void setDescription(String description) { this.description = description; }

    /**
     * 🔹 Helper for UI Display
     * Returns a string like: "Computer Science (#CS101)"
     */
    public String getFullDisplayName() {
        return getClassName() + " (#" + getClassCode() + ")";
    }

    @NonNull
    @Override
    public String toString() {
        return "ClassModel{" +
                "className='" + className + '\'' +
                ", classCode='" + classCode + '\'' +
                '}';
    }
}