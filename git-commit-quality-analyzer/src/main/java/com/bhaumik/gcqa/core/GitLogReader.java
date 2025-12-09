package com.bhaumik.gcqa.core;

import com.bhaumik.gcqa.model.CommitRecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
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
        builder.redirectErrorStream(true);

        Process process = builder.start();

        try (BufferedReader reader =
                 new BufferedReader(new InputStreamReader(process.getInputStream()))) {

            String line;
            while ((line = reader.readLine()) != null) {
                CommitRecord record = parseLine(line);
                if (record != null) {
                    records.add(record);
                }
            }
        }

        // waitFor can throw InterruptedException — handle it and propagate as IOException
        try {
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IOException("git log failed (exit code: " + exitCode + ")");
            }
        } catch (InterruptedException e) {
            // restore interrupt status and wrap
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while waiting for git process", e);
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

        // git --date=iso produces an offset like "2025-12-09 10:00:00 +0530"
        OffsetDateTime odt = OffsetDateTime.parse(dateStr, dateTimeFormatter);
        LocalDateTime dateTime = odt.toLocalDateTime();

        return new CommitRecord(hash, author, dateTime, message);
    }
}
