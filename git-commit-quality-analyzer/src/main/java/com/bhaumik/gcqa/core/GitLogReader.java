// Reads commit history from a local Git repository using git log

package com.bhaumik.gcqa.core;

import com.bhaumik.gcqa.model.CommitRecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class GitLogReader {

    private final Path repoPath;
    private final DateTimeFormatter dateTimeFormatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z");

    public GitLogReader(Path repoPath) {
        this.repoPath = repoPath;
    }

    public List<CommitRecord> readCommits(int limit) throws IOException {
        List<CommitRecord> records = new ArrayList<>();

        List<String> command = List.of(
                "git",
                "-C", repoPath.toString(),
                "log",
                "-n", String.valueOf(limit),
                "--pretty=format:%H|%an|%ad|%s",
                "--date=iso"
        );

        ProcessBuilder builder = new ProcessBuilder(command);
        Process process = builder.start();

        StringBuilder errorOutput = new StringBuilder();

        try (BufferedReader stdout =
                    new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader stderr =
                    new BufferedReader(new InputStreamReader(process.getErrorStream()))) {

            String line;
            while ((line = stdout.readLine()) != null) {
                CommitRecord record = parseLine(line);
                if (record != null) {
                    records.add(record);
                }
            }

            String errLine;
            while ((errLine = stderr.readLine()) != null) {
                errorOutput.append(errLine).append(System.lineSeparator());
            }

        } catch (IOException e) {
            throw new IOException("Error reading git command output", e);
        }

        try {
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IOException("git log failed with exit code " + exitCode +
                        ". Error: " + errorOutput);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("git log interrupted", e);
        }

        return records;
    }

    private CommitRecord parseLine(String line) {
        String[] parts = line.split("\\|", 4);
        if (parts.length < 4) {
            return null;
        }

        String hash = parts[0].trim();
        String author = parts[1].trim();
        String dateStr = parts[2].trim();
        String message = parts[3].trim();

        LocalDateTime dateTime;
        try {
            dateTime = LocalDateTime.parse(dateStr, dateTimeFormatter);
        } catch (Exception e) {
            // If date parsing fails, skip this commit
            return null;
        }

        return new CommitRecord(hash, author, dateTime, message);
    }
}

