package com.bhaumik.gcqa.core;

import com.bhaumik.gcqa.model.CommitRecord;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

public class DebugRunner {

    public static void main(String[] args) {
        // TODO: change this to your actual repo path
        String repoPath = "/home/azazil/Desktop/dev-pro";

        GitLogReader reader = new GitLogReader(Paths.get(repoPath));

        try {
            List<CommitRecord> commits = reader.readCommits(10);
            for (CommitRecord c : commits) {
                System.out.println(c.getHash() + " | " +
                        c.getAuthor() + " | " +
                        c.getDateTime() + " | " +
                        c.getMessage());
            }
        } catch (IOException e) {
            System.err.println("Failed to read git log: " + e.getMessage());
        }
    }
}

