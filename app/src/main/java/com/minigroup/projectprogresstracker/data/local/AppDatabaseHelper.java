package com.minigroup.projectprogresstracker.data.local;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class AppDatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "project_progress_tracker.db";
    public static final int DATABASE_VERSION = 1;

    public static final String TABLE_USERS = "users";
    public static final String TABLE_GROUPS = "groups";
    public static final String TABLE_TASKS = "tasks";

    public static final String COL_USER_EMAIL = "email";
    public static final String COL_USER_NAME = "name";
    public static final String COL_USER_PASSWORD = "password";
    public static final String COL_USER_ROLE = "role";
    public static final String COL_USER_CLASS_CODE = "class_code";
    public static final String COL_USER_EXTRA = "extra";

    public static final String COL_GROUP_ID = "group_id";
    public static final String COL_GROUP_NAME = "group_name";
    public static final String COL_GROUP_PROJECT_TITLE = "project_title";
    public static final String COL_GROUP_TECHNOLOGY = "technology";
    public static final String COL_GROUP_GUIDE_NAME = "guide_name";
    public static final String COL_GROUP_GUIDE_EMAIL = "guide_email";
    public static final String COL_GROUP_CLASS_CODE = "class_code";
    public static final String COL_GROUP_LEADER_EMAIL = "leader_email";
    public static final String COL_GROUP_STUDENT_EMAILS = "student_emails";
    public static final String COL_GROUP_GITHUB_LINK = "github_link";
    public static final String COL_GROUP_PROGRESS = "progress";

    public static final String COL_TASK_ID = "task_id";
    public static final String COL_TASK_NAME = "task_name";
    public static final String COL_TASK_CLASS_CODE = "class_code";
    public static final String COL_TASK_GROUP_ID = "group_id";
    public static final String COL_TASK_DESCRIPTION = "description";
    public static final String COL_TASK_DEADLINE = "deadline";
    public static final String COL_TASK_ASSIGNED_DATE = "assigned_date";
    public static final String COL_TASK_IS_COMPLETED = "is_completed";
    public static final String COL_TASK_ADMIN_EMAIL = "admin_email";

    private static volatile AppDatabaseHelper instance;

    public static AppDatabaseHelper getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabaseHelper.class) {
                if (instance == null) {
                    instance = new AppDatabaseHelper(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    private AppDatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_USERS + " ("
                + COL_USER_EMAIL + " TEXT PRIMARY KEY, "
                + COL_USER_NAME + " TEXT NOT NULL, "
                + COL_USER_PASSWORD + " TEXT NOT NULL, "
                + COL_USER_ROLE + " TEXT NOT NULL, "
                + COL_USER_CLASS_CODE + " TEXT, "
                + COL_USER_EXTRA + " TEXT"
                + ")");

        db.execSQL("CREATE TABLE " + TABLE_GROUPS + " ("
                + COL_GROUP_ID + " TEXT PRIMARY KEY, "
                + COL_GROUP_NAME + " TEXT NOT NULL, "
                + COL_GROUP_PROJECT_TITLE + " TEXT, "
                + COL_GROUP_TECHNOLOGY + " TEXT, "
                + COL_GROUP_GUIDE_NAME + " TEXT, "
                + COL_GROUP_GUIDE_EMAIL + " TEXT, "
                + COL_GROUP_CLASS_CODE + " TEXT NOT NULL, "
                + COL_GROUP_LEADER_EMAIL + " TEXT, "
                + COL_GROUP_STUDENT_EMAILS + " TEXT, "
                + COL_GROUP_GITHUB_LINK + " TEXT, "
                + COL_GROUP_PROGRESS + " INTEGER DEFAULT 0, "
                + "FOREIGN KEY(" + COL_GROUP_LEADER_EMAIL + ") REFERENCES "
                + TABLE_USERS + "(" + COL_USER_EMAIL + ") ON DELETE SET NULL"
                + ")");

        db.execSQL("CREATE TABLE " + TABLE_TASKS + " ("
                + COL_TASK_ID + " TEXT PRIMARY KEY, "
                + COL_TASK_NAME + " TEXT NOT NULL, "
                + COL_TASK_CLASS_CODE + " TEXT NOT NULL, "
                + COL_TASK_GROUP_ID + " TEXT, "
                + COL_TASK_DESCRIPTION + " TEXT, "
                + COL_TASK_DEADLINE + " TEXT, "
                + COL_TASK_ASSIGNED_DATE + " TEXT, "
                + COL_TASK_IS_COMPLETED + " INTEGER DEFAULT 0, "
                + COL_TASK_ADMIN_EMAIL + " TEXT, "
                + "FOREIGN KEY(" + COL_TASK_GROUP_ID + ") REFERENCES "
                + TABLE_GROUPS + "(" + COL_GROUP_ID + ") ON DELETE SET NULL, "
                + "FOREIGN KEY(" + COL_TASK_ADMIN_EMAIL + ") REFERENCES "
                + TABLE_USERS + "(" + COL_USER_EMAIL + ") ON DELETE SET NULL"
                + ")");

        db.execSQL("CREATE INDEX idx_groups_class_code ON " + TABLE_GROUPS + "(" + COL_GROUP_CLASS_CODE + ")");
        db.execSQL("CREATE INDEX idx_tasks_class_code ON " + TABLE_TASKS + "(" + COL_TASK_CLASS_CODE + ")");
        db.execSQL("CREATE INDEX idx_tasks_group_id ON " + TABLE_TASKS + "(" + COL_TASK_GROUP_ID + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GROUPS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }
}
