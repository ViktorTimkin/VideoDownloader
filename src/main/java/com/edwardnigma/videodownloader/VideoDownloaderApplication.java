package com.edwardnigma.videodownloader;

import com.edwardnigma.videodownloader.ui.MainWindow;
import javafx.application.Application;
import javafx.scene.Scene;

import javafx.stage.Stage;



public class VideoDownloaderApplication extends Application {


    @Override
    public void start(Stage stage) {

        MainWindow window = new MainWindow();

        Scene scene = new Scene(window.getRoot(), 900, 250);

        stage.setScene(scene);
        stage.setTitle("Video Downloader");
        stage.show();
    }
}
