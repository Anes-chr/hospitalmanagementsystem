package com.hospital.controller;

import com.hospital.model.*;
import com.hospital.service.PatientService;
import com.hospital.util.AlertUtil;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.layout.HBox;  // Make sure this import is present
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class PatientManagementController implements Initializable {

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> filterTypeCombo;

    @FXML
    private ComboBox<String> filterLocationCombo;

    @FXML
    private TableView<Patient> patientTable;

    @FXML
    private TableColumn<Patient, String> idColumn;

    @FXML
    private TableColumn<Patient, String> nameColumn;

    @FXML
    private TableColumn<Patient, Integer> ageColumn;

    @FXML
    private TableColumn<Patient, String> genderColumn;

    @FXML
    private TableColumn<Patient, String> contactColumn;

    @FXML
    private TableColumn<Patient, String> locationColumn;

    @FXML
    private TableColumn<Patient, String> typeColumn;

    @FXML
    private TableColumn<Patient, Void> actionsColumn;

    @FXML
    private Button addPatientButton;

    @FXML
    private Button viewDetailsButton;

    @FXML
    private Button editPatientButton;

    @FXML
    private Button deletePatientButton;

    @FXML
    private Button generateBillButton;

    @FXML
    private Label totalPatientsLabel;

    @FXML
    private Label inpatientsLabel;

    @FXML
    private Label outpatientsLabel;

    @FXML
    private Label emergencyLabel;

    private PatientService patientService;
    private ObservableList<Patient> patientsData;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        patientService = new PatientService();

        // Initialize columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        ageColumn.setCellValueFactory(new PropertyValueFactory<>("age"));
        genderColumn.setCellValueFactory(new PropertyValueFactory<>("gender"));
        contactColumn.setCellValueFactory(new PropertyValueFactory<>("contactNumber"));

        // Custom cell factory for location to display the full location
        locationColumn.setCellValueFactory(cellData -> {
            HospitalBlock location = cellData.getValue().getLocation();
            return new SimpleStringProperty(location != null ? location.getFullLocation() : "N/A");
        });

        // Patient type column
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("patientType"));

        // Setup action buttons column
        setupActionsColumn();

        // Setup filter combos
        filterTypeCombo.getItems().addAll("All Types", "InPatient", "OutPatient", "EmergencyPatient");
        filterTypeCombo.setValue("All Types");
        filterTypeCombo.setOnAction(event -> handleFilter());

        filterLocationCombo.getItems().add("All Locations");

        // Get unique locations from existing patients
        List<Patient> patients = patientService.getAllPatients();
        patients.stream()
                .map(Patient::getLocation)
                .filter(loc -> loc != null)
                .map(HospitalBlock::getFullLocation)
                .distinct()
                .forEach(loc -> {
                    if (!filterLocationCombo.getItems().contains(loc)) {
                        filterLocationCombo.getItems().add(loc);
                    }
                });

        filterLocationCombo.setValue("All Locations");
        filterLocationCombo.setOnAction(event -> handleFilter());

        // Setup search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> handleFilter());

        // Setup selection listener for enabling/disabling buttons
        patientTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> updateButtonStates(newValue));

        // Load patients
        loadPatients();
    }

    private void setupActionsColumn() {
        Callback<TableColumn<Patient, Void>, TableCell<Patient, Void>> cellFactory =
                new Callback<>() {
                    @Override
                    public TableCell<Patient, Void> call(TableColumn<Patient, Void> param) {
                        return new TableCell<>() {
                            private final Button viewBtn = new Button("View");
                            private final Button editBtn = new Button("Edit");
                            private final Button deleteBtn = new Button("Delete");

                            {
                                viewBtn.getStyleClass().add("btn-sm");
                                editBtn.getStyleClass().add("btn-sm");
                                deleteBtn.getStyleClass().add("btn-sm");

                                viewBtn.setOnAction(event -> {
                                    Patient patient = getTableView().getItems().get(getIndex());
                                    handleViewDetails(patient);
                                });

                                editBtn.setOnAction(event -> {
                                    Patient patient = getTableView().getItems().get(getIndex());
                                    handleEditPatient(patient);
                                });

                                deleteBtn.setOnAction(event -> {
                                    Patient patient = getTableView().getItems().get(getIndex());
                                    handleDeletePatient(patient);
                                });
                            }

                            @Override
                            public void updateItem(Void item, boolean empty) {
                                super.updateItem(item, empty);
                                if (empty) {
                                    setGraphic(null);
                                } else {
                                    HBox hbox = new HBox(5);
                                    hbox.getChildren().addAll(viewBtn, editBtn, deleteBtn);
                                    setGraphic(hbox);
                                }
                            }
                        };
                    }
                };

        actionsColumn.setCellFactory(cellFactory);
    }

    private void loadPatients() {
        try {
            List<Patient> patients = patientService.getAllPatients();
            patientsData = FXCollections.observableArrayList(patients);
            patientTable.setItems(patientsData);

            // Update statistics
            updateStatistics(patients);
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Error", "Could not load patients: " + e.getMessage());
        }
    }

    private void updateStatistics(List<Patient> patients) {
        totalPatientsLabel.setText(String.valueOf(patients.size()));

        long inpatientCount = patients.stream()
                .filter(p -> p instanceof InPatient)
                .count();
        inpatientsLabel.setText(String.valueOf(inpatientCount));

        long outpatientCount = patients.stream()
                .filter(p -> p instanceof OutPatient)
                .count();
        outpatientsLabel.setText(String.valueOf(outpatientCount));

        long emergencyCount = patients.stream()
                .filter(p -> p instanceof EmergencyPatient)
                .count();
        emergencyLabel.setText(String.valueOf(emergencyCount));
    }

    private void updateButtonStates(Patient selectedPatient) {
        boolean hasSelection = selectedPatient != null;
        viewDetailsButton.setDisable(!hasSelection);
        editPatientButton.setDisable(!hasSelection);
        deletePatientButton.setDisable(!hasSelection);
        generateBillButton.setDisable(!hasSelection);
    }

    @FXML
    private void handleFilter() {
        String searchText = searchField.getText().toLowerCase();
        String typeFilter = filterTypeCombo.getValue();
        String locationFilter = filterLocationCombo.getValue();

        List<Patient> allPatients = patientService.getAllPatients();

        // Apply type filter
        if (!"All Types".equals(typeFilter)) {
            allPatients = patientService.getPatientsByType(typeFilter);
        }

        // Apply location filter
        if (!"All Locations".equals(locationFilter)) {
            final String location = locationFilter;
            allPatients = allPatients.stream()
                    .filter(p -> p.getLocation() != null &&
                            p.getLocation().getFullLocation().contains(location))
                    .toList();
        }

        // Apply search text
        if (!searchText.isEmpty()) {
            final String search = searchText;
            allPatients = allPatients.stream()
                    .filter(p -> p.getName().toLowerCase().contains(search) ||
                            (p.getContactNumber() != null && p.getContactNumber().toLowerCase().contains(search)))
                    .toList();
        }

        patientsData = FXCollections.observableArrayList(allPatients);
        patientTable.setItems(patientsData);

        // Update statistics
        updateStatistics(allPatients);
    }

    @FXML
    public void handleAddPatient(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hospital/view/NewPatient.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Add New Patient");
            stage.initModality(Modality.APPLICATION_MODAL);

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/com/hospital/css/darkTheme.css").toExternalForm());

            stage.setScene(scene);
            stage.showAndWait();

            // Refresh the table to show the new patient
            loadPatients();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Error", "Could not open add patient form: " + e.getMessage());
        }
    }

    @FXML
    public void handleViewDetails() {
        Patient selectedPatient = patientTable.getSelectionModel().getSelectedItem();
        if (selectedPatient != null) {
            handleViewDetails(selectedPatient);
        } else {
            AlertUtil.showWarning("No Selection", "Please select a patient to view.");
        }
    }

    private void handleViewDetails(Patient patient) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hospital/view/PatientDetails.fxml"));
            Parent root = loader.load();

            PatientDetailsController controller = loader.getController();
            controller.initData(patient);

            Stage stage = new Stage();
            stage.setTitle("Patient Details");
            stage.initModality(Modality.APPLICATION_MODAL);

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/com/hospital/css/darkTheme.css").toExternalForm());

            stage.setScene(scene);
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Error", "Could not open patient details: " + e.getMessage());
        }
    }

    @FXML
    public void handleEditPatient() {
        Patient selectedPatient = patientTable.getSelectionModel().getSelectedItem();
        if (selectedPatient != null) {
            handleEditPatient(selectedPatient);
        } else {
            AlertUtil.showWarning("No Selection", "Please select a patient to edit.");
        }
    }

    private void handleEditPatient(Patient patient) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hospital/view/EditPatient.fxml"));
            Parent root = loader.load();

            EditPatientController controller = loader.getController();
            controller.initData(patient);

            Stage stage = new Stage();
            stage.setTitle("Edit Patient");
            stage.initModality(Modality.APPLICATION_MODAL);

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/com/hospital/css/darkTheme.css").toExternalForm());

            stage.setScene(scene);
            stage.showAndWait();

            // Refresh the table to show updated patient info
            loadPatients();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Error", "Could not open edit patient form: " + e.getMessage());
        }
    }

    @FXML
    public void handleDeletePatient() {
        Patient selectedPatient = patientTable.getSelectionModel().getSelectedItem();
        if (selectedPatient != null) {
            handleDeletePatient(selectedPatient);
        } else {
            AlertUtil.showWarning("No Selection", "Please select a patient to delete.");
        }
    }

    private void handleDeletePatient(Patient patient) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Patient");
        alert.setHeaderText("Delete Patient: " + patient.getName());
        alert.setContentText("Are you sure you want to delete this patient? This action cannot be undone.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                patientService.deletePatient(patient.getId());

                // Refresh the table
                loadPatients();

                AlertUtil.showInformation("Success", "Patient deleted successfully.");
            } catch (IOException e) {
                e.printStackTrace();
                AlertUtil.showError("Error", "Could not delete patient: " + e.getMessage());
            }
        }
    }

    @FXML
    public void handleGenerateBill() {
        Patient selectedPatient = patientTable.getSelectionModel().getSelectedItem();
        if (selectedPatient != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hospital/view/BillView.fxml"));
                Parent root = loader.load();

                BillViewController controller = loader.getController();
                controller.initData(selectedPatient);

                Stage stage = new Stage();
                stage.setTitle("Patient Bill");
                stage.initModality(Modality.APPLICATION_MODAL);

                Scene scene = new Scene(root);
                scene.getStylesheets().add(getClass().getResource("/com/hospital/css/darkTheme.css").toExternalForm());

                stage.setScene(scene);
                stage.show();

            } catch (IOException e) {
                e.printStackTrace();
                AlertUtil.showError("Error", "Could not generate bill: " + e.getMessage());
            }
        } else {
            AlertUtil.showWarning("No Selection", "Please select a patient to generate a bill.");
        }
    }

    @FXML
    public void handleRefresh() {
        loadPatients();
        searchField.clear();
        filterTypeCombo.setValue("All Types");
        filterLocationCombo.setValue("All Locations");
    }
}