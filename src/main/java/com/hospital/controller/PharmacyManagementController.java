package com.hospital.controller;

import com.hospital.model.Doctor;
import com.hospital.model.Medication;
import com.hospital.model.Patient;
import com.hospital.model.Prescription;
import com.hospital.service.DoctorService;
import com.hospital.service.MedicationService;
import com.hospital.service.PatientService;
import com.hospital.service.PrescriptionService;
import com.hospital.util.AlertUtil;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.function.Predicate;

public class PharmacyManagementController implements Initializable {

    // Medication Tab
    @FXML
    private TextField searchMedicationField;

    @FXML
    private ComboBox<String> categoryFilterCombo;

    @FXML
    private Button addMedicationButton;

    @FXML
    private TableView<Medication> medicationTable;

    @FXML
    private TableColumn<Medication, String> medIdColumn;

    @FXML
    private TableColumn<Medication, String> medNameColumn;

    @FXML
    private TableColumn<Medication, String> medCategoryColumn;

    @FXML
    private TableColumn<Medication, String> medDosageFormColumn;

    @FXML
    private TableColumn<Medication, Double> medPriceColumn;

    @FXML
    private TableColumn<Medication, Integer> medStockColumn;

    @FXML
    private TableColumn<Medication, String> medManufacturerColumn;

    @FXML
    private TableColumn<Medication, String> medExpiryColumn;

    @FXML
    private TableColumn<Medication, Boolean> medRequiresPrescriptionColumn;

    @FXML
    private TableColumn<Medication, String> medActionColumn;

    @FXML
    private Label totalMedicationsLabel;

    @FXML
    private Label lowStockLabel;

    @FXML
    private Label outOfStockLabel;

    // Prescription Tab
    @FXML
    private ComboBox<String> prescriptionStatusCombo;

    @FXML
    private Button createPrescriptionButton;

    @FXML
    private TableView<Prescription> prescriptionTable;

    @FXML
    private TableColumn<Prescription, String> prescIdColumn;

    @FXML
    private TableColumn<Prescription, String> prescPatientColumn;

    @FXML
    private TableColumn<Prescription, String> prescDoctorColumn;

    @FXML
    private TableColumn<Prescription, String> prescDateColumn;

    @FXML
    private TableColumn<Prescription, Integer> prescItemsColumn;

    @FXML
    private TableColumn<Prescription, Double> prescTotalColumn;

    @FXML
    private TableColumn<Prescription, String> prescStatusColumn;

    @FXML
    private TableColumn<Prescription, String> prescActionColumn;

    @FXML
    private Label totalPrescriptionsLabel;

    @FXML
    private Label pendingPrescriptionsLabel;

    @FXML
    private Label fulfilledPrescriptionsLabel;

    private MedicationService medicationService;
    private PrescriptionService prescriptionService;
    private PatientService patientService;
    private DoctorService doctorService;

    private ObservableList<Medication> allMedications;
    private FilteredList<Medication> filteredMedications;

    private ObservableList<Prescription> allPrescriptions;
    private FilteredList<Prescription> filteredPrescriptions;

    private NumberFormat currencyFormatter;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        medicationService = MedicationService.getInstance();
        prescriptionService = PrescriptionService.getInstance();
        patientService = new PatientService();
        doctorService = DoctorService.getInstance();

        currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);

        // Initialize medication tab
        initializeMedicationTab();

        // Initialize prescription tab
        initializePrescriptionTab();
    }

    private void initializeMedicationTab() {
        // Set up medication table columns
        medIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        medNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        medCategoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        medDosageFormColumn.setCellValueFactory(new PropertyValueFactory<>("dosageForm"));

        medPriceColumn.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getUnitPrice()).asObject());
        medPriceColumn.setCellFactory(column -> new TableCell<Medication, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(currencyFormatter.format(item));
                }
            }
        });

        medStockColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getStockQuantity()).asObject());
        medStockColumn.setCellFactory(column -> new TableCell<Medication, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.valueOf(item));

                    // Color-code based on stock level
                    if (item <= 0) {
                        setStyle("-fx-text-fill: #e53935;"); // Red for out of stock
                    } else if (item < 10) {
                        setStyle("-fx-text-fill: #ff9800;"); // Orange for low stock
                    } else {
                        setStyle(null); // Default for normal stock
                    }
                }
            }
        });

        medManufacturerColumn.setCellValueFactory(new PropertyValueFactory<>("manufacturer"));
        medExpiryColumn.setCellValueFactory(new PropertyValueFactory<>("expiryDate"));

        medRequiresPrescriptionColumn.setCellValueFactory(data -> new SimpleBooleanProperty(data.getValue().isPrescription()).asObject());
        medRequiresPrescriptionColumn.setCellFactory(column -> new TableCell<Medication, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item ? "Yes" : "No");
                }
            }
        });

        // Set up action column
        medActionColumn.setCellFactory(createMedicationActionCellFactory());

        // Load medications
        loadMedications();

        // Set up category filter
        categoryFilterCombo.getItems().add("All Categories");
        Set<String> categories = new HashSet<>();
        for (Medication medication : allMedications) {
            categories.add(medication.getCategory());
        }
        categoryFilterCombo.getItems().addAll(categories);
        categoryFilterCombo.getSelectionModel().select(0);

        // Set up search functionality
        searchMedicationField.textProperty().addListener((obs, oldVal, newVal) -> handleMedicationSearch());
    }

    private void initializePrescriptionTab() {
        // Set up prescription status filter
        prescriptionStatusCombo.getItems().addAll("All", "Pending", "Fulfilled");
        prescriptionStatusCombo.getSelectionModel().select(0);

        // Set up prescription table columns
        prescIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        prescPatientColumn.setCellValueFactory(data -> {
            Patient patient = patientService.getPatientById(data.getValue().getPatientId());
            String patientName = patient != null ? patient.getName() : "Unknown";
            return new SimpleStringProperty(patientName);
        });

        prescDoctorColumn.setCellValueFactory(data -> {
            Doctor doctor = doctorService.getDoctorById(data.getValue().getDoctorId());
            String doctorName = doctor != null ? doctor.getFullName() : "Unknown";
            return new SimpleStringProperty(doctorName);
        });

        prescDateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));

        prescItemsColumn.setCellValueFactory(data ->
                new SimpleIntegerProperty(data.getValue().getMedications().size()).asObject());

        prescTotalColumn.setCellValueFactory(data ->
                new SimpleDoubleProperty(data.getValue().calculateTotalCost()).asObject());
        prescTotalColumn.setCellFactory(column -> new TableCell<Prescription, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(currencyFormatter.format(item));
                }
            }
        });

        prescStatusColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().isFulfilled() ? "Fulfilled" : "Pending"));
        prescStatusColumn.setCellFactory(column -> new TableCell<Prescription, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setStyle(item.equals("Fulfilled") ?
                            "-fx-text-fill: #43a047;" : "-fx-text-fill: #ff9800;");
                }
            }
        });

        // Set up action column
        prescActionColumn.setCellFactory(createPrescriptionActionCellFactory());

        // Load prescriptions
        loadPrescriptions();
    }

    private void loadMedications() {
        List<Medication> medications = medicationService.getAllMedications();
        allMedications = FXCollections.observableArrayList(medications);

        // Create filtered list
        filteredMedications = new FilteredList<>(allMedications, p -> true);
        medicationTable.setItems(filteredMedications);

        // Update counts
        updateMedicationCounts();
    }

    private void loadPrescriptions() {
        List<Prescription> prescriptions = prescriptionService.getAllPrescriptions();
        allPrescriptions = FXCollections.observableArrayList(prescriptions);

        // Create filtered list
        filteredPrescriptions = new FilteredList<>(allPrescriptions, p -> true);
        prescriptionTable.setItems(filteredPrescriptions);

        // Update counts
        updatePrescriptionCounts();
    }

    private void updateMedicationCounts() {
        int totalCount = allMedications.size();
        int lowStockCount = medicationService.getLowStockMedications().size();
        int outOfStockCount = medicationService.getOutOfStockMedications().size();

        totalMedicationsLabel.setText(String.valueOf(totalCount));
        lowStockLabel.setText(String.valueOf(lowStockCount));
        outOfStockLabel.setText(String.valueOf(outOfStockCount));
    }

    private void updatePrescriptionCounts() {
        int totalCount = allPrescriptions.size();
        int pendingCount = (int) allPrescriptions.stream()
                .filter(p -> !p.isFulfilled())
                .count();
        int fulfilledCount = totalCount - pendingCount;

        totalPrescriptionsLabel.setText(String.valueOf(totalCount));
        pendingPrescriptionsLabel.setText(String.valueOf(pendingCount));
        fulfilledPrescriptionsLabel.setText(String.valueOf(fulfilledCount));
    }

    private Callback<TableColumn<Medication, String>, TableCell<Medication, String>> createMedicationActionCellFactory() {
        return new Callback<>() {
            @Override
            public TableCell<Medication, String> call(TableColumn<Medication, String> param) {
                return new TableCell<>() {
                    private final Button viewBtn = new Button("View");
                    private final Button editBtn = new Button("Edit");
                    private final Button stockBtn = new Button("Update Stock");

                    {
                        viewBtn.setOnAction(event -> {
                            Medication medication = getTableRow().getItem();
                            if (medication != null) {
                                showMedicationDetails(medication);
                            }
                        });

                        editBtn.setOnAction(event -> {
                            Medication medication = getTableRow().getItem();
                            if (medication != null) {
                                handleEditMedication(medication);
                            }
                        });

                        stockBtn.setOnAction(event -> {
                            Medication medication = getTableRow().getItem();
                            if (medication != null) {
                                handleUpdateStock(medication);
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
                            buttons.getChildren().addAll(viewBtn, editBtn, stockBtn);
                            setGraphic(buttons);
                        }
                    }
                };
            }
        };
    }

    private Callback<TableColumn<Prescription, String>, TableCell<Prescription, String>> createPrescriptionActionCellFactory() {
        return new Callback<>() {
            @Override
            public TableCell<Prescription, String> call(TableColumn<Prescription, String> param) {
                return new TableCell<>() {
                    private final Button viewBtn = new Button("View");
                    private final Button fulfillBtn = new Button("Fulfill");

                    {
                        viewBtn.setOnAction(event -> {
                            Prescription prescription = getTableRow().getItem();
                            if (prescription != null) {
                                showPrescriptionDetails(prescription);
                            }
                        });

                        fulfillBtn.setOnAction(event -> {
                            Prescription prescription = getTableRow().getItem();
                            if (prescription != null) {
                                handleFulfillPrescription(prescription);
                            }
                        });
                    }

                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);

                        if (empty) {
                            setGraphic(null);
                        } else {
                            Prescription prescription = getTableRow().getItem();

                            HBox buttons = new HBox(5);
                            buttons.getChildren().add(viewBtn);

                            if (prescription != null && !prescription.isFulfilled()) {
                                buttons.getChildren().add(fulfillBtn);
                            }

                            setGraphic(buttons);
                        }
                    }
                };
            }
        };
    }

    @FXML
    public void handleMedicationSearch() {
        String searchText = searchMedicationField.getText().toLowerCase();
        String selectedCategory = categoryFilterCombo.getValue();

        Predicate<Medication> categoryFilter;
        if (selectedCategory.equals("All Categories")) {
            categoryFilter = m -> true;
        } else {
            categoryFilter = m -> m.getCategory().equals(selectedCategory);
        }

        Predicate<Medication> searchFilter;
        if (searchText.isEmpty()) {
            searchFilter = m -> true;
        } else {
            searchFilter = m ->
                    m.getName().toLowerCase().contains(searchText) ||
                            m.getDescription().toLowerCase().contains(searchText) ||
                            m.getManufacturer().toLowerCase().contains(searchText);
        }

        filteredMedications.setPredicate(categoryFilter.and(searchFilter));
    }

    @FXML
    public void handleCategoryFilter() {
        handleMedicationSearch(); // Re-filter with new category selection
    }

    @FXML
    public void handlePrescriptionStatusFilter() {
        String selectedStatus = prescriptionStatusCombo.getValue();

        Predicate<Prescription> statusFilter;
        if (selectedStatus.equals("All")) {
            statusFilter = p -> true;
        } else if (selectedStatus.equals("Pending")) {
            statusFilter = p -> !p.isFulfilled();
        } else { // Fulfilled
            statusFilter = Prescription::isFulfilled;
        }

        filteredPrescriptions.setPredicate(statusFilter);
    }

    @FXML
    public void handleAddMedication() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hospital/view/MedicationForm.fxml"));
            Parent medicationFormView = loader.load();

            MedicationFormController controller = loader.getController();
            controller.initData(null); // New medication

            Stage stage = new Stage();
            stage.setTitle("Add New Medication");
            stage.initModality(Modality.APPLICATION_MODAL);

            Scene scene = new Scene(medicationFormView);
            scene.getStylesheets().add(getClass().getResource("/com/hospital/css/darkTheme.css").toExternalForm());

            stage.setScene(scene);
            stage.showAndWait();

            // Refresh medication list
            loadMedications();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Error", "Could not load medication form: " + e.getMessage());
        }
    }

    private void handleEditMedication(Medication medication) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hospital/view/MedicationForm.fxml"));
            Parent medicationFormView = loader.load();

            MedicationFormController controller = loader.getController();
            controller.initData(medication); // Existing medication

            Stage stage = new Stage();
            stage.setTitle("Edit Medication");
            stage.initModality(Modality.APPLICATION_MODAL);

            Scene scene = new Scene(medicationFormView);
            scene.getStylesheets().add(getClass().getResource("/com/hospital/css/darkTheme.css").toExternalForm());

            stage.setScene(scene);
            stage.showAndWait();

            // Refresh medication list
            loadMedications();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Error", "Could not load medication form: " + e.getMessage());
        }
    }

    private void handleUpdateStock(Medication medication) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hospital/view/StockUpdateForm.fxml"));
            Parent stockUpdateView = loader.load();

            StockUpdateFormController controller = loader.getController();
            controller.initData(medication);

            Stage stage = new Stage();
            stage.setTitle("Update Stock");
            stage.initModality(Modality.APPLICATION_MODAL);

            Scene scene = new Scene(stockUpdateView);
            scene.getStylesheets().add(getClass().getResource("/com/hospital/css/darkTheme.css").toExternalForm());

            stage.setScene(scene);
            stage.showAndWait();

            // Refresh medication list
            loadMedications();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Error", "Could not load stock update form: " + e.getMessage());
        }
    }

    private void showMedicationDetails(Medication medication) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hospital/view/MedicationDetails.fxml"));
            Parent detailsView = loader.load();

            MedicationDetailsController controller = loader.getController();
            controller.initData(medication);

            Stage stage = new Stage();
            stage.setTitle("Medication Details");
            stage.initModality(Modality.APPLICATION_MODAL);

            Scene scene = new Scene(detailsView);
            scene.getStylesheets().add(getClass().getResource("/com/hospital/css/darkTheme.css").toExternalForm());

            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Error", "Could not load medication details: " + e.getMessage());
        }
    }

    @FXML
    public void handleCreatePrescription() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hospital/view/PrescriptionForm.fxml"));
            Parent prescriptionFormView = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Create Prescription");
            stage.initModality(Modality.APPLICATION_MODAL);

            Scene scene = new Scene(prescriptionFormView);
            scene.getStylesheets().add(getClass().getResource("/com/hospital/css/darkTheme.css").toExternalForm());

            stage.setScene(scene);
            stage.showAndWait();

            // Refresh prescription list
            loadPrescriptions();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Error", "Could not load prescription form: " + e.getMessage());
        }
    }

    private void showPrescriptionDetails(Prescription prescription) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hospital/view/PrescriptionDetails.fxml"));
            Parent detailsView = loader.load();

            PrescriptionDetailsController controller = loader.getController();
            controller.initData(prescription);

            Stage stage = new Stage();
            stage.setTitle("Prescription Details");
            stage.initModality(Modality.APPLICATION_MODAL);

            Scene scene = new Scene(detailsView);
            scene.getStylesheets().add(getClass().getResource("/com/hospital/css/darkTheme.css").toExternalForm());

            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Error", "Could not load prescription details: " + e.getMessage());
        }
    }

    private void handleFulfillPrescription(Prescription prescription) {
        boolean confirm = AlertUtil.showConfirmation("Fulfill Prescription",
                "Are you sure you want to fulfill this prescription? This will update medication stock levels.");

        if (confirm) {
            try {
                prescriptionService.fulfillPrescription(prescription.getId());
                loadPrescriptions();
                loadMedications(); // Also refresh medications as stock levels will change
                AlertUtil.showInformation("Success", "Prescription fulfilled successfully.");
            } catch (IOException e) {
                e.printStackTrace();
                AlertUtil.showError("Error", "Could not fulfill prescription: " + e.getMessage());
            }
        }
    }
}