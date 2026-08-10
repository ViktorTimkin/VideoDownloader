package com.edwardnigma.videodownloader.ui;

import com.edwardnigma.videodownloader.service.SettingsService;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import com.edwardnigma.videodownloader.service.DownloaderService;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

public class MainWindow {

    private final BorderPane root;
    private final Label urlLabel;
    private final TextField urlField;
    private final ToggleButton quality360Button;
    private final ToggleButton quality480Button;
    private final ToggleButton quality720Button;
    private final ToggleButton quality1080Button;
    private final ToggleGroup toggleGroup;
    private final Label savePathLabel;
    private final ProgressBar progressBar;
    private final TextField savePathField;
    private final Button chooseFolderButton;
    private final Button downloadButton;
    private final Button cancelButton;
    private final Label downloadInfoLabel;
    private final DownloaderService downloaderService = new DownloaderService();
    private QualityVideo qualityVideo = QualityVideo.P480;
    private Task<Void> currentTask;
    private final CheckBox openFolderCheckBox;

    public MainWindow() {
        this.root = new BorderPane();
        root.setPadding(new Insets(20));
        this.urlLabel = new Label("URL");
        this.urlField = new TextField();
        this.quality360Button = new ToggleButton("360p");
        this.quality480Button = new ToggleButton("480p");
        this.quality720Button = new ToggleButton("720p");
        this.quality1080Button = new ToggleButton("1080p");
        this.toggleGroup = new ToggleGroup();
        quality360Button.setToggleGroup(toggleGroup);
        quality480Button.setToggleGroup(toggleGroup);
        quality720Button.setToggleGroup(toggleGroup);
        quality1080Button.setToggleGroup(toggleGroup);
        quality480Button.setSelected(true);
        this.savePathLabel = new Label("Save folder");
        this.savePathField = new TextField();
        savePathField.setPrefWidth(500);
        this.chooseFolderButton = new Button("Choose");
        this.downloadButton = new Button("Download");
        this.cancelButton = new Button("Cancel");
        this.progressBar = new ProgressBar(0);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        this.downloadInfoLabel = new Label();
        downloadInfoLabel.setWrapText(true);
        this.openFolderCheckBox = new CheckBox("Open folder");

        HBox qualityHBox = new HBox(10);
        qualityHBox.getChildren().addAll(
                quality360Button,
                quality480Button,
                quality720Button,
                quality1080Button
        );

        HBox savePathHBox = new HBox(20);
        savePathHBox.getChildren().addAll(
                savePathLabel,
                savePathField,
                chooseFolderButton,
                openFolderCheckBox
        );

        HBox downloadCancelButton = new HBox(710);
        downloadCancelButton.getChildren().addAll(
                downloadButton,
                cancelButton
        );

        VBox vBox = new VBox(15);
        vBox.getChildren().addAll(
                urlLabel,
                urlField,
                qualityHBox,
                savePathHBox,
                downloadCancelButton,
                progressBar,
                downloadInfoLabel
        );

        root.setCenter(vBox);

        initialize();
    }

    public Parent getRoot() {
        return root;
    }

    public String getUrl() {
        return urlField.getText();
    }

    public String getSavePath() {
        return savePathField.getText();
    }


    public QualityVideo getQualityVideo() {
        return qualityVideo;
    }

    private void initialize() {
        savePathField.setText(SettingsService.getLastFolder());

        urlField.textProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (!newValue.isBlank()) {
                        urlField.setStyle("");
                    }
                });
        savePathField.textProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (!newValue.isBlank()) {
                        savePathField.setStyle("");
                    }
                }
        );

        initializeChooseFolderButton();
        initializeQualityButtons();
        initializeDownloadButton();
        initializeCancelButton();
        openFolderCheckBox.setSelected(SettingsService.isOpenFolderAfterDownload());
        openFolderCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> SettingsService.saveOpenFolderAfterDownload(newValue));
    }

    private void initializeQualityButtons() {
        quality360Button.setOnAction(event -> qualityVideo = QualityVideo.P360);
        quality480Button.setOnAction(event -> qualityVideo = QualityVideo.P480);
        quality720Button.setOnAction(event -> qualityVideo = QualityVideo.P720);
        quality1080Button.setOnAction(event -> qualityVideo = QualityVideo.P1080);
    }

    private void initializeChooseFolderButton() {
        chooseFolderButton.setOnAction(event -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setInitialDirectory(new File(SettingsService.getLastFolder())); // стартовая папка
            File selectedDirectory = directoryChooser.showDialog(root.getScene().getWindow());//окно + выбор

            if (selectedDirectory != null) {
                savePathField.setText(selectedDirectory.getAbsolutePath());
                SettingsService.saveLastFolder(selectedDirectory.getAbsolutePath());
            }
        });
    }

    private void initializeDownloadButton() {
        downloadButton.setOnAction(event -> {

            if (getUrl().isBlank()) {
                urlField.setStyle("-fx-border-color: red;");
                return;
            }
            if (getSavePath().isBlank()) {
                savePathField.setStyle("-fx-border-color: red;");
                return;
            }

            lockInterface();

            currentTask = downloaderService.download(
                    getUrl(),
                    getSavePath(),
                    getQualityVideo()
            );
            downloadInfoLabel.textProperty().bind(currentTask.messageProperty()); // для доп информации
            progressBar.progressProperty().bind(currentTask.progressProperty());

            currentTask.setOnSucceeded(e -> {
                downloadInfoLabel.textProperty().unbind();
                downloadInfoLabel.setText("Download complete.");

                if (openFolderCheckBox.isSelected()) {
                    try {
                        Desktop.getDesktop().open(new File(getSavePath()));
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }

                unlockInterface();
            });
            currentTask.setOnFailed(e -> {
                downloadInfoLabel.textProperty().unbind();
                downloadInfoLabel.setText("Download error");
                unlockInterface();
            });
            currentTask.setOnCancelled(e -> {
                downloadInfoLabel.textProperty().unbind();
                downloadInfoLabel.setText("Download cancelled.");
                unlockInterface();
            });

            Thread thread = new Thread(currentTask);
            thread.setName("yt-dlp - download thread");
            thread.setDaemon(true);
            thread.start();
        });
    }

    private void initializeCancelButton() {
        cancelButton.setOnAction(event -> {
            if (currentTask != null) {
                currentTask.cancel();
            }
            try {
                downloaderService.cancelDownload();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        });
    }

    private void lockInterface() {
        downloadButton.setDisable(true);
        chooseFolderButton.setDisable(true);
        urlField.setDisable(true);
        savePathField.setDisable(true);
        quality360Button.setDisable(true);
        quality480Button.setDisable(true);
        quality720Button.setDisable(true);
        quality1080Button.setDisable(true);
        cancelButton.setDisable(false);

    }

    private void unlockInterface() {
        downloadButton.setDisable(false);
        chooseFolderButton.setDisable(false);
        urlField.setDisable(false);
        savePathField.setDisable(false);
        quality360Button.setDisable(false);
        quality480Button.setDisable(false);
        quality720Button.setDisable(false);
        quality1080Button.setDisable(false);
        cancelButton.setDisable(true);
        progressBar.progressProperty().unbind();
        progressBar.setProgress(0);
        currentTask = null; // чтобы не оставалась ссылка на старую задачу

    }

}