package com.minigroup.projectprogresstracker;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;

public class SessionManager {

    private static final String PREF_NAME = "UserSession";
    private static final String KEY_USER = "user";

    public static void saveUser(Context context, User user) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(user);
        editor.putString(KEY_USER, json);
        editor.apply();
    }

    public static User getUser(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_USER, null);
        if (json == null) return null;
        return new Gson().fromJson(json, User.class);
    }

    // 🔹 New: Quick check if session exists
    public static boolean isLoggedIn(Context context) {
        return getUser(context) != null;
    }

    public static void clearSession(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }

    public static void logout(Context context) {
        clearSession(context);
    }
}