package com.bhaumik.gcqa.model;

import java.time.LocalDateTime;

public class CommitRecord {
    private String hash;
    private String author;
    private LocalDateTime dateTime;
    private String message;

    public CommitRecord(String hash, String author, LocalDateTime dateTime, String message) {
        this.hash = hash;
        this.author = author;
        this.dateTime = dateTime;
        this.message = message;
    }

    public String getHash() { return hash; }
    public String getAuthor() { return author; }
    public LocalDateTime getDateTime() { return dateTime; }
    public String getMessage() { return message; }
}

