package com.minigroup.projectprogresstracker.data.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.minigroup.projectprogresstracker.Group;
import com.minigroup.projectprogresstracker.TaskModel;
import com.minigroup.projectprogresstracker.User;

import java.util.ArrayList;
import java.util.List;

/**
 * SQLite Repository using Cursor operations
 * Handles CRUD operations for Users, Groups, and Tasks
 */
@SuppressWarnings("unused")
public class AppRepository {

    private static final String TAG = "AppRepository";
    @SuppressWarnings("FieldCanBeLocal")
    private final AppDatabaseHelper dbHelper;
    private final SQLiteDatabase db;

    public AppRepository(Context context) {
        dbHelper = AppDatabaseHelper.getInstance(context);
        // Force database initialization
        db = dbHelper.getWritableDatabase();
        Log.d(TAG, "Repository initialized and database opened");
    }

    // ==================== USER OPERATIONS ====================

    /**
     * Insert a new user into the database
     */
    public boolean insertUser(User user) {
        if (user == null) {
            Log.e(TAG, "Cannot insert null user");
            return false;
        }

        ContentValues values = new ContentValues();
        values.put(AppDatabaseHelper.COL_USER_EMAIL, user.getEmail());
        values.put(AppDatabaseHelper.COL_USER_NAME, user.getName());
        values.put(AppDatabaseHelper.COL_USER_PASSWORD, user.getPassword());
        values.put(AppDatabaseHelper.COL_USER_ROLE, user.getRole());
        values.put(AppDatabaseHelper.COL_USER_CLASS_CODE, user.getClassCode());
        values.put(AppDatabaseHelper.COL_USER_EXTRA, "");

        long result = db.insert(AppDatabaseHelper.TABLE_USERS, null, values);
        boolean success = result != -1;
        Log.d(TAG, "Insert user: " + user.getEmail() + " - " + (success ? "SUCCESS" : "FAILED"));
        return success;
    }

    /**
     * Get user by email
     */
    public User getUserByEmail(String email) {
        if (email == null || email.isEmpty()) {
            Log.e(TAG, "Email is null or empty");
            return null;
        }

        String selection = AppDatabaseHelper.COL_USER_EMAIL + " = ?";
        Cursor cursor = db.query(
                AppDatabaseHelper.TABLE_USERS,
                null,
                selection,
                new String[]{email},
                null,
                null,
                null
        );

        User user = null;
        if (cursor.moveToFirst()) {
            user = cursorToUser(cursor);
            Log.d(TAG, "User found: " + email);
        } else {
            Log.d(TAG, "User not found: " + email);
        }
        cursor.close();
        return user;
    }

    /**
     * Get all users
     */
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        Cursor cursor = db.query(
                AppDatabaseHelper.TABLE_USERS,
                null,
                null,
                null,
                null,
                null,
                null
        );

