package com.edwardnigma.videodownloader.ui;

import javafx.scene.control.Alert;
import javafx.stage.Window;

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
}