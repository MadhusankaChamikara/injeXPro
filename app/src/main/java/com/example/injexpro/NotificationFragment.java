package com.example.injexpro;

import android.app.AlertDialog;
import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Button;
import androidx.annotation.NonNull;
import com.google.firebase.database.*;
import java.util.*;
import android.content.Context;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class NotificationFragment extends Fragment {
    private ListView notificationListView;
    private DatabaseReference notificationsRef;
    private NotificationAdapter adapter;
    private List<NotificationItem> notificationList;
    private static final String TAG = "NotificationFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        notificationListView = view.findViewById(R.id.notificationListView);
        notificationList = new ArrayList<>();
        adapter = new NotificationAdapter(getContext(), notificationList);
        notificationListView.setAdapter(adapter);

        // Initialize Firebase reference for notifications
        notificationsRef = FirebaseDatabase.getInstance().getReference("notifications");

        // Load saved notifications
        loadNotifications();

        Button clearButton = view.findViewById(R.id.clearAllButton);
        clearButton.setOnClickListener(v -> clearAllNotifications());

        return view;
    }

    private void loadNotifications() {
        notificationsRef.orderByChild("timestamp").limitToLast(20)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        notificationList.clear();
                        for (DataSnapshot notificationSnapshot : snapshot.getChildren()) {
                            NotificationItem item = notificationSnapshot.getValue(NotificationItem.class);
                            if (item != null) {
                                notificationList.add(0, item); // Add to start of list (newest first)
                            }
                        }
                        adapter.notifyDataSetChanged();
                        Log.d("NotificationFragment", "Notifications loaded: " + notificationList.size());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("NotificationFragment", "Error loading notifications", error.toException());
                    }
                });
    }


    private void clearAllNotifications() {
        new AlertDialog.Builder(getContext())
                .setTitle("Clear All Notifications")
                .setMessage("Are you sure you want to delete all notifications?")
                .setPositiveButton("Yes", (dialog, which) -> notificationsRef.removeValue())
                .setNegativeButton("No", null)
                .show();
    }

    // Notification Item class
    public static class NotificationItem {
        private String title;
        private String message;
        private String timestamp;
        private String type;

        public NotificationItem() {} // Required for Firebase

        public NotificationItem(String title, String message, String timestamp, String type) {
            this.title = title;
            this.message = message;
            this.timestamp = timestamp;
            this.type = type;
        }

        // Getters and setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
    }

    // Custom Adapter for notifications
    private class NotificationAdapter extends BaseAdapter {
        private Context context;
        private List<NotificationItem> notifications;

        public NotificationAdapter(Context context, List<NotificationItem> notifications) {
            this.context = context;
            this.notifications = notifications;
        }

        @Override
        public int getCount() {
            return notifications.size();
        }

        @Override
        public Object getItem(int position) {
            return notifications.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.notification_item, parent, false);
            }

            NotificationItem item = notifications.get(position);

            TextView titleText = convertView.findViewById(R.id.notificationTitle);
            TextView messageText = convertView.findViewById(R.id.notificationMessage);
            TextView timestampText = convertView.findViewById(R.id.notificationTimestamp);

            titleText.setText(item.getTitle());
            messageText.setText(item.getMessage());
            timestampText.setText(item.getTimestamp());

            // Set background color based on notification type
            switch (item.getType()) {
                case "warning":
                    convertView.setBackgroundResource(R.drawable.warning_background);
                    break;
                case "critical":
                    convertView.setBackgroundResource(R.drawable.critical_background);
                    break;
                default:
                    convertView.setBackgroundResource(R.drawable.normal_background);
                    break;
            }

            return convertView;
        }
    }
}