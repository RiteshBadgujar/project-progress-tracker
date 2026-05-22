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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class TeacherGroupWorkspaceActivity extends AppCompatActivity {

    private static final int PICK_FILE_REQUEST = 101;
    private TaskModel currentReviewingTask;
    private LinearLayout currentSubmissionsContainer;
    private ArrayList<Uri> stagedFileUris = new ArrayList<>();
    private ImageButton btnAttachInDialog;

    private Group group;
    private String groupId;
    private User teacherUser;

    private TextView tvProjectTitle, tvGroupName, tvProgressPercent;
    private TextInputEditText etGithubLink;
    private ProgressBar progressBar;
    private LinearLayout layoutTasksList;
    private ImageButton btnBack, btnOpenGroupChat;
    private MaterialButton btnOpenGithub, btnViewReports;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_group_workspace);

        teacherUser = SessionManager.getUser(this);
        groupId = getIntent().getStringExtra("GROUP_ID");

        if (groupId == null) {
            Toast.makeText(this, "Error: Group data missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        loadData();
        setupListeners();
    }

    private void initViews() {
        tvProjectTitle = findViewById(R.id.tvWorkspaceProjectTitle);
        tvGroupName = findViewById(R.id.tvWorkspaceGroupName);
        tvProgressPercent = findViewById(R.id.tvWorkspaceProgressPercent);
        etGithubLink = findViewById(R.id.etGithubLinkDisplay);
        progressBar = findViewById(R.id.workspaceProgressBar);
        layoutTasksList = findViewById(R.id.layoutTasksListTeacher);
        btnBack = findViewById(R.id.btnBackTeacherWorkspace);
        btnOpenGroupChat = findViewById(R.id.btnOpenGroupChat);
        btnOpenGithub = findViewById(R.id.btnOpenGithubTeacher);
        btnViewReports = findViewById(R.id.btnViewWeeklyReports);
    }

    private void loadData() {
        group = GroupStorage.getGroupById(this, groupId);
        if (group == null) {
            Toast.makeText(this, "Group not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvProjectTitle.setText(group.getProjectTitle());
        tvGroupName.setText("Group: " + group.getGroupName());

        String link = group.getGithubLink();
        if (link != null && !link.isEmpty()) {
            etGithubLink.setText(link);
            btnOpenGithub.setEnabled(true);
            btnOpenGithub.setAlpha(1.0f);
        } else {
            etGithubLink.setText("Not Linked");
            btnOpenGithub.setEnabled(false);
            btnOpenGithub.setAlpha(0.5f);
        }

        displayTasksAndCalculateProgress();
    }

    private void displayTasksAndCalculateProgress() {
        if (layoutTasksList == null || group == null) return;
        layoutTasksList.removeAllViews();

        ArrayList<TaskModel> classTasks = TaskStorage.getClassTasks(this, group.getClassCode());

        if (classTasks != null && !classTasks.isEmpty()) {
            LayoutInflater inflater = LayoutInflater.from(this);

            for (TaskModel task : classTasks) {
                View taskView = inflater.inflate(R.layout.item_task_status, layoutTasksList, false);

                TextView title = taskView.findViewById(R.id.tvTaskTitle);
                CheckBox cbStatus = taskView.findViewById(R.id.cbTaskStatus);
                MaterialButton btnOpen = taskView.findViewById(R.id.btnOpenTask);

                title.setText(task.getTaskName());

                boolean isCompleted = TaskStorage.isTaskCompletedForGroup(this, task.getTaskId(), groupId);
                cbStatus.setChecked(isCompleted);
                cbStatus.setEnabled(true);

                // Updated: Logic to show warning before toggling status
                cbStatus.setOnClickListener(v -> {
                    boolean isNowChecked = cbStatus.isChecked();

                    // Revert visual state immediately; update only after confirmation
                    cbStatus.setChecked(!isNowChecked);

                    String action = isNowChecked ? "Complete" : "Incomplete";
                    new MaterialAlertDialogBuilder(this)
                            .setTitle("Update Task Status?")
                            .setMessage("Are you sure you want to mark '" + task.getTaskName() + "' as " + action + "?")
                            .setPositiveButton("Confirm", (dialog, which) -> {
                                // Update Storage and UI
                                TaskStorage.updateGroupTaskStatus(this, task.getTaskId(), groupId, isNowChecked);
                                cbStatus.setChecked(isNowChecked);
                                updateProgressUI();
                                Toast.makeText(this, "Status updated to " + action, Toast.LENGTH_SHORT).show();
                            })
                            .setNegativeButton("Cancel", (dialog, which) -> {
                                dialog.dismiss();
                            })
                            .show();
                });

                btnOpen.setOnClickListener(v -> showTeacherTaskReviewDialog(task));

                layoutTasksList.addView(taskView);
            }
        }
        updateProgressUI();
    }

    private void updateProgressUI() {
        ArrayList<TaskModel> classTasks = TaskStorage.getClassTasks(this, group.getClassCode());
        int total = classTasks != null ? classTasks.size() : 0;
        int completed = 0;
        if (classTasks != null) {
            for (TaskModel t : classTasks) {
                if (TaskStorage.isTaskCompletedForGroup(this, t.getTaskId(), groupId)) completed++;
            }
        }

        int progress = (total == 0) ? 0 : (int) (((float) completed / total) * 100);
        if (progressBar != null) progressBar.setProgress(progress);
        if (tvProgressPercent != null) tvProgressPercent.setText(progress + "%");
    }

    private void showTeacherTaskReviewDialog(TaskModel task) {
        currentReviewingTask = task;
        stagedFileUris.clear();

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.layout_task_details_dialog, null);
        dialog.setContentView(view);

        TextView tvTitle = view.findViewById(R.id.dialogTaskTitle);
        TextView tvStatus = view.findViewById(R.id.tvTaskStatusBadge);
        currentSubmissionsContainer = view.findViewById(R.id.layoutSubmissionsList);
        EditText etFeedback = view.findViewById(R.id.etTeacherFeedback);
        View btnSend = view.findViewById(R.id.btnSendFeedback);
        btnAttachInDialog = view.findViewById(R.id.btnAttachSubTask);

        tvTitle.setText(task.getTaskName());
        boolean isCompleted = TaskStorage.isTaskCompletedForGroup(this, task.getTaskId(), groupId);
        tvStatus.setText(isCompleted ? "COMPLETED" : "PENDING REVIEW");

        if (btnAttachInDialog != null) {
            btnAttachInDialog.setOnClickListener(v -> openFilePicker());
        }

        btnSend.setOnClickListener(v -> {
            String feedback = etFeedback.getText().toString().trim();
            boolean hasSentSomething = false;

            if (!feedback.isEmpty()) {
                saveTeacherSubmission("text:" + feedback, "Feedback Message");
                hasSentSomething = true;
            }

            if (!stagedFileUris.isEmpty()) {
                for (Uri uri : stagedFileUris) {
                    saveTeacherSubmission(uri.toString(), FileUtils.getFileName(this, uri));
                }
                stagedFileUris.clear();
                btnAttachInDialog.setImageResource(R.drawable.ic_attach);
                hasSentSomething = true;
            }

            if (hasSentSomething) {
                etFeedback.setText("");
                hideKeyboard(etFeedback);
                loadSubmissionsIntoUI(task, currentSubmissionsContainer);
            } else {
                Toast.makeText(this, "Please enter a message or attach a file", Toast.LENGTH_SHORT).show();
            }
        });

        loadSubmissionsIntoUI(task, currentSubmissionsContainer);
        dialog.show();
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Select Files to Send"), PICK_FILE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK && data != null) {
            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    processPickedUri(data.getClipData().getItemAt(i).getUri());
                }
            } else if (data.getData() != null) {
                processPickedUri(data.getData());
            }

            if (!stagedFileUris.isEmpty() && btnAttachInDialog != null) {
                btnAttachInDialog.setImageResource(R.drawable.ic_check_circle);
            }
        }
    }

    private void processPickedUri(Uri uri) {
        if (uri != null) {
            try {
                getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } catch (SecurityException e) {
                Log.d("PERMISSION", "URI not persistable");
            }
            stagedFileUris.add(uri);
        }
    }

    private void saveTeacherSubmission(String uri, String fileName) {
        if (currentReviewingTask == null) return;

        Submission teacherSubmission = new Submission(
                currentReviewingTask.getTaskId(),
                groupId,
                teacherUser.getEmail(),
                uri,
                fileName,
                getCurrentTimestamp()
        );
        SubmissionStorage.saveSubmission(this, teacherSubmission);
    }

    private String getCurrentTimestamp() {
        return new SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(new Date());
    }

    private void loadSubmissionsIntoUI(TaskModel task, LinearLayout container) {
        container.removeAllViews();
        ArrayList<Submission> subs = SubmissionStorage.getSubmissionsByTaskAndGroup(this, task.getTaskId(), groupId);

        if (subs == null || subs.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("No history available.");
            empty.setPadding(20, 30, 20, 30);
            container.addView(empty);
            return;
        }

        for (Submission sub : subs) {
            boolean isFromTeacher = sub.getStudentEmail().equalsIgnoreCase(teacherUser.getEmail());
            int layoutId = isFromTeacher ? R.layout.item_submission_row_teacher : R.layout.item_submission_row;

            View row = getLayoutInflater().inflate(layoutId, container, false);
            boolean isDeleted = sub.getFileUri().startsWith("deleted:");

            if (isFromTeacher) {
                TextView tvFile = row.findViewById(R.id.tvFileNameTeacher);
                ImageView ivIcon = row.findViewById(R.id.ivFileIconTeacher);

                if (isDeleted) {
                    tvFile.setText("deleted Message");
                    tvFile.setAlpha(0.5f);
                    if (ivIcon != null) ivIcon.setAlpha(0.3f);
                } else if (sub.getFileUri().startsWith("text:")) {
                    tvFile.setText(sub.getFileUri().substring(5));
                    if (ivIcon != null) ivIcon.setImageResource(R.drawable.ic_teacher_feedback);
                } else {
                    tvFile.setText(sub.getFileName());
                    if (ivIcon != null) ivIcon.setImageResource(R.drawable.ic_teacher_feedback);
                }
                ((TextView) row.findViewById(R.id.tvUploadDateTimeTeacher)).setText(sub.getTimestamp());
            } else {
                TextView tvFile = row.findViewById(R.id.tvFileName);
                ImageView ivIcon = row.findViewById(R.id.ivFileIcon);

                if (isDeleted) {
                    tvFile.setText("This message was deleted");
                    tvFile.setAlpha(0.5f);
                    if (ivIcon != null) ivIcon.setAlpha(0.3f);
                } else if (sub.getFileUri().startsWith("text:")) {
                    tvFile.setText(sub.getFileUri().substring(5));
                    ivIcon.setImageResource(R.drawable.ic_message);
                } else {
                    tvFile.setText(sub.getFileName());
                    ivIcon.setImageResource(R.drawable.ic_file);
                }
                ((TextView) row.findViewById(R.id.tvFileInfo)).setText("By: " + sub.getStudentEmail());
                ((TextView) row.findViewById(R.id.tvUploadDateTime)).setText(sub.getTimestamp());
            }

            if (!isDeleted) {
                row.setOnClickListener(v -> showFileOptions(sub));
            } else {
                row.setOnClickListener(null);
            }

            container.addView(row);
        }
    }

    private void openFile(String uriString) {
        if (uriString == null || uriString.isEmpty() || uriString.startsWith("text:") || uriString.startsWith("deleted:")) return;
        try {
            Uri uri = Uri.parse(uriString);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            String type = getContentResolver().getType(uri);
            if (type == null) type = "*/*";
            intent.setDataAndType(uri, type);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Open file with:"));
        } catch (Exception e) {
            Toast.makeText(this, "Cannot open file", Toast.LENGTH_SHORT).show();
        }
    }

    public void downloadFileToSystem(String fileUriString, String fileName) {
        if (fileUriString == null || fileUriString.isEmpty() || fileUriString.startsWith("text:") || fileUriString.startsWith("deleted:")) return;
        try {
            Uri uri = Uri.parse(fileUriString);
            ContentValues values = new ContentValues();
            values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
            String mimeType = getContentResolver().getType(uri);
            values.put(MediaStore.Downloads.MIME_TYPE, mimeType != null ? mimeType : "application/octet-stream");
            values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

            Uri downloadUri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
            if (downloadUri != null) {
                try (InputStream in = getContentResolver().openInputStream(uri);
                     OutputStream out = getContentResolver().openOutputStream(downloadUri)) {
                    byte[] buffer = new byte[8192];
                    int len;
                    while ((len = in.read(buffer)) > 0) out.write(buffer, 0, len);
                    Toast.makeText(this, "✅ Downloaded to Downloads folder", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Download failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void showFileOptions(Submission sub) {
        ArrayList<String> optionsList = new ArrayList<>();

        if (!sub.getFileUri().startsWith("text:")) {
            optionsList.add("View");
            optionsList.add("Download");
        }

        if (sub.getStudentEmail().equalsIgnoreCase(teacherUser.getEmail())) {
            optionsList.add("Delete");
        }

        if (optionsList.isEmpty()) return;

        String[] options = optionsList.toArray(new String[0]);

        new MaterialAlertDialogBuilder(this)
                .setTitle(sub.getFileUri().startsWith("text:") ? "Message Options" : sub.getFileName())
                .setItems(options, (dialog, which) -> {
                    String choice = options[which];
                    if (choice.equals("View")) {
                        openFile(sub.getFileUri());
                    } else if (choice.equals("Download")) {
                        downloadFileToSystem(sub.getFileUri(), "Doc_" + sub.getFileName());
                    } else if (choice.equals("Delete")) {
                        confirmDeletion(sub);
                    }
                }).show();
    }

    private void confirmDeletion(Submission sub) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Confirm Delete")
                .setMessage("Are you sure you want to delete this message?")
                .setPositiveButton("Delete", (d, i) -> {
                    sub.setFileUri("deleted:" + sub.getFileUri());
                    SubmissionStorage.updateSubmission(this, sub);
                    loadSubmissionsIntoUI(currentReviewingTask, currentSubmissionsContainer);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void setupListeners() {
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
        btnOpenGithub.setOnClickListener(v -> {
            if (group != null && group.getGithubLink() != null && !group.getGithubLink().isEmpty()) {
                String url = group.getGithubLink();
                if (!url.startsWith("http://") && !url.startsWith("https://")) url = "https://" + url;
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(Intent.createChooser(intent, "Open GitHub"));
                } catch (Exception e) {
                    Toast.makeText(this, "Invalid link", Toast.LENGTH_SHORT).show();
                }
            }
        });
        btnViewReports.setOnClickListener(v -> showReportsPopup());

        // Open Group Analysis Dialog
        if (btnOpenGroupChat != null) {
            btnOpenGroupChat.setOnClickListener(v -> {
                if (group != null) {
                    showGroupAnalysisDialog(group);
                }
            });
        }
    }

    private void showGroupAnalysisDialog(Group group) {
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

        updateStatCard(dialog, R.id.statPending, "Pending", String.valueOf(pendingCount));
        updateStatCard(dialog, R.id.statCompleted, "Completed", String.valueOf(completedCount));

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

    private void updateStatCard(Dialog d, int resId, String label, String value) {
        View card = d.findViewById(resId);
        if (card != null) {
            TextView labelTv = card.findViewById(R.id.statLabel);
            TextView valueTv = card.findViewById(R.id.statValue);
            if (labelTv != null) labelTv.setText(label);
            if (valueTv != null) valueTv.setText(value);
        }
    }

    private void showReportsPopup() {
        BottomSheetDialog reportsDialog = new BottomSheetDialog(this);
        View reportsView = getLayoutInflater().inflate(R.layout.layout_reports_popup, null);
        reportsDialog.setContentView(reportsView);
        RecyclerView rvReports = reportsView.findViewById(R.id.rvReportsList);
        rvReports.setLayoutManager(new LinearLayoutManager(this));
        ArrayList<WeeklyReport> reportsList = ReportStorage.getReportsByGroupId(this, group.getGroupId());
        if (reportsList != null && !reportsList.isEmpty()) {
            rvReports.setAdapter(new ReportsAdapter(reportsList));
            reportsDialog.show();
        }
    }

    private void openPdf(String filePath) {
        if (filePath == null || filePath.isEmpty()) return;
        File file = new File(filePath);
        if (!file.exists()) return;
        try {
            Uri pdfUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(pdfUri, "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Error opening PDF", Toast.LENGTH_SHORT).show();
        }
    }

    private void hideKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    private class ReportsAdapter extends RecyclerView.Adapter<ReportsAdapter.ViewHolder> {
        private ArrayList<WeeklyReport> list;
        ReportsAdapter(ArrayList<WeeklyReport> list) { this.list = list; }
        @Override public ViewHolder onCreateViewHolder(ViewGroup p, int vt) {
            return new ViewHolder(LayoutInflater.from(p.getContext()).inflate(R.layout.item_weekly_report, p, false));
        }
        @Override public void onBindViewHolder(ViewHolder h, int p) {
            WeeklyReport r = list.get(p);
            h.sender.setText(r.getSenderEmail());
            h.date.setText(r.getDate());
            if (r.getPdfUri() != null) h.btnPdf.setOnClickListener(v -> openPdf(r.getPdfUri()));
        }
        @Override public int getItemCount() { return list.size(); }
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView sender, date, time;
            MaterialButton btnPdf;
            ViewHolder(View iv) {
                super(iv);
                sender = iv.findViewById(R.id.tvReportSender);
                date = iv.findViewById(R.id.tvReportDate);
                time = iv.findViewById(R.id.tvReportTime);
                btnPdf = iv.findViewById(R.id.btnViewPdf);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (groupId != null) loadData();
    }
}