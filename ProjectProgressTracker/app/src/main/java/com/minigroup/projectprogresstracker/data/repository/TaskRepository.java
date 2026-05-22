package com.minigroup.projectprogresstracker.data.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.minigroup.projectprogresstracker.TaskModel;
import com.minigroup.projectprogresstracker.data.local.AppDatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class TaskRepository {

    private final AppDatabaseHelper dbHelper;

    public TaskRepository(Context context) {
        this.dbHelper = AppDatabaseHelper.getInstance(context);
    }

    public long insert(TaskModel task) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.insert(AppDatabaseHelper.TABLE_TASKS, null, toValues(task));
    }

    public long insertData(TaskModel task) {
        return insert(task);
    }

    public List<TaskModel> getAll() {
        ArrayList<TaskModel> tasks = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(AppDatabaseHelper.TABLE_TASKS, null, null, null, null, null,
                    AppDatabaseHelper.COL_TASK_ASSIGNED_DATE + " DESC");
            while (cursor.moveToNext()) {
                tasks.add(fromCursor(cursor));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return tasks;
    }

    public List<TaskModel> getAllData() {
        return getAll();
    }

    public List<TaskModel> getByClassCode(String classCode) {
        ArrayList<TaskModel> tasks = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(
                    AppDatabaseHelper.TABLE_TASKS,
                    null,
                    AppDatabaseHelper.COL_TASK_CLASS_CODE + "=?",
                    new String[]{classCode},
                    null,
                    null,
                    AppDatabaseHelper.COL_TASK_ASSIGNED_DATE + " DESC"
            );
            while (cursor.moveToNext()) {
                tasks.add(fromCursor(cursor));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return tasks;
    }

    public TaskModel getById(String taskId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(
                    AppDatabaseHelper.TABLE_TASKS,
                    null,
                    AppDatabaseHelper.COL_TASK_ID + "=?",
                    new String[]{taskId},
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

    public int update(TaskModel task) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.update(
                AppDatabaseHelper.TABLE_TASKS,
                toValues(task),
                AppDatabaseHelper.COL_TASK_ID + "=?",
                new String[]{task.getTaskId()}
        );
    }

    public int updateData(TaskModel task) {
        return update(task);
    }

    public int delete(String taskId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(
                AppDatabaseHelper.TABLE_TASKS,
                AppDatabaseHelper.COL_TASK_ID + "=?",
                new String[]{taskId}
        );
    }

    public int deleteData(String taskId) {
        return delete(taskId);
    }

    private ContentValues toValues(TaskModel task) {
        ContentValues values = new ContentValues();
        values.put(AppDatabaseHelper.COL_TASK_ID, task.getTaskId());
        values.put(AppDatabaseHelper.COL_TASK_NAME, task.getTaskName());
        values.put(AppDatabaseHelper.COL_TASK_CLASS_CODE, task.getClassCode());

        if (task.getGroupId() == null) {
            values.putNull(AppDatabaseHelper.COL_TASK_GROUP_ID);
        } else {
            values.put(AppDatabaseHelper.COL_TASK_GROUP_ID, task.getGroupId());
        }

        values.put(AppDatabaseHelper.COL_TASK_DESCRIPTION, "Class Task");
        values.put(AppDatabaseHelper.COL_TASK_DEADLINE, task.getDeadline());
        values.put(AppDatabaseHelper.COL_TASK_ASSIGNED_DATE, task.getAssignedDate());
        values.put(AppDatabaseHelper.COL_TASK_IS_COMPLETED, task.isCompleted() ? 1 : 0);
        values.put(AppDatabaseHelper.COL_TASK_ADMIN_EMAIL, task.getAssignedBy());
        return values;
    }

    private TaskModel fromCursor(Cursor cursor) {
        TaskModel taskModel = new TaskModel();
        taskModel.setTaskId(cursor.getString(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_TASK_ID)));
        taskModel.setTaskName(cursor.getString(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_TASK_NAME)));
        taskModel.setClassCode(cursor.getString(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_TASK_CLASS_CODE)));
        taskModel.setGroupId(cursor.getString(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_TASK_GROUP_ID)));
        taskModel.setDeadline(cursor.getString(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_TASK_DEADLINE)));
        taskModel.setCurrentStatus(
                cursor.getInt(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_TASK_IS_COMPLETED)) == 1
                        ? "COMPLETED"
                        : "TODO"
        );
        taskModel.setCompleted(cursor.getInt(
                cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_TASK_IS_COMPLETED)) == 1);
        String adminEmail = cursor.getString(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_TASK_ADMIN_EMAIL));
        taskModel.setAssignedBy(adminEmail == null ? "Admin" : adminEmail);
        return taskModel;
    }
}
