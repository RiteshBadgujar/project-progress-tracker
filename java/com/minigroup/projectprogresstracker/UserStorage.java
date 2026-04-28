package com.minigroup.projectprogresstracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class UserStorage {

    private static final String PREF_NAME = "UserPrefs";
    private static final String KEY_USERS = "users";

    // 🔹 Save full list of users
    public static void saveUsers(Context context, ArrayList<User> users) {
        if (users == null) users = new ArrayList<>();
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(users);
        editor.putString(KEY_USERS, json);
        editor.apply();
    }

    // 🔹 Get all users
    public static ArrayList<User> getUsers(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_USERS, null);
        if (json == null || json.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<User>>() {}.getType();
            ArrayList<User> list = gson.fromJson(json, type);
            return list != null ? list : new ArrayList<>();
        } catch (Exception e) {
            Log.e("UserStorage", "Error parsing users JSON", e);
            return new ArrayList<>();
        }
    }

    // 🔹 NEW: Get single user by email (Used for Team Member Dialog)
    public static User getUserByEmail(Context context, String email) {
        if (email == null) return null;
        ArrayList<User> users = getUsers(context);
        for (User u : users) {
            if (u.getEmail() != null && u.getEmail().equalsIgnoreCase(email)) {
                return u;
            }
        }
        return null;
    }

    // 🔹 Add new user
    public static void addUser(Context context, User user) {
        ArrayList<User> users = getUsers(context);
        if (users == null) users = new ArrayList<>();
        users.add(user);
        saveUsers(context, users);
    }

    // 🔹 🔥 UPDATE EXISTING USER
    public static void updateUser(Context context, User updatedUser) {
        ArrayList<User> users = getUsers(context);
        boolean found = false;
        for (int i = 0; i < users.size(); i++) {
            User u = users.get(i);
            boolean sameEmail = u.getEmail() != null &&
                    updatedUser.getEmail() != null &&
                    u.getEmail().equalsIgnoreCase(updatedUser.getEmail());

            // Checking extra field if email matches is usually safer
            if (sameEmail) {
                users.set(i, updatedUser);
                found = true;
                break;
            }
        }
        if (found) {
            saveUsers(context, users);
        } else {
            Log.e("UserStorage", "Update failed: No user found -> " + updatedUser.getEmail());
        }
    }

    // 🔹 VALIDATE USER
    public static User validateUser(Context context, String email, String password, String role) {
        ArrayList<User> users = getUsers(context);
        for (User user : users) {
            if (user.getEmail() == null ||
                    user.getPassword() == null ||
                    user.getRole() == null) {
                continue;
            }
            if (user.getEmail().equalsIgnoreCase(email)
                    && user.getPassword().equals(password)
                    && user.getRole().equalsIgnoreCase(role)) {
                return user;
            }
        }
        return null;
    }

    // 🔹 Get Students by Class Code
    public static ArrayList<User> getStudentsByClass(Context context, String classCode) {
        ArrayList<User> allUsers = getUsers(context);
        ArrayList<User> filtered = new ArrayList<>();
        for (User u : allUsers) {
            if ("Student".equalsIgnoreCase(u.getRole()) &&
                    u.getClassCode() != null && u.getClassCode().equalsIgnoreCase(classCode)) {
                filtered.add(u);
            }
        }
        return filtered;
    }

    // 🔹 NEW: Get Available Students (Not in any group)
    public static ArrayList<User> getAvailableStudents(Context context, String classCode) {
        ArrayList<User> classStudents = getStudentsByClass(context, classCode);
        ArrayList<Group> allGroups = GroupStorage.getAllGroups(context);
        ArrayList<User> available = new ArrayList<>();

        for (User student : classStudents) {
            boolean inGroup = false;
            String email = student.getEmail();

            if (email == null) continue;

            for (Group group : allGroups) {
                boolean isLeader = email.equalsIgnoreCase(group.getLeaderEmail());
                boolean isMember = group.getStudentEmails() != null && group.getStudentEmails().contains(email);

                if (isLeader || isMember) {
                    inGroup = true;
                    break;
                }
            }
            if (!inGroup) {
                available.add(student);
            }
        }
        return available;
    }

    // 🔹 Get Users by Role (For Faculty Selection)
    public static ArrayList<User> getUsersByRole(Context context, String role) {
        ArrayList<User> allUsers = getUsers(context);
        ArrayList<User> filtered = new ArrayList<>();
        for (User u : allUsers) {
            if (u.getRole() != null && u.getRole().equalsIgnoreCase(role)) {
                filtered.add(u);
            }
        }
        return filtered;
    }

    // 🔹 Clear users (testing)
    public static void clearUsers(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }
}