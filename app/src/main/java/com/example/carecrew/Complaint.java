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
    public String userName;
    public String startWorkImageUrl;
    public String afterRepairImageUrl;
    public String repairNotes;
    public String closedAt;
    public String staffName;
    public long startTimeMillis;
    public long completionTimeMillis;
    public String cancelledBy;
    public String cancelReason;

    public Complaint() {
        // Required for Firebase
    }

    public Complaint(String id, String userId, String category, String description, String block, String floor, String roomNumber, String priority, String status, String imageUrl, String timestamp, String assignedTo) {
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
        this.assignedTo = assignedTo;
    }
}