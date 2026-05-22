package com.minigroup.projectprogresstracker;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.util.Map;

public class ClassStorage {

    private static final String PREF_NAME = "ClassPrefs";

    public static void saveClass(Context context, String adminEmail,
                                 String name, String code,
                                 String collegeName, String description) {

        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        ClassModel model = new ClassModel(name, code, collegeName, description);

        Gson gson = new Gson();
        String json = gson.toJson(model);

        editor.putString("class_" + adminEmail, json);
        editor.apply();
    }

    public static ClassModel getClass(Context context, String adminEmail) {

        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        String json = prefs.getString("class_" + adminEmail, null);
        if (json == null) return null;

        Gson gson = new Gson();
        ClassModel model = gson.fromJson(json, ClassModel.class);

        // 🔥 Fix old data
        if (model.getCollegeName() == null) model.setCollegeName("");
        if (model.getDescription() == null) model.setDescription("");

        return model;
    }

    public static ClassModel getClassByCode(Context context, String code) {

        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Gson gson = new Gson();

        Map<String, ?> all = prefs.getAll();

        for (String key : all.keySet()) {

            if (key.startsWith("class_")) {

                String json = prefs.getString(key, null);
                if (json == null) continue;

                ClassModel c = gson.fromJson(json, ClassModel.class);

                if (c != null && code.equals(c.getClassCode())) {

                    if (c.getCollegeName() == null) c.setCollegeName("");
                    if (c.getDescription() == null) c.setDescription("");

                    return c;
                }
            }
        }
        return null;
    }

    public static boolean isValidCode(Context context, String code) {
        return getClassByCode(context, code) != null;
    }
}