package com.hospital.controller;

import com.hospital.model.EmergencyPatient;
import com.hospital.model.InPatient;
import com.hospital.model.OutPatient;
import com.hospital.model.Patient;
import com.hospital.service.PatientService;
import com.hospital.util.AlertUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.function.Predicate;

public class PatientManagementController implements Initializable {

    @FXML
    private TextField searchField;

    @FXML
    private Button searchButton;

    @FXML
    private ComboBox<String> typeFilterCombo;

    @FXML
    private Button addPatientButton;

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
    private TableColumn<Patient, String> typeColumn;

    @FXML
    private TableColumn<Patient, String> contactColumn;

    @FXML
    private TableColumn<Patient, String> locationColumn;

    @FXML
    private TableColumn<Patient, String> registrationDateColumn;

    @FXML
    private TableColumn<Patient, String> actionColumn;

    @FXML
    private GridPane detailsPane;

    @FXML
    private Label detailsIdLabel;

    @FXML
    private Label detailsTypeLabel;

    @FXML
    private Label detailsNameLabel;

    @FXML
    private Label detailsAgeLabel;

    @FXML
    private Label detailsGenderLabel;

    @FXML
    private Label detailsBloodGroupLabel;

    @FXML
    private Label detailsContactLabel;

    @FXML
    private Label detailsRegistrationLabel;

    @FXML
    private Label detailsAddressLabel;

    @FXML
    private Label detailsLocationLabel;

    @FXML
    private TextArea detailsSpecificLabel;

    @FXML
    private Label detailsBillLabel;

    @FXML
    private Button editPatientButton;

    @FXML
    private Button deletePatientButton;

    @FXML
    private Button generateBillButton;

    @FXML
    private Label totalCountLabel;

    @FXML
    private Label inPatientCountLabel;

    @FXML
    private Label outPatientCountLabel;

    @FXML
    private Label emergencyCountLabel;

