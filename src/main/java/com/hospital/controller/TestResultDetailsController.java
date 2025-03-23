package com.hospital.controller;

import com.hospital.model.Doctor;
import com.hospital.model.MedicalTest;
import com.hospital.model.Patient;
import com.hospital.model.TestResult;
import com.hospital.service.DoctorService;
import com.hospital.service.MedicalTestService;
import com.hospital.service.PatientService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.net.URL;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class TestResultDetailsController implements Initializable {

    @FXML
    private Label resultIdLabel;

    @FXML
    private Label patientNameLabel;

    @FXML
    private Label doctorNameLabel;

    @FXML
    private Label testNameLabel;

    @FXML
    private Label dateLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private TextArea resultDetailsArea;

    @FXML
    private TextArea notesArea;

    @FXML
    private Button closeButton;

    private PatientService patientService;
    private DoctorService doctorService;
    private MedicalTestService medicalTestService;
    private NumberFormat currencyFormatter;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        patientService = new PatientService();
        doctorService = DoctorService.getInstance();
        medicalTestService = MedicalTestService.getInstance();
        currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);
    }

    public void initData(TestResult result) {
        resultIdLabel.setText(result.getId());

        // Get patient, doctor and test names
        Patient patient = patientService.getPatientById(result.getPatientId());
        patientNameLabel.setText(patient != null ? patient.getName() : "Unknown");

        Doctor doctor = doctorService.getDoctorById(result.getDoctorId());
        doctorNameLabel.setText(doctor != null ? doctor.getFullName() : "Unknown");

        MedicalTest test = medicalTestService.getMedicalTestById(result.getTestId());
        testNameLabel.setText(test != null ? test.getName() : "Unknown");

        dateLabel.setText(result.getDate());
        statusLabel.setText(result.getStatus());

        resultDetailsArea.setText(result.getResults() != null ? result.getResults() : "No results available yet");
        notesArea.setText(result.getNotes() != null ? result.getNotes() : "");
    }

    @FXML
    public void handleClose() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}