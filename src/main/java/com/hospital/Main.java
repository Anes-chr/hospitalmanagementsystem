package com.hospital;

import com.hospital.service.*;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.File;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Ensure data directory exists
        File dataDir = new File("data");
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }

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
        // Initialize services in the correct order
        // (first initialize services that don't depend on others)

        // 1. Initialize authentication service (creates default users)
        AuthService.getInstance();

        // 2. Initialize hospital service (creates default hospital and blocks)
        HospitalService.getInstance();

        // 3. Initialize doctor service (depends on hospital blocks)
        DoctorService.getInstance();

        // 4. Initialize patient service (creates sample patient data)
        PatientService patientService = new PatientService();
        patientService.initializeWithSampleData();

        // 5. Initialize appointment service (depends on doctors and patients)
        AppointmentService.getInstance().cleanupDeletedDoctorAppointments();

        // 6. Initialize medication and pharmacy services
        MedicationService.getInstance();
        PrescriptionService.getInstance();

        // 7. Initialize medical test service
        MedicalTestService.getInstance();
    }

    public static void main(String[] args) {
        launch(args);
    }
}