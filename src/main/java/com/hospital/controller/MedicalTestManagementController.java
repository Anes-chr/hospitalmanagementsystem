package com.hospital.controller;

import com.hospital.model.*;
import com.hospital.service.DoctorService;
import com.hospital.service.MedicalTestService;
import com.hospital.service.PatientService;
import com.hospital.util.AlertUtil;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
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

public class MedicalTestManagementController implements Initializable {

    // Test Catalog Tab
    @FXML
    private TextField searchTestField;

    @FXML
    private ComboBox<String> testCategoryCombo;

    @FXML
    private Button addTestButton;

    @FXML
    private TableView<MedicalTest> testTable;

    @FXML
    private TableColumn<MedicalTest, String> testIdColumn;

    @FXML
    private TableColumn<MedicalTest, String> testNameColumn;

    @FXML
    private TableColumn<MedicalTest, String> testCategoryColumn;

    @FXML
    private TableColumn<MedicalTest, String> testDurationColumn;

    @FXML
    private TableColumn<MedicalTest, Double> testCostColumn;

    @FXML
    private TableColumn<MedicalTest, Boolean> testFastingColumn;

    @FXML
    private TableColumn<MedicalTest, String> testActionColumn;

    // Test Results Tab
    @FXML
    private ComboBox<String> resultStatusCombo;

    @FXML
    private Button requestTestButton;

    @FXML
    private Button addResultButton;

    @FXML
    private TableView<TestResult> resultTable;

    @FXML
    private TableColumn<TestResult, String> resultIdColumn;

    @FXML
    private TableColumn<TestResult, String> resultPatientColumn;

    @FXML
    private TableColumn<TestResult, String> resultTestColumn;

    @FXML
    private TableColumn<TestResult, String> resultDoctorColumn;

    @FXML
    private TableColumn<TestResult, String> resultDateColumn;

    @FXML
    private TableColumn<TestResult, String> resultStatusColumn;

    @FXML
    private TableColumn<TestResult, String> resultActionColumn;

    // X-Ray Tab
    @FXML
    private ComboBox<String> xrayStatusCombo;

    @FXML
    private Button requestXrayButton;

    @FXML
    private Button updateXrayButton;

    @FXML
    private TableView<XRayExamination> xrayTable;

    @FXML
    private TableColumn<XRayExamination, String> xrayIdColumn;

    @FXML
    private TableColumn<XRayExamination, String> xrayPatientColumn;

    @FXML
    private TableColumn<XRayExamination, String> xrayDoctorColumn;

    @FXML
    private TableColumn<XRayExamination, String> xrayDateColumn;

    @FXML
    private TableColumn<XRayExamination, String> xrayBodyPartColumn;

    @FXML
    private TableColumn<XRayExamination, String> xrayStatusColumn;

    @FXML
    private TableColumn<XRayExamination, String> xrayActionColumn;

    private MedicalTestService medicalTestService;
    private PatientService patientService;
    private DoctorService doctorService;

    private ObservableList<MedicalTest> allTests;
    private FilteredList<MedicalTest> filteredTests;

    private ObservableList<TestResult> allResults;
    private FilteredList<TestResult> filteredResults;

    private ObservableList<XRayExamination> allXrays;
    private FilteredList<XRayExamination> filteredXrays;

    private NumberFormat currencyFormatter;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        medicalTestService = MedicalTestService.getInstance();
        patientService = new PatientService();
        doctorService = DoctorService.getInstance();

        currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);

        // Initialize test catalog tab
        initializeTestCatalogTab();

        // Initialize test results tab
        initializeTestResultsTab();

        // Initialize X-Ray examinations tab
        initializeXRayTab();
    }

    private void initializeTestCatalogTab() {
        // Set up test table columns
        testIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        testNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        testCategoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        testDurationColumn.setCellValueFactory(new PropertyValueFactory<>("duration"));

        testCostColumn.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getCost()).asObject());
        testCostColumn.setCellFactory(column -> new TableCell<MedicalTest, Double>() {
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

        testFastingColumn.setCellValueFactory(data -> new SimpleBooleanProperty(data.getValue().isFasting()).asObject());
        testFastingColumn.setCellFactory(column -> new TableCell<MedicalTest, Boolean>() {
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
        testActionColumn.setCellFactory(createTestActionCellFactory());

        // Load tests
        loadTests();

        // Set up category filter
        testCategoryCombo.getItems().add("All Categories");
        Set<String> categories = new HashSet<>();
        for (MedicalTest test : allTests) {
            categories.add(test.getCategory());
        }
        testCategoryCombo.getItems().addAll(categories);
        testCategoryCombo.getSelectionModel().select(0);

        // Set up search functionality
        searchTestField.textProperty().addListener((obs, oldVal, newVal) -> handleTestSearch());
    }

    private void initializeTestResultsTab() {
        // Set up result status filter
        resultStatusCombo.getItems().addAll("All", "Pending", "Completed", "Abnormal");
        resultStatusCombo.getSelectionModel().select(0);

        // Set up result table columns
        resultIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        resultPatientColumn.setCellValueFactory(data -> {
            Patient patient = patientService.getPatientById(data.getValue().getPatientId());
            String patientName = patient != null ? patient.getName() : "Unknown";
            return new SimpleStringProperty(patientName);
        });

        resultTestColumn.setCellValueFactory(data -> {
            MedicalTest test = medicalTestService.getMedicalTestById(data.getValue().getTestId());
            String testName = test != null ? test.getName() : "Unknown";
            return new SimpleStringProperty(testName);
        });

        resultDoctorColumn.setCellValueFactory(data -> {
            Doctor doctor = doctorService.getDoctorById(data.getValue().getDoctorId());
            String doctorName = doctor != null ? doctor.getFullName() : "Unknown";
            return new SimpleStringProperty(doctorName);
        });

        resultDateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        resultStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Set up action column
        resultActionColumn.setCellFactory(createResultActionCellFactory());

        // Load results
        loadTestResults();
    }

    private void initializeXRayTab() {
        // Set up X-Ray status filter
        xrayStatusCombo.getItems().addAll("All", "Requested", "Scheduled", "Completed");
        xrayStatusCombo.getSelectionModel().select(0);

        // Set up X-Ray table columns
        xrayIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        xrayPatientColumn.setCellValueFactory(data -> {
            Patient patient = patientService.getPatientById(data.getValue().getPatientId());
            String patientName = patient != null ? patient.getName() : "Unknown";
            return new SimpleStringProperty(patientName);
        });

        xrayDoctorColumn.setCellValueFactory(data -> {
            Doctor doctor = doctorService.getDoctorById(data.getValue().getRequestedByDoctorId());
            String doctorName = doctor != null ? doctor.getFullName() : "Unknown";
            return new SimpleStringProperty(doctorName);
        });

        xrayDateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        xrayBodyPartColumn.setCellValueFactory(new PropertyValueFactory<>("bodyPart"));
        xrayStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Set up action column
        xrayActionColumn.setCellFactory(createXRayActionCellFactory());

        // Load X-Rays
        loadXRayExaminations();
    }

    private void loadTests() {
        List<MedicalTest> tests = medicalTestService.getAllMedicalTests();
        allTests = FXCollections.observableArrayList(tests);

        // Create filtered list
        filteredTests = new FilteredList<>(allTests, p -> true);
        testTable.setItems(filteredTests);
    }

    private void loadTestResults() {
        List<TestResult> results = medicalTestService.getAllTestResults();
        allResults = FXCollections.observableArrayList(results);

        // Create filtered list
        filteredResults = new FilteredList<>(allResults, p -> true);
        resultTable.setItems(filteredResults);
    }

    private void loadXRayExaminations() {
        List<XRayExamination> xrays = medicalTestService.getAllXRayExaminations();
        allXrays = FXCollections.observableArrayList(xrays);

        // Create filtered list
        filteredXrays = new FilteredList<>(allXrays, p -> true);
        xrayTable.setItems(filteredXrays);
    }

    private Callback<TableColumn<MedicalTest, String>, TableCell<MedicalTest, String>> createTestActionCellFactory() {
        return new Callback<>() {
            @Override
            public TableCell<MedicalTest, String> call(TableColumn<MedicalTest, String> param) {
                return new TableCell<>() {
                    private final Button viewBtn = new Button("View");
                    private final Button editBtn = new Button("Edit");
                    private final Button requestBtn = new Button("Request");

                    {
                        viewBtn.setOnAction(event -> {
                            MedicalTest test = getTableRow().getItem();
                            if (test != null) {
                                showTestDetails(test);
                            }
                        });

                        editBtn.setOnAction(event -> {
                            MedicalTest test = getTableRow().getItem();
                            if (test != null) {
                                handleEditTest(test);
                            }
                        });

                        requestBtn.setOnAction(event -> {
                            MedicalTest test = getTableRow().getItem();
                            if (test != null) {
                                handleRequestTest(test);
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
                            buttons.getChildren().addAll(viewBtn, editBtn, requestBtn);
                            setGraphic(buttons);
                        }
                    }
                };
            }
        };
    }

    private Callback<TableColumn<TestResult, String>, TableCell<TestResult, String>> createResultActionCellFactory() {
        return new Callback<>() {
            @Override
            public TableCell<TestResult, String> call(TableColumn<TestResult, String> param) {
                return new TableCell<>() {
                    private final Button viewBtn = new Button("View");
                    private final Button updateBtn = new Button("Update");

                    {
                        viewBtn.setOnAction(event -> {
                            TestResult result = getTableRow().getItem();
                            if (result != null) {
                                showTestResultDetails(result);
                            }
                        });

                        updateBtn.setOnAction(event -> {
                            TestResult result = getTableRow().getItem();
                            if (result != null) {
                                handleUpdateResult(result);
                            }
                        });
                    }

                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);

                        if (empty) {
                            setGraphic(null);
                        } else {
                            TestResult result = getTableRow().getItem();

                            HBox buttons = new HBox(5);
                            buttons.getChildren().add(viewBtn);

                            if (result != null && result.getStatus().equals("Pending")) {
                                buttons.getChildren().add(updateBtn);
                            }

                            setGraphic(buttons);
                        }
                    }
                };
            }
        };
    }

    private Callback<TableColumn<XRayExamination, String>, TableCell<XRayExamination, String>> createXRayActionCellFactory() {
        return new Callback<>() {
            @Override
            public TableCell<XRayExamination, String> call(TableColumn<XRayExamination, String> param) {
                return new TableCell<>() {
                    private final Button viewBtn = new Button("View");
                    private final Button updateBtn = new Button("Update");

                    {
                        viewBtn.setOnAction(event -> {
                            XRayExamination xray = getTableRow().getItem();
                            if (xray != null) {
                                showXRayDetails(xray);
                            }
                        });

                        updateBtn.setOnAction(event -> {
                            XRayExamination xray = getTableRow().getItem();
                            if (xray != null) {
                                handleUpdateXRay(xray);
                            }
                        });
                    }

                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);

                        if (empty) {
                            setGraphic(null);
                        } else {
                            XRayExamination xray = getTableRow().getItem();

                            HBox buttons = new HBox(5);
                            buttons.getChildren().add(viewBtn);

                            if (xray != null && (xray.getStatus().equals("Requested") || xray.getStatus().equals("Scheduled"))) {
                                buttons.getChildren().add(updateBtn);
                            }

                            setGraphic(buttons);
                        }
                    }
                };
            }
        };
    }

    @FXML
    public void handleTestSearch() {
        String searchText = searchTestField.getText().toLowerCase();
        String selectedCategory = testCategoryCombo.getValue();

        Predicate<MedicalTest> categoryFilter;
        if (selectedCategory.equals("All Categories")) {
            categoryFilter = t -> true;
        } else {
            categoryFilter = t -> t.getCategory().equals(selectedCategory);
        }

        Predicate<MedicalTest> searchFilter;
        if (searchText.isEmpty()) {
            searchFilter = t -> true;
        } else {
            searchFilter = t ->
                    t.getName().toLowerCase().contains(searchText) ||
                            t.getDescription().toLowerCase().contains(searchText);
        }

        filteredTests.setPredicate(categoryFilter.and(searchFilter));
    }

    @FXML
    public void handleTestCategoryFilter() {
        handleTestSearch(); // Re-filter with new category selection
    }

    @FXML
    public void handleResultStatusFilter() {
        String selectedStatus = resultStatusCombo.getValue();

        if (selectedStatus.equals("All")) {
            filteredResults.setPredicate(r -> true);
        } else {
            filteredResults.setPredicate(r -> r.getStatus().equals(selectedStatus));
        }
    }

    @FXML
    public void handleXrayStatusFilter() {
        String selectedStatus = xrayStatusCombo.getValue();

        if (selectedStatus.equals("All")) {
            filteredXrays.setPredicate(x -> true);
        } else {
            filteredXrays.setPredicate(x -> x.getStatus().equals(selectedStatus));
        }
    }

    @FXML
    public void handleAddTest() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hospital/view/MedicalTestForm.fxml"));
            Parent testFormView = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Add Medical Test");
            stage.initModality(Modality.APPLICATION_MODAL);

            Scene scene = new Scene(testFormView);
            scene.getStylesheets().add(getClass().getResource("/com/hospital/css/darkTheme.css").toExternalForm());

            stage.setScene(scene);
            stage.showAndWait();

            // Refresh test list
            loadTests();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Error", "Could not load medical test form: " + e.getMessage());
        }
    }

    private void handleEditTest(MedicalTest test) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hospital/view/MedicalTestForm.fxml"));
            Parent testFormView = loader.load();

            MedicalTestFormController controller = loader.getController();
            controller.initData(test);

            Stage stage = new Stage();
            stage.setTitle("Edit Medical Test");
            stage.initModality(Modality.APPLICATION_MODAL);

            Scene scene = new Scene(testFormView);
            scene.getStylesheets().add(getClass().getResource("/com/hospital/css/darkTheme.css").toExternalForm());

            stage.setScene(scene);
            stage.showAndWait();

            // Refresh test list
            loadTests();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Error", "Could not load medical test form: " + e.getMessage());
        }
    }

    private void showTestDetails(MedicalTest test) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hospital/view/MedicalTestDetails.fxml"));
            Parent detailsView = loader.load();

            MedicalTestDetailsController controller = loader.getController();
            controller.initData(test);

            Stage stage = new Stage();
            stage.setTitle("Medical Test Details");
            stage.initModality(Modality.APPLICATION_MODAL);

            Scene scene = new Scene(detailsView);
            scene.getStylesheets().add(getClass().getResource("/com/hospital/css/darkTheme.css").toExternalForm());

            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Error", "Could not load test details: " + e.getMessage());
        }
    }

    @FXML
    public void handleRequestTest() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hospital/view/TestRequestForm.fxml"));
            Parent requestFormView = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Request Medical Test");
            stage.initModality(Modality.APPLICATION_MODAL);

            Scene scene = new Scene(requestFormView);
            scene.getStylesheets().add(getClass().getResource("/com/hospital/css/darkTheme.css").toExternalForm());

            stage.setScene(scene);
            stage.showAndWait();

            // Refresh results list
            loadTestResults();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Error", "Could not load test request form: " + e.getMessage());
        }
    }

    private void handleRequestTest(MedicalTest test) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hospital/view/TestRequestForm.fxml"));
            Parent requestFormView = loader.load();

            TestRequestFormController controller = loader.getController();
            controller.initData(test);

            Stage stage = new Stage();
            stage.setTitle("Request Medical Test");
            stage.initModality(Modality.APPLICATION_MODAL);

            Scene scene = new Scene(requestFormView);
            scene.getStylesheets().add(getClass().getResource("/com/hospital/css/darkTheme.css").toExternalForm());

            stage.setScene(scene);
            stage.showAndWait();

            // Refresh results list
            loadTestResults();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Error", "Could not load test request form: " + e.getMessage());
        }
    }

    @FXML
    public void handleAddResult() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hospital/view/TestResultForm.fxml"));
            Parent resultFormView = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Add Test Result");
            stage.initModality(Modality.APPLICATION_MODAL);

            Scene scene = new Scene(resultFormView);
            scene.getStylesheets().add(getClass().getResource("/com/hospital/css/darkTheme.css").toExternalForm());

            stage.setScene(scene);
            stage.showAndWait();

            // Refresh results list
            loadTestResults();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Error", "Could not load test result form: " + e.getMessage());
        }
    }

    private void showTestResultDetails(TestResult result) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hospital/view/TestResultDetails.fxml"));
            Parent detailsView = loader.load();

            TestResultDetailsController controller = loader.getController();
            controller.initData(result);

            Stage stage = new Stage();
            stage.setTitle("Test Result Details");
            stage.initModality(Modality.APPLICATION_MODAL);

            Scene scene = new Scene(detailsView);
            scene.getStylesheets().add(getClass().getResource("/com/hospital/css/darkTheme.css").toExternalForm());

            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Error", "Could not load result details: " + e.getMessage());
        }
    }

    private void handleUpdateResult(TestResult result) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hospital/view/TestResultForm.fxml"));
            Parent resultFormView = loader.load();

            TestResultFormController controller = loader.getController();
            controller.initData(result);

            Stage stage = new Stage();
            stage.setTitle("Update Test Result");
            stage.initModality(Modality.APPLICATION_MODAL);

            Scene scene = new Scene(resultFormView);
            scene.getStylesheets().add(getClass().getResource("/com/hospital/css/darkTheme.css").toExternalForm());

            stage.setScene(scene);
            stage.showAndWait();

            // Refresh results list
            loadTestResults();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Error", "Could not load test result form: " + e.getMessage());
        }
    }

    @FXML
    public void handleRequestXray() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hospital/view/XRayRequestForm.fxml"));
            Parent requestFormView = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Request X-Ray Examination");
            stage.initModality(Modality.APPLICATION_MODAL);

            Scene scene = new Scene(requestFormView);
            scene.getStylesheets().add(getClass().getResource("/com/hospital/css/darkTheme.css").toExternalForm());

            stage.setScene(scene);
            stage.showAndWait();

            // Refresh x-ray list
            loadXRayExaminations();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Error", "Could not load x-ray request form: " + e.getMessage());
        }
    }

    @FXML
    public void handleUpdateXrayResult() {
        XRayExamination selectedXRay = xrayTable.getSelectionModel().getSelectedItem();
        if (selectedXRay != null) {
            handleUpdateXRay(selectedXRay);
        } else {
            AlertUtil.showWarning("Selection Required", "Please select an X-Ray examination to update.");
        }
    }

    private void showXRayDetails(XRayExamination xray) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hospital/view/XRayDetails.fxml"));
            Parent detailsView = loader.load();

            XRayDetailsController controller = loader.getController();
            controller.initData(xray);

            Stage stage = new Stage();
            stage.setTitle("X-Ray Examination Details");
            stage.initModality(Modality.APPLICATION_MODAL);

            Scene scene = new Scene(detailsView);
            scene.getStylesheets().add(getClass().getResource("/com/hospital/css/darkTheme.css").toExternalForm());

            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Error", "Could not load x-ray details: " + e.getMessage());
        }
    }

    private void handleUpdateXRay(XRayExamination xray) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hospital/view/XRayUpdateForm.fxml"));
            Parent updateFormView = loader.load();

            XRayUpdateFormController controller = loader.getController();
            controller.initData(xray);

            Stage stage = new Stage();
            stage.setTitle("Update X-Ray Examination");
            stage.initModality(Modality.APPLICATION_MODAL);

            Scene scene = new Scene(updateFormView);
            scene.getStylesheets().add(getClass().getResource("/com/hospital/css/darkTheme.css").toExternalForm());

            stage.setScene(scene);
            stage.showAndWait();

            // Refresh x-ray list
            loadXRayExaminations();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Error", "Could not load x-ray update form: " + e.getMessage());
        }
    }
}