package com.edwardnigma.videodownloader;

import com.edwardnigma.videodownloader.ui.MainWindow;
import javafx.application.Application;
import javafx.scene.Scene;

import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Objects;


public class VideoDownloaderApplication extends Application {


    @Override
    public void start(Stage stage) {

        MainWindow window = new MainWindow();

        Scene scene = new Scene(window.getRoot(), 850, 300);
        stage.setMinHeight(350);
        stage.setMinWidth(800);
        stage.setScene(scene);
        stage.setTitle("Video Downloader");
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icon.png"))));
        stage.show();
    }
}
