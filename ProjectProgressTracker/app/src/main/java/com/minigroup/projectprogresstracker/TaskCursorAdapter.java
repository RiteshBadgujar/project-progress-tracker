package com.minigroup.projectprogresstracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class TaskCursorAdapter extends RecyclerView.Adapter<TaskCursorAdapter.TaskViewHolder> {

    private final ArrayList<TaskModel> items = new ArrayList<>();

    public void submitList(List<TaskModel> tasks) {
        items.clear();
        if (tasks != null) {
            items.addAll(tasks);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        TaskModel task = items.get(position);
        holder.taskName.setText(task.getTaskName());
        holder.assignedDate.setText("Assigned: " + task.getAssignedDate());
        holder.deadline.setText("Deadline: " + task.getDeadline());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView taskName;
        TextView assignedDate;
        TextView deadline;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskName = itemView.findViewById(R.id.rowTaskName);
            assignedDate = itemView.findViewById(R.id.rowAssignedDate);
            deadline = itemView.findViewById(R.id.rowTaskDeadline);
        }
    }
}
