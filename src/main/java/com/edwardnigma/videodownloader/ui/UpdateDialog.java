package com.edwardnigma.videodownloader.ui;

import com.edwardnigma.videodownloader.service.UpdateInfo;
import javafx.scene.control.Alert;
import javafx.stage.Window;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import java.util.Locale;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public final class UpdateDialog {

    private UpdateDialog() {
    }

    public static void showMessage(
            Window owner,
            Alert.AlertType type,
            String title,
            String message
    ) {
        Alert alert = new Alert(type);
        alert.initOwner(owner);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    // =подтверждение обновления
    public static boolean confirmUpdate(Window owner, UpdateInfo info) {

        double sizeMiB = info.downloadSize() / (1024.0 * 1024.0);

        ButtonType updateNow = new ButtonType(
                "Update now",
                ButtonBar.ButtonData.OK_DONE
        );

        ButtonType later = new ButtonType(
                "Later",
                ButtonBar.ButtonData.CANCEL_CLOSE
        );

        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        dialog.initOwner(owner);

        dialog.setTitle("yt-dlp Update");
        dialog.setHeaderText("A new yt-dlp version is available");

        dialog.setContentText(
                "Installed: " + info.installedVersion() +
                        "\nAvailable: " + info.latestVersion() +
                        "\nDownload size: " +
                        String.format(Locale.US, "%.2f MiB", sizeMiB) +
                        "\n\nUpdating is recommended to maintain compatibility " +
                        "with video hosting platforms."
        );

        dialog.getButtonTypes().setAll(updateNow, later);

        return dialog.showAndWait().orElse(later) == updateNow;
    }

    // обычные уведомления
    public static Stage showProgress(Window owner) {

        Stage progressStage = new Stage();
        progressStage.initOwner(owner);
        progressStage.initModality(Modality.WINDOW_MODAL);

        progressStage.setTitle("Updating yt-dlp");
        progressStage.setResizable(false);

        Label statusLabel = new Label("Downloading and installing yt-dlp...");
        ProgressIndicator indicator = new ProgressIndicator();

        VBox content = new VBox(15, statusLabel, indicator);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(20));

        progressStage.setScene(new Scene(content, 340, 150));

        progressStage.setOnCloseRequest(event -> event.consume());

        progressStage.show();

        return progressStage;
    }

}