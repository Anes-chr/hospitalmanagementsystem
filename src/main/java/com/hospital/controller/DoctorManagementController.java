package com.hospital.controller;

import com.hospital.model.Appointment;
import com.hospital.model.Doctor;
import com.hospital.service.AppointmentService;
import com.hospital.service.DoctorService;
import com.hospital.util.AlertUtil;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.function.Predicate;

public class DoctorManagementController implements Initializable {

    @FXML
    private TextField searchField;

    @FXML
    private Button searchButton;

    @FXML
    private ComboBox<String> specialtyFilterCombo;

    @FXML
    private Button addDoctorButton;

    @FXML
    private TableView<Doctor> doctorTable;

    @FXML
    private TableColumn<Doctor, String> idColumn;

    @FXML
    private TableColumn<Doctor, String> nameColumn;

    @FXML
    private TableColumn<Doctor, String> specializationColumn;

    @FXML
    private TableColumn<Doctor, String> contactColumn;

    @FXML
    private TableColumn<Doctor, String> locationColumn;

    @FXML
    private TableColumn<Doctor, Boolean> availabilityColumn;

    @FXML
    private TableColumn<Doctor, String> workingHoursColumn;

    @FXML
    private TableColumn<Doctor, String> actionColumn;

    @FXML
    private GridPane detailsPane;

    @FXML
    private Label detailsIdLabel;

    @FXML
    private Label detailsLicenseLabel;

    @FXML
    private Label detailsNameLabel;

    @FXML
    private Label detailsSpecializationLabel;

    @FXML
    private Label detailsEmailLabel;

    @FXML
    private Label detailsContactLabel;

    @FXML
    private Label detailsLocationLabel;

    @FXML
    private Label detailsWorkingHoursLabel;

    @FXML
    private Label detailsAvailabilityLabel;

    @FXML
    private Label detailsLastLoginLabel;

    @FXML
    private Button editDoctorButton;

    @FXML
    private Button deleteDoctorButton;

    @FXML
    private Button viewAppointmentsButton;

    @FXML
    private Label totalCountLabel;

    @FXML
    private Label availableCountLabel;

    @FXML
    private Label bookedTodayCountLabel;

    private DoctorService doctorService;
    private AppointmentService appointmentService;
    private ObservableList<Doctor> allDoctors;
    private FilteredList<Doctor> filteredDoctors;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        doctorService = DoctorService.getInstance();
        appointmentService = AppointmentService.getInstance();

        // Initialize the combo box for filtering
        specialtyFilterCombo.getItems().add("All Specialties");

        // Initialize table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        specializationColumn.setCellValueFactory(new PropertyValueFactory<>("specialization"));
        contactColumn.setCellValueFactory(new PropertyValueFactory<>("contactNumber"));

        locationColumn.setCellValueFactory(data -> {
            if (data.getValue().getAssignedBlock() != null) {
                return new SimpleStringProperty(data.getValue().getAssignedBlock().getBlockName());
            } else {
                return new SimpleStringProperty("N/A");
            }
        });

