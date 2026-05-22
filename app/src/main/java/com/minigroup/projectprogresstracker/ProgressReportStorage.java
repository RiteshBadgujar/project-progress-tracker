package com.minigroup.projectprogresstracker;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class ProgressReportStorage {

    private static final String PREF = "ProgressReportPrefs";
    private static final String KEY_PREFIX = "reports_"; // reports_groupId

    /**
     * Saves a new weekly report entry for a specific group.
     */
    public static void addReport(Context context, String groupId, Submission report) {
        if (context == null || groupId == null || report == null) return;

        ArrayList<Submission> reports = getReports(context, groupId);
        reports.add(0, report); // Newest report at the top

        SharedPreferences prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        Gson gson = new Gson();
        editor.putString(KEY_PREFIX + groupId, gson.toJson(reports));
        editor.apply();
    }

    /**
     * Retrieves the list of weekly reports for a specific group.
     */
    public static ArrayList<Submission> getReports(Context context, String groupId) {
        if (context == null || groupId == null) return new ArrayList<>();

        SharedPreferences prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_PREFIX + groupId, null);

        if (json == null || json.isEmpty()) {
            return new ArrayList<>();
        }

        try {
            // Updated to use Submission class
            Type type = new TypeToken<ArrayList<Submission>>() {}.getType();
            ArrayList<Submission> list = new Gson().fromJson(json, type);
            return list != null ? list : new ArrayList<>();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Deletes all reports for a group (useful if project is reset).
     */
    public static void clearReports(Context context, String groupId) {
        if (context == null || groupId == null) return;
        SharedPreferences prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        prefs.edit().remove(KEY_PREFIX + groupId).apply();
    }
}