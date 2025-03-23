package com.hospital.controller;

import com.hospital.model.EmergencyPatient;
import com.hospital.model.InPatient;
import com.hospital.model.OutPatient;
import com.hospital.model.Patient;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class PatientDetailsController implements Initializable {

    @FXML
    private Label idLabel;

    @FXML
    private Label nameLabel;

    @FXML
    private Label ageLabel;

    @FXML
    private Label genderLabel;

    @FXML
    private Label contactLabel;

    @FXML
    private Label bloodGroupLabel;

    @FXML
    private Label registrationDateLabel;

    @FXML
    private TextArea addressArea;

    @FXML
    private Label patientTypeLabel;

    @FXML
    private Label locationLabel;

    // InPatient specific fields
    @FXML
    private VBox inPatientDetails;

    @FXML
    private Label roomNumberLabel;

    @FXML
    private Label admissionDateLabel;

    @FXML
    private Label dailyRateLabel;

    @FXML
    private Label daysAdmittedLabel;

    @FXML
    private Label dischargeDateLabel;

    @FXML
    private Label attendingDoctorLabel;

    // OutPatient specific fields
    @FXML
    private VBox outPatientDetails;

    @FXML
    private Label appointmentDateLabel;

    @FXML
    private Label consultFeeLabel;

    @FXML
    private Label consultingDoctorLabel;

    @FXML
    private TextArea diagnosisArea;

    // EmergencyPatient specific fields
    @FXML
    private VBox emergencyPatientDetails;

    @FXML
    private Label severityLevelLabel;

    @FXML
    private Label treatmentCostLabel;

    @FXML
    private Label emergencyContactLabel;

    @FXML
    private TextArea treatmentDetailsArea;

    @FXML
    private Label admissionTimeLabel;

    @FXML
    private Button closeButton;

    private NumberFormat currencyFormatter;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);
    }

    public void initData(Patient patient) {
        // Set common patient details
        idLabel.setText(patient.getId());
        nameLabel.setText(patient.getName());
        ageLabel.setText(String.valueOf(patient.getAge()));
        genderLabel.setText(patient.getGender());
        contactLabel.setText(patient.getContactNumber());
        registrationDateLabel.setText(patient.getRegistrationDate());
        addressArea.setText(patient.getAddress());
        bloodGroupLabel.setText(patient.getBloodGroup());
        patientTypeLabel.setText(patient.getPatientType());

        if (patient.getLocation() != null) {
            locationLabel.setText(patient.getLocation().getFullLocation());
        } else {
            locationLabel.setText("N/A");
        }

        // Hide all specific detail sections initially
        inPatientDetails.setVisible(false);
        outPatientDetails.setVisible(false);
        emergencyPatientDetails.setVisible(false);

        // Show specific details based on patient type
        if (patient instanceof InPatient) {
            showInPatientDetails((InPatient) patient);
        } else if (patient instanceof OutPatient) {
            showOutPatientDetails((OutPatient) patient);
        } else if (patient instanceof EmergencyPatient) {
            showEmergencyPatientDetails((EmergencyPatient) patient);
        }
    }

    private void showInPatientDetails(InPatient patient) {
        inPatientDetails.setVisible(true);

        roomNumberLabel.setText(patient.getRoomNumber());
        admissionDateLabel.setText(patient.getAdmissionDate());
        dailyRateLabel.setText(currencyFormatter.format(patient.getDailyRate()));
        daysAdmittedLabel.setText(String.valueOf(patient.getNumberOfDaysAdmitted()));
        dischargeDateLabel.setText(patient.getDischargeDate() != null ? patient.getDischargeDate() : "Not discharged");
        attendingDoctorLabel.setText(patient.getAttendingDoctor());
    }

    private void showOutPatientDetails(OutPatient patient) {
        outPatientDetails.setVisible(true);

        appointmentDateLabel.setText(patient.getAppointmentDate());
        consultFeeLabel.setText(currencyFormatter.format(patient.getConsultFee()));
        consultingDoctorLabel.setText(patient.getConsultingDoctor());
        diagnosisArea.setText(patient.getDiagnosis());
    }

    private void showEmergencyPatientDetails(EmergencyPatient patient) {
        emergencyPatientDetails.setVisible(true);

        severityLevelLabel.setText(patient.getSeverityLevel());
        treatmentCostLabel.setText(currencyFormatter.format(patient.getEmergencyTreatmentCost()));
        emergencyContactLabel.setText(patient.getEmergencyContact());
        treatmentDetailsArea.setText(patient.getTreatmentDetails());
        admissionTimeLabel.setText(patient.getAdmissionTime());
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}