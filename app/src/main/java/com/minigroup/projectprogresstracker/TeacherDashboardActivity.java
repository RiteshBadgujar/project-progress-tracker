package com.minigroup.projectprogresstracker;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

public class TeacherDashboardActivity extends AppCompatActivity {

    private TextView tvDashboardTitle, tvTeacherName, tvClassName, tvClassCode;
    private BottomNavigationView bottomNavigation;
    private RelativeLayout btnNotifications;
    private View notificationBadge;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_dashboard);

        // 🔹 Initialize Views
        tvDashboardTitle = findViewById(R.id.tvDashboardTitle);
        tvTeacherName = findViewById(R.id.tvTeacherName);
        tvClassName = findViewById(R.id.tvClassName);
        tvClassCode = findViewById(R.id.tvClassCode);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        btnNotifications = findViewById(R.id.btnNotifications);
        notificationBadge = findViewById(R.id.notificationBadge);

        user = SessionManager.getUser(this);

        if (user == null) {
            goToLogin();
            return;
        }

        // 🔹 Set Header Data
        setupHeader();

        // 🔹 Default Fragment
        loadFragment(new HomeFragment());

        // 🔹 Bottom Navigation Logic
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                loadFragment(new HomeFragment());
                return true;
            } else if (itemId == R.id.nav_groups) {
                loadFragment(new GroupFragment());
                return true;
            }
            return false;
        });

        // 🔹 Notification Click logic
        if (btnNotifications != null) {
            btnNotifications.setOnClickListener(v -> showNotificationDialog());
        }
    }

    private void setupHeader() {
        tvTeacherName.setText("Welcome, Prof. " + user.getName());

        String code = user.getClassCode();

        if (code != null && !code.isEmpty()) {
            ClassModel cls = ClassStorage.getClassByCode(this, code);
            if (cls != null) {
                tvClassName.setText(cls.getClassName());
                tvClassCode.setText("#" + cls.getClassCode());
            } else {
                tvClassName.setText("Class Info Missing");
                tvClassCode.setText("#" + code);
            }
        } else {
            tvClassName.setText("No Class Assigned");
            tvClassCode.setText("#000000");
        }
    }

    public String getClassCode() {
        return (user != null) ? user.getClassCode() : null;
    }

    /**
     * Shows the notification popup with dynamic data and modern CardView items
     */
    private void showNotificationDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_notification_list, null);

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        ImageButton btnClose = dialogView.findViewById(R.id.btnCloseDialog);
        MaterialButton btnDismissAll = dialogView.findViewById(R.id.btnMarkAllRead);
        ListView listView = dialogView.findViewById(R.id.notificationListView);
        LinearLayout emptyState = dialogView.findViewById(R.id.tvNoNotifications);

        // 🔹 FETCH DATA
        String classCode = getClassCode();
        ArrayList<Announcement> announcements = AnnouncementStorage.getClassAnnouncements(this, classCode);

        // 🔹 VISIBILITY LOGIC
        if (announcements == null || announcements.isEmpty()) {
            if (listView != null) listView.setVisibility(View.GONE);
            if (emptyState != null) emptyState.setVisibility(View.VISIBLE);
        } else {
            if (listView != null) {
                listView.setVisibility(View.VISIBLE);
                // 🔹 ATTACH CUSTOM ADAPTER
                NotificationAdapter adapter = new NotificationAdapter(this, announcements);
                listView.setAdapter(adapter);
            }
            if (emptyState != null) emptyState.setVisibility(View.GONE);
        }

        btnClose.setOnClickListener(v -> dialog.dismiss());

        btnDismissAll.setOnClickListener(v -> {
            Toast.makeText(this, "Announcements marked as read", Toast.LENGTH_SHORT).show();
            if (listView != null) listView.setVisibility(View.GONE);
            if (emptyState != null) emptyState.setVisibility(View.VISIBLE);
        });

        if (notificationBadge != null) {
            notificationBadge.setVisibility(View.GONE);
        }

        dialog.show();
    }

    /**
     * 🔹 Custom Adapter to handle the MaterialCardView layout for Announcements
     */
    private static class NotificationAdapter extends ArrayAdapter<Announcement> {
        public NotificationAdapter(Context context, List<Announcement> items) {
            super(context, 0, items);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_notification, parent, false);
            }

            Announcement item = getItem(position);

            // 🔹 Map IDs from your modern XML
            TextView tvMessage = convertView.findViewById(R.id.txtMessage);
            TextView tvDate = convertView.findViewById(R.id.txtDate);

            if (item != null) {
                if (tvMessage != null) tvMessage.setText(item.getMessage());
                if (tvDate != null) tvDate.setText(item.getDate());
            }

            return convertView;
        }
    }

    private void loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                    .commit();
        }
    }

    public void performLogout() {
        SessionManager.clearSession(this);
        Toast.makeText(this, "Signed out successfully", Toast.LENGTH_SHORT).show();
        goToLogin();
    }

    private void goToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}