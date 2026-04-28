package com.minigroup.projectprogresstracker;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;

public class ManageUsersActivity extends AppCompatActivity {

    private ListView listView;
    private MaterialButton btnTeachers, btnStudents;
    private TextView listHeader;

    private String classCode;
    private ArrayList<User> allUsers;
    private ArrayList<User> filteredUsers;
    private String currentRole = "Teacher";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_users);

        listView = findViewById(R.id.userList);
        btnTeachers = findViewById(R.id.btnTeachers);
        btnStudents = findViewById(R.id.btnStudents);
        listHeader = findViewById(R.id.listHeader);

        classCode = getIntent().getStringExtra("classCode");

        refreshDataAndList("Teacher");

        btnTeachers.setOnClickListener(v -> loadUsers("Teacher"));
        btnStudents.setOnClickListener(v -> loadUsers("Student"));
    }

    private void refreshDataAndList(String role) {
        allUsers = UserStorage.getUsers(this);
        loadUsers(role);
    }

    private void loadUsers(String role) {
        this.currentRole = role;
        updateTabUI(role);

        filteredUsers = new ArrayList<>();
        for (User u : allUsers) {
            if (u.getClassCode() != null &&
                    u.getClassCode().equalsIgnoreCase(classCode) &&
                    u.getRole().equalsIgnoreCase(role)) {
                filteredUsers.add(u);
            }
        }

        BaseAdapter adapter = new BaseAdapter() {
            @Override
            public int getCount() { return filteredUsers.size(); }
            @Override
            public Object getItem(int i) { return filteredUsers.get(i); }
            @Override
            public long getItemId(int i) { return i; }

            @Override
            public View getView(int i, View view, ViewGroup viewGroup) {
                if (view == null) {
                    view = getLayoutInflater().inflate(R.layout.item_user, viewGroup, false);
                }

                if (view instanceof ViewGroup) {
                    ((ViewGroup) view).setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
                }

                User user = filteredUsers.get(i);

                TextView name = view.findViewById(R.id.txtName);
                TextView detail = view.findViewById(R.id.txtSubDetail);
                TextView initial = view.findViewById(R.id.txtInitial);
                ImageView btnDelete = view.findViewById(R.id.btnDeleteUser);

                // ✅ UPDATED: Numbering removed. Added "Prof." prefix for Teachers.
                if ("Teacher".equalsIgnoreCase(user.getRole())) {
                    name.setText("Prof. " + user.getName());
                } else {
                    name.setText(user.getName());
                }

                if (user.getName() != null && !user.getName().isEmpty()) {
                    initial.setText(user.getName().substring(0, 1).toUpperCase());
                }

                detail.setText("Teacher".equalsIgnoreCase(user.getRole()) ?
                        "ID: " + user.getExtra() : "Roll No: " + user.getExtra());

                view.setOnClickListener(v -> showUserDetailsDialog(user));

                btnDelete.setOnClickListener(v -> {
                    showDeleteConfirmation(user, i, this);
                });

                return view;
            }
        };

        listView.setAdapter(adapter);
    }

    private void showDeleteConfirmation(User user, int position, BaseAdapter adapter) {

        new MaterialAlertDialogBuilder(this)
                .setTitle("Remove User")
                .setMessage("Remove " + user.getName() + " from this class?")
                .setCancelable(false)
                .setPositiveButton("Remove", (dialog, which) -> {

                    ArrayList<User> masterList = UserStorage.getUsers(this);

                    for (User u : masterList) {

                        boolean sameEmail = u.getEmail() != null &&
                                user.getEmail() != null &&
                                u.getEmail().equalsIgnoreCase(user.getEmail());

                        boolean sameExtra = u.getExtra() != null &&
                                user.getExtra() != null &&
                                u.getExtra().equalsIgnoreCase(user.getExtra());

                        if (sameEmail || sameExtra) {
                            u.setClassCode(null);
                            break;
                        }
                    }

                    UserStorage.saveUsers(this, masterList);

                    this.allUsers = masterList;

                    filteredUsers.remove(position);
                    adapter.notifyDataSetChanged();

                    Toast.makeText(this, user.getName() + " removed", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateTabUI(String role) {
        int activeBg = Color.parseColor("#6366F1");
        int inactiveBg = Color.TRANSPARENT;
        int activeText = Color.WHITE;
        int inactiveText = Color.parseColor("#64748B");

        boolean isTeacher = "Teacher".equalsIgnoreCase(role);

        btnTeachers.setBackgroundTintList(ColorStateList.valueOf(isTeacher ? activeBg : inactiveBg));
        btnTeachers.setTextColor(isTeacher ? activeText : inactiveText);

        btnStudents.setBackgroundTintList(ColorStateList.valueOf(isTeacher ? inactiveBg : activeBg));
        btnStudents.setTextColor(isTeacher ? inactiveText : activeText);

        if (listHeader != null)
            listHeader.setText("ACTIVE " + role.toUpperCase() + "S");
    }

    private void showUserDetailsDialog(User user) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_user_details);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.90),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        ((TextView) dialog.findViewById(R.id.tvRole)).setText(user.getRole().toUpperCase());
        ((TextView) dialog.findViewById(R.id.tvName)).setText(user.getName());
        ((TextView) dialog.findViewById(R.id.tvEmail)).setText(user.getEmail());
        ((TextView) dialog.findViewById(R.id.tvClass))
                .setText(user.getClassCode() != null ? user.getClassCode() : "N/A");

        TextView tvExtra = dialog.findViewById(R.id.tvExtra);

        String extraLabel = "Teacher".equalsIgnoreCase(user.getRole()) ?
                "Teacher ID: " : "Roll No: ";

        tvExtra.setText(extraLabel + user.getExtra());

        setupCopyListener(dialog.findViewById(R.id.layoutCopyEmail), user.getEmail(), "Email copied");
        setupCopyListener(dialog.findViewById(R.id.layoutCopyClass), user.getClassCode(), "Class code copied");
        setupCopyListener(dialog.findViewById(R.id.layoutCopyExtra), user.getExtra(), "ID copied");

        View btnClose = dialog.findViewById(R.id.btnCloseDialog);
        if (btnClose != null) btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void setupCopyListener(View layout, String textToCopy, String toastMsg) {
        if (layout != null && textToCopy != null) {
            layout.setOnClickListener(v -> {
                ClipboardManager clipboard =
                        (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

                ClipData clip = ClipData.newPlainText("User Detail", textToCopy);

                if (clipboard != null) {
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(this, toastMsg, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}