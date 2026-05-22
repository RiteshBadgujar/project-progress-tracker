package com.minigroup.projectprogresstracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.minigroup.projectprogresstracker.data.local.AppRepository;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class TaskStorage {

    private static final String PREF = "TaskPrefs";
    private static final String KEY = "tasks";

    // New Preference for group-specific completion states
    private static final String PREF_GROUP_STATUS = "GroupTaskStatusPrefs";

    /**
     * Retrieves EVERY task stored in the app using the TaskModel.
     */
    public static ArrayList<TaskModel> getAllTasks(Context context) {
        if (context == null) return new ArrayList<>();

        // Try to get from SQLite first
        try {
            AppRepository repository = new AppRepository(context);
            List<TaskModel> sqliteTasks = repository.getAllTasks();
            if (sqliteTasks != null && !sqliteTasks.isEmpty()) {
                Log.d("TaskStorage", "Loaded " + sqliteTasks.size() + " tasks from SQLite");
                return new ArrayList<>(sqliteTasks);
            }
        } catch (Exception e) {
            Log.e("TaskStorage", "Error loading tasks from SQLite, falling back to SharedPreferences", e);
        }

        // Fallback to SharedPreferences
        SharedPreferences prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY, null);

        if (json == null || json.isEmpty()) {
            return new ArrayList<>();
        }

        try {
            Type type = new TypeToken<ArrayList<TaskModel>>() {}.getType();
            ArrayList<TaskModel> list = new Gson().fromJson(json, type);
            return list != null ? list : new ArrayList<>();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Saves the full master list of tasks.
     */
    public static void saveAllTasks(Context context, ArrayList<TaskModel> list) {
        if (context == null || list == null) return;

        SharedPreferences prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        Gson gson = new Gson();
        editor.putString(KEY, gson.toJson(list));
        editor.apply();
    }

    /**
     * Updates a specific group's completion status for a task.
     * Logic: Saves a boolean using a unique key: taskId + groupId
     */
    public static void updateGroupTaskStatus(Context context, String taskId, String groupId, boolean isCompleted) {
        if (context == null || taskId == null || groupId == null) return;

        SharedPreferences prefs = context.getSharedPreferences(PREF_GROUP_STATUS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        String uniqueKey = taskId + "_" + groupId;
        editor.putBoolean(uniqueKey, isCompleted);
        editor.apply();
    }

    /**
     * Checks if a specific task is completed for a specific group.
     */
    public static boolean isTaskCompletedForGroup(Context context, String taskId, String groupId) {
        if (context == null || taskId == null || groupId == null) return false;

        SharedPreferences prefs = context.getSharedPreferences(PREF_GROUP_STATUS, Context.MODE_PRIVATE);
        String uniqueKey = taskId + "_" + groupId;
        return prefs.getBoolean(uniqueKey, false);
    }

    /**
     * Updates a specific task in the master list.
     */
    public static void updateTask(Context context, TaskModel updatedTask) {
        if (context == null || updatedTask == null) return;

        ArrayList<TaskModel> allTasks = getAllTasks(context);
        for (int i = 0; i < allTasks.size(); i++) {
            if (allTasks.get(i).getTaskId().equals(updatedTask.getTaskId())) {
                allTasks.set(i, updatedTask);
                break;
            }
        }
        saveAllTasks(context, allTasks);
    }

    /**
     * Adds a new task to the start of the master list.
     */
    public static void addTask(Context context, TaskModel task) {
        if (context == null || task == null) return;

        // Insert into SQLite
        AppRepository repository = new AppRepository(context);
        boolean sqliteSuccess = repository.insertTask(task);
        Log.d("TaskStorage", "SQLite insert task: " + task.getTaskId() + " - " + (sqliteSuccess ? "SUCCESS" : "FAILED"));

        // Also save to SharedPreferences for backward compatibility
        ArrayList<TaskModel> list = getAllTasks(context);
        list.add(0, task); // Latest first
        saveAllTasks(context, list);
    }

    /**
     * Method used by HomeFragment.java
     */
    public static ArrayList<TaskModel> getTasks(Context context, String classCode) {
        return getClassTasks(context, classCode);
    }

    /**
     * Filters tasks by classCode.
     */
    public static ArrayList<TaskModel> getClassTasks(Context context, String classCode) {
        ArrayList<TaskModel> filtered = new ArrayList<>();
        if (context == null || classCode == null) return filtered;

        ArrayList<TaskModel> allTasks = getAllTasks(context);

        for (TaskModel t : allTasks) {
            if (t != null && t.getClassCode() != null && t.getClassCode().equalsIgnoreCase(classCode)) {
                filtered.add(t);
            }
        }
        return filtered;
    }

    /**
     * Deletes a specific task by ID.
     */
    public static void deleteTask(Context context, String taskId) {
        if (context == null || taskId == null) return;

        ArrayList<TaskModel> allTasks = getAllTasks(context);
        ArrayList<TaskModel> updatedList = new ArrayList<>();

        for (TaskModel t : allTasks) {
            if (t.getTaskId() != null && !t.getTaskId().equals(taskId)) {
                updatedList.add(t);
            }
        }
        saveAllTasks(context, updatedList);
    }
}