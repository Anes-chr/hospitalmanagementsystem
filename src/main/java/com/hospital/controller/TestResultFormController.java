package com.hospital.controller;

import com.hospital.model.Doctor;
import com.hospital.model.MedicalTest;
import com.hospital.model.Patient;
import com.hospital.model.TestResult;
import com.hospital.service.DoctorService;
import com.hospital.service.MedicalTestService;
import com.hospital.service.PatientService;
import com.hospital.util.AlertUtil;
import com.hospital.util.ValidationUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class TestResultFormController implements Initializable {

    @FXML
    private Label patientNameLabel;

    @FXML
    private Label doctorNameLabel;

    @FXML
    private Label testNameLabel;

    @FXML
    private Label dateLabel;

    @FXML
    private TextArea resultDetailsArea;

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
    private MedicalTestService medicalTestService;
    private TestResult testResult;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        patientService = new PatientService();
        doctorService = DoctorService.getInstance();
        medicalTestService = MedicalTestService.getInstance();

        // Initialize status combo box
        statusComboBox.getItems().addAll("Pending", "Completed", "Abnormal", "Cancelled");
    }

    public void initData(TestResult testResult) {
        this.testResult = testResult;

        // Get patient, doctor and test names
        Patient patient = patientService.getPatientById(testResult.getPatientId());
        patientNameLabel.setText(patient != null ? patient.getName() : "Unknown");

        Doctor doctor = doctorService.getDoctorById(testResult.getDoctorId());
        doctorNameLabel.setText(doctor != null ? doctor.getFullName() : "Unknown");

        MedicalTest test = medicalTestService.getMedicalTestById(testResult.getTestId());
        testNameLabel.setText(test != null ? test.getName() : "Unknown");

        dateLabel.setText(testResult.getDate());

        if (testResult.getResults() != null) {
            resultDetailsArea.setText(testResult.getResults());
        }

        if (testResult.getNotes() != null) {
            notesArea.setText(testResult.getNotes());
        }

        statusComboBox.setValue(testResult.getStatus());
    }

    @FXML
    public void handleSave(ActionEvent event) {
        if (!validateInput()) {
            return;
        }

        try {
            // Update test result
            testResult.setResults(resultDetailsArea.getText());
            testResult.setNotes(notesArea.getText());
            testResult.setStatus(statusComboBox.getValue());

            medicalTestService.updateTestResult(testResult);

            AlertUtil.showInformation("Success", "Test result updated successfully.");
            closeWindow();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Error", "Could not save test result: " + e.getMessage());
        }
    }

    @FXML
    public void handleCancel(ActionEvent event) {
        closeWindow();
    }

    private boolean validateInput() {
        if (statusComboBox.getValue() == null) {
            AlertUtil.showError("Validation Error", "Please select a status.");
            return false;
        }

        if (statusComboBox.getValue().equals("Completed") && ValidationUtil.isNotEmpty(resultDetailsArea.getText())) {
            if (!ValidationUtil.isNotEmpty(resultDetailsArea.getText())) {
                AlertUtil.showError("Validation Error", "Please enter the result details for a completed test.");
                return false;
            }
        }

        return true;
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}