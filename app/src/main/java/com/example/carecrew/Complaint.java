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
    public String timestamp;
    public String assignedTo;
    public float rating;
    public String review;

    public Complaint() {
        // Required for Firebase
    }

    public Complaint(String id, String userId, String category, String description, String block, String floor, String roomNumber, String priority, String status, String imageUrl, String timestamp) {
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

    public Complaint(String id, String userId, String category, String description, String block, String floor, String roomNumber, String priority, String status, String imageUrl, String timestamp, String assignedTo) {
        this(id, userId, category, description, block, floor, roomNumber, priority, status, imageUrl, timestamp);
        this.assignedTo = assignedTo;
    }

    public Complaint(String id, String userId, String category, String description, String block, String floor, String roomNumber, String priority, String status, String imageUrl, String timestamp, String assignedTo, float rating, String review) {
        this(id, userId, category, description, block, floor, roomNumber, priority, status, imageUrl, timestamp, assignedTo);
        this.rating = rating;
        this.review = review;
    }

    public long getTimestampLong() {
        if (timestamp == null || timestamp.isEmpty()) return 0;
        try {
            return Long.parseLong(timestamp);
        } catch (NumberFormatException e) {
            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale.getDefault());
                java.util.Date date = sdf.parse(timestamp);
                return date != null ? date.getTime() : 0;
            } catch (Exception ex) {
                return 0;
            }
        }
    }
}