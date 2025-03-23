package com.hospital.controller;

import com.hospital.model.Medication;
import com.hospital.service.MedicationService;
import com.hospital.util.AlertUtil;
import com.hospital.util.ValidationUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class MedicationFormController implements Initializable {

    @FXML
    private TextField nameField;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private ComboBox<String> categoryCombo;

    @FXML
    private TextField priceField;

    @FXML
    private TextField stockField;

    @FXML
    private TextField manufacturerField;

    @FXML
    private DatePicker expiryDatePicker;

    @FXML
    private ComboBox<String> dosageFormCombo;

    @FXML
    private CheckBox prescriptionRequiredCheck;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    private MedicationService medicationService;
    private Medication existingMedication;
    private boolean isEditMode = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        medicationService = MedicationService.getInstance();

        // Initialize category combo box
        categoryCombo.getItems().addAll("Pain Relief", "Antibiotics", "Cardiovascular",
                "Respiratory", "Gastrointestinal", "Diabetes", "Allergy", "Mental Health");

        // Initialize dosage form combo box
        dosageFormCombo.getItems().addAll("Tablets", "Capsules", "Liquid", "Injection",
                "Inhaler", "Cream", "Ointment", "Patch", "Drops");

        // Set up default values
        expiryDatePicker.setValue(LocalDate.now().plusYears(2));
    }

    public void initData(Medication medication) {
        if (medication != null) {
            this.existingMedication = medication;
            this.isEditMode = true;

            // Populate fields with medication data
            nameField.setText(medication.getName());
            descriptionArea.setText(medication.getDescription());
            categoryCombo.setValue(medication.getCategory());
            priceField.setText(String.valueOf(medication.getUnitPrice()));
            stockField.setText(String.valueOf(medication.getStockQuantity()));
            manufacturerField.setText(medication.getManufacturer());

            // Parse and set expiry date
            try {
                LocalDate expiryDate = LocalDate.parse(medication.getExpiryDate(),
                        DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                expiryDatePicker.setValue(expiryDate);
            } catch (Exception e) {
                // If date parsing fails, use default
                expiryDatePicker.setValue(LocalDate.now().plusYears(2));
            }

            dosageFormCombo.setValue(medication.getDosageForm());
            prescriptionRequiredCheck.setSelected(medication.isPrescription());
        }
    }

    @FXML
    public void handleSave(ActionEvent event) {
        if (!validateInput()) {
            return;
        }

        try {
            String name = nameField.getText();
            String description = descriptionArea.getText();
            String category = categoryCombo.getValue();
            double unitPrice = Double.parseDouble(priceField.getText());
            int stockQuantity = Integer.parseInt(stockField.getText());
            String manufacturer = manufacturerField.getText();
            String expiryDate = expiryDatePicker.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String dosageForm = dosageFormCombo.getValue();
            boolean prescriptionRequired = prescriptionRequiredCheck.isSelected();

            if (isEditMode) {
                // Update existing medication
                existingMedication.setName(name);
                existingMedication.setDescription(description);
                existingMedication.setCategory(category);
                existingMedication.setUnitPrice(unitPrice);
                existingMedication.setStockQuantity(stockQuantity);
                existingMedication.setManufacturer(manufacturer);
                existingMedication.setExpiryDate(expiryDate);
                existingMedication.setDosageForm(dosageForm);
                existingMedication.setPrescription(prescriptionRequired);

                medicationService.updateMedication(existingMedication);
                AlertUtil.showInformation("Success", "Medication updated successfully.");
            } else {
                // Create new medication
                Medication newMedication = new Medication(name, description, category, unitPrice,
                        stockQuantity, manufacturer, expiryDate, dosageForm, prescriptionRequired);

                medicationService.addMedication(newMedication);
                AlertUtil.showInformation("Success", "Medication added successfully.");
            }

            closeWindow();

        } catch (NumberFormatException e) {
            AlertUtil.showError("Input Error", "Please enter valid numbers for price and stock quantity.");
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Save Error", "Could not save medication: " + e.getMessage());
        }
    }

    @FXML
    public void handleCancel(ActionEvent event) {
        closeWindow();
    }

    private boolean validateInput() {
        if (!ValidationUtil.isNotEmpty(nameField.getText())) {
            AlertUtil.showError("Validation Error", "Medication name is required.");
            return false;
        }

        if (categoryCombo.getValue() == null) {
            AlertUtil.showError("Validation Error", "Please select a category.");
            return false;
        }

        if (!ValidationUtil.isValidNumber(priceField.getText())) {
            AlertUtil.showError("Validation Error", "Please enter a valid price.");
            return false;
        }

        if (!ValidationUtil.isValidNumber(stockField.getText())) {
            AlertUtil.showError("Validation Error", "Please enter a valid stock quantity.");
            return false;
        }

        if (!ValidationUtil.isNotEmpty(manufacturerField.getText())) {
            AlertUtil.showError("Validation Error", "Manufacturer is required.");
            return false;
        }

        if (expiryDatePicker.getValue() == null) {
            AlertUtil.showError("Validation Error", "Please select an expiry date.");
            return false;
        }

        if (dosageFormCombo.getValue() == null) {
            AlertUtil.showError("Validation Error", "Please select a dosage form.");
            return false;
        }

        return true;
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}