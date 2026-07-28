package com.edwardnigma.videodownloader.ui;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class MainWindow {

    private final VBox root;
    private final Label urlLabel;
    private final TextField urlField;
    private final Button downloadButton;

    public MainWindow() {
        this.root = new VBox(10); // отступы
        this.urlLabel = new Label("URL");
        this.urlField = new TextField();
        this.downloadButton = new Button("Скачать");

        root.getChildren().addAll(
                urlLabel,
                urlField,
                downloadButton
        );
    }

    public Parent getRoot() {
        return root;
    }

}