package com.minigroup.projectprogresstracker.data.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.minigroup.projectprogresstracker.Group;
import com.minigroup.projectprogresstracker.data.local.AppDatabaseHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GroupRepository {

    private final AppDatabaseHelper dbHelper;

    public GroupRepository(Context context) {
        this.dbHelper = AppDatabaseHelper.getInstance(context);
    }

    public long insert(Group group) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.insert(AppDatabaseHelper.TABLE_GROUPS, null, toValues(group));
    }

    public long insertData(Group group) {
        return insert(group);
    }

    public List<Group> getAll() {
        ArrayList<Group> groups = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(AppDatabaseHelper.TABLE_GROUPS, null, null, null, null, null,
                    AppDatabaseHelper.COL_GROUP_NAME + " ASC");
            while (cursor.moveToNext()) {
                groups.add(fromCursor(cursor));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return groups;
    }

    public List<Group> getAllData() {
        return getAll();
    }

    public Group getById(String groupId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(
                    AppDatabaseHelper.TABLE_GROUPS,
                    null,
                    AppDatabaseHelper.COL_GROUP_ID + "=?",
                    new String[]{groupId},
                    null,
                    null,
                    null
            );
            if (cursor.moveToFirst()) {
                return fromCursor(cursor);
            }
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public int update(Group group) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.update(
                AppDatabaseHelper.TABLE_GROUPS,
                toValues(group),
                AppDatabaseHelper.COL_GROUP_ID + "=?",
                new String[]{group.getGroupId()}
        );
    }

    public int updateData(Group group) {
        return update(group);
    }

    public int delete(String groupId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(
                AppDatabaseHelper.TABLE_GROUPS,
                AppDatabaseHelper.COL_GROUP_ID + "=?",
                new String[]{groupId}
        );
    }

    public int deleteData(String groupId) {
        return delete(groupId);
    }

    private ContentValues toValues(Group group) {
        ContentValues values = new ContentValues();
        values.put(AppDatabaseHelper.COL_GROUP_ID, group.getGroupId());
        values.put(AppDatabaseHelper.COL_GROUP_NAME, group.getGroupName());
        values.put(AppDatabaseHelper.COL_GROUP_PROJECT_TITLE, group.getProjectTitle());
        values.put(AppDatabaseHelper.COL_GROUP_TECHNOLOGY, group.getTechnology());
        values.put(AppDatabaseHelper.COL_GROUP_GUIDE_NAME, group.getGuideName());
        values.put(AppDatabaseHelper.COL_GROUP_GUIDE_EMAIL, group.getGuideEmail());
        values.put(AppDatabaseHelper.COL_GROUP_CLASS_CODE, group.getClassCode());
        values.put(AppDatabaseHelper.COL_GROUP_LEADER_EMAIL, group.getLeaderEmail());
        values.put(AppDatabaseHelper.COL_GROUP_GITHUB_LINK, group.getGithubLink());
        values.put(AppDatabaseHelper.COL_GROUP_PROGRESS, group.getProgress());
        values.put(AppDatabaseHelper.COL_GROUP_STUDENT_EMAILS, joinStudents(group.getStudentEmails()));
        return values;
    }

    private Group fromCursor(Cursor cursor) {
        Group group = new Group();
        group.setGroupId(cursor.getString(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_GROUP_ID)));
        group.setGroupName(cursor.getString(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_GROUP_NAME)));
        group.setProjectTitle(cursor.getString(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_GROUP_PROJECT_TITLE)));
        group.setTechnology(cursor.getString(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_GROUP_TECHNOLOGY)));
        group.setGuideName(cursor.getString(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_GROUP_GUIDE_NAME)));
        group.setGuideEmail(cursor.getString(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_GROUP_GUIDE_EMAIL)));
        group.setClassCode(cursor.getString(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_GROUP_CLASS_CODE)));
        group.setLeaderEmail(cursor.getString(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_GROUP_LEADER_EMAIL)));
        group.setGithubLink(cursor.getString(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_GROUP_GITHUB_LINK)));
        group.setProgress(cursor.getInt(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_GROUP_PROGRESS)));
        group.setStudentEmails(splitStudents(cursor.getString(
                cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_GROUP_STUDENT_EMAILS))));
        return group;
    }

    private String joinStudents(List<String> emails) {
        if (emails == null || emails.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < emails.size(); i++) {
            if (emails.get(i) == null) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(",");
            }
            builder.append(emails.get(i));
        }
        return builder.toString();
    }

    private ArrayList<String> splitStudents(String csv) {
        if (csv == null || csv.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(csv.split("\\s*,\\s*")));
    }
}
