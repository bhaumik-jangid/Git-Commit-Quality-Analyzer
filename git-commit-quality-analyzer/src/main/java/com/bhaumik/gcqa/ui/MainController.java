// JavaFX controller for the main dashboard UI

package com.bhaumik.gcqa.ui;

import com.bhaumik.gcqa.core.CommitAnalyzer;
import com.bhaumik.gcqa.core.GitLogReader;
import com.bhaumik.gcqa.model.CommitRecord;
import com.bhaumik.gcqa.model.CommitScore;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller for the main view.
 * Day 5: hook UI with real Git + analyzer.
 */
public class MainController {

    // TODO: change this to your actual repo path
    private static final String DEFAULT_REPO_PATH =
            "/home/azazil/Desktop/Cyni";

    @FXML
    private Label statusLabel;

    @FXML
    private TableView<CommitViewModel> commitTable;

    @FXML
    private TableColumn<CommitViewModel, String> hashColumn;

    @FXML
    private TableColumn<CommitViewModel, String> authorColumn;

    @FXML
    private TableColumn<CommitViewModel, String> dateColumn;

    @FXML
    private TableColumn<CommitViewModel, String> messageColumn;

    @FXML
    private TableColumn<CommitViewModel, Integer> scoreColumn;

    @FXML
    private TableColumn<CommitViewModel, String> categoryColumn;

    @FXML
    private PieChart qualityPieChart;

    @FXML
    private Label goodCountLabel;

    @FXML
    private Label averageCountLabel;

    @FXML
    private Label poorCountLabel;

    @FXML
    private javafx.scene.control.TextField repoPathField;

    private final ObservableList<CommitViewModel> commitData =
            FXCollections.observableArrayList();

    private final CommitAnalyzer analyzer = new CommitAnalyzer();

    private final DateTimeFormatter dateFormatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    public void initialize() {
        // Configure table columns
        hashColumn.setCellValueFactory(new PropertyValueFactory<>("hash"));
        authorColumn.setCellValueFactory(new PropertyValueFactory<>("author"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        messageColumn.setCellValueFactory(new PropertyValueFactory<>("message"));
        scoreColumn.setCellValueFactory(new PropertyValueFactory<>("score"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));

        commitTable.setItems(commitData);
        repoPathLabel.setText(DEFAULT_REPO_PATH);

        // Initial load
        loadDataFromRepo();
    }

    @FXML
    private void onRefreshClicked() {
        loadDataFromRepo();
    }

    private void loadDataFromRepo() {
        commitData.clear();
        statusLabel.setText("Loading commits...");

        String pathText = repoPathField.getText();
        if (pathText == null || pathText.isBlank()) {
                statusLabel.setText("Error: Repository path is empty.");
                showErrorAlert("Invalid Path", "Please enter a valid repository path.");
                updateSummaryAndChart(0, 0, 0);
                return;
        }

        GitLogReader reader = new GitLogReader(Paths.get(pathText.trim()));

        int good = 0;
        int average = 0;
        int poor = 0;

        try {
                List<CommitRecord> records = reader.readCommits(50);

                for (CommitRecord record : records) {
                CommitScore score = analyzer.analyze(record);
                String category = score.getCategory();

                switch (category) {
                        case "Good" -> good++;
                        case "Average" -> average++;
                        case "Poor" -> poor++;
                        default -> { }
                }

                String hashShort = record.getHash();
                if (hashShort != null && hashShort.length() > 7) {
                        hashShort = hashShort.substring(0, 7);
                }

                CommitViewModel vm = new CommitViewModel(
                        hashShort,
                        record.getAuthor(),
                        record.getDateTime().format(dateFormatter),
                        record.getMessage(),
                        score.getScore(),
                        category
                );

                commitData.add(vm);
                }

                updateSummaryAndChart(good, average, poor);

                statusLabel.setText("Loaded " + commitData.size() + " commits from repository.");

        } catch (IOException e) {
                System.err.println("Failed to load git commits: " + e.getMessage());
                commitData.clear();
                updateSummaryAndChart(0, 0, 0);

                statusLabel.setText("Error: " + e.getMessage());
                showErrorAlert("Failed to load commits", e.getMessage());
        }
    }

    private void updateSummaryAndChart(int good, int average, int poor) {
        goodCountLabel.setText(String.valueOf(good));
        averageCountLabel.setText(String.valueOf(average));
        poorCountLabel.setText(String.valueOf(poor));

        qualityPieChart.setData(FXCollections.observableArrayList(
                new PieChart.Data("Good", good),
                new PieChart.Data("Average", average),
                new PieChart.Data("Poor", poor)
        ));
    }

    private void showErrorAlert(String title, String message) {
        javafx.scene.control.Alert alert =
                new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
