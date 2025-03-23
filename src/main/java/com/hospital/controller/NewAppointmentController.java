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
import com.hospital.util.DateUtil;
import com.hospital.util.ValidationUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class NewAppointmentController implements Initializable {

    @FXML
    private ComboBox<Patient> patientComboBox;

    @FXML
    private ComboBox<Doctor> doctorComboBox;

    @FXML
    private ComboBox<String> specialtyComboBox;

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
    private Button cancelButton;

    @FXML
    private Button saveButton;

    private PatientService patientService;
    private DoctorService doctorService;
    private AppointmentService appointmentService;
    private HospitalService hospitalService;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        patientService = new PatientService();
        doctorService = DoctorService.getInstance();
        appointmentService = AppointmentService.getInstance();
        hospitalService = HospitalService.getInstance();

        // Initialize date picker with today's date
        datePicker.setValue(LocalDate.now());

        // Load patients
        loadPatients();

        // Load doctors
        loadDoctors();

        // Set up time options (30-minute intervals)
        List<String> timeSlots = new ArrayList<>();
        for (int hour = 8; hour < 18; hour++) {
            timeSlots.add(String.format("%02d:00", hour));
            timeSlots.add(String.format("%02d:30", hour));
        }
        timeComboBox.setItems(FXCollections.observableArrayList(timeSlots));
        timeComboBox.getSelectionModel().select("09:00");

        // Set up duration options
        durationComboBox.setItems(FXCollections.observableArrayList(
                "15 minutes", "30 minutes", "45 minutes", "1 hour", "1.5 hours", "2 hours"
        ));
        durationComboBox.getSelectionModel().select("30 minutes");

        // Load hospital blocks
        loadLocations();

        // Initialize specialty filter
        Set<String> specialties = new HashSet<>();
        for (Doctor doctor : doctorService.getAllDoctors()) {
            specialties.add(doctor.getSpecialization());
        }
        specialtyComboBox.getItems().add("All Specialties");
        specialtyComboBox.getItems().addAll(specialties);
        specialtyComboBox.getSelectionModel().select("All Specialties");
    }

    private void loadPatients() {
        List<Patient> patients = patientService.getAllPatients();
        ObservableList<Patient> patientList = FXCollections.observableArrayList(patients);

        patientComboBox.setItems(patientList);

        patientComboBox.setConverter(new StringConverter<Patient>() {
            @Override
            public String toString(Patient patient) {
                return patient == null ? "" : patient.getName();
            }

            @Override
            public Patient fromString(String string) {
                return patientComboBox.getItems().stream()
                        .filter(patient -> patient.getName().equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });

        if (!patients.isEmpty()) {
            patientComboBox.getSelectionModel().select(0);
        }
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

        if (!doctors.isEmpty()) {
            doctorComboBox.getSelectionModel().select(0);
        }
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

        if (!blocks.isEmpty()) {
            locationComboBox.getSelectionModel().select(0);
        }
    }

    @FXML
    public void handleSpecialtyFilter() {
        String selectedSpecialty = specialtyComboBox.getValue();

        List<Doctor> doctors;
        if (selectedSpecialty.equals("All Specialties")) {
            doctors = doctorService.getAllDoctors();
        } else {
            doctors = doctorService.getDoctorsBySpecialty(selectedSpecialty);
        }

        doctorComboBox.setItems(FXCollections.observableArrayList(doctors));
        if (!doctors.isEmpty()) {
            doctorComboBox.getSelectionModel().select(0);
        }
    }

    @FXML
    public void handleSearchPatient() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hospital/view/PatientSearch.fxml"));
            Parent searchView = loader.load();

            PatientSearchController controller = loader.getController();
            controller.initData(patient -> {
                patientComboBox.getSelectionModel().select(patient);
            });

            Stage stage = new Stage();
            stage.setTitle("Search Patient");
            stage.initModality(Modality.APPLICATION_MODAL);

            Scene scene = new Scene(searchView);
            scene.getStylesheets().add(getClass().getResource("/com/hospital/css/darkTheme.css").toExternalForm());

            stage.setScene(scene);
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Error", "Could not open patient search: " + e.getMessage());
        }
    }

    @FXML
    public void handleSave() {
        if (!validateInput()) {
            return;
        }

        try {
            Patient selectedPatient = patientComboBox.getValue();
            Doctor selectedDoctor = doctorComboBox.getValue();
            LocalDate date = datePicker.getValue();
            String timeSlot = timeComboBox.getValue();
            String duration = durationComboBox.getValue();
            String purpose = purposeField.getText();
            HospitalBlock location = locationComboBox.getValue();

            // Check if the time slot is available
            String dateStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            boolean isAvailable = appointmentService.isTimeSlotAvailable(
                    selectedDoctor.getId(), dateStr, timeSlot);

            if (!isAvailable) {
                AlertUtil.showWarning("Time Slot Unavailable",
                        "The selected time slot is already booked for this doctor. Please choose another time.");
                return;
            }

            // Create appointment
            Appointment appointment = new Appointment(
                    selectedPatient.getId(),
                    selectedDoctor.getId(),
                    dateStr,
                    timeSlot,
                    duration,
                    purpose,
                    location
            );

            appointment.setNotes(notesArea.getText());
            appointment.setStatus("Scheduled");

            appointmentService.addAppointment(appointment);

            AlertUtil.showInformation("Success", "Appointment scheduled successfully.");
            closeWindow();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Error", "Could not save appointment: " + e.getMessage());
        }
    }

    private boolean validateInput() {
        if (patientComboBox.getValue() == null) {
            AlertUtil.showError("Validation Error", "Please select a patient.");
            return false;
        }

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

        // Check if the selected date is in the past
        if (datePicker.getValue().isBefore(LocalDate.now())) {
            AlertUtil.showError("Validation Error", "Cannot schedule appointments in the past. Please select a future date.");
            return false;
        }

        return true;
    }

    @FXML
    public void handleCancel() {
        boolean confirm = AlertUtil.showConfirmation("Confirm", "Are you sure you want to cancel? Any unsaved data will be lost.");
        if (confirm) {
            closeWindow();
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}