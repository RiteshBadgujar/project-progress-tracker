package com.minigroup.projectprogresstracker;

public class User {

    private String name;
    private String email;
    private String password;
    private String role;
    private String classCode;
    private String extra;

    // 🔹 Constructor for Full Data (All fields)
    public User(String name, String email, String password, String role,
                String classCode, String extra) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.classCode = classCode;
        this.extra = extra;
    }

    // 🔹 Simplified Constructor (Used for Profile Updates/Initial Creation)
    public User(String name, String email, String password, String role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    // 🔹 Empty constructor (IMPORTANT for Gson serialization)
    public User() {
    }

    // 🔹 Getters
    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }

    public String getClassCode() {
        return classCode;
    }

    public String getExtra() {
        return extra;
    }

    // 🔹 Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setClassCode(String classCode) {
        this.classCode = classCode;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }
}