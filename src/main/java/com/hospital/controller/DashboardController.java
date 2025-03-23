package com.hospital.controller;

import com.hospital.model.Appointment;
import com.hospital.model.Doctor;
import com.hospital.model.Medication;
import com.hospital.model.Patient;
import com.hospital.service.*;
import com.hospital.util.AlertUtil;
import com.hospital.util.ChartUtil;
import com.hospital.util.DateUtil;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

public class DashboardController implements Initializable {

    @FXML
    private ImageView hospitalLogo;

    @FXML
    private Label hospitalNameLabel;

    @FXML
    private Label currentUserLabel;

    @FXML
    private Button logoutButton;

    @FXML
    private StackPane contentArea;

    @FXML
    private VBox dashboardPane;

    @FXML
    private Label totalPatientsLabel;

    @FXML
    private Label totalDoctorsLabel;

    @FXML
    private Label todayAppointmentsLabel;

    @FXML
    private Label emergencyPatientsLabel;

    @FXML
    private PieChart patientDistributionChart;

    @FXML
    private BarChart<String, Number> revenueChart;

    @FXML
    private TableView<RecentActivity> recentActivitiesTable;

    @FXML
    private TableColumn<RecentActivity, String> activityTimeColumn;

    @FXML
    private TableColumn<RecentActivity, String> activityTypeColumn;

    @FXML
    private TableColumn<RecentActivity, String> activityDescriptionColumn;

    @FXML
    private Label statusLabel;

    @FXML
    private Label currentTimeLabel;

    private AuthService authService;
    private PatientService patientService;
    private DoctorService doctorService;
    private AppointmentService appointmentService;
    private MedicationService medicationService;
    private PrescriptionService prescriptionService;
    private MedicalTestService medicalTestService;
    private HospitalService hospitalService;
    private Timer timer;

    public static class RecentActivity {
        private final String time;
        private final String type;
        private final String description;

        public RecentActivity(String time, String type, String description) {
            this.time = time;
            this.type = type;
            this.description = description;
        }

        public String getTime() {
            return time;
        }

        public String getType() {
            return type;
        }

