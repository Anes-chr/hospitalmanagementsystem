package com.hospital.controller;

import com.hospital.model.Medication;
import com.hospital.service.MedicationService;
import com.hospital.util.AlertUtil;
import com.hospital.util.ValidationUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class StockUpdateFormController implements Initializable {

    @FXML
    private Label medicationNameLabel;

    @FXML
    private Label currentStockLabel;

    @FXML
    private RadioButton addStockRadio;

    @FXML
    private RadioButton removeStockRadio;

    @FXML
    private ToggleGroup stockActionGroup;

    @FXML
    private TextField quantityField;

    @FXML
    private Button updateButton;

    @FXML
    private Button cancelButton;

    private MedicationService medicationService;
    private Medication medication;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        medicationService = MedicationService.getInstance();

        // Initialize toggle group
        stockActionGroup = new ToggleGroup();
        addStockRadio.setToggleGroup(stockActionGroup);
        removeStockRadio.setToggleGroup(stockActionGroup);

        // Set add stock as default
        addStockRadio.setSelected(true);
    }

    public void initData(Medication medication) {
        this.medication = medication;

        medicationNameLabel.setText(medication.getName());
        currentStockLabel.setText(String.valueOf(medication.getStockQuantity()));
    }

    @FXML
    public void handleUpdate(ActionEvent event) {
        if (!validateInput()) {
            return;
        }

        try {
            int quantity = Integer.parseInt(quantityField.getText());

            // If removing stock, make quantity negative
            if (removeStockRadio.isSelected()) {
                quantity = -quantity;
            }

            // Check if removing more than available
            if (quantity < 0 && Math.abs(quantity) > medication.getStockQuantity()) {
                AlertUtil.showError("Invalid Quantity",
                        "Cannot remove more than the current stock quantity.");
                return;
            }

            // Update stock
            medicationService.updateStock(medication.getId(), quantity);

            AlertUtil.showInformation("Success", "Stock updated successfully.");
            closeWindow();

        } catch (NumberFormatException e) {
            AlertUtil.showError("Input Error", "Please enter a valid quantity.");
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Update Error", "Could not update stock: " + e.getMessage());
        }
    }

    @FXML
    public void handleCancel(ActionEvent event) {
        closeWindow();
    }

    private boolean validateInput() {
        if (!ValidationUtil.isValidNumber(quantityField.getText())) {
            AlertUtil.showError("Validation Error", "Please enter a valid quantity.");
            return false;
        }

        int quantity = Integer.parseInt(quantityField.getText());
        if (quantity <= 0) {
            AlertUtil.showError("Validation Error", "Quantity must be a positive number.");
            return false;
        }

        return true;
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}