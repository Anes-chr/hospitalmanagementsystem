package com.hospital.controller;

import com.hospital.model.*;
import com.hospital.service.PatientService;
import com.hospital.service.HospitalService;
import com.hospital.util.AlertUtil;
import com.hospital.util.DateUtil;
import com.hospital.util.ValidationUtil;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class EditPatientController implements Initializable {

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
    private HospitalService hospitalService;
    private Patient patient;
    private String patientType;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        patientService = new PatientService();
        hospitalService = HospitalService.getInstance();

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
        List<String> mockDoctors = List.of(
                "Dr. John Smith", "Dr. Emily Johnson", "Dr. Michael Williams",
                "Dr. Sarah Brown", "Dr. David Miller", "Dr. Jennifer Davis"
        );
        attendingDoctorCombo.setItems(FXCollections.observableArrayList(mockDoctors));
        consultingDoctorCombo.setItems(FXCollections.observableArrayList(mockDoctors));

        // Set default dates
        admissionDatePicker.setValue(LocalDate.now());
        appointmentDatePicker.setValue(LocalDate.now());

        // Initially hide all specific forms
        inPatientForm.setVisible(false);
        outPatientForm.setVisible(false);
        emergencyPatientForm.setVisible(false);
    }

    public void initData(Patient patient) {
        this.patient = patient;

        // Set common fields
        nameField.setText(patient.getName());
        ageField.setText(String.valueOf(patient.getAge()));
        genderCombo.setValue(patient.getGender());
        contactField.setText(patient.getContactNumber());
        addressField.setText(patient.getAddress());
        bloodGroupCombo.setValue(patient.getBloodGroup());

        if (patient.getLocation() != null) {
            for (HospitalBlock block : blockCombo.getItems()) {
                if (block.getBlockName().equals(patient.getLocation().getBlockName())) {
                    blockCombo.setValue(block);
                    break;
                }
            }
        }

        // Set specific fields based on patient type
        patientType = patient.getPatientType();

        if (patient instanceof InPatient) {
            inPatientForm.setVisible(true);
            initInPatientData((InPatient) patient);
        } else if (patient instanceof OutPatient) {
            outPatientForm.setVisible(true);
            initOutPatientData((OutPatient) patient);
        } else if (patient instanceof EmergencyPatient) {
            emergencyPatientForm.setVisible(true);
            initEmergencyPatientData((EmergencyPatient) patient);
        }
    }

    private void initInPatientData(InPatient patient) {
        roomNumberField.setText(patient.getRoomNumber());
        try {
            LocalDate admissionDate = LocalDate.parse(patient.getAdmissionDate());
            admissionDatePicker.setValue(admissionDate);
        } catch (Exception e) {
            admissionDatePicker.setValue(LocalDate.now());
        }
        dailyRateField.setText(String.valueOf(patient.getDailyRate()));
        daysAdmittedField.setText(String.valueOf(patient.getNumberOfDaysAdmitted()));
        attendingDoctorCombo.setValue(patient.getAttendingDoctor());
    }

    private void initOutPatientData(OutPatient patient) {
        try {
            LocalDate appointmentDate = LocalDate.parse(patient.getAppointmentDate());
            appointmentDatePicker.setValue(appointmentDate);
        } catch (Exception e) {
            appointmentDatePicker.setValue(LocalDate.now());
        }
        consultFeeField.setText(String.valueOf(patient.getConsultFee()));
        consultingDoctorCombo.setValue(patient.getConsultingDoctor());
        diagnosisField.setText(patient.getDiagnosis());
    }

    private void initEmergencyPatientData(EmergencyPatient patient) {
        severityLevelCombo.setValue(patient.getSeverityLevel());
        treatmentCostField.setText(String.valueOf(patient.getEmergencyTreatmentCost()));
        emergencyContactField.setText(patient.getEmergencyContact());
        treatmentDetailsField.setText(patient.getTreatmentDetails());
        admissionTimeField.setText(patient.getAdmissionTime().split(" ")[1]); // Extract time part
    }

    private void loadHospitalBlocks() {
        List<HospitalBlock> blocks = hospitalService.getAllBlocks();
        blockCombo.setItems(FXCollections.observableArrayList(blocks));
        if (!blocks.isEmpty()) {
            blockCombo.setValue(blocks.get(0));
        }
    }

    @FXML
    private void handleSave(ActionEvent event) {
        if (!validateInput()) {
            return;
        }

        try {
            // Update common fields
            String name = nameField.getText();
            int age = Integer.parseInt(ageField.getText());
            String gender = genderCombo.getValue();
            String contactNumber = contactField.getText();
            String address = addressField.getText();
            String bloodGroup = bloodGroupCombo.getValue();
            HospitalBlock block = blockCombo.getValue();

            // Update specific fields based on patient type
            if (patient instanceof InPatient) {
                updateInPatient((InPatient) patient, name, age, gender, block, contactNumber, address, bloodGroup);
            } else if (patient instanceof OutPatient) {
                updateOutPatient((OutPatient) patient, name, age, gender, block, contactNumber, address, bloodGroup);
            } else if (patient instanceof EmergencyPatient) {
                updateEmergencyPatient((EmergencyPatient) patient, name, age, gender, block, contactNumber, address, bloodGroup);
            }

            patientService.updatePatient(patient);
            AlertUtil.showInformation("Success", "Patient information updated successfully.");
            closeWindow();
        } catch (NumberFormatException e) {
            AlertUtil.showError("Input Error", "Please enter valid numbers for numeric fields.");
        } catch (IOException e) {
            AlertUtil.showError("Save Error", "Could not update patient data: " + e.getMessage());
        }
    }

    private void updateInPatient(InPatient patient, String name, int age, String gender, HospitalBlock block,
                                 String contactNumber, String address, String bloodGroup) {
        patient.setName(name);
        patient.setAge(age);
        patient.setGender(gender);
        patient.setLocation(block);
        patient.setContactNumber(contactNumber);
        patient.setAddress(address);
        patient.setBloodGroup(bloodGroup);

        patient.setRoomNumber(roomNumberField.getText());
        patient.setAdmissionDate(admissionDatePicker.getValue().toString());
        patient.setDailyRate(Double.parseDouble(dailyRateField.getText()));
        patient.setNumberOfDaysAdmitted(Integer.parseInt(daysAdmittedField.getText()));
        patient.setAttendingDoctor(attendingDoctorCombo.getValue());
    }

    private void updateOutPatient(OutPatient patient, String name, int age, String gender, HospitalBlock block,
                                  String contactNumber, String address, String bloodGroup) {
        patient.setName(name);
        patient.setAge(age);
        patient.setGender(gender);
        patient.setLocation(block);
        patient.setContactNumber(contactNumber);
        patient.setAddress(address);
        patient.setBloodGroup(bloodGroup);

        patient.setAppointmentDate(appointmentDatePicker.getValue().toString());
        patient.setConsultFee(Double.parseDouble(consultFeeField.getText()));
        patient.setConsultingDoctor(consultingDoctorCombo.getValue());
        patient.setDiagnosis(diagnosisField.getText());
    }

    private void updateEmergencyPatient(EmergencyPatient patient, String name, int age, String gender, HospitalBlock block,
                                        String contactNumber, String address, String bloodGroup) {
        patient.setName(name);
        patient.setAge(age);
        patient.setGender(gender);
        patient.setLocation(block);
        patient.setContactNumber(contactNumber);
        patient.setAddress(address);
        patient.setBloodGroup(bloodGroup);

        patient.setSeverityLevel(severityLevelCombo.getValue());
        patient.setEmergencyTreatmentCost(Double.parseDouble(treatmentCostField.getText()));
        patient.setEmergencyContact(emergencyContactField.getText());
        patient.setTreatmentDetails(treatmentDetailsField.getText());

        // Preserve date part, update time part
        String[] dateParts = patient.getAdmissionTime().split(" ");
        if (dateParts.length > 0) {
            patient.setAdmissionTime(dateParts[0] + " " + admissionTimeField.getText());
        } else {
            patient.setAdmissionTime(DateUtil.getCurrentDate() + " " + admissionTimeField.getText());
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

        if (blockCombo.getValue() == null) {
            AlertUtil.showError("Validation Error", "Please select a hospital block.");
            return false;
        }

        // Validate type-specific fields
        if (patient instanceof InPatient && inPatientForm.isVisible()) {
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
        } else if (patient instanceof OutPatient && outPatientForm.isVisible()) {
            if (appointmentDatePicker.getValue() == null) {
                AlertUtil.showError("Validation Error", "Please select an appointment date.");
                return false;
            }

            if (!ValidationUtil.isValidNumber(consultFeeField.getText())) {
                AlertUtil.showError("Validation Error", "Please enter a valid consultation fee.");
                return false;
            }
        } else if (patient instanceof EmergencyPatient && emergencyPatientForm.isVisible()) {
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
        }

        return true;
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        boolean confirm = AlertUtil.showConfirmation("Confirm", "Are you sure you want to cancel? Any unsaved changes will be lost.");
        if (confirm) {
            closeWindow();
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}