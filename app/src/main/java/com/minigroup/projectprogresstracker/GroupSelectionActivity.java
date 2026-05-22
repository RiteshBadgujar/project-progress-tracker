package com.minigroup.projectprogresstracker;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;

public class GroupSelectionActivity extends AppCompatActivity {

    private User user;
    private String selectedGuideEmail = "";
    private String selectedGuideName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_selection);
        user = SessionManager.getUser(this);

        findViewById(R.id.btnCreateGroup).setOnClickListener(v -> showCreateGroupDialog());
        findViewById(R.id.btnJoinGroup).setOnClickListener(v -> showJoinGroupDialog());
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void showCreateGroupDialog() {
        Dialog mainDialog = new Dialog(this);
        mainDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mainDialog.setContentView(R.layout.dialog_create_group);

        if (mainDialog.getWindow() != null) {
            mainDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            mainDialog.getWindow().setLayout((int)(getResources().getDisplayMetrics().widthPixels * 0.95), ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        TextInputEditText etGroupName = mainDialog.findViewById(R.id.etGroupName);
        TextInputEditText etProjectTitle = mainDialog.findViewById(R.id.etProjectTitle);
        TextInputEditText etTechnology = mainDialog.findViewById(R.id.etTechnology);
        TextView tvLeaderEmail = mainDialog.findViewById(R.id.tvLeaderEmailDisplay);

        LinearLayout containerMembers = mainDialog.findViewById(R.id.containerSelectedMembers);
        LinearLayout containerGuide = mainDialog.findViewById(R.id.containerSelectedGuide);

        if (user != null && tvLeaderEmail != null) {
            tvLeaderEmail.setText(user.getEmail());
        }

        ArrayList<String> selectedMemberEmails = new ArrayList<>();

        mainDialog.findViewById(R.id.btnSelectGuide).setOnClickListener(v ->
                showPicker(true, null, containerGuide));

        mainDialog.findViewById(R.id.btnAddMember).setOnClickListener(v ->
                showPicker(false, selectedMemberEmails, containerMembers));

        mainDialog.findViewById(R.id.btnFinalCreateGroup).setOnClickListener(v -> {
            String gName = etGroupName.getText().toString().trim();
            String pTitle = etProjectTitle.getText().toString().trim();
            String tech = etTechnology.getText().toString().trim();

            if (gName.isEmpty() || pTitle.isEmpty()) {
                Toast.makeText(this, "Group Name and Project Title are required", Toast.LENGTH_SHORT).show();
                return;
            }

            Group newGroup = new Group(
                    null,
                    gName,
                    pTitle,
                    tech,
                    "",
                    selectedGuideEmail,
                    user.getClassCode(),
                    user.getEmail()
            );

            for (String email : selectedMemberEmails) {
                newGroup.addStudent(email);
            }

            if (!newGroup.getStudentEmails().contains(user.getEmail())) {
                newGroup.addStudent(user.getEmail());
            }

            GroupStorage.saveGroup(this, user.getClassCode(), newGroup);

            String toastMsg = "Group Created! Code: #" + newGroup.getGroupId();
            Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();

            mainDialog.dismiss();
            finish();
        });

        mainDialog.findViewById(R.id.btnCancelCreate).setOnClickListener(v -> mainDialog.dismiss());
        mainDialog.show();
    }

    private void showPicker(boolean isTeacher, ArrayList<String> selectedList, LinearLayout displayContainer) {
        Dialog picker = new Dialog(this);
        picker.setContentView(R.layout.dialog_user_selector);

        if (picker.getWindow() != null) {
            picker.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            picker.getWindow().setLayout((int)(getResources().getDisplayMetrics().widthPixels * 0.92), ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        TextView title = picker.findViewById(R.id.dialogTitle);
        title.setText(isTeacher ? "Select Faculty Guide" : "Select Team Members");

        LinearLayout list = picker.findViewById(R.id.listContainer);
        ArrayList<User> users = isTeacher ? UserStorage.getUsersByRole(this, "Teacher")
                : UserStorage.getAvailableStudents(this, user.getClassCode());

        for (User u : users) {
            if (!isTeacher && u.getEmail().equalsIgnoreCase(user.getEmail())) continue;

            View row = getLayoutInflater().inflate(R.layout.item_user_selection, null);
            setupRowData(row, u.getName(), u.getEmail(), isTeacher);
            CheckBox cb = row.findViewById(R.id.rowCheckBox);

            if (isTeacher) {
                cb.setVisibility(View.GONE);
                row.setOnClickListener(v -> {
                    selectedGuideEmail = u.getEmail();
                    selectedGuideName = u.getName();
                    displayContainer.removeAllViews();
                    addMemberRow(displayContainer, u.getName(), u.getEmail(), true);
                    picker.dismiss();
                });
            } else {
                cb.setChecked(selectedList.contains(u.getEmail()));
                row.setOnClickListener(v -> {
                    if (selectedList.contains(u.getEmail())) {
                        selectedList.remove(u.getEmail());
                        cb.setChecked(false);
                    } else {
                        selectedList.add(u.getEmail());
                        cb.setChecked(true);
                    }
                });
            }
            list.addView(row);
        }

        MaterialButton btn = picker.findViewById(R.id.btnConfirmSelection);
        if (isTeacher) btn.setVisibility(View.GONE);

        btn.setOnClickListener(v -> {
            displayContainer.removeAllViews();
            for (String email : selectedList) {
                User s = UserStorage.getUserByEmail(this, email);
                if (s != null) addMemberRow(displayContainer, s.getName(), s.getEmail(), false);
            }
            picker.dismiss();
        });

        picker.show();
    }

    private void setupRowData(View row, String name, String email, boolean isTeacher) {
        TextView tvLetter = row.findViewById(R.id.tvAvatarLetter);
        TextView tvName = row.findViewById(R.id.tvRowName);
        TextView tvEmail = row.findViewById(R.id.tvRowEmail);
        View avatarBg = row.findViewById(R.id.viewAvatarBg);

        tvName.setText(name);
        tvEmail.setText(email);
        tvLetter.setText(name.isEmpty() ? "U" : name.substring(0, 1).toUpperCase());

        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.OVAL);
        shape.setColor(Color.parseColor(isTeacher ? "#10B981" : getColorForName(name)));
        avatarBg.setBackground(shape);
    }

    private void addMemberRow(LinearLayout container, String name, String email, boolean isTeacher) {
        View rowView = getLayoutInflater().inflate(R.layout.item_user_selection, null);
        setupRowData(rowView, name, email, isTeacher);
        rowView.findViewById(R.id.rowCheckBox).setVisibility(View.GONE);
        rowView.setBackgroundResource(R.drawable.bg_selected_members);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(0, 0, 0, 16);
        rowView.setLayoutParams(params);
        container.addView(rowView);
    }

    private String getColorForName(String name) {
        String[] colors = {"#6366F1", "#F59E0B", "#EF4444", "#8B5CF6", "#EC4899", "#06B6D4", "#F97316"};
        return colors[Math.abs(name.toLowerCase().hashCode()) % colors.length];
    }

    private void showJoinGroupDialog() {
        Dialog joinDialog = new Dialog(this);
        joinDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        joinDialog.setContentView(R.layout.dialog_join_group);

        if (joinDialog.getWindow() != null) {
            joinDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            joinDialog.getWindow().setLayout((int)(getResources().getDisplayMetrics().widthPixels * 0.92), ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        TextInputEditText etJoinCode = joinDialog.findViewById(R.id.etJoinCode);
        MaterialButton btnConfirmJoin = joinDialog.findViewById(R.id.btnConfirmJoin);
        MaterialButton btnCancelJoin = joinDialog.findViewById(R.id.btnCancelJoin);

        btnConfirmJoin.setOnClickListener(v -> {
            String inputCode = etJoinCode.getText().toString().trim().toUpperCase();

            if (inputCode.length() != 6) {
                etJoinCode.setError("Please enter a valid 6-character code");
                return;
            }

            ArrayList<Group> classGroups = GroupStorage.getGroupsByClass(this, user.getClassCode());
            Group targetGroup = null;

            for (Group g : classGroups) {
                if (g.getGroupId().equalsIgnoreCase(inputCode)) {
                    targetGroup = g;
                    break;
                }
            }

            if (targetGroup != null) {
                if (targetGroup.getStudentEmails().contains(user.getEmail())) {
                    Toast.makeText(this, "Already a member", Toast.LENGTH_SHORT).show();
                } else {
                    targetGroup.addStudent(user.getEmail());
                    GroupStorage.saveGroup(this, user.getClassCode(), targetGroup);
                    Toast.makeText(this, "Joined successfully!", Toast.LENGTH_SHORT).show();
                    joinDialog.dismiss();
                    finish();
                }
            } else {
                Toast.makeText(this, "Invalid Code!", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancelJoin.setOnClickListener(v -> joinDialog.dismiss());
        joinDialog.show();
    }
}