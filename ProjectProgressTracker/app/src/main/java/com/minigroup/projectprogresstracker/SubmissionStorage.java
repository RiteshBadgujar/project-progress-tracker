package com.minigroup.projectprogresstracker;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SubmissionStorage {

    private static final String PREF_NAME = "ProjectSubmissions";
    private static final String KEY_SUBMISSIONS = "all_submissions";

    /**
     * Saves a new submission to local storage.
     */
    public static void saveSubmission(Context context, Submission submission) {
        ArrayList<Submission> submissions = getAllSubmissions(context);
        submissions.add(submission);

        saveList(context, submissions);
    }

    /**
     * Retrieves all submissions from storage.
     */
    public static ArrayList<Submission> getAllSubmissions(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_SUBMISSIONS, null);

        if (json == null) {
            return new ArrayList<>();
        }

        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<Submission>>() {}.getType();
        return gson.fromJson(json, type);
    }

    /**
     * UPDATED: Filters submissions specifically for a Task ID AND Group ID.
     * This prevents Group A's messages from showing up in Group B's workspace.
     */
    public static ArrayList<Submission> getSubmissionsByTaskAndGroup(Context context, String taskId, String groupId) {
        ArrayList<Submission> all = getAllSubmissions(context);
        ArrayList<Submission> filtered = new ArrayList<>();

        if (taskId == null || groupId == null) return filtered;

        for (Submission s : all) {
            // Check both IDs to ensure complete isolation
            if (taskId.equals(s.getTaskId()) && groupId.equals(s.getGroupId())) {
                filtered.add(s);
            }
        }
        return filtered;
    }

    /**
     * Updates an existing submission (e.g., when a teacher marks it as deleted).
     */
    public static void updateSubmission(Context context, Submission updatedSubmission) {
        ArrayList<Submission> submissions = getAllSubmissions(context);
        for (int i = 0; i < submissions.size(); i++) {
            if (submissions.get(i).getSubmissionId().equals(updatedSubmission.getSubmissionId())) {
                submissions.set(i, updatedSubmission);
                break;
            }
        }
        saveList(context, submissions);
    }

    /**
     * Deletes a specific submission from local storage.
     */
    public static void deleteSubmission(Context context, Submission submissionToDelete) {
        ArrayList<Submission> submissions = getAllSubmissions(context);

        // Remove based on unique SubmissionId
        submissions.removeIf(s -> s.getSubmissionId().equals(submissionToDelete.getSubmissionId()));

        saveList(context, submissions);
    }

    /**
     * Private helper to handle the actual SharedPreferences writing logic.
     */
    private static void saveList(Context context, ArrayList<Submission> list) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(list);
        editor.putString(KEY_SUBMISSIONS, json);
        editor.apply();
    }
}