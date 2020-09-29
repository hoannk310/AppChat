package com.nkh.appchat.model;

public class Messages {
    private String message, from,to,date, type,time,name;
    private boolean isSeen;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Messages() {
    }

    public Messages(String message, String from, String to, String date, String type, String time, String name, boolean isSeen) {
        this.message = message;
        this.from = from;
        this.to = to;
        this.date = date;
        this.type = type;
        this.time = time;
        this.name = name;
        this.isSeen = isSeen;
    }

    public boolean isSeen() {
        return isSeen;
    }

    public void setSeen(boolean seen) {
        isSeen = seen;
    }
}