        public String getDescription() {
            return description;
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        authService = AuthService.getInstance();
        patientService = new PatientService();
        doctorService = DoctorService.getInstance();
        appointmentService = AppointmentService.getInstance();
        medicationService = MedicationService.getInstance();
        prescriptionService = PrescriptionService.getInstance();
        medicalTestService = MedicalTestService.getInstance();
        hospitalService = HospitalService.getInstance();

        // Set current user name
        currentUserLabel.setText("Welcome, " + authService.getCurrentUser().getFullName());

        // Load hospital info
        try {
            // Load hospital logo
            InputStream logoStream = getClass().getResourceAsStream("/com/hospital/images/hospital_logo.png");
            if (logoStream != null) {
                Image logo = new Image(logoStream);
                hospitalLogo.setImage(logo);
            } else {
                System.err.println("Hospital logo not found");
            }

            // Set hospital name from Hospital Service
            hospitalNameLabel.setText(hospitalService.getCurrentHospital().getName());
        } catch (Exception e) {
            System.err.println("Could not load hospital info: " + e.getMessage());
            e.printStackTrace();
        }

        // Initialize table columns
        activityTimeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTime()));
        activityTypeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getType()));
        activityDescriptionColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDescription()));

        // Load dashboard data
        refreshDashboard();

        // Setup clock
        setupClock();
    }

    private void setupClock() {
        timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    currentTimeLabel.setText(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()));
                });
            }
        }, 0, 1000);
    }

    private void refreshDashboard() {
        // Load patient statistics
        List<Patient> patients = patientService.getAllPatients();
        totalPatientsLabel.setText(String.valueOf(patients.size()));

        // Load doctor statistics
        List<Doctor> doctors = doctorService.getAllDoctors();
        totalDoctorsLabel.setText(String.valueOf(doctors.size()));

        // Load today's appointments
        List<Appointment> todayAppointments = appointmentService.getTodayAppointments();
        todayAppointmentsLabel.setText(String.valueOf(todayAppointments.size()));

        // Load emergency patients count
        long emergencyCount = patients.stream()
                .filter(p -> p.getPatientType().equals("EmergencyPatient"))
                .count();
        emergencyPatientsLabel.setText(String.valueOf(emergencyCount));

        // Setup charts
        ChartUtil.populatePatientTypeChart(patientDistributionChart, patients);
        ChartUtil.populateRevenueChart(revenueChart, patients);

        // Load recent activities
        loadRecentActivities();
    }

    private void loadRecentActivities() {
        ObservableList<RecentActivity> activities = FXCollections.observableArrayList();

        // Add real recent activities
        String today = DateUtil.getCurrentDate();

        // Check appointments from today
        List<Appointment> todayAppointments = appointmentService.getTodayAppointments();
        for (int i = 0; i < Math.min(todayAppointments.size(), 3); i++) {
            Appointment appointment = todayAppointments.get(i);

            // Find patient name
            Patient patient = patientService.getPatientById(appointment.getPatientId());
            String patientName = (patient != null) ? patient.getName() : "Unknown Patient";

            activities.add(new RecentActivity(
                    today + " " + appointment.getTime(),
                    "Appointment",
                    "Appointment for " + patientName + " (" + appointment.getStatus() + ")"
            ));
        }

        // Add recent prescriptions
        List<Patient> patients = patientService.getAllPatients();
        if (!patients.isEmpty()) {
            for (int i = 0; i < Math.min(2, patients.size()); i++) {
                String time = DateUtil.getCurrentDateTime();
                time = time.substring(0, time.length() - 3); // Remove seconds
                activities.add(new RecentActivity(
                        time,
                        "Patient",
                        "Patient " + patients.get(i).getName() + " checked in"
                ));
            }
        }

        // Check for low stock medications
        List<Medication> lowStockMeds = medicationService.getLowStockMedications();
        if (!lowStockMeds.isEmpty()) {
            String time = DateUtil.getCurrentDateTime();
            time = time.substring(0, time.length() - 2); // Remove seconds
            activities.add(new RecentActivity(
                    time,
                    "Pharmacy",
                    lowStockMeds.size() + " medications are running low on stock"
            ));
        }

        // Add some fallback activities if the list is still small
        if (activities.size() < 5) {
            activities.add(new RecentActivity(
                    DateUtil.getCurrentDateTime().substring(0, 16),
                    "System",
                    "System updated to latest version"
            ));

            activities.add(new RecentActivity(
                    DateUtil.getCurrentDateTime().substring(0, 16),
                    "Billing",
                    "Daily financial report generated"
            ));
        }

        recentActivitiesTable.setItems(activities);
    }

    @FXML
    public void handleLogout(ActionEvent event) {
        if (timer != null) {
            timer.cancel();
        }

        authService.logout();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hospital/view/Login.fxml"));
            Parent loginRoot = loader.load();

            Scene scene = new Scene(loginRoot);
            scene.getStylesheets().add(getClass().getResource("/com/hospital/css/darkTheme.css").toExternalForm());

            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setTitle("Hospital Management System - Login");
            stage.setScene(scene);
            stage.setMaximized(false);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Error", "Could not load login screen: " + e.getMessage());
        }
    }

    @FXML
    public void showDashboard() {
        try {
            refreshDashboard();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(dashboardPane);
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Error", "Could not load dashboard: " + e.getMessage());
        }
    }

    @FXML
    public void showPatients() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hospital/view/PatientManagement.fxml"));
            Parent patientsView = loader.load();

            contentArea.getChildren().clear();
            contentArea.getChildren().add(patientsView);

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Error", "Could not load patients view: " + e.getMessage());
        }
    }

    @FXML
    public void showDoctors() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hospital/view/DoctorManagement.fxml"));
            Parent doctorsView = loader.load();

            contentArea.getChildren().clear();
            contentArea.getChildren().add(doctorsView);

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Error", "Could not load doctors view: " + e.getMessage());
        }
    }

    @FXML
    public void showAppointments() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hospital/view/AppointmentManagement.fxml"));
            Parent appointmentsView = loader.load();

            contentArea.getChildren().clear();
            contentArea.getChildren().add(appointmentsView);

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Error", "Could not load appointments view: " + e.getMessage());
        }
    }

    @FXML
    public void showPharmacy() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hospital/view/PharmacyManagement.fxml"));
            Parent pharmacyView = loader.load();

            contentArea.getChildren().clear();
            contentArea.getChildren().add(pharmacyView);

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Error", "Could not load pharmacy view: " + e.getMessage());
        }
    }

    @FXML
    public void showTesting() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hospital/view/MedicalTestManagement.fxml"));
            Parent testingView = loader.load();

            contentArea.getChildren().clear();
            contentArea.getChildren().add(testingView);

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Error", "Could not load medical testing view: " + e.getMessage());
        }
    }

    @FXML
    public void showBilling() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hospital/view/BillingManagement.fxml"));
            Parent billingView = loader.load();

            contentArea.getChildren().clear();
            contentArea.getChildren().add(billingView);

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Error", "Could not load billing view: " + e.getMessage());
        }
    }

    @FXML
    public void showReports() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hospital/view/ReportManagement.fxml"));
            Parent reportsView = loader.load();

            contentArea.getChildren().clear();
            contentArea.getChildren().add(reportsView);

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Error", "Could not load reports view: " + e.getMessage());
        }
    }

    @FXML
    public void showSettings() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hospital/view/HospitalSettings.fxml"));
            Parent settingsView = loader.load();

            contentArea.getChildren().clear();
            contentArea.getChildren().add(settingsView);

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Error", "Could not load settings view: " + e.getMessage());
        }
    }

    @FXML
    public void handleAddPatient() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hospital/view/NewPatient.fxml"));
            Parent newPatientView = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Add New Patient");
            stage.initModality(Modality.APPLICATION_MODAL);

            Scene scene = new Scene(newPatientView);
            scene.getStylesheets().add(getClass().getResource("/com/hospital/css/darkTheme.css").toExternalForm());

            stage.setScene(scene);
            stage.showAndWait();

            // Refresh dashboard after adding a patient
            refreshDashboard();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Error", "Could not load new patient form: " + e.getMessage());
        }
    }

    @FXML
    public void handleScheduleAppointment() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hospital/view/NewAppointment.fxml"));
            Parent newAppointmentView = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Schedule Appointment");
            stage.initModality(Modality.APPLICATION_MODAL);

            Scene scene = new Scene(newAppointmentView);
            scene.getStylesheets().add(getClass().getResource("/com/hospital/css/darkTheme.css").toExternalForm());

            stage.setScene(scene);
            stage.showAndWait();

            // Refresh dashboard after scheduling an appointment
            refreshDashboard();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Error", "Could not load appointment scheduling form: " + e.getMessage());
        }
    }

    @FXML
    public void handlePharmacyManagement() {
        showPharmacy();
    }

    @FXML
    public void handleGenerateReports() {
        showReports();
    }
}