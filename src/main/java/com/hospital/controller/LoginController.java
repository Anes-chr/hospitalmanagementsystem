package com.hospital.controller;

import com.hospital.service.AuthService;
import com.hospital.util.AlertUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private ComboBox<String> roleComboBox;

    @FXML
    private Button loginButton;

    @FXML
    private Label statusLabel;

    @FXML
    private ImageView logoImageView;

    private AuthService authService;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        authService = AuthService.getInstance();

        // Set up role combo box
        roleComboBox.getItems().addAll("Admin", "Doctor", "Patient");
        roleComboBox.setValue("Admin");

        // Load hospital logo
        try {
            Image logo = new Image(getClass().getResourceAsStream("/com/hospital/images/hospital_logo.png"));
            logoImageView.setImage(logo);
        } catch (Exception e) {
            System.err.println("Could not load logo image: " + e.getMessage());
        }

        // Set up event handlers
        loginButton.setOnAction(this::handleLogin);

        // Set up enter key behavior
        passwordField.setOnAction(this::handleLogin);
    }

    @FXML
    public void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String role = roleComboBox.getValue();

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please enter both username and password.");
            return;
        }

        boolean success = authService.login(username, password);

        if (success && authService.getCurrentUserRole().equalsIgnoreCase(role)) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hospital/view/Dashboard.fxml"));
                Parent dashboardRoot = loader.load();

                Scene scene = new Scene(dashboardRoot);
                scene.getStylesheets().add(getClass().getResource("/com/hospital/css/darkTheme.css").toExternalForm());

                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setTitle("HMS - " + role + " Dashboard");
                stage.setScene(scene);
                stage.setMaximized(true);
                stage.show();

            } catch (IOException e) {
                e.printStackTrace();
                AlertUtil.showError("Error", "Could not load dashboard: " + e.getMessage());
            }
        } else {
            statusLabel.setText("Invalid username, password, or role selection.");
        }
    }
}