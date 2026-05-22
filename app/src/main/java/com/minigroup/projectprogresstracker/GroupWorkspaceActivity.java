package com.minigroup.projectprogresstracker;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.text.SimpleDateFormat;

public class GroupWorkspaceActivity extends AppCompatActivity {

    private Group group;
    private User user;
    private String groupId;
    private TaskModel selectedTaskForUpload;
    private BottomSheetDialog currentDialog;
    private ArrayList<Uri> stagedFileUris = new ArrayList<>();
    private ImageButton btnAttachInDialog;

    private TextView workspaceProjectTitle, workspaceGuideName, workspaceProgressPercent, txtGithubDisplay;
    private ProgressBar workspaceProgressBar;
    private ImageButton btnBack, btnEditGithub, btnOpenChatStudent;
    private LinearLayout layoutGithubInput, layoutGithubActive, mainWorkspaceContent, layoutTasksList;
    private EditText etGithubLink;
    private MaterialButton btnSaveGithub, btnOpenGithub, btnSubmitWeeklyReport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_workspace);

        user = SessionManager.getUser(this);
        groupId = getIntent().getStringExtra("GROUP_ID");

        if (!loadGroupData(groupId)) return;

        if (group.getGuideName() == null || group.getGuideName().trim().isEmpty()) {
            Toast.makeText(this, "🔒 Access Restricted: Waiting for Mentor to accept.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        initViews();
        setupUI();
        refreshWorkspaceState();
    }

    public void downloadFileToSystem(String fileUriString, String fileName) {
        if (fileUriString == null || fileUriString.isEmpty() || fileUriString.startsWith("text:") || fileUriString.equals("deleted:true")) {
            return;
        }

        try {
            Uri uri = Uri.parse(fileUriString);
            if (uri.getScheme() != null && uri.getScheme().equals("content")) {
                try {
                    getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } catch (SecurityException e) {
                    Log.d("DOWNLOAD", "Non-persistable URI or permission already held");
                }
            }

            ContentValues values = new ContentValues();
            values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
            values.put(MediaStore.Downloads.MIME_TYPE, "application/octet-stream");
            values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

            Uri downloadUri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
            if (downloadUri != null) {
                try (InputStream in = getContentResolver().openInputStream(uri);
                     OutputStream out = getContentResolver().openOutputStream(downloadUri)) {
                    if (in == null || out == null) throw new Exception("Stream creation failed");
                    byte[] buffer = new byte[8192];
                    int len;
                    while ((len = in.read(buffer)) > 0) {
                        out.write(buffer, 0, len);
                    }
                    Toast.makeText(this, "✅ Downloaded to: Downloads/" + fileName, Toast.LENGTH_LONG).show();
                }
            }
        } catch (Exception e) {
            Log.e("DOWNLOAD_ERROR", "Error: " + e.getMessage());
            Toast.makeText(this, "Download failed", Toast.LENGTH_SHORT).show();
        }
    }

    public void openFile(String fileUriString) {
        if (fileUriString == null || fileUriString.isEmpty() || fileUriString.startsWith("text:") || fileUriString.equals("deleted:true")) {
            Toast.makeText(this, "File no longer available", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            Uri uri = Uri.parse(fileUriString);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            String type = getContentResolver().getType(uri);
            if (type == null) type = "*/*";
            intent.setDataAndType(uri, type);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(Intent.createChooser(intent, "Open file with:"));
        } catch (Exception e) {
            Log.e("VIEW_ERROR", "Error opening file: " + e.getMessage());
            Toast.makeText(this, "Unable to open file", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean loadGroupData(String groupId) {
        if (user == null || groupId == null) {
            Toast.makeText(this, "Session error. Please login again.", Toast.LENGTH_SHORT).show();
            finish();
            return false;
        }
        group = GroupStorage.getGroupById(this, groupId);
        if (group == null) {
            Toast.makeText(this, "Error: Group not found.", Toast.LENGTH_SHORT).show();
            finish();
            return false;
        }
        return true;
    }

    private void initViews() {
        workspaceProjectTitle = findViewById(R.id.workspaceProjectTitle);
        workspaceGuideName = findViewById(R.id.workspaceGuideName);
        workspaceProgressPercent = findViewById(R.id.workspaceProgressPercent);
        workspaceProgressBar = findViewById(R.id.workspaceProgressBar);
        btnBack = findViewById(R.id.btnBackWorkspace);
        btnOpenChatStudent = findViewById(R.id.btnOpenChatStudent);
        layoutGithubInput = findViewById(R.id.layoutGithubInput);
        layoutGithubActive = findViewById(R.id.layoutGithubActive);
        etGithubLink = findViewById(R.id.etGithubLink);
        btnSaveGithub = findViewById(R.id.btnSaveGithub);
        btnOpenGithub = findViewById(R.id.btnOpenGithub);
        btnEditGithub = findViewById(R.id.btnEditGithub);
        txtGithubDisplay = findViewById(R.id.txtGithubDisplay);
        mainWorkspaceContent = findViewById(R.id.mainWorkspaceContent);
        btnSubmitWeeklyReport = findViewById(R.id.btnSubmitWeeklyReport);
        layoutTasksList = findViewById(R.id.layoutTasksList);
    }

    private void setupUI() {
        String title = group.getProjectTitle();
        workspaceProjectTitle.setText(title != null && !title.isEmpty() ? title : "Project Workspace");
        workspaceGuideName.setText("Guide: " + group.getGuideName());
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
        if (btnOpenChatStudent != null) btnOpenChatStudent.setOnClickListener(v -> showGroupChatDialog());

        btnSaveGithub.setOnClickListener(v -> {
            String link = etGithubLink.getText().toString().trim();
            if (link.contains("github.com")) {
                group.setGithubLink(link);
                GroupStorage.updateGroup(this, group);
                refreshWorkspaceState();
                Toast.makeText(this, "Workspace Updated!", Toast.LENGTH_SHORT).show();
                hideKeyboard();
            } else {
                Toast.makeText(this, "Please enter a valid GitHub URL", Toast.LENGTH_SHORT).show();
            }
        });

        btnEditGithub.setOnClickListener(v -> {
            layoutGithubActive.setVisibility(View.GONE);
            layoutGithubInput.setVisibility(View.VISIBLE);
            etGithubLink.setText(group.getGithubLink());
            etGithubLink.requestFocus();
        });

        btnOpenGithub.setOnClickListener(v -> {
            String url = group.getGithubLink();
            if (url != null && !url.isEmpty()) {
                if (!url.startsWith("http://") && !url.startsWith("https://")) url = "https://" + url;
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                try {
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(this, "No browser found to open link", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnSubmitWeeklyReport.setOnClickListener(v -> {
            Intent intent = new Intent(GroupWorkspaceActivity.this, WeeklyReportActivity.class);
            intent.putExtra("GROUP_ID", groupId);
            intent.putExtra("PROJECT_TITLE", group.getProjectTitle());
            if (user != null) intent.putExtra("SUBMITTER_EMAIL", user.getEmail());
            startActivity(intent);
        });
    }

    private void showStudentTaskDialog(TaskModel task) {
        selectedTaskForUpload = task;
        stagedFileUris.clear();
        currentDialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.layout_task_details_dialog, null);
        currentDialog.setContentView(view);

        TextView tvTitle = view.findViewById(R.id.dialogTaskTitle);
        TextView tvStatus = view.findViewById(R.id.tvTaskStatusBadge);
        LinearLayout submissionsList = view.findViewById(R.id.layoutSubmissionsList);
        btnAttachInDialog = view.findViewById(R.id.btnAttachSubTask);
        EditText etInput = view.findViewById(R.id.etTeacherFeedback);
        View btnSend = view.findViewById(R.id.btnSendFeedback);

        tvTitle.setText(task.getTaskName());

        boolean isDone = TaskStorage.isTaskCompletedForGroup(this, task.getTaskId(), groupId);
        tvStatus.setText(isDone ? "COMPLETED" : "SUBMISSION OPEN");

        btnAttachInDialog.setOnClickListener(v -> openFilePicker());

        btnSend.setOnClickListener(v -> {
            String message = etInput.getText().toString().trim();
            boolean hasContent = false;
            if (!message.isEmpty()) {
                saveSubmission("text:" + message, "Message");
                hasContent = true;
            }
            if (!stagedFileUris.isEmpty()) {
                for (Uri uri : stagedFileUris) saveSubmission(uri.toString(), getFileName(uri));
                stagedFileUris.clear();
                btnAttachInDialog.setImageResource(R.drawable.ic_attach);
                hasContent = true;
            }
            if (hasContent) {
                etInput.setText("");
                hideKeyboard();
                loadSubmissionsIntoUI(task, submissionsList);
            }
        });

        loadSubmissionsIntoUI(task, submissionsList);
        currentDialog.show();
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Select Files"), 1001);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) processSelectedUri(data.getClipData().getItemAt(i).getUri());
            } else if (data.getData() != null) {
                processSelectedUri(data.getData());
            }
            if (!stagedFileUris.isEmpty() && btnAttachInDialog != null) btnAttachInDialog.setImageResource(R.drawable.ic_check_circle);
        }
    }

    private void processSelectedUri(Uri fileUri) {
        if (fileUri != null && selectedTaskForUpload != null) {
            try {
                getContentResolver().takePersistableUriPermission(fileUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } catch (Exception e) { Log.e("PERMISSION", "Error"); }
            stagedFileUris.add(fileUri);
        }
    }

    private void saveSubmission(String uri, String fileName) {
        String timestamp = new SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(new Date());
        Submission newSub = new Submission(selectedTaskForUpload.getTaskId(), groupId, user.getEmail(), uri, fileName, timestamp);
        SubmissionStorage.saveSubmission(this, newSub);
    }

    private void loadSubmissionsIntoUI(TaskModel task, LinearLayout container) {
        container.removeAllViews();
        ArrayList<Submission> subs = SubmissionStorage.getSubmissionsByTaskAndGroup(this, task.getTaskId(), groupId);

        if (subs.isEmpty()) {
            TextView noData = new TextView(this);
            noData.setText("No activity yet.");
            noData.setPadding(20, 40, 20, 40);
            noData.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            container.addView(noData);
            return;
        }

        for (Submission sub : subs) {
            View row;
            boolean isTeacher = sub.getStudentEmail().equalsIgnoreCase(group.getGuideEmail());
            boolean isDeleted = sub.getFileUri().equals("deleted:true");

            if (isTeacher) {
                row = getLayoutInflater().inflate(R.layout.item_submission_row_teacher, container, false);
                TextView tvFile = row.findViewById(R.id.tvFileNameTeacher);
                TextView tvInfo = row.findViewById(R.id.tvFileInfoTeacher);
                TextView tvDate = row.findViewById(R.id.tvUploadDateTimeTeacher);

                if (isDeleted) {
                    tvFile.setText("Deleted Message");
                    tvFile.setAlpha(0.5f);
                } else if (sub.getFileUri().startsWith("text:")) {
                    tvFile.setText(sub.getFileUri().substring(5));
                } else {
                    tvFile.setText(sub.getFileName());
                }
                tvInfo.setText("From: Mentor / Guide");
                tvDate.setText(sub.getTimestamp().replace(", ", "\n"));
            } else {
                row = getLayoutInflater().inflate(R.layout.item_submission_row, container, false);
                TextView tvFile = row.findViewById(R.id.tvFileName);
                TextView tvInfo = row.findViewById(R.id.tvFileInfo);
                TextView tvDate = row.findViewById(R.id.tvUploadDateTime);
                ImageView ivIcon = row.findViewById(R.id.ivFileIcon);

                if (isDeleted) {
                    ivIcon.setImageResource(R.drawable.ic_message);
                    tvFile.setText("Deleted Message");
                    tvFile.setTypeface(null, android.graphics.Typeface.ITALIC);
                    tvFile.setAlpha(0.5f);
                } else if (sub.getFileUri().startsWith("text:")) {
                    ivIcon.setImageResource(R.drawable.ic_message);
                    tvFile.setText(sub.getFileUri().substring(5));
                } else {
                    ivIcon.setImageResource(R.drawable.ic_file);
                    tvFile.setText(sub.getFileName());
                }
                tvInfo.setText("By: " + sub.getStudentEmail());
                tvDate.setText(sub.getTimestamp().replace(", ", "\n"));
            }

            if (!isDeleted) {
                row.setOnClickListener(v -> showOptionsDialog(sub, task, container));
            }
            container.addView(row);
        }
    }

    private void showOptionsDialog(Submission sub, TaskModel task, LinearLayout container) {
        if (sub.getFileUri().equals("deleted:true")) return;

        ArrayList<String> optionsList = new ArrayList<>();

        if (!sub.getFileUri().startsWith("text:")) {
            optionsList.add("View");
            optionsList.add("Download");
        }

        if (sub.getStudentEmail().equalsIgnoreCase(user.getEmail())) {
            optionsList.add("Delete");
        }

        final String[] options = optionsList.toArray(new String[0]);
        if (options.length == 0) return;

        new MaterialAlertDialogBuilder(this)
                .setTitle(sub.getFileName())
                .setItems(options, (dialog, which) -> {
                    String selectedOption = options[which];
                    if (selectedOption.equals("View")) openFile(sub.getFileUri());
                    else if (selectedOption.equals("Download")) downloadFileToSystem(sub.getFileUri(), "Submission_" + sub.getFileName());
                    else if (selectedOption.equals("Delete")) deleteSubmission(sub, task, container);
                }).show();
    }

    private void deleteSubmission(Submission sub, TaskModel task, LinearLayout container) {
        if (!sub.getStudentEmail().equalsIgnoreCase(user.getEmail())) return;

        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete Submission?")
                .setMessage("This will hide the content but keep the record in chat.")
                .setPositiveButton("Delete", (dialog, i) -> {
                    sub.setFileUri("deleted:true");
                    sub.setFileName("Deleted Message");
                    SubmissionStorage.updateSubmission(this, sub);
                    Toast.makeText(this, "Message Deleted", Toast.LENGTH_SHORT).show();
                    loadSubmissionsIntoUI(task, container);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) result = cursor.getString(nameIndex);
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) result = result.substring(cut + 1);
        }
        return result;
    }

    private void refreshWorkspaceState() {
        if (group == null) return;
        boolean hasGithub = group.getGithubLink() != null && !group.getGithubLink().isEmpty();
        layoutGithubInput.setVisibility(hasGithub ? View.GONE : View.VISIBLE);
        layoutGithubActive.setVisibility(hasGithub ? View.VISIBLE : View.GONE);
        mainWorkspaceContent.setVisibility(hasGithub ? View.VISIBLE : View.GONE);
        if (hasGithub) {
            txtGithubDisplay.setText(group.getGithubLink());
            if (btnSubmitWeeklyReport != null) btnSubmitWeeklyReport.setVisibility(View.VISIBLE);
            displayTasks();
        }
        calculateProgress();
    }

    private void displayTasks() {
        if (layoutTasksList == null || user == null || group == null) return;
        layoutTasksList.removeAllViews();
        ArrayList<TaskModel> allTasks = TaskStorage.getClassTasks(this, user.getClassCode());
        if (allTasks == null) return;

        LayoutInflater inflater = LayoutInflater.from(this);
        for (TaskModel task : allTasks) {
            View taskView = inflater.inflate(R.layout.item_task_status, layoutTasksList, false);
            TextView tvTitle = taskView.findViewById(R.id.tvTaskTitle);
            CheckBox cbStatus = taskView.findViewById(R.id.cbTaskStatus);
            MaterialButton btnOpen = taskView.findViewById(R.id.btnOpenTask);

            tvTitle.setText(task.getTaskName());

            boolean isDone = TaskStorage.isTaskCompletedForGroup(this, task.getTaskId(), groupId);
            cbStatus.setChecked(isDone);

            cbStatus.setEnabled(false);
            btnOpen.setOnClickListener(v -> showStudentTaskDialog(task));
            layoutTasksList.addView(taskView);
        }
    }

    private void showGroupChatDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_group_analysis);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.95);
            int height = (int) (getResources().getDisplayMetrics().heightPixels * 0.90);
            dialog.getWindow().setLayout(width, height);
        }

        TextView title = dialog.findViewById(R.id.analysisTitle);
        TextView bigPercent = dialog.findViewById(R.id.txtBigPercentage);
        ProgressBar circularProgress = dialog.findViewById(R.id.circularProgress);
        LinearLayout taskContainer = dialog.findViewById(R.id.taskContainer);
        LinearLayout memberContainer = dialog.findViewById(R.id.memberContainer);

        ArrayList<TaskModel> classTasks = TaskStorage.getClassTasks(this, group.getClassCode());
        int completedCount = 0;
        if (classTasks != null) {
            for (TaskModel t : classTasks) {
                if (TaskStorage.isTaskCompletedForGroup(this, t.getTaskId(), group.getGroupId())) {
                    completedCount++;
                }
            }
        }

        int totalTasks = (classTasks != null) ? classTasks.size() : 0;
        int progress = (totalTasks == 0) ? 0 : (int) (((float) completedCount / totalTasks) * 100);
        int pendingCount = totalTasks - completedCount;

        title.setText(group.getGroupName());
        bigPercent.setText(progress + "%");
        circularProgress.setProgress(progress);

        View statPending = dialog.findViewById(R.id.statPending);
        if (statPending != null) {
            ((TextView) statPending.findViewById(R.id.statLabel)).setText("Pending");
            ((TextView) statPending.findViewById(R.id.statValue)).setText(String.valueOf(pendingCount));
        }

        View statCompleted = dialog.findViewById(R.id.statCompleted);
        if (statCompleted != null) {
            ((TextView) statCompleted.findViewById(R.id.statLabel)).setText("Completed");
            ((TextView) statCompleted.findViewById(R.id.statValue)).setText(String.valueOf(completedCount));
        }

        if (memberContainer != null) {
            memberContainer.removeAllViews();
            ArrayList<User> members = group.getMembers(this);
            if (members != null) {
                for (User member : members) {
                    View mView = getLayoutInflater().inflate(R.layout.item_member_analysis, memberContainer, false);
                    ((TextView) mView.findViewById(R.id.memberName)).setText(member.getName());
                    ((TextView) mView.findViewById(R.id.memberEmail)).setText(member.getEmail());
                    memberContainer.addView(mView);
                }
            }
        }

        if (taskContainer != null && classTasks != null) {
            taskContainer.removeAllViews();
            for (TaskModel task : classTasks) {
                View tView = getLayoutInflater().inflate(R.layout.item_task_analysis_row, taskContainer, false);
                ((TextView) tView.findViewById(R.id.taskName)).setText(task.getTaskName());

                ImageView icon = tView.findViewById(R.id.statusIcon);
                boolean isDone = TaskStorage.isTaskCompletedForGroup(this, task.getTaskId(), group.getGroupId());
                icon.setImageTintList(ColorStateList.valueOf(isDone ? Color.parseColor("#10B981") : Color.parseColor("#F59E0B")));

                taskContainer.addView(tView);
            }
        }

        dialog.findViewById(R.id.btnCloseAnalysis).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void calculateProgress() {
        if (user == null || group == null) return;
        ArrayList<TaskModel> allTasks = TaskStorage.getClassTasks(this, user.getClassCode());
        if (allTasks == null || allTasks.isEmpty()) return;

        int completed = 0;
        for (TaskModel t : allTasks) {
            if (TaskStorage.isTaskCompletedForGroup(this, t.getTaskId(), groupId)) {
                completed++;
            }
        }
        updateProgressUI((int) (((float) completed / allTasks.size()) * 100));
    }

    private void updateProgressUI(int percentage) {
        if (workspaceProgressBar != null) workspaceProgressBar.setProgress(percentage);
        if (workspaceProgressPercent != null) workspaceProgressPercent.setText(percentage + "%");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (groupId != null) {
            group = GroupStorage.getGroupById(this, groupId);
            refreshWorkspaceState();
        }
    }
}