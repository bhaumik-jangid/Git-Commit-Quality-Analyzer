package com.bhaumik.gcqa.ui;

public class CommitViewModel {
    private final String hash;
    private final String author;
    private final String date;
    private final String message;
    private final int score;
    private final String category;

    public CommitViewModel(String hash, String author, String date,
                           String message, int score, String category) {
        this.hash = hash;
        this.author = author;
        this.date = date;
        this.message = message;
        this.score = score;
        this.category = category;
    }

    public String getHash() { return hash; }
    public String getAuthor() { return author; }
    public String getDate() { return date; }
    public String getMessage() { return message; }
    public int getScore() { return score; }
    public String getCategory() { return category; }
}
