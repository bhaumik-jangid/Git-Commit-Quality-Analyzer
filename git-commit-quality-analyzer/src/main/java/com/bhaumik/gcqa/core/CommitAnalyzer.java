package com.bhaumik.gcqa.core;

import com.bhaumik.gcqa.model.CommitRecord;
import com.bhaumik.gcqa.model.CommitScore;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class CommitAnalyzer {

    private static final Set<String> GOOD_KEYWORDS = new HashSet<>(Arrays.asList(
            "add", "fix", "update", "refactor", "remove",
            "implement", "improve", "docs", "test"
    ));

    private static final Set<String> BAD_KEYWORDS = new HashSet<>(Arrays.asList(
            "temp", "wip", "test", "misc", "changes", "stuff"
    ));

    public CommitScore analyze(CommitRecord record) {
        String message = record.getMessage();
        if (message == null) {
            return new CommitScore(0, "Poor");
        }

        String normalized = message.trim();
        String lower = normalized.toLowerCase(Locale.ROOT);

        int score = 0;

        // 1. Length score
        int lengthScore = lengthScore(normalized);
        score += lengthScore;

        // 2. Keyword score
        int keywordScore = keywordScore(lower);
        score += keywordScore;

        // 3. Bad pattern penalty
        int badPenalty = badPenalty(lower);
        score += badPenalty;

        // 4. Starts-with-verb bonus
        int startBonus = startsWithVerbBonus(lower);
        score += startBonus;

        // Clamp between 0 and 100
        if (score < 0) score = 0;
        if (score > 100) score = 100;

        String category = categorize(score);

        return new CommitScore(score, category);
    }

    private int lengthScore(String msg) {
        int len = msg.length();
        if (len < 10) {
            return 0;
        } else if (len <= 50) {
            return 20;
        } else if (len <= 100) {
            return 30;
        } else {
            return 15;
        }
    }

    private int keywordScore(String lowerMsg) {
        int score = 0;
        for (String kw : GOOD_KEYWORDS) {
            if (lowerMsg.contains(kw)) {
                score += 10;
            }
        }
        return Math.min(score, 40);
    }

    private int badPenalty(String lowerMsg) {
        int penalty = 0;
        for (String bad : BAD_KEYWORDS) {
            if (lowerMsg.contains(bad)) {
                penalty -= 10;
            }
        }
        return Math.max(penalty, -20);
    }

    private int startsWithVerbBonus(String lowerMsg) {
        // Get first word
        String[] parts = lowerMsg.split("\\s+");
        if (parts.length == 0) {
            return 0;
        }
        String firstWord = parts[0];
        if (GOOD_KEYWORDS.contains(firstWord)) {
            return 10;
        }
        return 0;
    }

    private String categorize(int score) {
        if (score >= 70) {
            return "Good";
        } else if (score >= 40) {
            return "Average";
        } else {
            return "Poor";
        }
    }
}

