package com.minigroup.projectprogresstracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Optimized ReportStorage
 * Handles the saving and retrieval of WeeklyReport objects using SharedPreferences.
 * Now handles WeeklyReport objects containing Group Names and Display IDs.
 */
public class ReportStorage {
    private static final String PREF_NAME = "ReportPrefs";
    private static final String KEY_REPORTS = "all_reports";
    private static final String TAG = "ReportStorage";

    /**
     * Saves a new report to the top of the list (newest first).
     * Automatically serializes all fields including groupName and displayGroupId.
     */
    public static boolean saveReport(Context context, WeeklyReport report) {
        if (report == null || context == null) return false;

        ArrayList<WeeklyReport> reports = getAllReports(context);
        // Add to index 0 so the most recent report appears at the top of the UI list
        reports.add(0, report);

        return saveListToPrefs(context, reports);
    }

    /**
     * Retrieves all reports linked to a specific Group.
     * Use the internal unique key (rawGroupId) for consistent filtering.
     */
    public static ArrayList<WeeklyReport> getReportsByGroupId(Context context, String rawGroupId) {
        ArrayList<WeeklyReport> allReports = getAllReports(context);
        ArrayList<WeeklyReport> groupReports = new ArrayList<>();

        if (rawGroupId == null || context == null) return groupReports;

        for (WeeklyReport r : allReports) {
            // Filter using the internal unique database key
            if (r != null && r.getGroupId() != null && rawGroupId.equals(r.getGroupId())) {
                groupReports.add(r);
            }
        }
        return groupReports;
    }

    /**
     * Internal helper to load the full list from JSON.
     */
    public static ArrayList<WeeklyReport> getAllReports(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_REPORTS, null);

        if (json == null || json.isEmpty()) return new ArrayList<>();

        try {
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<WeeklyReport>>() {}.getType();
            ArrayList<WeeklyReport> list = gson.fromJson(json, type);

            if (list == null) return new ArrayList<>();

            // Cleanup: ensure no null entries exist in the returned list
            ArrayList<WeeklyReport> cleanList = new ArrayList<>();
            for (WeeklyReport w : list) {
                if (w != null) cleanList.add(w);
            }
            return cleanList;

        } catch (Exception e) {
            Log.e(TAG, "Failed to parse reports: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Internal helper to commit the list to storage.
     */
    private static boolean saveListToPrefs(Context context, ArrayList<WeeklyReport> list) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            Gson gson = new Gson();
            String json = gson.toJson(list);
            editor.putString(KEY_REPORTS, json);
            // Use commit() for immediate disk writing
            return editor.commit();
        } catch (Exception e) {
            Log.e(TAG, "Error saving list: " + e.getMessage());
            return false;
        }
    }

    /**
     * Clears all reports from the device.
     * Highly recommended to call this once after updating the WeeklyReport model
     * to ensure storage consistency.
     */
    public static void clearAllReports(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
        Log.d(TAG, "Storage wiped clean.");
    }
}