package com.hospital.controller;

import com.hospital.model.MedicalTest;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.net.URL;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class MedicalTestDetailsController implements Initializable {

    @FXML
    private Label nameLabel;

    @FXML
    private Label categoryLabel;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private Label costLabel;

    @FXML
    private Label durationLabel;

    @FXML
    private Label fastingRequiredLabel;

    @FXML
    private TextArea preparationArea;

    @FXML
    private Button closeButton;

    private NumberFormat currencyFormatter;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);
    }

    public void initData(MedicalTest test) {
        nameLabel.setText(test.getName());
        categoryLabel.setText(test.getCategory());
        descriptionArea.setText(test.getDescription());
        costLabel.setText(currencyFormatter.format(test.getCost()));
        durationLabel.setText(test.getDuration());
        fastingRequiredLabel.setText(test.isFasting() ? "Yes" : "No");
        preparationArea.setText(test.getPreparation());
    }

    @FXML
    public void handleClose() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}