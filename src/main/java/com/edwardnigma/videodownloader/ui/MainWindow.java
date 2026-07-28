package com.edwardnigma.videodownloader.ui;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class MainWindow {

    private final BorderPane root;
    private final Label urlLabel;
    private final TextField urlField;
    private final Button quality480Button;
    private final Button quality720Button;
    private final Label promptText;
    private final ProgressBar progressBar;
    private final TextField savePathField;
    private final Button chooseFolder;
    private final Button downloadButton;


    private QualityVideo qualityVideo =  QualityVideo.P480;

    public MainWindow() {
        this.root = new BorderPane();
        root.setPadding(new Insets(20));
        this.urlLabel = new Label("URL");
        this.urlField = new TextField();
        this.quality480Button = new Button("480");
        this.quality720Button = new Button("720");
        this.promptText = new Label("Папка сохранения");
        this.savePathField = new TextField();
        this.chooseFolder = new Button("Выбрать");
        this.downloadButton = new Button("Скачать");
        this.progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(400);

        HBox qualityHBox = new HBox(10);
        qualityHBox.getChildren().addAll(
                quality480Button,
                quality720Button
        );

        HBox savePathHBox = new HBox(10);
        savePathHBox.getChildren().addAll(
                promptText,
                savePathField,
                chooseFolder
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
        quality480Button.setOnAction(event -> {
            qualityVideo = QualityVideo.P480;
        });
        quality720Button.setOnAction(event -> {
            qualityVideo = QualityVideo.P720;
        });
        chooseFolder.setOnAction(event -> {});
    }


}