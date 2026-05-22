package com.minigroup.projectprogresstracker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.ArrayList;

public class TaskAdapter extends ArrayAdapter<Task> {

    public TaskAdapter(Context context, ArrayList<Task> tasks) {
        super(context, 0, tasks);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Task task = getItem(position);

        if (convertView == null) {
            // Inflating the layout using the exact XML structure you provided
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_assigned_task, parent, false);
        }

        // --- View Bindings matching your XML IDs ---
        TextView tvName = convertView.findViewById(R.id.rowTaskName);
        TextView tvAssignedDate = convertView.findViewById(R.id.rowAssignedDate);
        TextView tvDeadline = convertView.findViewById(R.id.rowTaskDeadline);

        if (task != null) {
            // Bind Task Name
            if (tvName != null) {
                tvName.setText(task.getTaskName());
            }

            // Bind Assigned Date
            if (tvAssignedDate != null) {
                String assigned = task.getAssignedDate();
                tvAssignedDate.setText("Assigned: " + (assigned != null ? assigned : "N/A"));
            }

            // Bind Deadline
            if (tvDeadline != null) {
                String deadline = task.getDeadline();
                tvDeadline.setText("Deadline: " + (deadline != null ? deadline : "N/A"));
            }
        }

        return convertView;
    }
}