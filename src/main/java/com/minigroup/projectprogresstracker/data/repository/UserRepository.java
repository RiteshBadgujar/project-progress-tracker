package com.minigroup.projectprogresstracker.data.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.minigroup.projectprogresstracker.User;
import com.minigroup.projectprogresstracker.data.local.AppDatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class UserRepository {

    private final AppDatabaseHelper dbHelper;

    public UserRepository(Context context) {
        this.dbHelper = AppDatabaseHelper.getInstance(context);
    }

    public long insert(User user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.insert(AppDatabaseHelper.TABLE_USERS, null, toValues(user));
    }

    public long insertData(User user) {
        return insert(user);
    }

    public List<User> getAll() {
        ArrayList<User> users = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(AppDatabaseHelper.TABLE_USERS, null, null, null, null, null,
                    AppDatabaseHelper.COL_USER_NAME + " ASC");
            while (cursor.moveToNext()) {
                users.add(fromCursor(cursor));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return users;
    }

    public List<User> getAllData() {
        return getAll();
    }

    public User getById(String email) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(
                    AppDatabaseHelper.TABLE_USERS,
                    null,
                    AppDatabaseHelper.COL_USER_EMAIL + "=?",
                    new String[]{email},
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

    public int update(User user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.update(
                AppDatabaseHelper.TABLE_USERS,
                toValues(user),
                AppDatabaseHelper.COL_USER_EMAIL + "=?",
                new String[]{user.getEmail()}
        );
    }

    public int updateData(User user) {
        return update(user);
    }

    public int delete(String email) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(
                AppDatabaseHelper.TABLE_USERS,
                AppDatabaseHelper.COL_USER_EMAIL + "=?",
                new String[]{email}
        );
    }

    public int deleteData(String email) {
        return delete(email);
    }

    private ContentValues toValues(User user) {
        ContentValues values = new ContentValues();
        values.put(AppDatabaseHelper.COL_USER_EMAIL, user.getEmail());
        values.put(AppDatabaseHelper.COL_USER_NAME, user.getName());
        values.put(AppDatabaseHelper.COL_USER_PASSWORD, user.getPassword());
        values.put(AppDatabaseHelper.COL_USER_ROLE, user.getRole());
        if (user.getClassCode() == null) {
            values.putNull(AppDatabaseHelper.COL_USER_CLASS_CODE);
        } else {
            values.put(AppDatabaseHelper.COL_USER_CLASS_CODE, user.getClassCode());
        }
        if (user.getExtra() == null) {
            values.putNull(AppDatabaseHelper.COL_USER_EXTRA);
        } else {
            values.put(AppDatabaseHelper.COL_USER_EXTRA, user.getExtra());
        }
        return values;
    }

    private User fromCursor(Cursor cursor) {
        User user = new User();
        user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_USER_EMAIL)));
        user.setName(cursor.getString(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_USER_NAME)));
        user.setPassword(cursor.getString(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_USER_PASSWORD)));
        user.setRole(cursor.getString(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_USER_ROLE)));
        user.setClassCode(cursor.getString(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_USER_CLASS_CODE)));
        user.setExtra(cursor.getString(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_USER_EXTRA)));
        return user;
    }
}
