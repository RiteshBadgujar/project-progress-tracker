package com.minigroup.projectprogresstracker;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class AdminDashboardActivity extends AppCompatActivity {

    private MaterialButton logoutBtn, createEditClassBtn, updateProfileBtn;
    private MaterialButton shareClassBtn, notifyBtn, assignTaskBtn;
    private MaterialButton manageUsersBtn, manageGroupsBtn;
    private MaterialCardView groupsStatsCard;

    private TextView studentCount, teacherCount, groupCount;
    private TextView welcomeMsg, classInfo, classNameDisplay;

    private boolean classCreated = false;
    private String className = "", classCode = "", adminEmail = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        View mainLayout = findViewById(R.id.admin_main_layout);
        if (mainLayout != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainLayout, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        initViews();
        loadAdminSession();
        setupClickListeners();
    }

    private void initViews() {
        logoutBtn = findViewById(R.id.logoutBtn);
        createEditClassBtn = findViewById(R.id.createEditClassBtn);
        shareClassBtn = findViewById(R.id.shareClassBtn);
        notifyBtn = findViewById(R.id.notifyBtn);
        assignTaskBtn = findViewById(R.id.assignTaskBtn);
        manageUsersBtn = findViewById(R.id.manageUsersBtn);
        manageGroupsBtn = findViewById(R.id.manageGroupsBtn);
        updateProfileBtn = findViewById(R.id.updateProfileBtn);

        groupsStatsCard = findViewById(R.id.groupsStatsCard);

        studentCount = findViewById(R.id.studentCount);
        teacherCount = findViewById(R.id.teacherCount);
        groupCount = findViewById(R.id.groupCount);

        welcomeMsg = findViewById(R.id.welcomeMsg);
        classInfo = findViewById(R.id.classInfo);
        classNameDisplay = findViewById(R.id.classNameDisplay);
    }

    private void loadAdminSession() {
        User admin = SessionManager.getUser(this);
        if (admin == null) {
            finish();
            return;
        }

        adminEmail = admin.getEmail();
        welcomeMsg.setText("Welcome, " + admin.getName() + "!");

        ClassModel savedClass = ClassStorage.getClass(this, adminEmail);
        if (savedClass != null) {
            classCreated = true;
            className = savedClass.getClassName();
            classCode = savedClass.getClassCode();
        } else {
            classCreated = false;
        }

        updateUIState();
        updateStats();
    }

    private void setupClickListeners() {
        classInfo.setOnClickListener(v -> {
            if (classCreated && !classCode.isEmpty()) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Class Code", classCode);
                if (clipboard != null) {
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(this, "Class Code copied: " + classCode, Toast.LENGTH_SHORT).show();
                }
            }
        });

        createEditClassBtn.setOnClickListener(v -> {
            if (!classCreated) showCreateClassDialog();
            else showEditClassDialog();
        });

        if (updateProfileBtn != null) {
            updateProfileBtn.setOnClickListener(v -> showProfileDialog());
        }

        if (groupsStatsCard != null) {
            groupsStatsCard.setOnClickListener(v -> {
                if (!classCreated) {
                    Toast.makeText(this, "Create a class first", Toast.LENGTH_SHORT).show();
                    return;
                }
                showMonitorGroupsDialog();
            });
        }

        if (manageGroupsBtn != null) {
            manageGroupsBtn.setOnClickListener(v -> {
                if (!classCreated) {
                    Toast.makeText(this, "Create a class first", Toast.LENGTH_SHORT).show();
                    return;
                }
                showMonitorGroupsDialog();
            });
        }

        shareClassBtn.setOnClickListener(v -> {
            if (!classCreated) {
                Toast.makeText(this, "Create a class first", Toast.LENGTH_SHORT).show();
                return;
            }
            shareClass();
        });

        notifyBtn.setOnClickListener(v -> {
            if (!classCreated) {
                Toast.makeText(this, "Create a class first", Toast.LENGTH_SHORT).show();
                return;
            }
            showAnnouncementDialog();
        });

        assignTaskBtn.setOnClickListener(v -> {
            if (!classCreated) {
                Toast.makeText(this, "Create a class first", Toast.LENGTH_SHORT).show();
                return;
            }
            showTaskDialog();
        });

        if (manageUsersBtn != null) {
            manageUsersBtn.setOnClickListener(v -> {
                if (!classCreated) {
                    Toast.makeText(this, "Create a class first", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(this, ManageUsersActivity.class);
                intent.putExtra("classCode", classCode);
                startActivity(intent);
            });
        }

        logoutBtn.setOnClickListener(v -> {
            SessionManager.logout(this);
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void showMonitorGroupsDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_monitor_groups);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        ListView listView = dialog.findViewById(R.id.groupListView);
        LinearLayout emptyState = dialog.findViewById(R.id.emptyGroupsText);
        ImageButton btnClose = dialog.findViewById(R.id.btnClosePopup);
        MaterialButton btnRefresh = dialog.findViewById(R.id.btnRefreshTeams);

        ArrayList<Group> groups = GroupStorage.getGroupsByClass(this, classCode);

        if (groups.isEmpty()) {
            if (emptyState != null) emptyState.setVisibility(View.VISIBLE);
            if (listView != null) listView.setVisibility(View.GONE);
        } else {
            if (emptyState != null) emptyState.setVisibility(View.GONE);
            if (listView != null) listView.setVisibility(View.VISIBLE);

            BaseAdapter adapter = new BaseAdapter() {
                @Override
                public int getCount() { return groups.size(); }
                @Override
                public Object getItem(int i) { return groups.get(i); }
                @Override
                public long getItemId(int i) { return i; }

                @Override
                public View getView(int i, View view, ViewGroup viewGroup) {
                    if (view == null) {
                        view = getLayoutInflater().inflate(R.layout.item_group_row, viewGroup, false);
                    }
                    Group g = groups.get(i);

                    TextView title = view.findViewById(R.id.txtProjectTitle);
                    TextView info = view.findViewById(R.id.txtGroupInfo);
                    TextView guide = view.findViewById(R.id.txtGuideName);
                    TextView email = view.findViewById(R.id.txtGuideEmail);
                    TextView progressText = view.findViewById(R.id.txtProgressPercent);
                    ProgressBar bar = view.findViewById(R.id.projectProgressBar);

                    String rawId = g.getGroupId();
                    String cleanId = (rawId != null && rawId.length() > 6)
                            ? rawId.substring(0, 6).toUpperCase()
                            : (rawId != null ? rawId.toUpperCase() : "N/A");

                    title.setText(g.getProjectTitle() != null && !g.getProjectTitle().isEmpty() ? g.getProjectTitle() : "No Title Set");
                    info.setText(g.getGroupName() + " • #" + cleanId);

                    String guideName = (g.getGuideName() == null || g.getGuideName().isEmpty()) ? "-Pending Guide" : g.getGuideName();
                    guide.setText(guideName);
                    email.setText(g.getGuideEmail() != null ? g.getGuideEmail() : "");

                    // Calculate real-time progress based on task storage
                    ArrayList<TaskModel> classTasks = TaskStorage.getClassTasks(AdminDashboardActivity.this, classCode);
                    int completed = 0;
                    for (TaskModel t : classTasks) {
                        if (TaskStorage.isTaskCompletedForGroup(AdminDashboardActivity.this, t.getTaskId(), g.getGroupId())) {
                            completed++;
                        }
                    }
                    int progress = (classTasks.isEmpty()) ? 0 : (int) (((float) completed / classTasks.size()) * 100);

                    progressText.setText(progress + "%");

                    if (bar != null) {
                        bar.setVisibility(View.VISIBLE);
                        bar.setIndeterminate(false);
                        bar.setMax(100);
                        bar.setProgress(progress);
                        bar.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#6366F1")));
                    }

                    return view;
                }
            };
            listView.setAdapter(adapter);

            listView.setOnItemClickListener((parent, view, position, id) -> {
                showGroupAnalysisDialog(groups.get(position));
            });
        }

        if (btnClose != null) btnClose.setOnClickListener(v -> dialog.dismiss());
        if (btnRefresh != null) btnRefresh.setOnClickListener(v -> {
            dialog.dismiss();
            showMonitorGroupsDialog();
        });

        dialog.show();

        if (dialog.getWindow() != null) {
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.92);
            int height = (int) (getResources().getDisplayMetrics().heightPixels * 0.85);
            dialog.getWindow().setLayout(width, height);
        }
    }

    private void showGroupAnalysisDialog(Group group) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_group_analysis);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView title = dialog.findViewById(R.id.analysisTitle);
        TextView bigPercent = dialog.findViewById(R.id.txtBigPercentage);
        ProgressBar circularProgress = dialog.findViewById(R.id.circularProgress);
        LinearLayout memberContainer = dialog.findViewById(R.id.memberContainer);
        LinearLayout taskContainer = dialog.findViewById(R.id.taskContainer);

        // Real-time calculation for analysis
        ArrayList<TaskModel> classTasks = TaskStorage.getClassTasks(this, classCode);
        int completedCount = 0;
        for (TaskModel t : classTasks) {
            if (TaskStorage.isTaskCompletedForGroup(this, t.getTaskId(), group.getGroupId())) {
                completedCount++;
            }
        }
        int progress = (classTasks.isEmpty()) ? 0 : (int) (((float) completedCount / classTasks.size()) * 100);
        int pendingCount = classTasks.size() - completedCount;

        title.setText(group.getGroupName());
        bigPercent.setText(progress + "%");
        circularProgress.setProgress(progress);

        View pendingCard = dialog.findViewById(R.id.statPending);
        View completedCard = dialog.findViewById(R.id.statCompleted);

        if (pendingCard != null) {
            TextView value = pendingCard.findViewById(R.id.statValue);
            if (value != null) value.setText(String.valueOf(pendingCount));
        }

        if (completedCard != null) {
            TextView value = completedCard.findViewById(R.id.statValue);
            if (value != null) value.setText(String.valueOf(completedCount));
        }

        memberContainer.removeAllViews();
        ArrayList<User> members = group.getMembers(this);
        if (members != null) {
            for (User member : members) {
                View mView = getLayoutInflater().inflate(R.layout.item_member_analysis, memberContainer, false);
                ((TextView) mView.findViewById(R.id.memberName)).setText(member.getName());
                ((TextView) mView.findViewById(R.id.memberEmail)).setText(member.getEmail());
                ((TextView) mView.findViewById(R.id.memberInitials)).setText(getInitials(member.getName()));
                memberContainer.addView(mView);
            }
        }

        taskContainer.removeAllViews();
        for (TaskModel task : classTasks) {
            View tView = getLayoutInflater().inflate(R.layout.item_task_analysis_row, taskContainer, false);
            ((TextView) tView.findViewById(R.id.taskName)).setText(task.getTaskName());

            ImageView icon = tView.findViewById(R.id.statusIcon);
            if (icon != null) {
                boolean isDone = TaskStorage.isTaskCompletedForGroup(this, task.getTaskId(), group.getGroupId());
                int statusColor = isDone ? Color.parseColor("#10B981") : Color.parseColor("#F59E0B");
                icon.setImageTintList(ColorStateList.valueOf(statusColor));
            }

            taskContainer.addView(tView);
        }

        dialog.findViewById(R.id.btnCloseAnalysis).setOnClickListener(v -> dialog.dismiss());
        dialog.show();

        if (dialog.getWindow() != null) {
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.95);
            int height = (int) (getResources().getDisplayMetrics().heightPixels * 0.90);
            dialog.getWindow().setLayout(width, height);
        }
    }

    private String getInitials(String name) {
        if (name == null || name.isEmpty()) return "??";
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2) {
            return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
        }
        return parts[0].substring(0, Math.min(parts[0].length(), 2)).toUpperCase();
    }

    private void showProfileDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_update_profile);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        User currentUser = SessionManager.getUser(this);
        if (currentUser == null) return;

        TextView tvEmail = dialog.findViewById(R.id.tvEmailDisplay);
        TextInputEditText etName = dialog.findViewById(R.id.etUpdateName);
        TextInputEditText etCurrentPassword = dialog.findViewById(R.id.etCurrentPassword);
        TextInputEditText etNewPassword = dialog.findViewById(R.id.etUpdatePassword);
        MaterialButton btnSave = dialog.findViewById(R.id.btnSaveProfile);
        MaterialButton btnCancel = dialog.findViewById(R.id.btnCancelProfile);

        if (tvEmail != null) tvEmail.setText(currentUser.getEmail());
        if (etName != null) etName.setText(currentUser.getName());

        btnSave.setOnClickListener(v -> {
            String currentPassInput = etCurrentPassword.getText().toString();
            String newName = etName.getText().toString().trim();
            String newPass = etNewPassword.getText().toString().trim();

            if (currentPassInput.isEmpty()) {
                Toast.makeText(this, "Please verify current password", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!currentPassInput.equals(currentUser.getPassword())) {
                Toast.makeText(this, "Incorrect current password", Toast.LENGTH_SHORT).show();
                return;
            }

            if (newName.isEmpty()) {
                Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            String finalPassword = newPass.isEmpty() ? currentUser.getPassword() : newPass;
            User updatedUser = new User(newName, currentUser.getEmail(), finalPassword, currentUser.getRole());
            updatedUser.setClassCode(currentUser.getClassCode());

            UserStorage.updateUser(this, updatedUser);
            SessionManager.saveUser(this, updatedUser);

            welcomeMsg.setText("Welcome, " + newName + "!");
            Toast.makeText(this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        if (btnCancel != null) btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
        setDialogSize(dialog);
    }

    private String generateRandomClassCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder code = new StringBuilder();
        Random rnd = new Random();
        while (code.length() < 6) {
            code.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return code.toString();
    }

    private void setDialogSize(Dialog dialog) {
        if (dialog.getWindow() != null) {
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.95);
            dialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    private void showCreateClassDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_create_class);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextInputEditText nameInput = dialog.findViewById(R.id.classNameInput);
        TextInputEditText collegeInput = dialog.findViewById(R.id.collegeNameInput);
        TextInputEditText descInput = dialog.findViewById(R.id.descriptionInput);
        MaterialButton saveBtn = dialog.findViewById(R.id.saveClassBtn);
        TextView cancelBtn = dialog.findViewById(R.id.btnCancelEdit);

        saveBtn.setOnClickListener(v -> {
            String n = nameInput.getText().toString().trim();
            String col = collegeInput.getText().toString().trim();
            String desc = (descInput != null) ? descInput.getText().toString().trim() : "";

            if (n.isEmpty() || col.isEmpty()) {
                Toast.makeText(this, "Class Name and College are required", Toast.LENGTH_SHORT).show();
                return;
            }

            String generatedCode = generateRandomClassCode();
            ClassStorage.saveClass(this, adminEmail, n, generatedCode, col, desc);
            classCreated = true;
            className = n;
            classCode = generatedCode;

            User admin = SessionManager.getUser(this);
            admin.setClassCode(generatedCode);
            UserStorage.updateUser(this, admin);
            SessionManager.saveUser(this, admin);

            updateUIState();
            updateStats();
            dialog.dismiss();
            Toast.makeText(this, "Class Created! Code: " + generatedCode, Toast.LENGTH_LONG).show();
        });

        if (cancelBtn != null) cancelBtn.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
        setDialogSize(dialog);
    }

    private void showEditClassDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_edit_class);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView codeDisplay = dialog.findViewById(R.id.classCodeView);
        TextInputEditText nameInput = dialog.findViewById(R.id.classNameInput);
        TextInputEditText collegeInput = dialog.findViewById(R.id.collegeNameInput);
        TextInputEditText descInput = dialog.findViewById(R.id.descriptionInput);
        MaterialButton saveBtn = dialog.findViewById(R.id.saveClassBtn);
        TextView cancelBtn = dialog.findViewById(R.id.btnCancelEdit);

        ClassModel current = ClassStorage.getClass(this, adminEmail);
        if(current != null) {
            if (codeDisplay != null) codeDisplay.setText("Code: " + current.getClassCode());
            if (nameInput != null) nameInput.setText(current.getClassName());
            if (collegeInput != null) collegeInput.setText(current.getCollegeName());
            if (descInput != null) descInput.setText(current.getDescription());
        }

        saveBtn.setOnClickListener(v -> {
            String newName = nameInput.getText().toString().trim();
            String college = collegeInput.getText().toString().trim();
            String desc = (descInput != null) ? descInput.getText().toString().trim() : "";

            if (newName.isEmpty() || college.isEmpty()) {
                Toast.makeText(this, "Name and College cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            ClassStorage.saveClass(this, adminEmail, newName, classCode, college, desc);
            className = newName;

            updateUIState();
            dialog.dismiss();
            Toast.makeText(this, "Class Updated", Toast.LENGTH_SHORT).show();
        });

        if (cancelBtn != null) cancelBtn.setOnClickListener(view -> dialog.dismiss());
        dialog.show();
        setDialogSize(dialog);
    }

    private void showAnnouncementDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_announcement);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        EditText input = dialog.findViewById(R.id.announcementInput);
        Button sendBtn = dialog.findViewById(R.id.sendAnnouncementBtn);
        ListView listView = dialog.findViewById(R.id.announcementList);

        ArrayList<Announcement> list = AnnouncementStorage.getClassAnnouncements(this, classCode);

        BaseAdapter adapter = new BaseAdapter() {
            @Override
            public int getCount() { return list.size(); }
            @Override
            public Object getItem(int i) { return list.get(i); }
            @Override
            public long getItemId(int i) { return i; }

            @Override
            public View getView(int i, View view, ViewGroup viewGroup) {
                if (view == null) {
                    view = getLayoutInflater().inflate(R.layout.item_notification, viewGroup, false);
                }

                Announcement a = list.get(i);
                MaterialCardView card = view.findViewById(R.id.announcementCard);
                TextView msg = view.findViewById(R.id.txtMessage);
                TextView dateText = view.findViewById(R.id.txtDate);

                msg.setText(a.getMessage());

                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault());
                dateText.setText(sdf.format(new Date()));

                if (i % 2 == 0) {
                    card.setCardBackgroundColor(Color.parseColor("#EEF2FF"));
                    card.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#C7D2FE")));
                    msg.setTextColor(Color.parseColor("#4338CA"));
                } else {
                    card.setCardBackgroundColor(Color.parseColor("#FFFFFF"));
                    card.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#E2E8F0")));
                    msg.setTextColor(Color.parseColor("#1E293B"));
                }

                return view;
            }
        };

        listView.setAdapter(adapter);

        sendBtn.setOnClickListener(v -> {
            String msg = input.getText().toString().trim();
            if (msg.isEmpty()) return;

            Announcement newA = new Announcement(msg, classCode);
            AnnouncementStorage.addAnnouncement(this, classCode, newA);

            list.add(0, newA);
            adapter.notifyDataSetChanged();
            input.setText("");
            Toast.makeText(this, "Announcement Sent!", Toast.LENGTH_SHORT).show();
        });

        dialog.show();
        setDialogSize(dialog);
    }

    private void showTaskDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_task);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextInputEditText input = dialog.findViewById(R.id.taskName);
        LinearLayout selector = dialog.findViewById(R.id.deadlineSelector);
        TextView deadline = dialog.findViewById(R.id.deadlineText);
        Button btn = dialog.findViewById(R.id.addTaskBtn);
        ListView listView = dialog.findViewById(R.id.taskList);

        final String[] date = {""};

        if (selector != null) {
            selector.setOnClickListener(v -> {
                Calendar cal = Calendar.getInstance();
                new DatePickerDialog(this, (view, y, m, d) -> {
                    date[0] = d + "/" + (m + 1) + "/" + y;
                    if (deadline != null) deadline.setText("Deadline: " + date[0]);
                }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
            });
        }

        ArrayList<TaskModel> tasks = TaskStorage.getClassTasks(this, classCode);
        ArrayAdapter<TaskModel> adapter = new ArrayAdapter<TaskModel>(this, R.layout.item_task, tasks) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null)
                    convertView = getLayoutInflater().inflate(R.layout.item_task, parent, false);

                TaskModel t = getItem(position);
                if (t != null) {
                    ((TextView) convertView.findViewById(R.id.rowTaskName)).setText(t.getTaskName());
                    ((TextView) convertView.findViewById(R.id.rowAssignedDate))
                            .setText("Assigned: " + t.getAssignedDate());
                    ((TextView) convertView.findViewById(R.id.rowTaskDeadline))
                            .setText("Deadline: " + t.getDeadline());
                }
                return convertView;
            }
        };

        if (listView != null) listView.setAdapter(adapter);

        if (btn != null) {
            btn.setOnClickListener(v -> {
                String name = (input != null) ? input.getText().toString().trim() : "";
                if (name.isEmpty() || date[0].isEmpty()) {
                    Toast.makeText(this, "Enter task and deadline", Toast.LENGTH_SHORT).show();
                    return;
                }

                TaskModel t = new TaskModel(name, classCode, date[0]);
                TaskStorage.addTask(this, t);
                tasks.add(0, t);
                adapter.notifyDataSetChanged();
                if (input != null) input.setText("");
                if (deadline != null) deadline.setText("Select Deadline");
            });
        }

        dialog.show();
        setDialogSize(dialog);
    }

    private void shareClass() {
        String msg = "Project Tracker - Join Class\nName: " + className + "\nCode: " + classCode;
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, msg);
        startActivity(Intent.createChooser(intent, "Share Class Code Via"));
    }

    private void updateUIState() {
        if (classCreated) {
            classNameDisplay.setText(className);
            classInfo.setText("CODE: " + classCode);
            createEditClassBtn.setText("Edit Class Details");
        } else {
            classNameDisplay.setText("No Class Active");
            classInfo.setText("Create Class First");
            createEditClassBtn.setText("Create New Class");
        }
    }

    private void updateStats() {
        ArrayList<User> users = UserStorage.getUsers(this);
        int s = 0, t = 0;

        for (User u : users) {
            if (u.getClassCode() != null && u.getClassCode().equals(classCode)) {
                if ("Student".equals(u.getRole())) s++;
                else t++;
            }
        }

        studentCount.setText(String.valueOf(s));
        teacherCount.setText(String.valueOf(t));
        groupCount.setText(String.valueOf(GroupStorage.getGroupsByClass(this, classCode).size()));
    }
}