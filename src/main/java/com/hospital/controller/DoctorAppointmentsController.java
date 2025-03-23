package com.hospital.controller;

import com.hospital.model.Appointment;
import com.hospital.model.Doctor;
import com.hospital.model.Patient;
import com.hospital.service.AppointmentService;
import com.hospital.service.PatientService;
import com.hospital.util.AlertUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class DoctorAppointmentsController implements Initializable {

    @FXML
    private Label doctorNameLabel;

    @FXML
    private Label specializationLabel;

    @FXML
    private ComboBox<String> filterComboBox;

    @FXML
    private DatePicker filterDatePicker;

    @FXML
    private TableView<Appointment> appointmentTable;

    @FXML
    private TableColumn<Appointment, String> dateColumn;

    @FXML
    private TableColumn<Appointment, String> timeColumn;

    @FXML
    private TableColumn<Appointment, String> patientColumn;

    @FXML
    private TableColumn<Appointment, String> purposeColumn;

    @FXML
    private TableColumn<Appointment, String> statusColumn;

    @FXML
    private Button completeButton;

    @FXML
    private Button cancelButton;

    @FXML
    private Button closeButton;

    private Doctor doctor;
    private AppointmentService appointmentService;
    private PatientService patientService;
    private ObservableList<Appointment> appointments;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        appointmentService = AppointmentService.getInstance();
        patientService = new PatientService();

        // Initialize filter combo box
        filterComboBox.getItems().addAll("All Appointments", "Today", "Upcoming", "Completed", "Cancelled");
        filterComboBox.getSelectionModel().select("All Appointments");

        // Initialize table columns
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));

        patientColumn.setCellValueFactory(data -> {
            Patient patient = patientService.getPatientById(data.getValue().getPatientId());
            String patientName = patient != null ? patient.getName() : "Unknown";
            return new SimpleStringProperty(patientName);
        });

        purposeColumn.setCellValueFactory(new PropertyValueFactory<>("purpose"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Add selection listener
        appointmentTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            updateButtonStates(newVal);
        });

        // Add filter listeners
        filterComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            loadAppointments();
        });

        filterDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadAppointments();
            }
        });
    }

    public void initData(Doctor doctor) {
        this.doctor = doctor;

        // Update doctor info
        doctorNameLabel.setText(doctor.getFullName());
        specializationLabel.setText(doctor.getSpecialization());

        // Load appointments
        loadAppointments();
    }

    private void loadAppointments() {
        if (doctor == null) return;

        // Get all appointments for this doctor
        List<Appointment> allAppointments = appointmentService.getAppointmentsByDoctorId(doctor.getId());

        // Apply filters
        String filter = filterComboBox.getValue();

        List<Appointment> filteredAppointments;

        switch (filter) {
            case "Today":
                filteredAppointments = appointmentService.getTodayAppointments().stream()
                        .filter(a -> a.getDoctorId().equals(doctor.getId()))
                        .collect(java.util.stream.Collectors.toList());
                break;
            case "Upcoming":
                filteredAppointments = appointmentService.getUpcomingAppointments().stream()
                        .filter(a -> a.getDoctorId().equals(doctor.getId()))
                        .collect(java.util.stream.Collectors.toList());
                break;
            case "Completed":
                filteredAppointments = allAppointments.stream()
                        .filter(a -> a.getStatus().equals("Completed"))
                        .collect(java.util.stream.Collectors.toList());
                break;
            case "Cancelled":
                filteredAppointments = allAppointments.stream()
                        .filter(a -> a.getStatus().equals("Cancelled"))
                        .collect(java.util.stream.Collectors.toList());
                break;
            default:
                filteredAppointments = allAppointments;
                break;
        }

        // Filter by date if selected
        if (filterDatePicker.getValue() != null) {
            String dateStr = filterDatePicker.getValue().toString();
            filteredAppointments = filteredAppointments.stream()
                    .filter(a -> a.getDate().equals(dateStr))
                    .collect(java.util.stream.Collectors.toList());
        }

        appointments = FXCollections.observableArrayList(filteredAppointments);
        appointmentTable.setItems(appointments);

        // Update button states
        updateButtonStates(appointmentTable.getSelectionModel().getSelectedItem());
    }

    private void updateButtonStates(Appointment appointment) {
        boolean hasSelection = appointment != null;
        boolean isActive = hasSelection &&
                !appointment.getStatus().equals("Completed") &&
                !appointment.getStatus().equals("Cancelled");

        completeButton.setDisable(!isActive);
        cancelButton.setDisable(!isActive);
    }

    @FXML
    public void handleComplete(ActionEvent event) {
        Appointment selectedAppointment = appointmentTable.getSelectionModel().getSelectedItem();
        if (selectedAppointment != null) {
            try {
                selectedAppointment.setStatus("Completed");
                appointmentService.updateAppointment(selectedAppointment);
                loadAppointments();
                AlertUtil.showInformation("Success", "Appointment marked as completed.");
            } catch (IOException e) {
                e.printStackTrace();
                AlertUtil.showError("Error", "Could not update appointment: " + e.getMessage());
            }
        }
    }

    @FXML
    public void handleCancel(ActionEvent event) {
        Appointment selectedAppointment = appointmentTable.getSelectionModel().getSelectedItem();
        if (selectedAppointment != null) {
            boolean confirm = AlertUtil.showConfirmation("Confirm Cancellation",
                    "Are you sure you want to cancel this appointment?");

            if (confirm) {
                try {
                    selectedAppointment.setStatus("Cancelled");
                    appointmentService.updateAppointment(selectedAppointment);
                    loadAppointments();
                    AlertUtil.showInformation("Success", "Appointment cancelled successfully.");
                } catch (IOException e) {
                    e.printStackTrace();
                    AlertUtil.showError("Error", "Could not cancel appointment: " + e.getMessage());
                }
            }
        }
    }

    @FXML
    public void handleClose(ActionEvent event) {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}