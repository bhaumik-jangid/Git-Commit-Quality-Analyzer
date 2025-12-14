package com.bhaumik.gcqa.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.*;
import java.util.*;

@RestController
@RequestMapping("/api/repo")
public class RepoController {

    private static final String BASE_REPO_DIR = "/home/azazil/git-repos";

    // -------------------- PREPARE / CLONE REPO --------------------
    @PostMapping("/prepare")
    public ResponseEntity<Map<String, String>> prepareRepo(
            @RequestBody Map<String, String> body
    ) throws IOException, InterruptedException {

        String repoUrl = body.get("repoUrl");
        if (repoUrl == null || repoUrl.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        String repoName = repoUrl
                .substring(repoUrl.lastIndexOf('/') + 1)
                .replace(".git", "");

        Path repoPath = Paths.get(BASE_REPO_DIR, repoName);

        if (!Files.exists(repoPath)) {
            Files.createDirectories(repoPath.getParent());

            ProcessBuilder pb = new ProcessBuilder(
                    "git", "clone", repoUrl, repoPath.toString()
            );
            pb.redirectErrorStream(true);

            Process process = pb.start();
            process.waitFor();
        }

        return ResponseEntity.ok(
                Map.of("repoPath", repoPath.toString())
        );
    }

    // -------------------- LIST CLONED REPOS --------------------
    @GetMapping("/list")
    public ResponseEntity<List<Map<String, String>>> listRepos()
            throws IOException, InterruptedException {

        Path baseDir = Paths.get(BASE_REPO_DIR);
        if (!Files.exists(baseDir)) {
            return ResponseEntity.ok(List.of());
        }

        List<Map<String, String>> repos = new ArrayList<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(baseDir)) {
            for (Path repo : stream) {
                if (!Files.isDirectory(repo.resolve(".git"))) continue;

                String repoUrl = getRepoUrl(repo);
                String author = getFirstCommitAuthor(repo);
                String created = getFirstCommitDate(repo);

                repos.add(Map.of(
                        "name", repo.getFileName().toString(),
                        "repoPath", repo.toString(),
                        "repoUrl", repoUrl,
                        "author", author,
                        "created", created
                ));
            }
        }

        return ResponseEntity.ok(repos);
    }

    // -------------------- GIT HELPERS --------------------
    private String runGit(Path repo, String... args)
            throws IOException, InterruptedException {

        List<String> cmd = new ArrayList<>();
        cmd.add("git");
        cmd.add("-C");
        cmd.add(repo.toString());
        cmd.addAll(List.of(args));

        Process p = new ProcessBuilder(cmd).start();
        BufferedReader br = new BufferedReader(
                new InputStreamReader(p.getInputStream())
        );
        String out = br.readLine();
        p.waitFor();
        return out == null ? "-" : out;
    }

    private String getRepoUrl(Path repo) throws IOException, InterruptedException {
        return runGit(repo, "remote", "get-url", "origin");
    }

    private String getFirstCommitAuthor(Path repo) throws IOException, InterruptedException {
        return runGit(repo, "log", "--reverse", "--format=%an", "-1");
    }

    private String getFirstCommitDate(Path repo) throws IOException, InterruptedException {
        return runGit(repo, "log", "--reverse", "--format=%ad", "--date=short", "-1");
    }
}
