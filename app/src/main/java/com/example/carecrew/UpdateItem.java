package com.example.carecrew;

public class UpdateItem {
    public enum UpdateType {
        RESOLVED, IN_PROGRESS, URGENT, ANNOUNCEMENT
    }

    private String message;
    private String timestamp;
    private UpdateType type;

    public UpdateItem(String message, String timestamp, UpdateType type) {
        this.message = message;
        this.timestamp = timestamp;
        this.type = type;
    }

    public String getMessage() { return message; }
    public String getTimestamp() { return timestamp; }
    public UpdateType getType() { return type; }
}