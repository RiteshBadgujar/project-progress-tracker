package com.minigroup.projectprogresstracker;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.minigroup.projectprogresstracker.data.repository.TaskRepository;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskManagementActivity extends AppCompatActivity {

    private static final String TAG = "TaskManagementSQLite";

    private TaskRepository taskRepository;
    private TaskCursorAdapter adapter;
    private ExecutorService dbExecutor;

    private EditText etTaskName;
    private EditText etClassCode;
    private EditText etDeadline;
    private Button btnInsert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_management);

        taskRepository = new TaskRepository(this);
        dbExecutor = Executors.newSingleThreadExecutor();
        adapter = new TaskCursorAdapter();

        etTaskName = findViewById(R.id.etTaskName);
        etClassCode = findViewById(R.id.etClassCode);
        etDeadline = findViewById(R.id.etDeadline);
        btnInsert = findViewById(R.id.btnInsertTask);

        RecyclerView recyclerView = findViewById(R.id.rvTasks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        btnInsert.setOnClickListener(v -> insertTaskAndRefresh());

        fetchTasksAndRefreshUi();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbExecutor != null) {
            dbExecutor.shutdown();
        }
    }

    private void insertTaskAndRefresh() {
        final String taskName = etTaskName.getText().toString().trim();
        final String classCode = etClassCode.getText().toString().trim();
        final String deadline = etDeadline.getText().toString().trim();

        if (taskName.isEmpty() || classCode.isEmpty() || deadline.isEmpty()) {
            Toast.makeText(this, "Task name, class code and deadline are required.", Toast.LENGTH_SHORT).show();
            return;
        }

        dbExecutor.execute(() -> {
            TaskModel task = new TaskModel(taskName, classCode, deadline);
            long insertResult = taskRepository.insertData(task);

            Log.d(TAG, "insertData() result = " + insertResult);
            if (insertResult == -1L) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Insert failed", Toast.LENGTH_SHORT).show());
                return;
            }

            List<TaskModel> updatedTasks = taskRepository.getByClassCode(classCode);
            Log.d(TAG, "getByClassCode() count = " + updatedTasks.size());
            for (TaskModel item : updatedTasks) {
                Log.d(TAG, "Task -> id=" + item.getTaskId()
                        + ", title=" + item.getTaskName()
                        + ", deadline=" + item.getDeadline());
            }

            runOnUiThread(() -> {
                adapter.submitList(updatedTasks);
                adapter.notifyDataSetChanged();
                Toast.makeText(this, "Task inserted and list updated", Toast.LENGTH_SHORT).show();
                clearInputs();
            });
        });
    }

    private void fetchTasksAndRefreshUi() {
        final String classCode = etClassCode.getText().toString().trim();
        dbExecutor.execute(() -> {
            List<TaskModel> tasks = classCode.isEmpty()
                    ? taskRepository.getAllData()
                    : taskRepository.getByClassCode(classCode);

            Log.d(TAG, "fetchTasksAndRefreshUi() count = " + tasks.size());

            runOnUiThread(() -> {
                adapter.submitList(tasks);
                adapter.notifyDataSetChanged();
            });
        });
    }

    private void clearInputs() {
        etTaskName.setText("");
        etDeadline.setText("");
    }
}