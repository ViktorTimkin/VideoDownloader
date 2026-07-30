package com.edwardnigma.videodownloader.ui;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;

import java.io.File;

public class MainWindow {

    private final BorderPane root;
    private final Label urlLabel;
    private final TextField urlField;
    private final ToggleButton quality480Button;
    private final ToggleButton  quality720Button;
    private final ToggleGroup toggleGroup;
    private final Label savePathLabel;
    private final ProgressBar progressBar;
    private final TextField savePathField;
    private final Button chooseFolderButton;
    private final Button downloadButton;


    private QualityVideo qualityVideo =  QualityVideo.P480;

    public MainWindow() {
        this.root = new BorderPane();
        root.setPadding(new Insets(20));
        this.urlLabel = new Label("URL");
        this.urlField = new TextField();
        this.quality480Button = new ToggleButton ("480p");
        this.quality720Button = new ToggleButton ("720p");
        this.toggleGroup = new ToggleGroup();
        quality480Button.setToggleGroup(toggleGroup);
        quality480Button.setSelected(true);
        quality720Button.setToggleGroup(toggleGroup);
        this.savePathLabel = new Label("Папка сохранения");
        this.savePathField = new TextField();
        savePathField.setPrefWidth(500);
        this.chooseFolderButton = new Button("Выбрать");
        this.downloadButton = new Button("Скачать");
        this.progressBar = new ProgressBar(0);
        progressBar.setMaxWidth(Double.MAX_VALUE);


        HBox qualityHBox = new HBox(10);
        qualityHBox.getChildren().addAll(
                quality480Button,
                quality720Button
        );

        HBox savePathHBox = new HBox(10);
        savePathHBox.getChildren().addAll(
                savePathLabel,
                savePathField,
                chooseFolderButton
        );

        VBox vBox = new VBox(10);
        vBox.getChildren().addAll(
                urlLabel,
                urlField,
                qualityHBox,
                savePathHBox,
                downloadButton,
                progressBar
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
        initializeChooseFolderButton();
        initializeQualityButtons();
    }
    private void initializeQualityButtons() {
        quality480Button.setOnAction(event -> qualityVideo = QualityVideo.P480);
        quality720Button.setOnAction(event -> qualityVideo = QualityVideo.P720);
    }
    private void initializeChooseFolderButton() {
        chooseFolderButton.setOnAction(event -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setInitialDirectory(new File(System.getProperty("user.home"))); // стартовая папка
            File selectedDirectory = directoryChooser.showDialog(root.getScene().getWindow());//окно + выбор

            if (selectedDirectory != null){
                savePathField.setText(selectedDirectory.getAbsolutePath());
            }
        });
    }
}