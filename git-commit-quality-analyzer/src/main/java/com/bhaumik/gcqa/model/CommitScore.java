package com.bhaumik.gcqa.model;

public class CommitScore {
    private int score;
    private String category; 

    public CommitScore(int score, String category) {
        this.score = score;
        this.category = category;
    }

    public int getScore() { return score; }
    public String getCategory() { return category; }
}

