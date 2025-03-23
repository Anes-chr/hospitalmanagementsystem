package com.hospital.controller;

import com.hospital.model.MedicalTest;
import com.hospital.service.MedicalTestService;
import com.hospital.util.AlertUtil;
import com.hospital.util.ValidationUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MedicalTestFormController implements Initializable {

    @FXML
    private TextField nameField;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private ComboBox<String> categoryCombo;

    @FXML
    private TextField costField;

    @FXML
    private TextArea preparationArea;

    @FXML
    private TextField durationField;

    @FXML
    private CheckBox fastingRequiredCheck;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    private MedicalTestService medicalTestService;
    private MedicalTest existingTest;
    private boolean isEditMode = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        medicalTestService = MedicalTestService.getInstance();

        // Initialize category combo box
        categoryCombo.getItems().addAll("Hematology", "Chemistry", "Endocrinology",
                "Immunology", "Microbiology", "Urology", "Cardiology", "Neurology",
                "Infectious Disease", "Other");
    }

    public void initData(MedicalTest test) {
        if (test != null) {
            this.existingTest = test;
            this.isEditMode = true;

            // Populate fields with test data
            nameField.setText(test.getName());
            descriptionArea.setText(test.getDescription());
            categoryCombo.setValue(test.getCategory());
            costField.setText(String.valueOf(test.getCost()));
            preparationArea.setText(test.getPreparation());
            durationField.setText(test.getDuration());
            fastingRequiredCheck.setSelected(test.isFasting());
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
            double cost = Double.parseDouble(costField.getText());
            String preparation = preparationArea.getText();
            String duration = durationField.getText();
            boolean fastingRequired = fastingRequiredCheck.isSelected();

            if (isEditMode) {
                // Update existing test
                existingTest.setName(name);
                existingTest.setDescription(description);
                existingTest.setCategory(category);
                existingTest.setCost(cost);
                existingTest.setPreparation(preparation);
                existingTest.setDuration(duration);
                existingTest.setFasting(fastingRequired);

                medicalTestService.updateMedicalTest(existingTest);
                AlertUtil.showInformation("Success", "Medical test updated successfully.");
            } else {
                // Create new test
                MedicalTest newTest = new MedicalTest(name, description, category, cost,
                        preparation, duration, fastingRequired);

                medicalTestService.addMedicalTest(newTest);
                AlertUtil.showInformation("Success", "Medical test added successfully.");
            }

            closeWindow();

        } catch (NumberFormatException e) {
            AlertUtil.showError("Input Error", "Please enter a valid cost.");
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Save Error", "Could not save medical test: " + e.getMessage());
        }
    }

    @FXML
    public void handleCancel(ActionEvent event) {
        closeWindow();
    }

    private boolean validateInput() {
        if (!ValidationUtil.isNotEmpty(nameField.getText())) {
            AlertUtil.showError("Validation Error", "Test name is required.");
            return false;
        }

        if (categoryCombo.getValue() == null) {
            AlertUtil.showError("Validation Error", "Please select a category.");
            return false;
        }

        if (!ValidationUtil.isValidNumber(costField.getText())) {
            AlertUtil.showError("Validation Error", "Please enter a valid cost.");
            return false;
        }

        if (!ValidationUtil.isNotEmpty(durationField.getText())) {
            AlertUtil.showError("Validation Error", "Duration is required.");
            return false;
        }

        return true;
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}