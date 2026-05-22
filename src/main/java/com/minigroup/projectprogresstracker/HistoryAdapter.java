package com.minigroup.projectprogresstracker;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private Context context;
    private ArrayList<Submission> historyList;

    public HistoryAdapter(Context context, ArrayList<Submission> historyList) {
        this.context = context;
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Using the unified row layout
        View view = LayoutInflater.from(context).inflate(R.layout.item_submission_row, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        Submission entry = historyList.get(position);

        // 1. Handle Timestamp (formatted as Date \n Time)
        if (entry.getTimestamp() != null) {
            holder.tvUploadDateTime.setText(entry.getTimestamp().replace(", ", "\n"));
        }

        // 2. Differentiate between File Submission and Teacher Feedback
        // Logic: If fileUri is empty, it's a text feedback message
        boolean isFeedback = entry.getFileUri() == null || entry.getFileUri().isEmpty();

        if (isFeedback) {
            // UI for Teacher Feedback
            holder.ivFileIcon.setImageResource(R.drawable.ic_send); // Use send or chat icon
            holder.ivFileIcon.setColorFilter(Color.parseColor("#3B82F6")); // Blue tint
            holder.ivFileIcon.setBackgroundResource(R.drawable.bg_icon_circle_light);

            holder.tvFileName.setText(entry.getFileName()); // In feedback, fileName stores the message
            holder.tvFileName.setTypeface(null, Typeface.NORMAL);

            holder.tvFileInfo.setText("Instructor Feedback");
            holder.tvFileInfo.setTextColor(Color.parseColor("#3B82F6"));

            // Disable click for text feedback
            holder.itemView.setOnClickListener(null);
        } else {
            // UI for File Submission
            holder.ivFileIcon.setImageResource(R.drawable.ic_file);
            holder.ivFileIcon.setColorFilter(null); // Clear tint
            holder.ivFileIcon.setBackgroundResource(R.drawable.bg_icon_circle_light);

            holder.tvFileName.setText(entry.getFileName());
            holder.tvFileName.setTypeface(null, Typeface.BOLD);

            holder.tvFileInfo.setText("By: " + entry.getStudentEmail());
            holder.tvFileInfo.setTextColor(Color.parseColor("#64748B"));

            // Handle file opening on click
            holder.itemView.setOnClickListener(v -> {
                if (context instanceof GroupWorkspaceActivity) {
                    ((GroupWorkspaceActivity) context).openFile(entry.getFileUri());
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return historyList != null ? historyList.size() : 0;
    }

    public static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvFileName, tvFileInfo, tvUploadDateTime;
        ImageView ivFileIcon;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            // Matching IDs from item_submission_row.xml
            tvFileName = itemView.findViewById(R.id.tvFileName);
            tvFileInfo = itemView.findViewById(R.id.tvFileInfo);
            tvUploadDateTime = itemView.findViewById(R.id.tvUploadDateTime);
            ivFileIcon = itemView.findViewById(R.id.ivFileIcon);
        }
    }
}