    private PatientService patientService;
    private ObservableList<Patient> allPatients;
    private FilteredList<Patient> filteredPatients;
    private NumberFormat currencyFormatter;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        patientService = new PatientService();
        currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);

        // Initialize the combo box for filtering
        typeFilterCombo.getItems().addAll("All Types", "InPatient", "OutPatient", "EmergencyPatient");
        typeFilterCombo.getSelectionModel().selectFirst();

        // Initialize table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        ageColumn.setCellValueFactory(new PropertyValueFactory<>("age"));
        genderColumn.setCellValueFactory(new PropertyValueFactory<>("gender"));

        typeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPatientType()));

        contactColumn.setCellValueFactory(new PropertyValueFactory<>("contactNumber"));

        locationColumn.setCellValueFactory(data -> {
            if (data.getValue().getLocation() != null) {
                return new SimpleStringProperty(data.getValue().getLocation().getBlockName());
            } else {
                return new SimpleStringProperty("N/A");
            }
        });

        registrationDateColumn.setCellValueFactory(new PropertyValueFactory<>("registrationDate"));

        // Add action buttons to each row
        actionColumn.setCellFactory(createActionButtonCellFactory());

        // Load patients
        loadPatients();

        // Add selection listener to populate details
        patientTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                showPatientDetails(newSelection);
            }
        });

        // Set up search functionality
        searchField.textProperty().addListener((obs, oldVal, newVal) -> handleSearch());
    }

    private void loadPatients() {
        // Initialize with sample data if empty
        patientService.initializeWithSampleData();

        List<Patient> patients = patientService.getAllPatients();
        allPatients = FXCollections.observableArrayList(patients);

        // Create filtered list
        filteredPatients = new FilteredList<>(allPatients, p -> true);
        patientTable.setItems(filteredPatients);

        // Update counts
        updatePatientCounts();
    }

    private void updatePatientCounts() {
        int totalCount = allPatients.size();
        int inPatientCount = (int) allPatients.stream()
                .filter(p -> p.getPatientType().equals("InPatient"))
                .count();
        int outPatientCount = (int) allPatients.stream()
                .filter(p -> p.getPatientType().equals("OutPatient"))
                .count();
        int emergencyCount = (int) allPatients.stream()
                .filter(p -> p.getPatientType().equals("EmergencyPatient"))
                .count();

        totalCountLabel.setText(String.valueOf(totalCount));
        inPatientCountLabel.setText(String.valueOf(inPatientCount));
        outPatientCountLabel.setText(String.valueOf(outPatientCount));
        emergencyCountLabel.setText(String.valueOf(emergencyCount));
    }

    private Callback<TableColumn<Patient, String>, TableCell<Patient, String>> createActionButtonCellFactory() {
        return new Callback<>() {
            @Override
            public TableCell<Patient, String> call(TableColumn<Patient, String> param) {
                return new TableCell<>() {
                    private final Button viewBtn = new Button("View");
                    private final Button editBtn = new Button("Edit");
                    private final Button deleteBtn = new Button("Delete");

                    {
                        viewBtn.setOnAction(event -> {
                            Patient patient = getTableRow().getItem();
                            if (patient != null) {
                                showPatientDetails(patient);
                            }
                        });

                        editBtn.setOnAction(event -> {
                            Patient patient = getTableRow().getItem();
                            if (patient != null) {
                                handleEditPatient(patient);
                            }
                        });

                        deleteBtn.setOnAction(event -> {
                            Patient patient = getTableRow().getItem();
                            if (patient != null) {
                                handleDeletePatient(patient);
                            }
                        });
                    }

                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);

                        if (empty) {
                            setGraphic(null);
                        } else {
                            HBox buttons = new HBox(5);
                            buttons.getChildren().addAll(viewBtn, editBtn, deleteBtn);
                            setGraphic(buttons);
                        }
                    }
                };
            }
        };
    }

    private void showPatientDetails(Patient patient) {
        detailsIdLabel.setText(patient.getId());
        detailsTypeLabel.setText(patient.getPatientType());
        detailsNameLabel.setText(patient.getName());
        detailsAgeLabel.setText(String.valueOf(patient.getAge()));
        detailsGenderLabel.setText(patient.getGender());
        detailsBloodGroupLabel.setText(patient.getBloodGroup());
        detailsContactLabel.setText(patient.getContactNumber());
        detailsRegistrationLabel.setText(patient.getRegistrationDate());
        detailsAddressLabel.setText(patient.getAddress());

        if (patient.getLocation() != null) {
            detailsLocationLabel.setText(patient.getLocation().getFullLocation());
        } else {
            detailsLocationLabel.setText("N/A");
        }

        detailsBillLabel.setText(currencyFormatter.format(patient.calculateBill()));

        // Show type-specific details
        StringBuilder specificDetails = new StringBuilder();

        if (patient instanceof InPatient) {
            InPatient inPatient = (InPatient) patient;
            specificDetails.append("Room Number: ").append(inPatient.getRoomNumber()).append("\n");
            specificDetails.append("Admission Date: ").append(inPatient.getAdmissionDate()).append("\n");
            specificDetails.append("Daily Rate: ").append(currencyFormatter.format(inPatient.getDailyRate())).append("\n");
            specificDetails.append("Days Admitted: ").append(inPatient.getNumberOfDaysAdmitted()).append("\n");
            specificDetails.append("Attending Doctor: ").append(inPatient.getAttendingDoctor()).append("\n");
            if (inPatient.getDischargeDate() != null) {
                specificDetails.append("Discharge Date: ").append(inPatient.getDischargeDate());
            }
        } else if (patient instanceof OutPatient) {
            OutPatient outPatient = (OutPatient) patient;
            specificDetails.append("Appointment Date: ").append(outPatient.getAppointmentDate()).append("\n");
            specificDetails.append("Consult Fee: ").append(currencyFormatter.format(outPatient.getConsultFee())).append("\n");
            specificDetails.append("Consulting Doctor: ").append(outPatient.getConsultingDoctor()).append("\n");
            specificDetails.append("Diagnosis: ").append(outPatient.getDiagnosis());
        } else if (patient instanceof EmergencyPatient) {
            EmergencyPatient emergencyPatient = (EmergencyPatient) patient;
            specificDetails.append("Severity Level: ").append(emergencyPatient.getSeverityLevel()).append("\n");
            specificDetails.append("Emergency Contact: ").append(emergencyPatient.getEmergencyContact()).append("\n");
            specificDetails.append("Treatment Details: ").append(emergencyPatient.getTreatmentDetails()).append("\n");
            specificDetails.append("Admission Time: ").append(emergencyPatient.getAdmissionTime()).append("\n");
            specificDetails.append("Treatment Cost: ").append(currencyFormatter.format(emergencyPatient.getEmergencyTreatmentCost()));
        }

        detailsSpecificLabel.setText(specificDetails.toString());
    }

    @FXML
    public void handleSearch() {
        String searchText = searchField.getText().toLowerCase();
        String selectedType = typeFilterCombo.getValue();

        Predicate<Patient> typeFilter;
        if (selectedType.equals("All Types")) {
            typeFilter = p -> true;
        } else {
            typeFilter = p -> p.getPatientType().equals(selectedType);
        }

        Predicate<Patient> searchFilter;
        if (searchText.isEmpty()) {
            searchFilter = p -> true;
        } else {
            searchFilter = p ->
                    p.getName().toLowerCase().contains(searchText) ||
                            p.getContactNumber().toLowerCase().contains(searchText) ||
                            (p.getAddress() != null && p.getAddress().toLowerCase().contains(searchText));
        }

        filteredPatients.setPredicate(typeFilter.and(searchFilter));
    }

    @FXML
    public void handleFilterChange() {
        handleSearch(); // Re-filter with new type selection
    }

    @FXML
    public void handleAddPatient() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hospital/view/NewPatient.fxml"));
            Parent newPatientView = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Add New Patient");
            stage.initModality(Modality.APPLICATION_MODAL);

            Scene scene = new Scene(newPatientView);
            scene.getStylesheets().add(getClass().getResource("/com/hospital/css/darkTheme.css").toExternalForm());

            stage.setScene(scene);
            stage.showAndWait();

            // Refresh patient list
            loadPatients();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Error", "Could not load new patient form: " + e.getMessage());
        }
    }

    @FXML
    public void handleEditPatient() {
        Patient selectedPatient = patientTable.getSelectionModel().getSelectedItem();
        if (selectedPatient != null) {
            handleEditPatient(selectedPatient);
        } else {
            AlertUtil.showWarning("Selection Required", "Please select a patient to edit.");
        }
    }

    private void handleEditPatient(Patient patient) {
        // In a real app, this would load the edit form with patient data
        AlertUtil.showInformation("Coming Soon", "Patient editing will be implemented in a future update.");
    }

    @FXML
    public void handleDeletePatient() {
        Patient selectedPatient = patientTable.getSelectionModel().getSelectedItem();
        if (selectedPatient != null) {
            handleDeletePatient(selectedPatient);
        } else {
            AlertUtil.showWarning("Selection Required", "Please select a patient to delete.");
        }
    }

    private void handleDeletePatient(Patient patient) {
        boolean confirm = AlertUtil.showConfirmation("Confirm Deletion",
                "Are you sure you want to delete the patient record for " + patient.getName() + "?");

        if (confirm) {
            try {
                patientService.deletePatient(patient.getId());
                loadPatients();
                AlertUtil.showInformation("Success", "Patient record deleted successfully.");
            } catch (IOException e) {
                e.printStackTrace();
                AlertUtil.showError("Error", "Could not delete patient record: " + e.getMessage());
            }
        }
    }

    @FXML
    public void handleGenerateBill() {
        Patient selectedPatient = patientTable.getSelectionModel().getSelectedItem();
        if (selectedPatient != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hospital/view/BillView.fxml"));
                Parent billView = loader.load();

                BillViewController controller = loader.getController();
                controller.initData(selectedPatient);

                Stage stage = new Stage();
                stage.setTitle("Patient Bill");
                stage.initModality(Modality.APPLICATION_MODAL);

                Scene scene = new Scene(billView);
                scene.getStylesheets().add(getClass().getResource("/com/hospital/css/darkTheme.css").toExternalForm());

                stage.setScene(scene);
                stage.showAndWait();

            } catch (IOException e) {
                e.printStackTrace();
                AlertUtil.showError("Error", "Could not generate bill: " + e.getMessage());
            }
        } else {
            AlertUtil.showWarning("Selection Required", "Please select a patient to generate a bill.");
        }
    }
}