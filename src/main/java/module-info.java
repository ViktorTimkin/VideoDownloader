module com.edwardnigma.videodownloader {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.almasb.fxgl.all;
    requires javafx.graphics;

    opens com.edwardnigma.videodownloader to javafx.fxml;
    exports com.edwardnigma.videodownloader;
}