module com.example.projectflow {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;
    opens com.example.projectflow to javafx.fxml;
    opens controllers to javafx.fxml;
    opens dao to javafx.fxml;
    opens models to javafx.fxml;
    opens utils to javafx.fxml;
    exports com.example.projectflow;
    exports controllers;
    exports dao;
    exports models;
    exports utils;
}