package com.hospital.controller;

import com.hospital.model.Doctor;
import com.hospital.model.Patient;
import com.hospital.model.XRayExamination;
import com.hospital.service.DoctorService;
import com.hospital.service.PatientService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class XRayDetailsController implements Initializable {

    @FXML
    private Label xrayIdLabel;

    @FXML
    private Label patientNameLabel;

    @FXML
    private Label doctorNameLabel;

    @FXML
    private Label dateLabel;

    @FXML
    private Label bodyPartLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private TextArea reasonArea;

    @FXML
    private TextArea resultsArea;

    @FXML
    private ImageView xrayImageView;

    @FXML
    private Button closeButton;

    private PatientService patientService;
    private DoctorService doctorService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        patientService = new PatientService();
        doctorService = DoctorService.getInstance();
    }

    public void initData(XRayExamination xray) {
        xrayIdLabel.setText(xray.getId());

        // Get patient and doctor names
        Patient patient = patientService.getPatientById(xray.getPatientId());
        patientNameLabel.setText(patient != null ? patient.getName() : "Unknown");

        Doctor doctor = doctorService.getDoctorById(xray.getRequestedByDoctorId());
        doctorNameLabel.setText(doctor != null ? doctor.getFullName() : "Unknown");

        dateLabel.setText(xray.getDate());
        bodyPartLabel.setText(xray.getBodyPart());
        statusLabel.setText(xray.getStatus());

        reasonArea.setText(xray.getReason());
        resultsArea.setText(xray.getResults() != null ? xray.getResults() : "No results available yet");

        // Load X-ray image if available
        if (xray.getImagePath() != null && !xray.getImagePath().isEmpty()) {
            try {
                Image image = new Image(xray.getImagePath());
                xrayImageView.setImage(image);
            } catch (Exception e) {
                // Use placeholder or leave blank
                xrayImageView.setImage(null);
            }
        } else {
            // Use placeholder or leave blank
            xrayImageView.setImage(null);
        }
    }

    @FXML
    public void handleClose() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}