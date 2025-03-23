package com.hospital.controller;

import com.hospital.model.Medication;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.net.URL;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class MedicationDetailsController implements Initializable {

    @FXML
    private Label nameLabel;

    @FXML
    private Label categoryLabel;

    @FXML
    private Label descriptionLabel;

    @FXML
    private Label priceLabel;

    @FXML
    private Label stockLabel;

    @FXML
    private Label manufacturerLabel;

    @FXML
    private Label expiryDateLabel;

    @FXML
    private Label dosageFormLabel;

    @FXML
    private Label prescriptionRequiredLabel;

    @FXML
    private Button closeButton;

    private NumberFormat currencyFormatter;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);
    }

    public void initData(Medication medication) {
        nameLabel.setText(medication.getName());
        categoryLabel.setText(medication.getCategory());
        descriptionLabel.setText(medication.getDescription());
        priceLabel.setText(currencyFormatter.format(medication.getUnitPrice()));
        stockLabel.setText(String.valueOf(medication.getStockQuantity()));
        manufacturerLabel.setText(medication.getManufacturer());
        expiryDateLabel.setText(medication.getExpiryDate());
        dosageFormLabel.setText(medication.getDosageForm());
        prescriptionRequiredLabel.setText(medication.isPrescription() ? "Yes" : "No");
    }

    @FXML
    public void handleClose() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}