        if (cursor.moveToFirst()) {
            do {
                users.add(cursorToUser(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        Log.d(TAG, "Fetched " + users.size() + " users");
        return users;
    }

    /**
     * Update user information
     */
    public boolean updateUser(User user) {
        if (user == null || user.getEmail() == null) {
            Log.e(TAG, "Cannot update null user or user with null email");
            return false;
        }

        ContentValues values = new ContentValues();
        values.put(AppDatabaseHelper.COL_USER_NAME, user.getName());
        values.put(AppDatabaseHelper.COL_USER_PASSWORD, user.getPassword());
        values.put(AppDatabaseHelper.COL_USER_ROLE, user.getRole());
        values.put(AppDatabaseHelper.COL_USER_CLASS_CODE, user.getClassCode());

        int result = db.update(
                AppDatabaseHelper.TABLE_USERS,
                values,
                AppDatabaseHelper.COL_USER_EMAIL + " = ?",
                new String[]{user.getEmail()}
        );

        boolean success = result > 0;
        Log.d(TAG, "Update user: " + user.getEmail() + " - " + (success ? "SUCCESS" : "FAILED"));
        return success;
    }

    /**
     * Delete user by email
     */
    public boolean deleteUser(String email) {
        if (email == null || email.isEmpty()) {
            Log.e(TAG, "Email is null or empty");
            return false;
        }

        int result = db.delete(
                AppDatabaseHelper.TABLE_USERS,
                AppDatabaseHelper.COL_USER_EMAIL + " = ?",
                new String[]{email}
        );

        boolean success = result > 0;
        Log.d(TAG, "Delete user: " + email + " - " + (success ? "SUCCESS" : "FAILED"));
        return success;
    }

    // ==================== GROUP OPERATIONS ====================

    /**
     * Insert a new group
     */
    public boolean insertGroup(Group group) {
        if (group == null) {
            Log.e(TAG, "Cannot insert null group");
            return false;
        }

        ContentValues values = new ContentValues();
        values.put(AppDatabaseHelper.COL_GROUP_ID, group.getGroupId());
        values.put(AppDatabaseHelper.COL_GROUP_NAME, group.getGroupName());
        values.put(AppDatabaseHelper.COL_GROUP_PROJECT_TITLE, group.getProjectTitle());
        values.put(AppDatabaseHelper.COL_GROUP_TECHNOLOGY, group.getTechnology());
        values.put(AppDatabaseHelper.COL_GROUP_GUIDE_NAME, group.getGuideName());
        values.put(AppDatabaseHelper.COL_GROUP_GUIDE_EMAIL, group.getGuideEmail());
        values.put(AppDatabaseHelper.COL_GROUP_CLASS_CODE, group.getClassCode());
        values.put(AppDatabaseHelper.COL_GROUP_LEADER_EMAIL, group.getLeaderEmail());
        // Store student emails as JSON string
        values.put(AppDatabaseHelper.COL_GROUP_STUDENT_EMAILS, convertListToJson(group.getStudentEmails()));
        values.put(AppDatabaseHelper.COL_GROUP_GITHUB_LINK, group.getGithubLink());
        values.put(AppDatabaseHelper.COL_GROUP_PROGRESS, group.getProgress());

        long result = db.insert(AppDatabaseHelper.TABLE_GROUPS, null, values);
        boolean success = result != -1;
        Log.d(TAG, "Insert group: " + group.getGroupId() + " - " + (success ? "SUCCESS" : "FAILED"));
        return success;
    }

    /**
     * Get group by ID
     */
    public Group getGroupById(String groupId) {
        if (groupId == null || groupId.isEmpty()) {
            Log.e(TAG, "Group ID is null or empty");
            return null;
        }

        String selection = AppDatabaseHelper.COL_GROUP_ID + " = ?";
        Cursor cursor = db.query(
                AppDatabaseHelper.TABLE_GROUPS,
                null,
                selection,
                new String[]{groupId},
                null,
                null,
                null
        );

        Group group = null;
        if (cursor.moveToFirst()) {
            group = cursorToGroup(cursor);
            Log.d(TAG, "Group found: " + groupId);
        } else {
            Log.d(TAG, "Group not found: " + groupId);
        }
        cursor.close();
        return group;
    }

    /**
     * Get all groups for a class
     */
    public List<Group> getGroupsByClass(String classCode) {
        List<Group> groups = new ArrayList<>();
        if (classCode == null || classCode.isEmpty()) {
            Log.e(TAG, "Class code is null or empty");
            return groups;
        }

        String selection = AppDatabaseHelper.COL_GROUP_CLASS_CODE + " = ?";
        Cursor cursor = db.query(
                AppDatabaseHelper.TABLE_GROUPS,
                null,
                selection,
                new String[]{classCode},
                null,
                null,
                null
        );

        if (cursor.moveToFirst()) {
            do {
                groups.add(cursorToGroup(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        Log.d(TAG, "Fetched " + groups.size() + " groups for class: " + classCode);
        return groups;
    }

    /**
     * Get all groups
     */
    public List<Group> getAllGroups() {
        List<Group> groups = new ArrayList<>();
        Cursor cursor = db.query(
                AppDatabaseHelper.TABLE_GROUPS,
                null,
                null,
                null,
                null,
                null,
                null
        );

        if (cursor.moveToFirst()) {
            do {
                groups.add(cursorToGroup(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        Log.d(TAG, "Fetched " + groups.size() + " total groups");
        return groups;
    }

    /**
     * Update group
     */
    public boolean updateGroup(Group group) {
        if (group == null || group.getGroupId() == null) {
            Log.e(TAG, "Cannot update null group or group with null ID");
            return false;
        }

        ContentValues values = new ContentValues();
        values.put(AppDatabaseHelper.COL_GROUP_NAME, group.getGroupName());
        values.put(AppDatabaseHelper.COL_GROUP_PROJECT_TITLE, group.getProjectTitle());
        values.put(AppDatabaseHelper.COL_GROUP_TECHNOLOGY, group.getTechnology());
        values.put(AppDatabaseHelper.COL_GROUP_GUIDE_NAME, group.getGuideName());
        values.put(AppDatabaseHelper.COL_GROUP_GUIDE_EMAIL, group.getGuideEmail());
        values.put(AppDatabaseHelper.COL_GROUP_STUDENT_EMAILS, convertListToJson(group.getStudentEmails()));
        values.put(AppDatabaseHelper.COL_GROUP_GITHUB_LINK, group.getGithubLink());
        values.put(AppDatabaseHelper.COL_GROUP_PROGRESS, group.getProgress());

        int result = db.update(
                AppDatabaseHelper.TABLE_GROUPS,
                values,
                AppDatabaseHelper.COL_GROUP_ID + " = ?",
                new String[]{group.getGroupId()}
        );

        boolean success = result > 0;
        Log.d(TAG, "Update group: " + group.getGroupId() + " - " + (success ? "SUCCESS" : "FAILED"));
        return success;
    }

    /**
     * Delete group by ID
     */
    public boolean deleteGroup(String groupId) {
        if (groupId == null || groupId.isEmpty()) {
            Log.e(TAG, "Group ID is null or empty");
            return false;
        }

        int result = db.delete(
                AppDatabaseHelper.TABLE_GROUPS,
                AppDatabaseHelper.COL_GROUP_ID + " = ?",
                new String[]{groupId}
        );

        boolean success = result > 0;
        Log.d(TAG, "Delete group: " + groupId + " - " + (success ? "SUCCESS" : "FAILED"));
        return success;
    }

    // ==================== TASK OPERATIONS ====================

    /**
     * Insert a new task
     */
    public boolean insertTask(TaskModel task) {
        if (task == null) {
            Log.e(TAG, "Cannot insert null task");
            return false;
        }

        ContentValues values = new ContentValues();
        values.put(AppDatabaseHelper.COL_TASK_ID, task.getTaskId());
        values.put(AppDatabaseHelper.COL_TASK_NAME, task.getTaskName());
        values.put(AppDatabaseHelper.COL_TASK_CLASS_CODE, task.getClassCode());
        values.put(AppDatabaseHelper.COL_TASK_GROUP_ID, task.getGroupId());
        values.put(AppDatabaseHelper.COL_TASK_DESCRIPTION, "");
        values.put(AppDatabaseHelper.COL_TASK_DEADLINE, task.getDeadline());
        // Store createdAt timestamp as text for database
        values.put(AppDatabaseHelper.COL_TASK_ASSIGNED_DATE, String.valueOf(task.getCreatedAt()));
        values.put(AppDatabaseHelper.COL_TASK_IS_COMPLETED, task.isCompleted() ? 1 : 0);
        values.put(AppDatabaseHelper.COL_TASK_ADMIN_EMAIL, task.getAssignedBy());

        long result = db.insert(AppDatabaseHelper.TABLE_TASKS, null, values);
        boolean success = result != -1;
        Log.d(TAG, "Insert task: " + task.getTaskId() + " - " + (success ? "SUCCESS" : "FAILED"));
        return success;
    }

    /**
     * Get task by ID
     */
    public TaskModel getTaskById(String taskId) {
        if (taskId == null || taskId.isEmpty()) {
            Log.e(TAG, "Task ID is null or empty");
            return null;
        }

        String selection = AppDatabaseHelper.COL_TASK_ID + " = ?";
        Cursor cursor = db.query(
                AppDatabaseHelper.TABLE_TASKS,
                null,
                selection,
                new String[]{taskId},
                null,
                null,
                null
        );

        TaskModel task = null;
        if (cursor.moveToFirst()) {
            task = cursorToTaskModel(cursor);
            Log.d(TAG, "Task found: " + taskId);
        } else {
            Log.d(TAG, "Task not found: " + taskId);
        }
        cursor.close();
        return task;
    }

    /**
     * Get all tasks for a class
     */
    public List<TaskModel> getTasksByClass(String classCode) {
        List<TaskModel> tasks = new ArrayList<>();
        if (classCode == null || classCode.isEmpty()) {
            Log.e(TAG, "Class code is null or empty");
            return tasks;
        }

        String selection = AppDatabaseHelper.COL_TASK_CLASS_CODE + " = ?";
        Cursor cursor = db.query(
                AppDatabaseHelper.TABLE_TASKS,
                null,
                selection,
                new String[]{classCode},
                null,
                null,
                null
        );

        if (cursor.moveToFirst()) {
            do {
                tasks.add(cursorToTaskModel(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        Log.d(TAG, "Fetched " + tasks.size() + " tasks for class: " + classCode);
        return tasks;
    }

    /**
     * Get all tasks for a group
     */
    public List<TaskModel> getTasksByGroup(String groupId) {
        List<TaskModel> tasks = new ArrayList<>();
        if (groupId == null || groupId.isEmpty()) {
            Log.e(TAG, "Group ID is null or empty");
            return tasks;
        }

        String selection = AppDatabaseHelper.COL_TASK_GROUP_ID + " = ?";
        Cursor cursor = db.query(
                AppDatabaseHelper.TABLE_TASKS,
                null,
                selection,
                new String[]{groupId},
                null,
                null,
                null
        );

        if (cursor.moveToFirst()) {
            do {
                tasks.add(cursorToTaskModel(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        Log.d(TAG, "Fetched " + tasks.size() + " tasks for group: " + groupId);
        return tasks;
    }

    /**
     * Get all tasks
     */
    public List<TaskModel> getAllTasks() {
        List<TaskModel> tasks = new ArrayList<>();
        Cursor cursor = db.query(
                AppDatabaseHelper.TABLE_TASKS,
                null,
                null,
                null,
                null,
                null,
                null
        );

        if (cursor.moveToFirst()) {
            do {
                tasks.add(cursorToTaskModel(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        Log.d(TAG, "Fetched " + tasks.size() + " total tasks");
        return tasks;
    }

    /**
     * Update task
     */
    public boolean updateTask(TaskModel task) {
        if (task == null || task.getTaskId() == null) {
            Log.e(TAG, "Cannot update null task or task with null ID");
            return false;
        }

        ContentValues values = new ContentValues();
        values.put(AppDatabaseHelper.COL_TASK_NAME, task.getTaskName());
        values.put(AppDatabaseHelper.COL_TASK_DEADLINE, task.getDeadline());
        values.put(AppDatabaseHelper.COL_TASK_IS_COMPLETED, task.isCompleted() ? 1 : 0);

        int result = db.update(
                AppDatabaseHelper.TABLE_TASKS,
                values,
                AppDatabaseHelper.COL_TASK_ID + " = ?",
                new String[]{task.getTaskId()}
        );

        boolean success = result > 0;
        Log.d(TAG, "Update task: " + task.getTaskId() + " - " + (success ? "SUCCESS" : "FAILED"));
        return success;
    }

    /**
     * Delete task by ID
     */
    public boolean deleteTask(String taskId) {
        if (taskId == null || taskId.isEmpty()) {
            Log.e(TAG, "Task ID is null or empty");
            return false;
        }

        int result = db.delete(
                AppDatabaseHelper.TABLE_TASKS,
                AppDatabaseHelper.COL_TASK_ID + " = ?",
                new String[]{taskId}
        );

        boolean success = result > 0;
        Log.d(TAG, "Delete task: " + taskId + " - " + (success ? "SUCCESS" : "FAILED"));
        return success;
    }

    // ==================== HELPER METHODS ====================

    /**
     * Convert cursor to User object
     */
    private User cursorToUser(Cursor cursor) {
        User user = new User();
        int emailIndex = cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_USER_EMAIL);
        int nameIndex = cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_USER_NAME);
        int passwordIndex = cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_USER_PASSWORD);
        int roleIndex = cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_USER_ROLE);
        int classCodeIndex = cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_USER_CLASS_CODE);

        user.setEmail(cursor.getString(emailIndex));
        user.setName(cursor.getString(nameIndex));
        user.setPassword(cursor.getString(passwordIndex));
        user.setRole(cursor.getString(roleIndex));
        user.setClassCode(cursor.getString(classCodeIndex));
        return user;
    }

    /**
     * Convert cursor to Group object
     */
    private Group cursorToGroup(Cursor cursor) {
        Group group = new Group();
        int idIndex = cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_GROUP_ID);
        int nameIndex = cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_GROUP_NAME);
        int titleIndex = cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_GROUP_PROJECT_TITLE);
        int techIndex = cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_GROUP_TECHNOLOGY);
        int guideNameIndex = cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_GROUP_GUIDE_NAME);
        int guideEmailIndex = cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_GROUP_GUIDE_EMAIL);
        int classCodeIndex = cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_GROUP_CLASS_CODE);
        int leaderIndex = cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_GROUP_LEADER_EMAIL);
        int studentIndex = cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_GROUP_STUDENT_EMAILS);
        int githubIndex = cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_GROUP_GITHUB_LINK);
        int progressIndex = cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_GROUP_PROGRESS);

        group.setGroupId(cursor.getString(idIndex));
        group.setGroupName(cursor.getString(nameIndex));
        group.setProjectTitle(cursor.getString(titleIndex));
        group.setTechnology(cursor.getString(techIndex));
        group.setGuideName(cursor.getString(guideNameIndex));
        group.setGuideEmail(cursor.getString(guideEmailIndex));
        group.setClassCode(cursor.getString(classCodeIndex));
        group.setLeaderEmail(cursor.getString(leaderIndex));
        group.setStudentEmails(convertJsonToList(cursor.getString(studentIndex)));
        group.setGithubLink(cursor.getString(githubIndex));
        group.setProgress(cursor.getInt(progressIndex));
        return group;
    }

    /**
     * Convert cursor to TaskModel object
     */
    private TaskModel cursorToTaskModel(Cursor cursor) {
        TaskModel task = new TaskModel();
        int idIndex = cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_TASK_ID);
        int nameIndex = cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_TASK_NAME);
        int classIndex = cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_TASK_CLASS_CODE);
        int groupIndex = cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_TASK_GROUP_ID);
        int deadlineIndex = cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_TASK_DEADLINE);
        int dateIndex = cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_TASK_ASSIGNED_DATE);
        int completedIndex = cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_TASK_IS_COMPLETED);
        int adminIndex = cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_TASK_ADMIN_EMAIL);

