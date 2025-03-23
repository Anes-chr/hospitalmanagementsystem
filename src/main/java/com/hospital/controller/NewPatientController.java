package com.hospital.controller;

import com.hospital.dao.HospitalBlockDao;
import com.hospital.model.EmergencyPatient;
import com.hospital.model.HospitalBlock;
import com.hospital.model.InPatient;
import com.hospital.model.OutPatient;
import com.hospital.model.Patient;
import com.hospital.service.PatientService;
import com.hospital.util.AlertUtil;
import com.hospital.util.DateUtil;
import com.hospital.util.ValidationUtil;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class NewPatientController implements Initializable {

    @FXML
    private ComboBox<String> patientTypeCombo;

    @FXML
    private TextField nameField;

    @FXML
    private TextField ageField;

    @FXML
    private ComboBox<String> genderCombo;

    @FXML
    private TextField contactField;

    @FXML
    private TextArea addressField;

    @FXML
    private ComboBox<String> bloodGroupCombo;

    @FXML
    private ComboBox<HospitalBlock> blockCombo;

    // InPatient specific fields
    @FXML
    private VBox inPatientForm;

    @FXML
    private TextField roomNumberField;

    @FXML
    private DatePicker admissionDatePicker;

    @FXML
    private TextField dailyRateField;

    @FXML
    private TextField daysAdmittedField;

    @FXML
    private ComboBox<String> attendingDoctorCombo;

    // OutPatient specific fields
    @FXML
    private VBox outPatientForm;

    @FXML
    private DatePicker appointmentDatePicker;

    @FXML
    private TextField consultFeeField;

    @FXML
    private ComboBox<String> consultingDoctorCombo;

    @FXML
    private TextArea diagnosisField;

    // EmergencyPatient specific fields
    @FXML
    private VBox emergencyPatientForm;

    @FXML
    private ComboBox<String> severityLevelCombo;

    @FXML
    private TextField treatmentCostField;

    @FXML
    private TextField emergencyContactField;

    @FXML
    private TextArea treatmentDetailsField;

    @FXML
    private TextField admissionTimeField;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    private PatientService patientService;
    private HospitalBlockDao blockDao;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        patientService = new PatientService();
        blockDao = new HospitalBlockDao();

        // Initialize patient type combo box
        patientTypeCombo.setItems(FXCollections.observableArrayList("InPatient", "OutPatient", "EmergencyPatient"));
        patientTypeCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            updateFormVisibility(newVal);
        });
        patientTypeCombo.getSelectionModel().selectFirst();

        // Initialize gender combo box
        genderCombo.setItems(FXCollections.observableArrayList("Male", "Female", "Other"));
        genderCombo.getSelectionModel().selectFirst();

        // Initialize blood group combo box
        bloodGroupCombo.setItems(FXCollections.observableArrayList("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"));
        bloodGroupCombo.getSelectionModel().selectFirst();

        // Load hospital blocks
        loadHospitalBlocks();

        // Initialize severity level combo box
        severityLevelCombo.setItems(FXCollections.observableArrayList("Low", "Medium", "High", "Critical"));
        severityLevelCombo.getSelectionModel().selectFirst();

        // Initialize doctors combo boxes (in real app, this would load from database)
        List<String> mockDoctors = Arrays.asList(
                "Dr. John Smith", "Dr. Emily Johnson", "Dr. Michael Williams",
                "Dr. Sarah Brown", "Dr. David Miller", "Dr. Jennifer Davis"
        );
        attendingDoctorCombo.setItems(FXCollections.observableArrayList(mockDoctors));
        consultingDoctorCombo.setItems(FXCollections.observableArrayList(mockDoctors));

        // Set default dates
        admissionDatePicker.setValue(LocalDate.now());
        appointmentDatePicker.setValue(LocalDate.now());

        // Set initial form visibility
        updateFormVisibility("InPatient");
    }

    private void loadHospitalBlocks() {
        List<HospitalBlock> blocks = blockDao.getAllEntities();

        // If no blocks exist, create some default ones
        if (blocks.isEmpty()) {
            blocks = createDefaultBlocks();
        }

        blockCombo.setItems(FXCollections.observableArrayList(blocks));
        blockCombo.getSelectionModel().selectFirst();
    }

    private List<HospitalBlock> createDefaultBlocks() {
        List<HospitalBlock> defaultBlocks = new ArrayList<>();

        defaultBlocks.add(new HospitalBlock("A", 1, "General Medicine"));
        defaultBlocks.add(new HospitalBlock("B", 2, "Cardiology"));
        defaultBlocks.add(new HospitalBlock("C", 1, "Pediatrics"));
        defaultBlocks.add(new HospitalBlock("D", 3, "Surgery"));
        defaultBlocks.add(new HospitalBlock("E", 1, "Emergency"));

        // Save the blocks to the database
        for (HospitalBlock block : defaultBlocks) {
            try {
                blockDao.save(block);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return defaultBlocks;
    }

    private void updateFormVisibility(String patientType) {
        inPatientForm.setVisible(false);
        outPatientForm.setVisible(false);
        emergencyPatientForm.setVisible(false);

        switch (patientType) {
            case "InPatient":
                inPatientForm.setVisible(true);
                break;
            case "OutPatient":
                outPatientForm.setVisible(true);
                break;
            case "EmergencyPatient":
                emergencyPatientForm.setVisible(true);
                break;
        }
    }

    @FXML
    private void handleSave(ActionEvent event) {
        if (!validateInput()) {
            return;
        }

        try {
            // Get the selected patient type
            String patientType = patientTypeCombo.getValue();

            // Get common patient fields
            String name = nameField.getText();
            int age = Integer.parseInt(ageField.getText());
            String gender = genderCombo.getValue();
            String contactNumber = contactField.getText();
            String address = addressField.getText();
            String bloodGroup = bloodGroupCombo.getValue();
            HospitalBlock block = blockCombo.getValue();
            String registrationDate = DateUtil.getCurrentDate();

            Patient patient = null;

            // Create patient based on type
            switch (patientType) {
                case "InPatient":
                    String roomNumber = roomNumberField.getText();
                    String admissionDate = admissionDatePicker.getValue().toString();
                    double dailyRate = Double.parseDouble(dailyRateField.getText());
                    int daysAdmitted = Integer.parseInt(daysAdmittedField.getText());
                    String attendingDoctor = attendingDoctorCombo.getValue();

                    patient = new InPatient(name, age, gender, block, registrationDate, contactNumber, address,
                            bloodGroup, roomNumber, admissionDate, dailyRate, daysAdmitted, attendingDoctor);
                    break;

                case "OutPatient":
                    String appointmentDate = appointmentDatePicker.getValue().toString();
                    double consultFee = Double.parseDouble(consultFeeField.getText());
                    String consultingDoctor = consultingDoctorCombo.getValue();
                    String diagnosis = diagnosisField.getText();

                    patient = new OutPatient(name, age, gender, block, registrationDate, contactNumber, address,
                            bloodGroup, appointmentDate, consultFee, consultingDoctor, diagnosis);
                    break;

                case "EmergencyPatient":
                    String severityLevel = severityLevelCombo.getValue();
                    double treatmentCost = Double.parseDouble(treatmentCostField.getText());
                    String emergencyContact = emergencyContactField.getText();
                    String treatmentDetails = treatmentDetailsField.getText();
                    String admissionTime = DateUtil.getCurrentDate() + " " + admissionTimeField.getText();

                    patient = new EmergencyPatient(name, age, gender, block, registrationDate, contactNumber, address,
                            bloodGroup, severityLevel, treatmentCost, emergencyContact, treatmentDetails, admissionTime);
                    break;
            }

            if (patient != null) {
                patientService.savePatient(patient);
                AlertUtil.showInformation("Success", "Patient has been successfully registered.");
                closeWindow();
            }
        } catch (NumberFormatException e) {
            AlertUtil.showError("Input Error", "Please enter valid numbers for all numeric fields.");
        } catch (IOException e) {
            AlertUtil.showError("Save Error", "Could not save patient data: " + e.getMessage());
        }
    }

    private boolean validateInput() {
        // Validate common fields
        if (!ValidationUtil.isNotEmpty(nameField.getText())) {
            AlertUtil.showError("Validation Error", "Please enter patient name.");
            return false;
        }

        if (!ValidationUtil.isValidNumber(ageField.getText())) {
            AlertUtil.showError("Validation Error", "Please enter a valid age.");
            return false;
        }

        if (!ValidationUtil.isNotEmpty(contactField.getText())) {
            AlertUtil.showError("Validation Error", "Please enter a contact number.");
            return false;
        }

        if (!ValidationUtil.isNotEmpty(addressField.getText())) {
            AlertUtil.showError("Validation Error", "Please enter an address.");
            return false;
        }

        if (blockCombo.getValue() == null) {
            AlertUtil.showError("Validation Error", "Please select a hospital block.");
            return false;
        }

        // Validate type-specific fields
        String patientType = patientTypeCombo.getValue();
        switch (patientType) {
            case "InPatient":
                if (!ValidationUtil.isNotEmpty(roomNumberField.getText())) {
                    AlertUtil.showError("Validation Error", "Please enter a room number.");
                    return false;
                }

                if (admissionDatePicker.getValue() == null) {
                    AlertUtil.showError("Validation Error", "Please select an admission date.");
                    return false;
                }

                if (!ValidationUtil.isValidNumber(dailyRateField.getText())) {
                    AlertUtil.showError("Validation Error", "Please enter a valid daily rate.");
                    return false;
                }

                if (!ValidationUtil.isValidNumber(daysAdmittedField.getText())) {
                    AlertUtil.showError("Validation Error", "Please enter a valid number of days.");
                    return false;
                }

                if (attendingDoctorCombo.getValue() == null) {
                    AlertUtil.showError("Validation Error", "Please select an attending doctor.");
                    return false;
                }
                break;

            case "OutPatient":
                if (appointmentDatePicker.getValue() == null) {
                    AlertUtil.showError("Validation Error", "Please select an appointment date.");
                    return false;
                }

                if (!ValidationUtil.isValidNumber(consultFeeField.getText())) {
                    AlertUtil.showError("Validation Error", "Please enter a valid consultation fee.");
                    return false;
                }

                if (consultingDoctorCombo.getValue() == null) {
                    AlertUtil.showError("Validation Error", "Please select a consulting doctor.");
                    return false;
                }
                break;

            case "EmergencyPatient":
                if (severityLevelCombo.getValue() == null) {
                    AlertUtil.showError("Validation Error", "Please select a severity level.");
                    return false;
                }

                if (!ValidationUtil.isValidNumber(treatmentCostField.getText())) {
                    AlertUtil.showError("Validation Error", "Please enter a valid treatment cost.");
                    return false;
                }

                if (!ValidationUtil.isNotEmpty(emergencyContactField.getText())) {
                    AlertUtil.showError("Validation Error", "Please enter an emergency contact.");
                    return false;
                }

                if (!ValidationUtil.isNotEmpty(treatmentDetailsField.getText())) {
                    AlertUtil.showError("Validation Error", "Please enter treatment details.");
                    return false;
                }
                break;
        }

        return true;
    }

    @FXML
    private void handleCancel(ActionEvent event) {
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