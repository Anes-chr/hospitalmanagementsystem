package com.hospital.controller;

import com.hospital.model.Doctor;
import com.hospital.model.MedicalTest;
import com.hospital.model.Patient;
import com.hospital.model.TestResult;
import com.hospital.service.DoctorService;
import com.hospital.service.MedicalTestService;
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
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class TestRequestFormController implements Initializable {

    @FXML
    private ComboBox<Patient> patientComboBox;

    @FXML
    private ComboBox<Doctor> doctorComboBox;

    @FXML
    private ComboBox<MedicalTest> testComboBox;

    @FXML
    private DatePicker scheduleDatePicker;

    @FXML
    private TextArea notesArea;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    private PatientService patientService;
    private DoctorService doctorService;
    private MedicalTestService medicalTestService;
    private MedicalTest preselectedTest;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        patientService = new PatientService();
        doctorService = DoctorService.getInstance();
        medicalTestService = MedicalTestService.getInstance();

        // Initialize date picker with today's date
        scheduleDatePicker.setValue(LocalDate.now());

        // Load patients
        loadPatients();

        // Load doctors
        loadDoctors();

        // Load tests
        loadTests();
    }

    public void initData(MedicalTest test) {
        this.preselectedTest = test;
        if (test != null && testComboBox.getItems() != null) {
            for (MedicalTest t : testComboBox.getItems()) {
                if (t.getId().equals(test.getId())) {
                    testComboBox.getSelectionModel().select(t);
                    break;
                }
            }
        }
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

    private void loadTests() {
        List<MedicalTest> tests = medicalTestService.getAllMedicalTests();
        ObservableList<MedicalTest> testList = FXCollections.observableArrayList(tests);

        testComboBox.setItems(testList);

        testComboBox.setConverter(new StringConverter<MedicalTest>() {
            @Override
            public String toString(MedicalTest test) {
                return test == null ? "" : test.getName();
            }

            @Override
            public MedicalTest fromString(String string) {
                return testComboBox.getItems().stream()
                        .filter(test -> test.getName().equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });

        if (!tests.isEmpty()) {
            if (preselectedTest != null) {
                for (MedicalTest test : tests) {
                    if (test.getId().equals(preselectedTest.getId())) {
                        testComboBox.getSelectionModel().select(test);
                        break;
                    }
                }
            } else {
                testComboBox.getSelectionModel().select(0);
            }
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
    public void handleSave(ActionEvent event) {
        if (!validateInput()) {
            return;
        }

        try {
            Patient selectedPatient = patientComboBox.getValue();
            Doctor selectedDoctor = doctorComboBox.getValue();
            MedicalTest selectedTest = testComboBox.getValue();
            LocalDate scheduleDate = scheduleDatePicker.getValue();
            String notes = notesArea.getText();

            // Create test result
            TestResult result = new TestResult();
            result.setPatientId(selectedPatient.getId());
            result.setDoctorId(selectedDoctor.getId());
            result.setTestId(selectedTest.getId());
            result.setDate(scheduleDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            result.setStatus("Pending");
            result.setNotes(notes);

            medicalTestService.addTestResult(result);

            AlertUtil.showInformation("Success", "Medical test request created successfully.");
            closeWindow();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Error", "Could not save test request: " + e.getMessage());
        }
    }

    @FXML
    public void handleCancel(ActionEvent event) {
        closeWindow();
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

        if (testComboBox.getValue() == null) {
            AlertUtil.showError("Validation Error", "Please select a medical test.");
            return false;
        }

        if (scheduleDatePicker.getValue() == null) {
            AlertUtil.showError("Validation Error", "Please select a date.");
            return false;
        }

        return true;
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}