        task.setTaskId(cursor.getString(idIndex));
        task.setTaskName(cursor.getString(nameIndex));
        task.setClassCode(cursor.getString(classIndex));
        task.setGroupId(cursor.getString(groupIndex));
        task.setDeadline(cursor.getString(deadlineIndex));
        // Use setCreatedAt for the assigned date (timestamp)
        if (!cursor.isNull(dateIndex)) {
            try {
                long timestamp = Long.parseLong(cursor.getString(dateIndex));
                task.setCreatedAt(timestamp);
            } catch (NumberFormatException e) {
                task.setCreatedAt(System.currentTimeMillis());
            }
        }
        task.setCompleted(cursor.getInt(completedIndex) == 1);
        task.setAssignedBy(cursor.getString(adminIndex));
        return task;
    }

    /**
     * Convert ArrayList to JSON string for storage
     */
    private String convertListToJson(ArrayList<String> list) {
        if (list == null || list.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            sb.append("\"").append(list.get(i)).append("\"");
            if (i < list.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Convert JSON string to ArrayList
     */
    private ArrayList<String> convertJsonToList(String json) {
        ArrayList<String> list = new ArrayList<>();
        if (json == null || json.isEmpty() || json.equals("[]")) return list;

        String content = json.replace("[", "").replace("]", "").replace("\"", "");
        if (!content.isEmpty()) {
            String[] items = content.split(",");
            for (String item : items) {
                if (!item.trim().isEmpty()) {
                    list.add(item.trim());
                }
            }
        }
        return list;
    }

    /**
     * Close database connection (optional cleanup)
     */
    public void closeDatabase() {
        if (db != null && db.isOpen()) {
            db.close();
            Log.d(TAG, "Database connection closed");
        }
    }
}

