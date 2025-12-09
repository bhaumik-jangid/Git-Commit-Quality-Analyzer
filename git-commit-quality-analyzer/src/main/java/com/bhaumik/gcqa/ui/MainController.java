package com.bhaumik.gcqa.ui;

import com.bhaumik.gcqa.core.CommitAnalyzer;
import com.bhaumik.gcqa.core.GitLogReader;
import com.bhaumik.gcqa.model.CommitRecord;
import com.bhaumik.gcqa.model.CommitScore;
import javafx.animation.ScaleTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Duration;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MainController {

    // set some default so app opens with something
    private static final String DEFAULT_REPO_PATH =
            "/home/azazil/Desktop/dev-pro/git-commit-quality-analyzer";

    // top bar
    @FXML
    private TextField repoPathField;

    // table
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

    // charts
    @FXML
    private PieChart qualityPieChart;

    @FXML
    private LineChart<Number, Number> scoreTrendChart;

    // bottom bar stats
    @FXML
    private Label goodCountLabel;
    @FXML
    private Label averageCountLabel;
    @FXML
    private Label poorCountLabel;
    @FXML
    private Label statusLabel;

    // top stat cards
    @FXML
    private Label totalCommitsLabel;
    @FXML
    private Label goodPercentLabel;
    @FXML
    private Label poorPercentLabel;
    @FXML
    private Label avgScoreLabel;

    private final ObservableList<CommitViewModel> commitData =
            FXCollections.observableArrayList();

    private final CommitAnalyzer analyzer = new CommitAnalyzer();

    private final DateTimeFormatter dateFormatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    public void initialize() {
        // table columns mapping
        hashColumn.setCellValueFactory(new PropertyValueFactory<>("hash"));
        authorColumn.setCellValueFactory(new PropertyValueFactory<>("author"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        messageColumn.setCellValueFactory(new PropertyValueFactory<>("message"));
        scoreColumn.setCellValueFactory(new PropertyValueFactory<>("score"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));

        commitTable.setItems(commitData);

        // default repo path
        repoPathField.setText(DEFAULT_REPO_PATH);

        // chart basic setup
        scoreTrendChart.setLegendVisible(false);

        loadDataFromRepo();
    }

    @FXML
    private void onRefreshClicked() {
        loadDataFromRepo();
    }

    /**
     * Adds hover effects to each slice in the given pie chart.
     * Hover: scale up hovered slice, dim others, and show tooltip "commits / totalCommits".
     */
    public void addPieHoverEffects(PieChart pieChart, int totalCommits) {

        for (PieChart.Data data : pieChart.getData()) {

            // data.getNode() might be null at this point; attach when node becomes available
            data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    Node slice = newNode;

                    // create tooltip for this slice (value / total)
                    Tooltip tt = new Tooltip((int) data.getPieValue() + " / " + totalCommits);

                    // Scale transition for hover-in (we'll stop it on exit and reset)
                    ScaleTransition hoverScale = new ScaleTransition(Duration.millis(180), slice);
                    hoverScale.setToX(1.15);
                    hoverScale.setToY(1.15);

                    // mouse entered
                    slice.setOnMouseEntered(e -> {
                        // update tooltip text (in case values changed)
                        tt.setText((int) data.getPieValue() + " / " + totalCommits);
                        Tooltip.install(slice, tt);

                        // play scale
                        hoverScale.playFromStart();

                        // dim other slices
                        for (PieChart.Data d2 : pieChart.getData()) {
                            if (d2 != data && d2.getNode() != null) {
                                d2.getNode().setOpacity(0.35);
                            }
                        }
                    });

                    // mouse exited
                    slice.setOnMouseExited(e -> {
                        // stop animation & reset scale
                        hoverScale.stop();
                        slice.setScaleX(1.0);
                        slice.setScaleY(1.0);

                        // reset opacity for all slices
                        for (PieChart.Data d2 : pieChart.getData()) {
                            if (d2.getNode() != null) {
                                d2.getNode().setOpacity(1.0);
                            }
                        }

                        Tooltip.uninstall(slice, tt);
                    });

                    // make slice cursor-hand if desired (optional)
                    slice.setStyle("-fx-cursor: hand;");
                }
            });
        }
    }

    private void loadDataFromRepo() {
        commitData.clear();
        qualityPieChart.getData().clear();
        scoreTrendChart.getData().clear();
        statusLabel.setText("Loading commits...");

        String pathText = repoPathField.getText();
        if (pathText == null || pathText.isBlank()) {
            statusLabel.setText("Error: Repository path is empty.");
            showErrorAlert("Invalid Path", "Please enter a valid repository path.");
            resetSummaryAnalytics();
            return;
        }

        GitLogReader reader = new GitLogReader(Paths.get(pathText.trim()));

        int good = 0;
        int average = 0;
        int poor = 0;
        int totalScore = 0;

        try {
            List<CommitRecord> records = reader.readCommits(Integer.MAX_VALUE);

            int index = 1;
            XYChart.Series<Number, Number> series = new XYChart.Series<>();

            for (CommitRecord record : records) {
                CommitScore score = analyzer.analyze(record);
                String category = score.getCategory();
                int s = score.getScore();

                switch (category) {
                    case "Good" -> good++;
                    case "Average" -> average++;
                    case "Poor" -> poor++;
                    default -> {
                    }
                }

                totalScore += s;

                String hashShort = record.getHash();
                if (hashShort != null && hashShort.length() > 7) {
                    hashShort = hashShort.substring(0, 7);
                }

                CommitViewModel vm = new CommitViewModel(
                        hashShort,
                        record.getAuthor(),
                        record.getDateTime().format(dateFormatter),
                        record.getMessage(),
                        s,
                        category
                );

                commitData.add(vm);

                series.getData().add(new XYChart.Data<>(index, s));
                index++;
            }

            scoreTrendChart.getData().add(series);

            updateSummaryAndCharts(good, average, poor, totalScore, commitData.size());

            statusLabel.setText("Loaded " + commitData.size() + " commits from repository.");

        } catch (IOException e) {
            System.err.println("Failed to load git commits: " + e.getMessage());
            commitData.clear();
            resetSummaryAnalytics();

            statusLabel.setText("Error: " + e.getMessage());
            showErrorAlert("Failed to load commits", e.getMessage());
        }
    }

    private void resetSummaryAnalytics() {
        goodCountLabel.setText("0");
        averageCountLabel.setText("0");
        poorCountLabel.setText("0");

        totalCommitsLabel.setText("0");
        goodPercentLabel.setText("0%");
        poorPercentLabel.setText("0%");
        avgScoreLabel.setText("0");

        qualityPieChart.setData(FXCollections.observableArrayList());
        // safe to call; will attach to nothing if there are no slices
        addPieHoverEffects(qualityPieChart, 0);
        scoreTrendChart.getData().clear();
    }

    private void updateSummaryAndCharts(int good, int average, int poor,
                                        int totalScore, int totalCommits) {

        goodCountLabel.setText(String.valueOf(good));
        averageCountLabel.setText(String.valueOf(average));
        poorCountLabel.setText(String.valueOf(poor));
        totalCommitsLabel.setText(String.valueOf(totalCommits));

        if (totalCommits > 0) {
            double goodPct = (good * 100.0) / totalCommits;
            double poorPct = (poor * 100.0) / totalCommits;
            double avgScore = totalScore / (double) totalCommits;

            goodPercentLabel.setText(String.format("%.1f%%", goodPct));
            poorPercentLabel.setText(String.format("%.1f%%", poorPct));
            avgScoreLabel.setText(String.format("%.1f", avgScore));
        } else {
            goodPercentLabel.setText("0%");
            poorPercentLabel.setText("0%");
            avgScoreLabel.setText("0");
        }

        qualityPieChart.setData(FXCollections.observableArrayList(
                new PieChart.Data("Good", good),
                new PieChart.Data("Average", average),
                new PieChart.Data("Poor", poor)
        ));

        // attach hover effects now that data is in place
        addPieHoverEffects(qualityPieChart, totalCommits);
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
