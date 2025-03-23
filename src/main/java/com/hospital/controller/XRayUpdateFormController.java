package com.hospital.controller;

import com.hospital.model.Doctor;
import com.hospital.model.Patient;
import com.hospital.model.XRayExamination;
import com.hospital.service.DoctorService;
import com.hospital.service.MedicalTestService;
import com.hospital.service.PatientService;
import com.hospital.util.AlertUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class XRayUpdateFormController implements Initializable {

    @FXML
    private Label patientNameLabel;

    @FXML
    private Label doctorNameLabel;

    @FXML
    private Label dateLabel;

    @FXML
    private Label bodyPartLabel;

    @FXML
    private ComboBox<String> statusComboBox;

    @FXML
    private TextArea resultsArea;

    @FXML
    private TextField imagePathField;

    @FXML
    private Button browseImageButton;

    @FXML
    private ImageView previewImageView;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    private PatientService patientService;
    private DoctorService doctorService;
    private MedicalTestService medicalTestService;
    private XRayExamination xrayExamination;
    private String selectedImagePath;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        patientService = new PatientService();
        doctorService = DoctorService.getInstance();
        medicalTestService = MedicalTestService.getInstance();

        // Initialize status combo box
        statusComboBox.getItems().addAll("Requested", "Scheduled", "Completed", "Cancelled");

        // Set up browse button
        browseImageButton.setOnAction(event -> handleBrowseImage());
    }

    public void initData(XRayExamination xray) {
        this.xrayExamination = xray;

        // Get patient and doctor names
        Patient patient = patientService.getPatientById(xray.getPatientId());
        patientNameLabel.setText(patient != null ? patient.getName() : "Unknown");

        Doctor doctor = doctorService.getDoctorById(xray.getRequestedByDoctorId());
        doctorNameLabel.setText(doctor != null ? doctor.getFullName() : "Unknown");

        dateLabel.setText(xray.getDate());
        bodyPartLabel.setText(xray.getBodyPart());

        statusComboBox.setValue(xray.getStatus());

        if (xray.getResults() != null) {
            resultsArea.setText(xray.getResults());
        }

        if (xray.getImagePath() != null && !xray.getImagePath().isEmpty()) {
            imagePathField.setText(xray.getImagePath());
            selectedImagePath = xray.getImagePath();

            try {
                Image image = new Image(xray.getImagePath());
                previewImageView.setImage(image);
            } catch (Exception e) {
                // Image couldn't be loaded
                previewImageView.setImage(null);
            }
        }
    }

    @FXML
    public void handleBrowseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select X-Ray Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File selectedFile = fileChooser.showOpenDialog(browseImageButton.getScene().getWindow());
        if (selectedFile != null) {
            selectedImagePath = selectedFile.toURI().toString();
            imagePathField.setText(selectedFile.getPath());

            try {
                Image image = new Image(selectedImagePath);
                previewImageView.setImage(image);
            } catch (Exception e) {
                AlertUtil.showError("Image Error", "Could not load the selected image.");
            }
        }
    }

    @FXML
    public void handleSave(ActionEvent event) {
        try {
            // Update X-ray examination
            xrayExamination.setStatus(statusComboBox.getValue());
            xrayExamination.setResults(resultsArea.getText());

            if (selectedImagePath != null && !selectedImagePath.isEmpty()) {
                xrayExamination.setImagePath(selectedImagePath);
            }

            medicalTestService.updateXRayExamination(xrayExamination);

            AlertUtil.showInformation("Success", "X-Ray examination updated successfully.");
            closeWindow();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Error", "Could not save X-Ray examination: " + e.getMessage());
        }
    }

    @FXML
    public void handleCancel(ActionEvent event) {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}