        availabilityColumn.setCellValueFactory(new PropertyValueFactory<>("available"));
        availabilityColumn.setCellFactory(column -> new TableCell<Doctor, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item ? "Yes" : "No");
                    setStyle(item ? "-fx-text-fill: #43a047;" : "-fx-text-fill: #e53935;");
                }
            }
        });

        workingHoursColumn.setCellValueFactory(new PropertyValueFactory<>("workingHours"));

        // Add action buttons to each row
        actionColumn.setCellFactory(createActionButtonCellFactory());

        // Load doctors
        loadDoctors();

        // Add selection listener to populate details
        doctorTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                showDoctorDetails(newSelection);
            }
        });

        // Set up search functionality
        searchField.textProperty().addListener((obs, oldVal, newVal) -> handleSearch());
    }

    private void loadDoctors() {
        List<Doctor> doctors = doctorService.getAllDoctors();
        allDoctors = FXCollections.observableArrayList(doctors);

        // Populate specialty filter
        Set<String> specialties = new HashSet<>();
        for (Doctor doctor : doctors) {
            specialties.add(doctor.getSpecialization());
        }
        specialtyFilterCombo.getItems().addAll(specialties);
        specialtyFilterCombo.getSelectionModel().selectFirst();

        // Create filtered list
        filteredDoctors = new FilteredList<>(allDoctors, p -> true);
        doctorTable.setItems(filteredDoctors);

        // Update counts
        updateDoctorCounts();
    }

    private void updateDoctorCounts() {
        int totalCount = allDoctors.size();
        int availableCount = (int) allDoctors.stream()
                .filter(Doctor::isAvailable)
                .count();

        List<Appointment> todayAppointments = appointmentService.getTodayAppointments();
        Set<String> bookedDoctors = new HashSet<>();
        for (Appointment appointment : todayAppointments) {
            bookedDoctors.add(appointment.getDoctorId());
        }
        int bookedCount = bookedDoctors.size();

        totalCountLabel.setText(String.valueOf(totalCount));
        availableCountLabel.setText(String.valueOf(availableCount));
        bookedTodayCountLabel.setText(String.valueOf(bookedCount));
    }

    private Callback<TableColumn<Doctor, String>, TableCell<Doctor, String>> createActionButtonCellFactory() {
        return new Callback<>() {
            @Override
            public TableCell<Doctor, String> call(TableColumn<Doctor, String> param) {
                return new TableCell<>() {
                    private final Button viewBtn = new Button("View");
                    private final Button editBtn = new Button("Edit");
                    private final Button deleteBtn = new Button("Delete");

                    {
                        viewBtn.setOnAction(event -> {
                            Doctor doctor = getTableRow().getItem();
                            if (doctor != null) {
                                showDoctorDetails(doctor);
                            }
                        });

                        editBtn.setOnAction(event -> {
                            Doctor doctor = getTableRow().getItem();
                            if (doctor != null) {
                                handleEditDoctor(doctor);
                            }
                        });

                        deleteBtn.setOnAction(event -> {
                            Doctor doctor = getTableRow().getItem();
                            if (doctor != null) {
                                handleDeleteDoctor(doctor);
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

    private void showDoctorDetails(Doctor doctor) {
        detailsIdLabel.setText(doctor.getId());
        detailsNameLabel.setText(doctor.getFullName());
        detailsLicenseLabel.setText(doctor.getLicenseNumber());
        detailsSpecializationLabel.setText(doctor.getSpecialization());
        detailsEmailLabel.setText(doctor.getEmail());
        detailsContactLabel.setText(doctor.getContactNumber());

        if (doctor.getAssignedBlock() != null) {
            detailsLocationLabel.setText(doctor.getAssignedBlock().getFullLocation());
        } else {
            detailsLocationLabel.setText("N/A");
        }

        detailsWorkingHoursLabel.setText(doctor.getWorkingHours());
        detailsAvailabilityLabel.setText(doctor.isAvailable() ? "Available" : "Not Available");
        detailsLastLoginLabel.setText(doctor.getLastLogin() != null ? doctor.getLastLogin() : "Never");
    }

    @FXML
    public void handleSearch() {
        String searchText = searchField.getText().toLowerCase();
        String selectedSpecialty = specialtyFilterCombo.getValue();

        Predicate<Doctor> specialtyFilter;
        if (selectedSpecialty.equals("All Specialties")) {
            specialtyFilter = d -> true;
        } else {
            specialtyFilter = d -> d.getSpecialization().equals(selectedSpecialty);
        }

        Predicate<Doctor> searchFilter;
        if (searchText.isEmpty()) {
            searchFilter = d -> true;
        } else {
            searchFilter = d ->
                    d.getFullName().toLowerCase().contains(searchText) ||
                            d.getContactNumber().toLowerCase().contains(searchText) ||
                            d.getEmail().toLowerCase().contains(searchText);
        }

        filteredDoctors.setPredicate(specialtyFilter.and(searchFilter));
    }

    @FXML
    public void handleFilterChange() {
        handleSearch(); // Re-filter with new specialty selection
    }

    @FXML
    public void handleAddDoctor() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hospital/view/NewDoctor.fxml"));
            Parent newDoctorView = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Add New Doctor");
            stage.initModality(Modality.APPLICATION_MODAL);

            Scene scene = new Scene(newDoctorView);
            scene.getStylesheets().add(getClass().getResource("/com/hospital/css/darkTheme.css").toExternalForm());

            stage.setScene(scene);
            stage.showAndWait();

            // Refresh doctor list
            loadDoctors();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Error", "Could not load new doctor form: " + e.getMessage());
        }
    }

    @FXML
    public void handleEditDoctor() {
        Doctor selectedDoctor = doctorTable.getSelectionModel().getSelectedItem();
        if (selectedDoctor != null) {
            handleEditDoctor(selectedDoctor);
        } else {
            AlertUtil.showWarning("Selection Required", "Please select a doctor to edit.");
        }
    }

    private void handleEditDoctor(Doctor doctor) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hospital/view/EditDoctor.fxml"));
            Parent editDoctorView = loader.load();

            EditDoctorController controller = loader.getController();
            controller.initData(doctor);

            Stage stage = new Stage();
            stage.setTitle("Edit Doctor");
            stage.initModality(Modality.APPLICATION_MODAL);

            Scene scene = new Scene(editDoctorView);
            scene.getStylesheets().add(getClass().getResource("/com/hospital/css/darkTheme.css").toExternalForm());

            stage.setScene(scene);
            stage.showAndWait();

            // Refresh doctor list
            loadDoctors();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Error", "Could not load edit doctor form: " + e.getMessage());
        }
    }

    @FXML
    public void handleDeleteDoctor() {
        Doctor selectedDoctor = doctorTable.getSelectionModel().getSelectedItem();
        if (selectedDoctor != null) {
            handleDeleteDoctor(selectedDoctor);
        } else {
            AlertUtil.showWarning("Selection Required", "Please select a doctor to delete.");
        }
    }

    private void handleDeleteDoctor(Doctor doctor) {
        boolean confirm = AlertUtil.showConfirmation("Confirm Deletion",
                "Are you sure you want to delete " + doctor.getFullName() + "?");

        if (confirm) {
            try {
                doctorService.deleteDoctor(doctor.getId());
                loadDoctors();
                AlertUtil.showInformation("Success", "Doctor deleted successfully.");
            } catch (IOException e) {
                e.printStackTrace();
                AlertUtil.showError("Error", "Could not delete doctor: " + e.getMessage());
            }
        }
    }

    @FXML
    public void handleViewAppointments() {
        Doctor selectedDoctor = doctorTable.getSelectionModel().getSelectedItem();
        if (selectedDoctor != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hospital/view/DoctorAppointments.fxml"));
                Parent appointmentsView = loader.load();

                DoctorAppointmentsController controller = loader.getController();
                controller.initData(selectedDoctor);

                Stage stage = new Stage();
                stage.setTitle("Appointments for " + selectedDoctor.getFullName());
                stage.initModality(Modality.APPLICATION_MODAL);

                Scene scene = new Scene(appointmentsView);
                scene.getStylesheets().add(getClass().getResource("/com/hospital/css/darkTheme.css").toExternalForm());

                stage.setScene(scene);
                stage.show();

            } catch (IOException e) {
                e.printStackTrace();
                AlertUtil.showError("Error", "Could not load doctor appointments: " + e.getMessage());
            }
        } else {
            AlertUtil.showWarning("Selection Required", "Please select a doctor to view appointments.");
        }
    }
}