module com.hospital {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;

    opens com.hospital to javafx.fxml;
    opens com.hospital.controller to javafx.fxml;
    opens com.hospital.model to javafx.fxml, com.fasterxml.jackson.databind;

    exports com.hospital;
    exports com.hospital.controller;
    exports com.hospital.model;
}