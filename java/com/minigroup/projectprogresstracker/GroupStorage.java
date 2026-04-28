package com.minigroup.projectprogresstracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Map;

public class GroupStorage {

    private static final String PREF_NAME = "GroupPrefs";
    private static final String TAG = "GroupStorage";

    /**
     * Normalizes the class code to ensure storage is consistent.
     */
    private static String formatKey(String classCode) {
        return "groups_" + (classCode != null ? classCode.trim().toLowerCase() : "unknown");
    }

    /**
     * Updates an existing group object in the storage.
     */
    public static boolean updateGroup(Context context, Group group) {
        if (group != null && group.getClassCode() != null) {
            Log.d(TAG, "Updating group: " + group.getGroupName());
            saveGroup(context, group.getClassCode(), group);
            return true;
        }
        return false;
    }

    /**
     * Retrieves all groups associated with a specific class code.
     */
    public static ArrayList<Group> getGroupsByClass(Context context, String classCode) {
        if (classCode == null || context == null) return new ArrayList<>();

        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String key = formatKey(classCode);
        String json = prefs.getString(key, null);

        if (json == null) return new ArrayList<>();

        try {
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<Group>>() {}.getType();
            ArrayList<Group> groups = gson.fromJson(json, type);
            return groups != null ? groups : new ArrayList<>();
        } catch (Exception e) {
            Log.e(TAG, "Error parsing groups JSON", e);
            return new ArrayList<>();
        }
    }

    /**
     * Finds a group by ID without needing the class code.
     */
    public static Group getGroupById(Context context, String groupId) {
        if (context == null || groupId == null) return null;
        ArrayList<Group> all = getAllGroups(context);
        for (Group g : all) {
            if (g.getGroupId() != null && g.getGroupId().equals(groupId)) return g;
        }
        return null;
    }

    /**
     * Global deletion: Deletes a group by ID regardless of class code.
     * This is used when the last member leaves a group.
     */
    public static void deleteGroup(Context context, String groupId) {
        if (context == null || groupId == null) return;

        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Map<String, ?> allEntries = prefs.getAll();

        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            if (entry.getKey().startsWith("groups_")) {
                // Key format is "groups_classname", we need to extract the actual suffix
                String key = entry.getKey();
                String classCodeSuffix = key.replace("groups_", "");

                ArrayList<Group> groups = getGroupsByClass(context, classCodeSuffix);
                boolean removed = false;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    removed = groups.removeIf(g -> g.getGroupId() != null && g.getGroupId().equals(groupId));
                } else {
                    for (int i = 0; i < groups.size(); i++) {
                        if (groups.get(i).getGroupId() != null && groups.get(i).getGroupId().equals(groupId)) {
                            groups.remove(i);
                            removed = true;
                            break;
                        }
                    }
                }

                if (removed) {
                    saveGroups(context, classCodeSuffix, groups);
                    Log.d(TAG, "Group " + groupId + " permanently deleted.");
                }
            }
        }
    }

    /**
     * Saves the entire list of groups for a specific class.
     */
    public static void saveGroups(Context context, String classCode, ArrayList<Group> groups) {
        if (classCode == null || context == null) return;

        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(formatKey(classCode), new Gson().toJson(groups)).apply();
    }

    /**
     * Adds or Updates a single group within its specific class.
     */
    public static void saveGroup(Context context, String classCode, Group group) {
        if (group == null || group.getGroupId() == null || classCode == null || context == null) return;

        ArrayList<Group> groups = getGroupsByClass(context, classCode);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            groups.removeIf(g -> g.getGroupId() != null && g.getGroupId().equals(group.getGroupId()));
        } else {
            for (int i = 0; i < groups.size(); i++) {
                if (groups.get(i).getGroupId() != null && groups.get(i).getGroupId().equals(group.getGroupId())) {
                    groups.remove(i);
                    break;
                }
            }
        }

        groups.add(group);
        saveGroups(context, classCode, groups);
    }

    /**
     * Identifies which group a student belongs to in a specific class.
     */
    public static Group getGroupByStudent(Context context, String studentEmail, String classCode) {
        if (context == null || studentEmail == null || classCode == null) return null;

        ArrayList<Group> classGroups = getGroupsByClass(context, classCode);
        for (Group g : classGroups) {
            if (g.getStudentEmails() != null && g.getStudentEmails().contains(studentEmail)) {
                return g;
            }
        }
        return null;
    }

    /**
     * Standard removal for a known class code and group ID.
     */
    public static void removeGroup(Context context, String groupId, String classCode) {
        if (context == null || groupId == null || classCode == null) return;

        ArrayList<Group> groups = getGroupsByClass(context, classCode);
        boolean removed = false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            removed = groups.removeIf(g -> g.getGroupId() != null && g.getGroupId().equals(groupId));
        } else {
            for (int i = 0; i < groups.size(); i++) {
                if (groups.get(i).getGroupId() != null && groups.get(i).getGroupId().equals(groupId)) {
                    groups.remove(i);
                    removed = true;
                    break;
                }
            }
        }

        if (removed) saveGroups(context, classCode, groups);
    }

    /**
     * Gets all groups across all classes in the app.
     */
    public static ArrayList<Group> getAllGroups(Context context) {
        ArrayList<Group> allGroups = new ArrayList<>();
        if (context == null) return allGroups;

        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Map<String, ?> allEntries = prefs.getAll();
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<Group>>() {}.getType();

        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            if (entry.getKey().startsWith("groups_")) {
                try {
                    ArrayList<Group> classGroups = gson.fromJson(entry.getValue().toString(), type);
                    if (classGroups != null) allGroups.addAll(classGroups);
                } catch (Exception ignored) {}
            }
        }
        return allGroups;
    }

    /**
     * Clears all group data.
     */
    public static void clearAllGroups(Context context) {
        if (context == null) return;
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().clear().apply();
        Log.d(TAG, "All Group Storage cleared.");
    }
}