package com.minigroup.projectprogresstracker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class NotificationAdapter extends ArrayAdapter<Announcement> {

    public NotificationAdapter(Context context, ArrayList<Announcement> announcements) {
        super(context, 0, announcements);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // 1. Get the data item for this position
        Announcement announcement = getItem(position);

        // 2. Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_notification, parent, false);
        }

        // 3. Lookup view for data population
        // These IDs must match your res/layout/item_notification.xml
        TextView tvMessage = convertView.findViewById(R.id.txtMessage);
        TextView tvDate = convertView.findViewById(R.id.txtDate);

        // 4. Populate the data into the template view using the data object
        if (announcement != null) {
            // Set message text
            if (tvMessage != null) {
                tvMessage.setText(announcement.getMessage());
            }

            // Set formatted date
            if (tvDate != null) {
                // Ensure we use the timestamp from the announcement object
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault());

                // If timestamp is 0 (uninitialized), use current time as fallback
                long time = announcement.getTimestamp();
                if (time == 0) time = System.currentTimeMillis();

                String formattedDate = sdf.format(new Date(time));
                tvDate.setText(formattedDate);
            }
        }

        // 5. Return the completed view to render on screen
        return convertView;
    }
}