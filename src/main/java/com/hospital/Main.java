package com.hospital;

import com.hospital.service.AuthService;
import com.hospital.service.HospitalService;
import com.hospital.service.PatientService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Initialize services to create default data
        initializeServices();

        // Load login view
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hospital/view/Login.fxml"));
        Parent root = loader.load();

        // Set up scene and stylesheets
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/com/hospital/css/darkTheme.css").toExternalForm());

        // Configure primary stage
        primaryStage.setTitle("Hospital Management System");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);

        // Set application icon
        try {
            primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/com/hospital/images/hospital_icon.png")));
        } catch (Exception e) {
            System.err.println("Could not load application icon: " + e.getMessage());
        }

        // Show the stage
        primaryStage.show();
    }

    private void initializeServices() {
        // Initialize authentication service (creates default users)
        AuthService.getInstance();

        // Initialize hospital service (creates default hospital and blocks)
        HospitalService.getInstance();

        // Initialize patient service (creates sample patient data)
        PatientService patientService = new PatientService();
        patientService.initializeWithSampleData();
    }

    public static void main(String[] args) {
        launch(args);
    }
}