package com.bhaumik.gcqa.api;

import com.bhaumik.gcqa.core.CommitAnalyzer;
import com.bhaumik.gcqa.core.GitLogReader;
import com.bhaumik.gcqa.model.CommitRecord;
import com.bhaumik.gcqa.model.CommitScore;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class AnalyzeController {

    private final CommitAnalyzer analyzer = new CommitAnalyzer();

    @GetMapping("/analyze")
    public ResponseEntity<?> analyze(
            @RequestParam String repoPath,
            @RequestParam(defaultValue = "200") int limit
    ) {
        try {
            GitLogReader reader = new GitLogReader(Paths.get(repoPath));
            List<CommitRecord> records = reader.readCommits(limit > 0 ? limit : Integer.MAX_VALUE);

            var dto = records.stream().map(r -> {
                CommitScore s = analyzer.analyze(r);
                return new CommitDTO(r.getHash(), r.getAuthor(), r.getDateTime().toString(),
                        r.getMessage(), s.getScore(), s.getCategory());
            }).collect(Collectors.toList());

            return ResponseEntity.ok(dto);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body("Error: " + ex.getMessage());
        }
    }

    public static record CommitDTO(String hash, String author, String date, String message, int score, String category) { }
}
