package com.hospital.controller;

import com.hospital.model.Patient;
import com.hospital.model.Doctor;
import com.hospital.model.Prescription;
import com.hospital.service.PatientService;
import com.hospital.service.DoctorService;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.net.URL;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class PrescriptionDetailsController implements Initializable {

    @FXML
    private Label prescriptionIdLabel;

    @FXML
    private Label patientNameLabel;

    @FXML
    private Label doctorNameLabel;

    @FXML
    private Label dateLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private Label fulfilledDateLabel;

    @FXML
    private Label notesLabel;

    @FXML
    private TableView<Prescription.PrescriptionItem> medicationsTable;

    @FXML
    private TableColumn<Prescription.PrescriptionItem, String> medicationNameColumn;

    @FXML
    private TableColumn<Prescription.PrescriptionItem, Integer> quantityColumn;

    @FXML
    private TableColumn<Prescription.PrescriptionItem, Double> priceColumn;

    @FXML
    private TableColumn<Prescription.PrescriptionItem, String> dosageColumn;

    @FXML
    private TableColumn<Prescription.PrescriptionItem, String> instructionsColumn;

    @FXML
    private Label totalCostLabel;

    @FXML
    private Button closeButton;

    private PatientService patientService;
    private DoctorService doctorService;
    private NumberFormat currencyFormatter;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        patientService = new PatientService();
        doctorService = DoctorService.getInstance();
        currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);

        // Set up table columns
        medicationNameColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getMedication().getName()));

        quantityColumn.setCellValueFactory(data ->
                new SimpleIntegerProperty(data.getValue().getQuantity()).asObject());

        priceColumn.setCellValueFactory(data ->
                new SimpleDoubleProperty(data.getValue().getMedication().getUnitPrice()).asObject());

        dosageColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getDosage()));

        instructionsColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getInstructions()));
    }

    public void initData(Prescription prescription) {
        prescriptionIdLabel.setText(prescription.getId());

        // Get patient and doctor names
        Patient patient = patientService.getPatientById(prescription.getPatientId());
        patientNameLabel.setText(patient != null ? patient.getName() : "Unknown");

        Doctor doctor = doctorService.getDoctorById(prescription.getDoctorId());
        doctorNameLabel.setText(doctor != null ? doctor.getFullName() : "Unknown");

        dateLabel.setText(prescription.getDate());
        statusLabel.setText(prescription.isFulfilled() ? "Fulfilled" : "Pending");

        if (prescription.isFulfilled() && prescription.getFulfilledDate() != null) {
            fulfilledDateLabel.setText(prescription.getFulfilledDate());
        } else {
            fulfilledDateLabel.setText("N/A");
        }

        notesLabel.setText(prescription.getNotes());

        // Populate medications table
        medicationsTable.setItems(FXCollections.observableArrayList(prescription.getMedications()));

        // Set total cost
        totalCostLabel.setText(currencyFormatter.format(prescription.calculateTotalCost()));
    }

    @FXML
    public void handleClose() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}