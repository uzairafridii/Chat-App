package com.example.chatapp;

public class Message
{
    private String message , type;
    private boolean seen;
    private long timestamp;
    private String from;

    public Message() {
    }

    public Message(String message, String type, boolean seen, long timestamp, String from) {
        this.message = message;
        this.type = type;
        this.seen = seen;
        this.timestamp = timestamp;
        this.from = from;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
