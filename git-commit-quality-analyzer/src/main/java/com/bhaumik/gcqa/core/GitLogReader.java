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

    public List<CommitRecord> readCommits(int maxCount) throws IOException {

        List<CommitRecord> records = new ArrayList<>();

        List<String> command = new ArrayList<>();
        command.add("git");
        command.add("-C");
        command.add(repoPath.toString());
        command.add("log");
        command.add("--pretty=format:%H|%an|%ad|%s");
        command.add("--date=iso");

        if (maxCount > 0) {
            command.add("-n");
            command.add(String.valueOf(maxCount));
        }

        ProcessBuilder builder = new ProcessBuilder(command);
        Process process = builder.start();

        StringBuilder errorOutput = new StringBuilder();

        try (
            BufferedReader stdout = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            BufferedReader stderr = new BufferedReader(
                    new InputStreamReader(process.getErrorStream()))
        ) {

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

        }

        try {
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IOException(
                        "git log failed with exit code " + exitCode +
                        "\nError:\n" + errorOutput
                );
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("git log interrupted", e);
        }

        return records;
    }

    private CommitRecord parseLine(String line) {
        String[] parts = line.split("\\|", 4);
        if (parts.length < 4) return null;

        try {
            return new CommitRecord(
                    parts[0].trim(),
                    parts[1].trim(),
                    LocalDateTime.parse(parts[2].trim(), dateTimeFormatter),
                    parts[3].trim()
            );
        } catch (Exception e) {
            return null;
        }
    }
}
