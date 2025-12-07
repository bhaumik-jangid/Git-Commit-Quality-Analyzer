package com.bhaumik.gcqa.core;

import com.bhaumik.gcqa.model.CommitRecord;
import com.bhaumik.gcqa.model.CommitScore;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

public class DebugRunner {

    public static void main(String[] args) {
        // TODO: change this to your actual repo path
        String repoPath = "/home/azazil/Desktop/dev-pro";

        GitLogReader reader = new GitLogReader(Paths.get(repoPath));
        CommitAnalyzer analyzer = new CommitAnalyzer();

        try {
            List<CommitRecord> commits = reader.readCommits(15);
            System.out.println("Analyzing last " + commits.size() + " commits...\n");

            for (CommitRecord c : commits) {
                CommitScore score = analyzer.analyze(c);
                System.out.println(
                        c.getHash().substring(0, 7) + " | " +
                        score.getScore() + " (" + score.getCategory() + ") | " +
                        c.getMessage()
                );
            }
        } catch (IOException e) {
            System.err.println("Failed to read git log: " + e.getMessage());
        }
    }
}
