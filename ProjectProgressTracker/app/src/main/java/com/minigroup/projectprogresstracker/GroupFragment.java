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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

public class GroupFragment extends Fragment {

    private String currentClassCode;
    private ListView lvAssignedGroups;
    private LinearLayout layoutEmptyMain;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_groups, container, false);

        // Get class code from TeacherDashboardActivity context
        if (getActivity() instanceof TeacherDashboardActivity) {
            currentClassCode = ((TeacherDashboardActivity) getActivity()).getClassCode();
        }

        lvAssignedGroups = view.findViewById(R.id.lvAssignedGroups);
        layoutEmptyMain = view.findViewById(R.id.layoutEmptyMain);

        setupGroupList();

        return view;
    }

    private void setupGroupList() {
        User teacher = SessionManager.getUser(getContext());
        if (getContext() == null || currentClassCode == null || teacher == null) return;

        ArrayList<Group> allGroups = GroupStorage.getGroupsByClass(getContext(), currentClassCode);
        ArrayList<Group> assignedGroups = new ArrayList<>();

        for (Group g : allGroups) {
            // Filter: Only groups mentored by the logged-in teacher
            if (teacher.getEmail().equalsIgnoreCase(g.getGuideEmail()) && g.getGuideName() != null && !g.getGuideName().isEmpty()) {
                assignedGroups.add(g);
            }
        }

        // Toggle Empty State Visibility
        if (assignedGroups.isEmpty()) {
            if (layoutEmptyMain != null) layoutEmptyMain.setVisibility(View.VISIBLE);
            if (lvAssignedGroups != null) lvAssignedGroups.setVisibility(View.GONE);
        } else {
            if (layoutEmptyMain != null) layoutEmptyMain.setVisibility(View.GONE);
            if (lvAssignedGroups != null) {
                lvAssignedGroups.setVisibility(View.VISIBLE);
                AssignedGroupAdapter adapter = new AssignedGroupAdapter(getContext(), assignedGroups);
                lvAssignedGroups.setAdapter(adapter);
            }
        }
    }

    private class AssignedGroupAdapter extends ArrayAdapter<Group> {
        public AssignedGroupAdapter(Context context, List<Group> groups) {
            super(context, 0, groups);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_assigned_group, parent, false);
            }

            final Group group = getItem(position);
            TextView tvTitle = convertView.findViewById(R.id.tvAssignedProjectTitle);
            TextView tvGroupName = convertView.findViewById(R.id.tvAssignedGroupName);
            TextView tvInitial = convertView.findViewById(R.id.tvGroupInitial);
            TextView tvProgressText = convertView.findViewById(R.id.tvAssignedProgressPercent);
            TextView tvTaskCount = convertView.findViewById(R.id.tvTaskCount);
            ProgressBar progressBar = convertView.findViewById(R.id.pbAssignedProgress);
            ImageButton btnDelete = convertView.findViewById(R.id.btnDeleteGroup);

            if (group != null) {
                // Set click listener on the entire row view to open the dashboard
                convertView.setOnClickListener(v -> {
                    Intent intent = new Intent(getContext(), TeacherGroupWorkspaceActivity.class);
                    intent.putExtra("GROUP_ID", group.getGroupId());
                    getContext().startActivity(intent);
                });

                tvTitle.setText(group.getProjectTitle());
                tvGroupName.setText(group.getGroupName());

                String initial = (group.getProjectTitle() == null || group.getProjectTitle().isEmpty())
                        ? "G" : group.getProjectTitle().substring(0, 1).toUpperCase();
                tvInitial.setText(initial);

                // ✅ Updated: Real Progress Calculation
                ArrayList<TaskModel> allTasks = TaskStorage.getClassTasks(getContext(), currentClassCode);
                int total = (allTasks != null) ? allTasks.size() : 0;
                int completed = 0;

                if (allTasks != null) {
                    for (TaskModel t : allTasks) {
                        // Check against the cross-reference storage for this group
                        if (TaskStorage.isTaskCompletedForGroup(getContext(), t.getTaskId(), group.getGroupId())) {
                            completed++;
                        }
                    }
                }

                if (tvTaskCount != null) tvTaskCount.setText(completed + "/" + total);
                int progress = (total == 0) ? 0 : (int) (((float) completed / total) * 100);
                progressBar.setProgress(progress);
                tvProgressText.setText(progress + "%");

                // Unassign logic
                btnDelete.setOnClickListener(v -> {
                    new MaterialAlertDialogBuilder(getContext())
                            .setTitle("Unassign Group?")
                            .setMessage("Stop mentoring '" + group.getGroupName() + "'? This removes it from your dashboard.")
                            .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                            .setPositiveButton("Unassign", (dialog, which) -> {
                                group.setGuideName("");
                                group.setGuideEmail("");

                                if (GroupStorage.updateGroup(getContext(), group)) {
                                    Toast.makeText(getContext(), "Group unassigned", Toast.LENGTH_SHORT).show();
                                    setupGroupList();
                                }
                            })
                            .show();
                });
            }
            return convertView;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setupGroupList();
    }
}