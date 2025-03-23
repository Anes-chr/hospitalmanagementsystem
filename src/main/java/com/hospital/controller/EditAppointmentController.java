package com.hospital.controller;

import com.hospital.model.Appointment;
import com.hospital.model.Doctor;
import com.hospital.model.HospitalBlock;
import com.hospital.model.Patient;
import com.hospital.service.AppointmentService;
import com.hospital.service.DoctorService;
import com.hospital.service.HospitalService;
import com.hospital.service.PatientService;
import com.hospital.util.AlertUtil;
import com.hospital.util.ValidationUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class EditAppointmentController implements Initializable {

    @FXML
    private Label patientNameLabel;

    @FXML
    private ComboBox<Doctor> doctorComboBox;

    @FXML
    private DatePicker datePicker;

    @FXML
    private ComboBox<String> timeComboBox;

    @FXML
    private ComboBox<String> durationComboBox;

    @FXML
    private ComboBox<HospitalBlock> locationComboBox;

    @FXML
    private TextField purposeField;

    @FXML
    private TextArea notesArea;

    @FXML
    private ComboBox<String> statusComboBox;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    private PatientService patientService;
    private DoctorService doctorService;
    private AppointmentService appointmentService;
    private HospitalService hospitalService;
    private Appointment appointment;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        patientService = new PatientService();
        doctorService = DoctorService.getInstance();
        appointmentService = AppointmentService.getInstance();
        hospitalService = HospitalService.getInstance();

        // Set up time options (30-minute intervals)
        List<String> timeSlots = new java.util.ArrayList<>();
        for (int hour = 8; hour < 18; hour++) {
            timeSlots.add(String.format("%02d:00", hour));
            timeSlots.add(String.format("%02d:30", hour));
        }
        timeComboBox.setItems(FXCollections.observableArrayList(timeSlots));

        // Set up duration options
        durationComboBox.setItems(FXCollections.observableArrayList(
                "15 minutes", "30 minutes", "45 minutes", "1 hour", "1.5 hours", "2 hours"
        ));

        // Set up status options
        statusComboBox.setItems(FXCollections.observableArrayList(
                "Scheduled", "Completed", "Cancelled", "No-Show"
        ));

        // Load hospital blocks
        loadLocations();

        // Load doctors
        loadDoctors();
    }

    public void initData(Appointment appointment) {
        this.appointment = appointment;

        // Get patient name
        Patient patient = patientService.getPatientById(appointment.getPatientId());
        patientNameLabel.setText(patient != null ? patient.getName() : "Unknown");

        // Set doctor
        Doctor doctor = doctorService.getDoctorById(appointment.getDoctorId());
        if (doctor != null) {
            for (Doctor d : doctorComboBox.getItems()) {
                if (d.getId().equals(doctor.getId())) {
                    doctorComboBox.setValue(d);
                    break;
                }
            }
        }

        // Set date
        try {
            LocalDate date = LocalDate.parse(appointment.getDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            datePicker.setValue(date);
        } catch (Exception e) {
            datePicker.setValue(LocalDate.now());
        }

        // Set time
        timeComboBox.setValue(appointment.getTime());

        // Set duration
        durationComboBox.setValue(appointment.getDuration());

        // Set location
        if (appointment.getLocation() != null) {
            for (HospitalBlock block : locationComboBox.getItems()) {
                if (block.getBlockName().equals(appointment.getLocation().getBlockName())) {
                    locationComboBox.setValue(block);
                    break;
                }
            }
        }

        // Set purpose and notes
        purposeField.setText(appointment.getPurpose());
        notesArea.setText(appointment.getNotes());

        // Set status
        statusComboBox.setValue(appointment.getStatus());
    }

    private void loadDoctors() {
        List<Doctor> doctors = doctorService.getAllDoctors();
        ObservableList<Doctor> doctorList = FXCollections.observableArrayList(doctors);

        doctorComboBox.setItems(doctorList);

        doctorComboBox.setConverter(new StringConverter<Doctor>() {
            @Override
            public String toString(Doctor doctor) {
                return doctor == null ? "" : doctor.getFullName();
            }

            @Override
            public Doctor fromString(String string) {
                return doctorComboBox.getItems().stream()
                        .filter(doctor -> doctor.getFullName().equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });
    }

    private void loadLocations() {
        List<HospitalBlock> blocks = hospitalService.getAllBlocks();
        ObservableList<HospitalBlock> blockList = FXCollections.observableArrayList(blocks);

        locationComboBox.setItems(blockList);

        locationComboBox.setConverter(new StringConverter<HospitalBlock>() {
            @Override
            public String toString(HospitalBlock block) {
                return block == null ? "" : block.getFullLocation();
            }

            @Override
            public HospitalBlock fromString(String string) {
                return locationComboBox.getItems().stream()
                        .filter(block -> block.getFullLocation().equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });
    }

    @FXML
    public void handleSave(ActionEvent event) {
        if (!validateInput()) {
            return;
        }

        try {
            Doctor selectedDoctor = doctorComboBox.getValue();
            LocalDate date = datePicker.getValue();
            String timeSlot = timeComboBox.getValue();
            String duration = durationComboBox.getValue();
            String purpose = purposeField.getText();
            HospitalBlock location = locationComboBox.getValue();
            String status = statusComboBox.getValue();

            // Check if the time slot is available (only if doctor or date/time changed)
            boolean timeSlotChanged = !appointment.getDoctorId().equals(selectedDoctor.getId()) ||
                    !appointment.getDate().equals(date.toString()) ||
                    !appointment.getTime().equals(timeSlot);

            if (timeSlotChanged && !status.equals("Cancelled") && !status.equals("Completed")) {
                String dateStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                boolean isAvailable = appointmentService.isTimeSlotAvailable(
                        selectedDoctor.getId(), dateStr, timeSlot);

                if (!isAvailable) {
                    AlertUtil.showWarning("Time Slot Unavailable",
                            "The selected time slot is already booked for this doctor. Please choose another time.");
                    return;
                }
            }

            // Update appointment
            appointment.setDoctorId(selectedDoctor.getId());
            appointment.setDate(date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            appointment.setTime(timeSlot);
            appointment.setDuration(duration);
            appointment.setPurpose(purpose);
            appointment.setLocation(location);
            appointment.setNotes(notesArea.getText());
            appointment.setStatus(status);

            appointmentService.updateAppointment(appointment);

            AlertUtil.showInformation("Success", "Appointment updated successfully.");
            closeWindow();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Error", "Could not update appointment: " + e.getMessage());
        }
    }

    @FXML
    public void handleCancel(ActionEvent event) {
        closeWindow();
    }

    private boolean validateInput() {
        if (doctorComboBox.getValue() == null) {
            AlertUtil.showError("Validation Error", "Please select a doctor.");
            return false;
        }

        if (datePicker.getValue() == null) {
            AlertUtil.showError("Validation Error", "Please select a date.");
            return false;
        }

        if (timeComboBox.getValue() == null) {
            AlertUtil.showError("Validation Error", "Please select a time.");
            return false;
        }

        if (durationComboBox.getValue() == null) {
            AlertUtil.showError("Validation Error", "Please select a duration.");
            return false;
        }

        if (locationComboBox.getValue() == null) {
            AlertUtil.showError("Validation Error", "Please select a location.");
            return false;
        }

        if (!ValidationUtil.isNotEmpty(purposeField.getText())) {
            AlertUtil.showError("Validation Error", "Please enter a purpose for the appointment.");
            return false;
        }

        if (statusComboBox.getValue() == null) {
            AlertUtil.showError("Validation Error", "Please select a status.");
            return false;
        }

        return true;
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}