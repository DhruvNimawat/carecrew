package com.example.carecrew;

public class Complaint {
    public String id;
    public String userId;
    public String category;
    public String description;
    public String block;
    public String floor;
    public String roomNumber;
    public String priority;
    public String status;
    public String imageUrl;
    public Object timestamp; // Changed to Object to handle both String and Long from Firebase
    public String assignedTo;
    public Object completionTimeMillis; // Added to handle missing field warnings
    public Object rating; // Added to handle missing field warnings
    public String staffName; // Added to handle missing field warnings
    public String userName; // Added to handle field from AcceptedJobAdapter
    public String startWorkImageUrl;
    public String afterRepairImageUrl;
    public long startTimeMillis;
    public String closedAt;
    public String repairNotes;
    public String cancelledBy;
    public String cancelReason;
    public String userEmail;
    public String title;
    public String ticketId;
    public long timestampLong;

    public Complaint() {
        // Required for Firebase
    }

    public Complaint(String id, String userId, String category, String description, String block, String floor, String roomNumber, String priority, String status, String imageUrl, Object timestamp) {
        this.id = id;
        this.userId = userId;
        this.category = category;
        this.description = description;
        this.block = block;
        this.floor = floor;
        this.roomNumber = roomNumber;
        this.priority = priority;
        this.status = status;
        this.imageUrl = imageUrl;
        this.timestamp = timestamp;
    }

    public Complaint(String id, String userId, String category, String description, String block, String floor, String roomNumber, String priority, String status, String imageUrl, Object timestamp, String assignedTo) {
        this(id, userId, category, description, block, floor, roomNumber, priority, status, imageUrl, timestamp);
        this.assignedTo = assignedTo;
    }

    @com.google.firebase.database.Exclude
    public String getTimestampString() {
        if (timestamp instanceof String) {
            return (String) timestamp;
        } else if (timestamp instanceof Long) {
            return String.valueOf(timestamp);
        }
        return "";
    }

    @com.google.firebase.database.Exclude
    public long getTimestampLong() {
        if (timestamp == null) return timestampLong;
        if (timestamp instanceof Long) return (Long) timestamp;
        if (timestamp instanceof String) {
            String ts = (String) timestamp;
            if (ts.isEmpty()) return timestampLong;
            try {
                return Long.parseLong(ts);
            } catch (NumberFormatException e) {
                try {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale.getDefault());
                    java.util.Date date = sdf.parse(ts);
                    return date != null ? date.getTime() : 0;
                } catch (Exception ex) {
                    return 0;
                }
            }
        }
        return timestampLong;
    }
}