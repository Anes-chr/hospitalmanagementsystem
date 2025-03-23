package com.hospital.controller;

import com.hospital.model.Doctor;
import com.hospital.model.HospitalBlock;
import com.hospital.service.DoctorService;
import com.hospital.service.HospitalService;
import com.hospital.util.AlertUtil;
import com.hospital.util.ValidationUtil;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class EditDoctorController implements Initializable {

    @FXML
    private TextField fullNameField;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField contactNumberField;

    @FXML
    private TextField licenseNumberField;

    @FXML
    private ComboBox<String> specializationCombo;

    @FXML
    private ComboBox<HospitalBlock> blockCombo;

    @FXML
    private TextField workingHoursField;

    @FXML
    private CheckBox availableCheckbox;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    private DoctorService doctorService;
    private HospitalService hospitalService;
    private Doctor doctor;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        doctorService = DoctorService.getInstance();
        hospitalService = HospitalService.getInstance();

        // Initialize specialization combo box
        specializationCombo.getItems().addAll(
                "General Medicine", "Cardiology", "Pediatrics", "Orthopedics",
                "Neurology", "Dermatology", "Ophthalmology", "Gynecology",
                "Urology", "Psychiatry", "Emergency Medicine", "Oncology"
        );

        // Initialize hospital blocks
        loadHospitalBlocks();
    }

    public void initData(Doctor doctor) {
        this.doctor = doctor;

        // Populate fields with doctor data
        fullNameField.setText(doctor.getFullName());
        usernameField.setText(doctor.getUsername());
        // Don't populate password for security
        passwordField.clear();
        emailField.setText(doctor.getEmail());
        contactNumberField.setText(doctor.getContactNumber());
        licenseNumberField.setText(doctor.getLicenseNumber());
        specializationCombo.setValue(doctor.getSpecialization());

        if (doctor.getAssignedBlock() != null) {
            for (HospitalBlock block : blockCombo.getItems()) {
                if (block.getBlockName().equals(doctor.getAssignedBlock().getBlockName())) {
                    blockCombo.setValue(block);
                    break;
                }
            }
        }

        workingHoursField.setText(doctor.getWorkingHours());
        availableCheckbox.setSelected(doctor.isAvailable());
    }

    private void loadHospitalBlocks() {
        List<HospitalBlock> blocks = hospitalService.getAllBlocks();
        blockCombo.setItems(FXCollections.observableArrayList(blocks));

        blockCombo.setConverter(new StringConverter<HospitalBlock>() {
            @Override
            public String toString(HospitalBlock block) {
                return block == null ? "" : block.getFullLocation();
            }

            @Override
            public HospitalBlock fromString(String string) {
                return blockCombo.getItems().stream()
                        .filter(block -> block.getFullLocation().equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });

        if (!blocks.isEmpty()) {
            blockCombo.getSelectionModel().select(0);
        }
    }

    @FXML
    public void handleSave(ActionEvent event) {
        if (!validateInput()) {
            return;
        }

        try {
            // Update doctor
            doctor.setFullName(fullNameField.getText());
            doctor.setUsername(usernameField.getText());

            // Only update password if provided
            if (!passwordField.getText().isEmpty()) {
                doctor.setPassword(passwordField.getText());
            }

            doctor.setEmail(emailField.getText());
            doctor.setContactNumber(contactNumberField.getText());
            doctor.setLicenseNumber(licenseNumberField.getText());
            doctor.setSpecialization(specializationCombo.getValue());
            doctor.setAssignedBlock(blockCombo.getValue());
            doctor.setWorkingHours(workingHoursField.getText());
            doctor.setAvailable(availableCheckbox.isSelected());

            doctorService.updateDoctor(doctor);

            AlertUtil.showInformation("Success", "Doctor information updated successfully.");
            closeWindow();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Save Error", "Could not save doctor information: " + e.getMessage());
        }
    }

    @FXML
    public void handleCancel(ActionEvent event) {
        closeWindow();
    }

    private boolean validateInput() {
        if (!ValidationUtil.isNotEmpty(fullNameField.getText())) {
            AlertUtil.showError("Validation Error", "Full name is required.");
            return false;
        }

        if (!ValidationUtil.isNotEmpty(usernameField.getText())) {
            AlertUtil.showError("Validation Error", "Username is required.");
            return false;
        }

        if (!ValidationUtil.isNotEmpty(emailField.getText())) {
            AlertUtil.showError("Validation Error", "Email is required.");
            return false;
        }

        if (!ValidationUtil.isNotEmpty(contactNumberField.getText())) {
            AlertUtil.showError("Validation Error", "Contact number is required.");
            return false;
        }

        if (!ValidationUtil.isNotEmpty(licenseNumberField.getText())) {
            AlertUtil.showError("Validation Error", "License number is required.");
            return false;
        }

        if (specializationCombo.getValue() == null) {
            AlertUtil.showError("Validation Error", "Please select a specialization.");
            return false;
        }

        if (blockCombo.getValue() == null) {
            AlertUtil.showError("Validation Error", "Please select a hospital block.");
            return false;
        }

        if (!ValidationUtil.isNotEmpty(workingHoursField.getText())) {
            AlertUtil.showError("Validation Error", "Working hours are required.");
            return false;
        }

        return true;
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}