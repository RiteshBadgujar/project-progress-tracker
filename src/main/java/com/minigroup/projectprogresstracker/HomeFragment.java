package com.minigroup.projectprogresstracker;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private String currentClassCode;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        if (getActivity() instanceof TeacherDashboardActivity) {
            currentClassCode = ((TeacherDashboardActivity) getActivity()).getClassCode();
        }

        View btnTasks = view.findViewById(R.id.btnAssignedTasks);
        if (btnTasks != null) {
            btnTasks.setOnClickListener(v -> showTaskPopup());
        }

        View btnUpdateProfile = view.findViewById(R.id.btnUpdateProfile);
        if (btnUpdateProfile != null) {
            btnUpdateProfile.setOnClickListener(v -> showUpdateProfilePopup());
        }

        View btnSelectGroups = view.findViewById(R.id.btnSelectGroups);
        if (btnSelectGroups != null) {
            btnSelectGroups.setOnClickListener(v -> showGroupSelectionPopup());
        }

        View btnGroupReq = view.findViewById(R.id.btnGroupRequest);
        if (btnGroupReq != null) {
            btnGroupReq.setOnClickListener(v -> showGroupRequestPopup());
        }

        View btnLogout = view.findViewById(R.id.btnLogout);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                if (getActivity() instanceof TeacherDashboardActivity) {
                    ((TeacherDashboardActivity) getActivity()).performLogout();
                }
            });
        }

        return view;
    }

    private void showGroupRequestPopup() {
        User teacher = SessionManager.getUser(getContext());
        if (getContext() == null || currentClassCode == null || teacher == null) return;

        final Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_group_requests);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(dialog.getWindow().getAttributes());
            lp.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.95);
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setAttributes(lp);
        }

        ListView listView = dialog.findViewById(R.id.lvGroupRequests);
        LinearLayout emptyState = dialog.findViewById(R.id.layoutEmptyRequests);
        ImageButton closeBtn = dialog.findViewById(R.id.btnDismissRequests);

        ArrayList<Group> allGroups = GroupStorage.getGroupsByClass(getContext(), currentClassCode);
        ArrayList<Group> requests = new ArrayList<>();

        for (Group g : allGroups) {
            if (teacher.getEmail().equalsIgnoreCase(g.getGuideEmail()) && g.getGuideName().isEmpty()) {
                requests.add(g);
            }
        }

        if (requests.isEmpty()) {
            if (emptyState != null) emptyState.setVisibility(View.VISIBLE);
            if (listView != null) listView.setVisibility(View.GONE);
        } else {
            if (emptyState != null) emptyState.setVisibility(View.GONE);
            if (listView != null) {
                listView.setVisibility(View.VISIBLE);
                GroupRequestAdapter adapter = new GroupRequestAdapter(getContext(), requests, dialog);
                listView.setAdapter(adapter);
            }
        }

        if (closeBtn != null) closeBtn.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showGroupSelectionPopup() {
        if (getContext() == null || currentClassCode == null) {
            Toast.makeText(getContext(), "Class code missing!", Toast.LENGTH_SHORT).show();
            return;
        }

        final Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_select_group);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(dialog.getWindow().getAttributes());
            lp.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.95);
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setAttributes(lp);
        }

        ListView listView = dialog.findViewById(R.id.lvGroups);
        LinearLayout emptyState = dialog.findViewById(R.id.layoutEmptyGroups);
        ImageButton closeBtn = dialog.findViewById(R.id.btnDismiss);
        TextView title = dialog.findViewById(R.id.tvPopupTitle);

        if (title != null) title.setText("Choose a Group");

        ArrayList<Group> allGroups = GroupStorage.getGroupsByClass(getContext(), currentClassCode);
        ArrayList<Group> availableGroups = new ArrayList<>();

        for (Group g : allGroups) {
            if (g.getGuideEmail().isEmpty() && g.getGuideName().isEmpty()) {
                availableGroups.add(g);
            }
        }

        if (availableGroups.isEmpty()) {
            if (emptyState != null) emptyState.setVisibility(View.VISIBLE);
            if (listView != null) listView.setVisibility(View.GONE);
        } else {
            if (emptyState != null) emptyState.setVisibility(View.GONE);
            if (listView != null) {
                listView.setVisibility(View.VISIBLE);
                GroupSelectionAdapter adapter = new GroupSelectionAdapter(getContext(), availableGroups, dialog);
                listView.setAdapter(adapter);
            }
        }

        if (closeBtn != null) closeBtn.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showUpdateProfilePopup() {
        if (getContext() == null) return;

        final Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_update_profile);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(dialog.getWindow().getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setAttributes(lp);
        }

        TextView tvEmail = dialog.findViewById(R.id.tvEmailDisplay);
        TextInputEditText etCurrentPass = dialog.findViewById(R.id.etCurrentPassword);
        TextInputEditText etNewName = dialog.findViewById(R.id.etUpdateName);
        TextInputEditText etNewPass = dialog.findViewById(R.id.etUpdatePassword);
        View btnSave = dialog.findViewById(R.id.btnSaveProfile);
        View btnCancel = dialog.findViewById(R.id.btnCancelProfile);

        User user = SessionManager.getUser(getContext());
        if (user != null) {
            if (tvEmail != null) tvEmail.setText(user.getEmail());
            if (etNewName != null) etNewName.setText(user.getName());
        }

        btnSave.setOnClickListener(v -> {
            String currentPass = (etCurrentPass != null) ? etCurrentPass.getText().toString() : "";
            String newName = (etNewName != null) ? etNewName.getText().toString().trim() : "";
            String newPass = (etNewPass != null) ? etNewPass.getText().toString().trim() : "";

            if (user == null || !currentPass.equals(user.getPassword())) {
                if (etCurrentPass != null) etCurrentPass.setError("Incorrect password");
                return;
            }

            if (newName.isEmpty()) {
                if (etNewName != null) etNewName.setError("Name is required");
                return;
            }

            user.setName(newName);
            if (!newPass.isEmpty()) user.setPassword(newPass);

            SessionManager.saveUser(getContext(), user);
            UserStorage.updateUser(getContext(), user);

            Toast.makeText(getContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();

            if (getActivity() != null) getActivity().recreate();
        });

        if (btnCancel != null) btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showTaskPopup() {
        if (getContext() == null || currentClassCode == null) {
            Toast.makeText(getContext(), "Class code missing!", Toast.LENGTH_SHORT).show();
            return;
        }

        final Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_task_list);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(dialog.getWindow().getAttributes());
            lp.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.95);
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setAttributes(lp);
        }

        ImageButton closeBtn = dialog.findViewById(R.id.btnCloseDialog);
        ListView taskListView = dialog.findViewById(R.id.taskListView);
        LinearLayout emptyState = dialog.findViewById(R.id.tvNoTasks);
        TextView dialogTitle = dialog.findViewById(R.id.dialogTitle);

        if (dialogTitle != null) dialogTitle.setText("Assigned Tasks");

        // ✅ FIXED: Using TaskModel instead of Task
        ArrayList<TaskModel> taskList = TaskStorage.getClassTasks(getContext(), currentClassCode);

        if (taskList == null || taskList.isEmpty()) {
            if (emptyState != null) emptyState.setVisibility(View.VISIBLE);
            if (taskListView != null) taskListView.setVisibility(View.GONE);
        } else {
            if (emptyState != null) emptyState.setVisibility(View.GONE);
            if (taskListView != null) {
                taskListView.setVisibility(View.VISIBLE);
                TaskAdapter adapter = new TaskAdapter(getContext(), taskList);
                taskListView.setAdapter(adapter);
            }
        }

        if (closeBtn != null) closeBtn.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private class GroupRequestAdapter extends ArrayAdapter<Group> {
        private final Dialog parentDialog;
        private final List<Group> requestList;

        public GroupRequestAdapter(Context context, List<Group> groups, Dialog dialog) {
            super(context, 0, groups);
            this.parentDialog = dialog;
            this.requestList = groups;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_group_request, parent, false);
            }

            Group group = getItem(position);
            TextView tvName = convertView.findViewById(R.id.tvReqGroupName);
            TextView tvProject = convertView.findViewById(R.id.tvReqProjectTitle);
            TextView tvTech = convertView.findViewById(R.id.tvReqTech);
            TextView tvMembers = convertView.findViewById(R.id.tvReqMembers);
            View btnAccept = convertView.findViewById(R.id.btnAccept);
            View btnDecline = convertView.findViewById(R.id.btnDecline);

            if (group != null) {
                tvName.setText(group.getGroupName());
                tvProject.setText(group.getProjectTitle());
                tvTech.setText(group.getTechnology());
                tvMembers.setText("Students: " + group.getMembersListString());

                btnAccept.setOnClickListener(v -> {
                    User teacher = SessionManager.getUser(getContext());
                    if (teacher != null) {
                        group.setGuideName(teacher.getName());
                        group.setGuideEmail(teacher.getEmail());
                        GroupStorage.updateGroup(getContext(), group);
                        Toast.makeText(getContext(), "Accepted " + group.getGroupName(), Toast.LENGTH_SHORT).show();

                        requestList.remove(position);
                        notifyDataSetChanged();
                        if (requestList.isEmpty()) parentDialog.dismiss();
                    }
                });

                btnDecline.setOnClickListener(v -> {
                    group.setGuideEmail("");
                    group.setGuideName("");
                    GroupStorage.updateGroup(getContext(), group);
                    Toast.makeText(getContext(), "Request declined", Toast.LENGTH_SHORT).show();

                    requestList.remove(position);
                    notifyDataSetChanged();
                    if (requestList.isEmpty()) parentDialog.dismiss();
                });
            }
            return convertView;
        }
    }

    private class GroupSelectionAdapter extends ArrayAdapter<Group> {
        private final Dialog parentDialog;

        public GroupSelectionAdapter(Context context, List<Group> groups, Dialog dialog) {
            super(context, 0, groups);
            this.parentDialog = dialog;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_group_select, parent, false);
            }

            Group group = getItem(position);
            TextView tvName = convertView.findViewById(R.id.tvGroupName);
            TextView tvProject = convertView.findViewById(R.id.tvProjectTitle);
            TextView tvTech = convertView.findViewById(R.id.tvTechnology);
            TextView tvMembers = convertView.findViewById(R.id.tvMembers);
            View btnSelect = convertView.findViewById(R.id.btnConfirmSelect);

            if (group != null) {
                tvName.setText(group.getGroupName());
                tvProject.setText(group.getProjectTitle());
                tvTech.setText(group.getTechnology());
                tvMembers.setText("Members: " + group.getMembersListString());

                btnSelect.setOnClickListener(v -> {
                    User teacher = SessionManager.getUser(getContext());
                    if (teacher != null) {
                        group.setGuideName(teacher.getName());
                        group.setGuideEmail(teacher.getEmail());
                        GroupStorage.updateGroup(getContext(), group);
                        Toast.makeText(getContext(), "You are now guide for " + group.getGroupName(), Toast.LENGTH_SHORT).show();
                        parentDialog.dismiss();
                    }
                });
            }
            return convertView;
        }
    }

    // ✅ FIXED: Updated to use TaskModel instead of Task
    private static class TaskAdapter extends ArrayAdapter<TaskModel> {

        public TaskAdapter(Context context, List<TaskModel> tasks) {
            super(context, 0, tasks);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_task, parent, false);
            }

            TaskModel task = getItem(position);
            TextView tvName = convertView.findViewById(R.id.rowTaskName);
            TextView tvAssigned = convertView.findViewById(R.id.rowAssignedDate);
            TextView tvDeadline = convertView.findViewById(R.id.rowTaskDeadline);

            if (task != null) {
                tvName.setText(task.getTaskName());
                // ✅ Using getAssignedDate() which provides the formatted date string from the model
                tvAssigned.setText("Assigned: " + task.getAssignedDate());
                tvDeadline.setText("Deadline: " + task.getDeadline());
            }

            return convertView;
        }
    }
}