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
        if (message == null || message.trim().isEmpty()) {
            return new CommitScore(0, "Poor");
        }

        String normalized = message.trim();
        String lower = normalized.toLowerCase(Locale.ROOT);

        int score = 60;

        int len = normalized.length();
        if (len >= 15 && len <= 72) score += 10;
        else if (len > 72) score += 5;
        else if (len < 10) score -= 10;

        if (lower.matches(".*\\b(add|implement|introduce|create)\\b.*")) score += 8;
        if (lower.matches(".*\\b(refactor|optimize|cleanup|restructure)\\b.*")) score += 6;
        if (lower.matches(".*\\b(fix|bug|issue|hotfix)\\b.*")) score += 4;
        if (lower.matches(".*\\b(test|ci|pipeline|build)\\b.*")) score += 6;
        if (lower.matches(".*\\b(docs|readme|comment)\\b.*")) score += 3;

        if (lower.matches("^(update|changes|misc|temp|test)$")) score -= 20;
        if (lower.length() < 6) score -= 15;

        if (normalized.contains(":")) score += 5;        
        if (normalized.contains("#")) score += 3;        
        if (Character.isUpperCase(normalized.charAt(0))) score += 2;

        score = Math.max(0, Math.min(100, score));

        return new CommitScore(score, categorize(score));
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
