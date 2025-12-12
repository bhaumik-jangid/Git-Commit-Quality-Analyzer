package com.bhaumik.gcqa.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * Controller for the main view.
 * Day 4: only placeholder data and wiring.
 */
public class MainController {

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

    private final ObservableList<CommitViewModel> commitData =
            FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Configure table columns
        hashColumn.setCellValueFactory(new PropertyValueFactory<>("hash"));
        authorColumn.setCellValueFactory(new PropertyValueFactory<>("author"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        messageColumn.setCellValueFactory(new PropertyValueFactory<>("message"));
        scoreColumn.setCellValueFactory(new PropertyValueFactory<>("score"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));

        // For Day 4: add some dummy placeholder rows
        commitData.addAll(
                new CommitViewModel("a1b2c3d", "User 1", "2025-12-07",
                        "Add initial project structure", 80, "Good"),
                new CommitViewModel("d4e5f6g", "User 2", "2025-12-06",
                        "Update README", 60, "Average"),
                new CommitViewModel("h7i8j9k", "User 3", "2025-12-05",
                        "fix", 20, "Poor")
        );

        commitTable.setItems(commitData);

        // Summary counts (dummy for now)
        goodCountLabel.setText("1");
        averageCountLabel.setText("1");
        poorCountLabel.setText("1");

        // Dummy pie chart data
        qualityPieChart.setData(FXCollections.observableArrayList(
                new PieChart.Data("Good", 1),
                new PieChart.Data("Average", 1),
                new PieChart.Data("Poor", 1)
        ));
    }
}
