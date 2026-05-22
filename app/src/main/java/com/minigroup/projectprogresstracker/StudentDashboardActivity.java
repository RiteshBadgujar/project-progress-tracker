package com.minigroup.projectprogresstracker;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

public class StudentDashboardActivity extends AppCompatActivity {

    private TextView tvWelcome, tvClassName, tvClassCode, tvGroup, tvProjectTitle;
    private TextView tvCollegeName, tvDescription;
    private ImageButton btnNotification;
    private TextView tvNotificationBadge;
    private MaterialButton btnUpdateProfile, btnGroupDetails, btnAssignedTasks, btnLogout;
    private BottomNavigationView bottomNavigation;

    private User user;

    private static final String PREFS_NAME = "NotificationPrefs";
    private static final String KEY_LAST_COUNT = "last_seen_count";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);

        initViews();
        loadStudentSession();
        setupClickListeners();
        setupBottomNavigation();
        updateNotificationBadge();
    }

    private void initViews() {
        tvWelcome = findViewById(R.id.tvWelcome);
        tvClassName = findViewById(R.id.tvClassName);
        tvClassCode = findViewById(R.id.tvClassCode);
        tvCollegeName = findViewById(R.id.tvCollegeName);
        tvDescription = findViewById(R.id.tvDescription);
        tvGroup = findViewById(R.id.tvGroup);
        tvProjectTitle = findViewById(R.id.tvProjectTitle);
        btnNotification = findViewById(R.id.btnNotification);
        tvNotificationBadge = findViewById(R.id.tvNotificationBadge);

        btnUpdateProfile = findViewById(R.id.btnUpdateProfile);
        btnGroupDetails = findViewById(R.id.btnGroupDetails);
        btnAssignedTasks = findViewById(R.id.btnAssignedTasks);
        btnLogout = findViewById(R.id.btnLogout);

        bottomNavigation = findViewById(R.id.bottomNavigation);
    }

    private void loadStudentSession() {
        user = SessionManager.getUser(this);
        if (user == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        tvWelcome.setText("Hello, " + user.getName() + "!");

        if (user.getClassCode() != null) {
            ClassModel cls = ClassStorage.getClassByCode(this, user.getClassCode());
            if (cls != null) {
                tvClassName.setText(cls.getClassName());
                tvClassCode.setText("#" + cls.getClassCode());
                tvCollegeName.setText(cls.getCollegeName());
                tvDescription.setText(cls.getDescription());
            }
            loadGroupInfo();
        }
    }

    private void updateNotificationBadge() {
        if (user == null || user.getClassCode() == null) return;

        ArrayList<Announcement> announcements = AnnouncementStorage.getClassAnnouncements(this, user.getClassCode());
        int totalCount = (announcements != null) ? announcements.size() : 0;

        int lastSeenCount = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .getInt(KEY_LAST_COUNT + user.getClassCode(), 0);

        int unseenCount = totalCount - lastSeenCount;

        if (unseenCount > 0) {
            tvNotificationBadge.setVisibility(View.VISIBLE);
            tvNotificationBadge.setText(unseenCount > 9 ? "9+" : String.valueOf(unseenCount));
        } else {
            tvNotificationBadge.setVisibility(View.GONE);
        }
    }

    private void loadGroupInfo() {
        Group myGroup = GroupStorage.getGroupByStudent(this, user.getEmail(), user.getClassCode());

        if (myGroup != null) {
            tvGroup.setText("Group: " + myGroup.getGroupName());
            String title = myGroup.getProjectTitle();
            tvProjectTitle.setText("Project: " + (title != null && !title.isEmpty() ? title : "Not Yet Set"));
        } else {
            tvGroup.setText("Group: Not Assigned");
            tvProjectTitle.setText("Project: Not Yet Set");
        }
    }

    private void setupClickListeners() {
        btnNotification.setOnClickListener(v -> showNotificationsDialog());
        btnUpdateProfile.setOnClickListener(v -> showUpdateProfileDialog());
        btnGroupDetails.setOnClickListener(v -> handleOpenGroupDialog());
        btnAssignedTasks.setOnClickListener(v -> showTasksDialog());
        btnLogout.setOnClickListener(v -> {
            SessionManager.logout(this);
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void handleOpenGroupDialog() {
        Group myGroup = GroupStorage.getGroupByStudent(this, user.getEmail(), user.getClassCode());
        if (myGroup != null) {
            showGroupDetailsDialog(myGroup);
        } else {
            startActivity(new Intent(this, GroupSelectionActivity.class));
        }
    }

    private void handleOpenGroupWorkspace() {
        Group myGroup = GroupStorage.getGroupByStudent(this, user.getEmail(), user.getClassCode());

        if (myGroup == null) {
            startActivity(new Intent(this, GroupSelectionActivity.class));
        } else if (myGroup.getGuideName() == null || myGroup.getGuideName().isEmpty()) {
            Toast.makeText(this, "⚠️ Access Denied: Project Guide has not accepted the request yet!", Toast.LENGTH_LONG).show();
        } else {
            Intent intent = new Intent(this, GroupWorkspaceActivity.class);
            intent.putExtra("GROUP_ID", myGroup.getGroupId());
            startActivity(intent);
        }
    }

    private void showGroupDetailsDialog(Group group) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_group_details);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(dialog.getWindow().getAttributes());
            lp.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            lp.dimAmount = 0.7f;
            dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            dialog.getWindow().setAttributes(lp);
        }

        TextView tvTitle = dialog.findViewById(R.id.tvProjectTitleDetail);
        TextView tvName = dialog.findViewById(R.id.tvGroupNameDetail);
        TextView tvCode = dialog.findViewById(R.id.tvGroupCodeDetail);
        TextView tvTechStack = dialog.findViewById(R.id.tvTechStackDetail);
        TextView tvGuideName = dialog.findViewById(R.id.tvGuideNameDetail);
        TextView tvGuideEmail = dialog.findViewById(R.id.tvGuideEmailDetail);

        LinearLayout containerMembers = dialog.findViewById(R.id.containerMembersList);
        MaterialButton btnClose = dialog.findViewById(R.id.btnCloseGroup);
        MaterialButton btnLeave = dialog.findViewById(R.id.btnLeaveGroup);
        ImageButton btnShareIcon = dialog.findViewById(R.id.btnShareGroupIcon);

        tvName.setText(group.getGroupName());

        String fullCode = group.getGroupId().toUpperCase();
        tvCode.setText("#" + fullCode);

        // ✅ Click to Copy Logic
        tvCode.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Group ID", fullCode);
            if (clipboard != null) {
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this, "Group ID Copied: " + fullCode, Toast.LENGTH_SHORT).show();
            }
        });

        tvTitle.setText(group.getProjectTitle() != null && !group.getProjectTitle().isEmpty() ? group.getProjectTitle() : "TBD");

        if (tvTechStack != null) {
            tvTechStack.setText(group.getTechnology() != null && !group.getTechnology().isEmpty() ? group.getTechnology() : "Not Specified");
        }

        if (group.getGuideEmail() == null || group.getGuideEmail().isEmpty()) {
            tvGuideName.setText("Guide Not Selected");
            tvGuideName.setTextColor(Color.RED);
            tvGuideEmail.setText("No request sent");
        } else if (group.getGuideName() == null || group.getGuideName().isEmpty()) {
            tvGuideName.setText("Pending Approval");
            tvGuideName.setTextColor(Color.parseColor("#F59E0B"));
            tvGuideEmail.setText("Requested: " + group.getGuideEmail());
        } else {
            tvGuideName.setText(group.getGuideName());
            tvGuideName.setTextColor(Color.parseColor("#0F172A"));
            tvGuideEmail.setText(group.getGuideEmail());
        }

        if (btnLeave != null) {
            btnLeave.setOnClickListener(v -> {
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Leave Group?")
                        .setMessage("Are you sure you want to leave '" + group.getGroupName() + "'? This action cannot be undone without a new invitation.")
                        .setNegativeButton("Cancel", (d, which) -> d.dismiss())
                        .setPositiveButton("Leave", (d, which) -> {
                            ArrayList<String> members = group.getStudentEmails();
                            if (members != null) {
                                members.remove(user.getEmail());

                                if (members.isEmpty()) {
                                    GroupStorage.deleteGroup(this, group.getGroupId());
                                } else {
                                    group.setStudentEmails(members);
                                    GroupStorage.updateGroup(this, group);
                                }

                                dialog.dismiss();
                                loadGroupInfo();
                                Toast.makeText(this, "Successfully left the group", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .show();
            });
        }

        // ✅ Professional Share Logic
        View.OnClickListener shareAction = v -> {
            String projectStr = (group.getProjectTitle() != null && !group.getProjectTitle().isEmpty()) ? group.getProjectTitle() : "TBD";
            String shareMessage = "🚀 *Join my Project Team on Progress Tracker!*\n\n" +
                    "🏢 *Group:* " + group.getGroupName() + "\n" +
                    "📝 *Title:* " + projectStr + "\n" +
                    "💻 *Tech:* " + (group.getTechnology() != null ? group.getTechnology() : "TBD") + "\n" +
                    "🔑 *Team Code:* " + fullCode + "\n\n" +
                    "Enter this code in the app to join our workspace!";

            Intent sendIntent = new Intent(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            sendIntent.setType("text/plain");
            startActivity(Intent.createChooser(sendIntent, "Share Group Info"));
        };

        if (btnShareIcon != null) btnShareIcon.setOnClickListener(shareAction);

        containerMembers.removeAllViews();
        if (group.getStudentEmails() != null) {
            for (String email : group.getStudentEmails()) {
                User member = UserStorage.getUserByEmail(this, email);
                String displayName = (member != null) ? member.getName() : "Member";

                View row = getLayoutInflater().inflate(R.layout.item_user_selection, containerMembers, false);
                TextView tvRowName = row.findViewById(R.id.tvRowName);
                TextView tvRowEmail = row.findViewById(R.id.tvRowEmail);
                TextView tvAvatarLetter = row.findViewById(R.id.tvAvatarLetter);
                View viewAvatarBg = row.findViewById(R.id.viewAvatarBg);
                View checkBox = row.findViewById(R.id.rowCheckBox);

                if (checkBox != null) checkBox.setVisibility(View.GONE);

                if (tvAvatarLetter != null && viewAvatarBg != null) {
                    tvAvatarLetter.setText(displayName.substring(0, 1).toUpperCase());
                    GradientDrawable shape = new GradientDrawable();
                    shape.setShape(GradientDrawable.OVAL);
                    shape.setColor(Color.parseColor(getColorForName(displayName)));
                    viewAvatarBg.setBackground(shape);
                }

                if (email.equalsIgnoreCase(user.getEmail())) {
                    tvRowName.setText(displayName + " (You)");
                    tvRowName.setTextColor(Color.parseColor("#6366F1"));
                    tvRowName.setTypeface(null, Typeface.BOLD);
                } else {
                    tvRowName.setText(displayName);
                }

                tvRowEmail.setText(email);
                containerMembers.addView(row);
            }
        }

        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private String getColorForName(String name) {
        String[] colors = {"#6366F1", "#F59E0B", "#EF4444", "#8B5CF6", "#EC4899", "#06B6D4", "#F97316", "#10B981"};
        return colors[Math.abs(name.toLowerCase().hashCode()) % colors.length];
    }

    private void showUpdateProfileDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_update_profile);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.95);
            dialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        TextInputEditText etCurrentPass = dialog.findViewById(R.id.etCurrentPassword);
        TextInputEditText etUpdateName = dialog.findViewById(R.id.etUpdateName);
        TextInputEditText etUpdatePass = dialog.findViewById(R.id.etUpdatePassword);
        MaterialButton btnSave = dialog.findViewById(R.id.btnSaveProfile);
        TextView tvEmail = dialog.findViewById(R.id.tvEmailDisplay);

        if (user != null) {
            etUpdateName.setText(user.getName());
            if (tvEmail != null) tvEmail.setText(user.getEmail());
        }

        btnSave.setOnClickListener(v -> {
            String currentPassInput = etCurrentPass.getText().toString();
            if (!currentPassInput.equals(user.getPassword())) {
                etCurrentPass.setError("Incorrect Password");
                return;
            }

            user.setName(etUpdateName.getText().toString().trim());
            String newPass = etUpdatePass.getText().toString().trim();
            if (!newPass.isEmpty()) user.setPassword(newPass);

            UserStorage.updateUser(this, user);
            SessionManager.saveUser(this, user);

            tvWelcome.setText("Hello, " + user.getName() + "!");
            dialog.dismiss();
            Toast.makeText(this, "Profile Updated!", Toast.LENGTH_SHORT).show();
        });

        View btnCancel = dialog.findViewById(R.id.btnCancelProfile);
        if (btnCancel != null) btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.nav_home);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) return true;
            if (id == R.id.nav_group) {
                handleOpenGroupWorkspace();
                return false;
            }
            return false;
        });
    }

    private void showTasksDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_task_list);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.95);
            dialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        ImageButton btnClose = dialog.findViewById(R.id.btnCloseDialog);
        ListView taskListView = dialog.findViewById(R.id.taskListView);
        View noTasksView = dialog.findViewById(R.id.tvNoTasks);

        ArrayList<TaskModel> tasks = TaskStorage.getClassTasks(this, user.getClassCode());

        if (tasks == null || tasks.isEmpty()) {
            if (taskListView != null) taskListView.setVisibility(View.GONE);
            if (noTasksView != null) noTasksView.setVisibility(View.VISIBLE);
        } else {
            if (taskListView != null) {
                taskListView.setVisibility(View.VISIBLE);
                taskListView.setDivider(null);
                taskListView.setDividerHeight(0);

                StudentTaskAdapter adapter = new StudentTaskAdapter(this, tasks);
                taskListView.setAdapter(adapter);
            }
            if (noTasksView != null) noTasksView.setVisibility(View.GONE);
        }

        if (btnClose != null) btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private class StudentTaskAdapter extends android.widget.ArrayAdapter<TaskModel> {
        public StudentTaskAdapter(android.content.Context context, java.util.List<TaskModel> tasks) {
            super(context, 0, tasks);
        }
        @Override
        public View getView(int position, View convertView, android.view.ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.item_task, parent, false);
            }
            TaskModel task = getItem(position);
            TextView tvName = convertView.findViewById(R.id.rowTaskName);
            TextView tvAssigned = convertView.findViewById(R.id.rowAssignedDate);
            TextView tvDeadline = convertView.findViewById(R.id.rowTaskDeadline);

            if (task != null) {
                tvName.setText(task.getTaskName());
                tvAssigned.setText("Assigned: " + task.getAssignedDate());
                tvDeadline.setText("Deadline: " + task.getDeadline());
            }
            return convertView;
        }
    }

    private void showNotificationsDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_notification_list);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.95);
            dialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        ImageButton btnClose = dialog.findViewById(R.id.btnCloseDialog);
        ListView notificationListView = dialog.findViewById(R.id.notificationListView);
        View noNotificationsView = dialog.findViewById(R.id.tvNoNotifications);
        MaterialButton btnMarkRead = dialog.findViewById(R.id.btnMarkAllRead);

        ArrayList<Announcement> announcements = AnnouncementStorage.getClassAnnouncements(this, user.getClassCode());

        if (announcements == null || announcements.isEmpty()) {
            if (notificationListView != null) notificationListView.setVisibility(View.GONE);
            if (noNotificationsView != null) noNotificationsView.setVisibility(View.VISIBLE);
        } else {
            if (notificationListView != null) {
                notificationListView.setVisibility(View.VISIBLE);
                NotificationAdapter adapter = new NotificationAdapter(this, announcements);
                notificationListView.setAdapter(adapter);
            }
            if (noNotificationsView != null) noNotificationsView.setVisibility(View.GONE);

            getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit()
                    .putInt(KEY_LAST_COUNT + user.getClassCode(), announcements.size()).apply();
            tvNotificationBadge.setVisibility(View.GONE);
        }

        if (btnClose != null) btnClose.setOnClickListener(v -> dialog.dismiss());
        if (btnMarkRead != null) btnMarkRead.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStudentSession();
        updateNotificationBadge();
        bottomNavigation.setSelectedItemId(R.id.nav_home);
    }
}