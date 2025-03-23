package com.hospital.controller;

import com.hospital.model.Appointment;
import com.hospital.model.Doctor;
import com.hospital.model.Patient;
import com.hospital.service.AppointmentService;
import com.hospital.service.DoctorService;
import com.hospital.service.PatientService;
import com.hospital.util.AlertUtil;
import com.hospital.util.DateUtil;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Predicate;

public class AppointmentManagementController implements Initializable {

    @FXML
    private DatePicker datePicker;

    @FXML
    private ComboBox<String> statusFilterCombo;

    @FXML
    private Button scheduleButton;

    @FXML
    private TableView<Appointment> appointmentTable;

    @FXML
    private TableColumn<Appointment, String> idColumn;

    @FXML
    private TableColumn<Appointment, String> patientColumn;

    @FXML
    private TableColumn<Appointment, String> doctorColumn;

    @FXML
    private TableColumn<Appointment, String> dateColumn;

    @FXML
    private TableColumn<Appointment, String> timeColumn;

    @FXML
    private TableColumn<Appointment, String> durationColumn;

    @FXML
    private TableColumn<Appointment, String> purposeColumn;

    @FXML
    private TableColumn<Appointment, String> statusColumn;

    @FXML
    private TableColumn<Appointment, String> actionColumn;

    @FXML
    private GridPane detailsPane;

    @FXML
    private Label detailsIdLabel;

    @FXML
    private Label detailsStatusLabel;

    @FXML
    private Label detailsPatientLabel;

    @FXML
    private Label detailsDoctorLabel;

    @FXML
    private Label detailsDateLabel;

    @FXML
    private Label detailsTimeLabel;

    @FXML
    private Label detailsDurationLabel;

    @FXML
    private Label detailsLocationLabel;

    @FXML
    private Label detailsPurposeLabel;

    @FXML
    private TextArea detailsNotesArea;

    @FXML
    private Button editAppointmentButton;

    @FXML
    private Button cancelAppointmentButton;

    @FXML
    private Button completeAppointmentButton;

    @FXML
    private Label todayCountLabel;

    @FXML
    private Label scheduledCountLabel;

    @FXML
    private Label completedCountLabel;

    @FXML
    private Label cancelledCountLabel;

    private AppointmentService appointmentService;
    private PatientService patientService;
    private DoctorService doctorService;
    private ObservableList<Appointment> allAppointments;
    private FilteredList<Appointment> filteredAppointments;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        appointmentService = AppointmentService.getInstance();
        patientService = new PatientService();
        doctorService = DoctorService.getInstance();

        // Initialize datePicker to today
        datePicker.setValue(LocalDate.now());

        // Initialize the combo box for status filtering
        statusFilterCombo.getItems().addAll("All Statuses", "Scheduled", "Completed", "Cancelled", "No-Show");
        statusFilterCombo.getSelectionModel().selectFirst();

        // Initialize table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        patientColumn.setCellValueFactory(data -> {
            Patient patient = patientService.getPatientById(data.getValue().getPatientId());
            String patientName = patient != null ? patient.getName() : "Unknown";
            return new SimpleStringProperty(patientName);
        });

        doctorColumn.setCellValueFactory(data -> {
            Doctor doctor = doctorService.getDoctorById(data.getValue().getDoctorId());
            String doctorName = doctor != null ? doctor.getFullName() : "Unknown";
            return new SimpleStringProperty(doctorName);
        });

        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));
        durationColumn.setCellValueFactory(new PropertyValueFactory<>("duration"));
        purposeColumn.setCellValueFactory(new PropertyValueFactory<>("purpose"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Add action buttons to each row
        actionColumn.setCellFactory(createActionButtonCellFactory());

        // Load appointments for today
        loadAppointments();

        // Add selection listener to populate details
        appointmentTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                showAppointmentDetails(newSelection);
            }
        });
    }

    private void loadAppointments() {
        List<Appointment> appointments = appointmentService.getAllAppointments();
        allAppointments = FXCollections.observableArrayList(appointments);

        // Create filtered list
        filteredAppointments = new FilteredList<>(allAppointments, createDateFilter());
        appointmentTable.setItems(filteredAppointments);

        // Update counts
        updateAppointmentCounts();
    }

    private void updateAppointmentCounts() {
        int todayCount = appointmentService.getTodayAppointments().size();

        int scheduledCount = (int) allAppointments.stream()
                .filter(a -> a.getStatus().equals("Scheduled"))
                .count();

        int completedCount = (int) allAppointments.stream()
                .filter(a -> a.getStatus().equals("Completed"))
                .count();

        int cancelledCount = (int) allAppointments.stream()
                .filter(a -> a.getStatus().equals("Cancelled") || a.getStatus().equals("No-Show"))
                .count();

        todayCountLabel.setText(String.valueOf(todayCount));
        scheduledCountLabel.setText(String.valueOf(scheduledCount));
        completedCountLabel.setText(String.valueOf(completedCount));
        cancelledCountLabel.setText(String.valueOf(cancelledCount));
    }

    private Predicate<Appointment> createDateFilter() {
        LocalDate date = datePicker.getValue();
        String dateStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        String status = statusFilterCombo.getValue();

        Predicate<Appointment> dateFilter = a -> a.getDate().equals(dateStr);

        if (status.equals("All Statuses")) {
            return dateFilter;
        } else {
            return dateFilter.and(a -> a.getStatus().equals(status));
        }
    }

    private Callback<TableColumn<Appointment, String>, TableCell<Appointment, String>> createActionButtonCellFactory() {
        return new Callback<>() {
            @Override
            public TableCell<Appointment, String> call(TableColumn<Appointment, String> param) {
                return new TableCell<>() {
                    private final Button viewBtn = new Button("View");
                    private final Button editBtn = new Button("Edit");
                    private final Button cancelBtn = new Button("Cancel");

                    {
                        viewBtn.setOnAction(event -> {
                            Appointment appointment = getTableRow().getItem();
                            if (appointment != null) {
                                showAppointmentDetails(appointment);
                            }
                        });

                        editBtn.setOnAction(event -> {
                            Appointment appointment = getTableRow().getItem();
                            if (appointment != null) {
                                handleEditAppointment(appointment);
                            }
                        });

                        cancelBtn.setOnAction(event -> {
                            Appointment appointment = getTableRow().getItem();
                            if (appointment != null) {
                                handleCancelAppointment(appointment);
                            }
                        });
                    }

                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);

                        if (empty) {
                            setGraphic(null);
                        } else {
                            Appointment appointment = getTableRow().getItem();

                            HBox buttons = new HBox(5);
                            buttons.getChildren().add(viewBtn);

                            if (appointment != null && !appointment.getStatus().equals("Cancelled") &&
                                    !appointment.getStatus().equals("Completed")) {
                                buttons.getChildren().addAll(editBtn, cancelBtn);
                            }

                            setGraphic(buttons);
                        }
                    }
                };
            }
        };
    }

    private void showAppointmentDetails(Appointment appointment) {
        detailsIdLabel.setText(appointment.getId());
        detailsStatusLabel.setText(appointment.getStatus());

        Patient patient = patientService.getPatientById(appointment.getPatientId());
        detailsPatientLabel.setText(patient != null ? patient.getName() : "Unknown");

        Doctor doctor = doctorService.getDoctorById(appointment.getDoctorId());
        detailsDoctorLabel.setText(doctor != null ? doctor.getFullName() : "Unknown");

        detailsDateLabel.setText(appointment.getDate());
        detailsTimeLabel.setText(appointment.getTime());
        detailsDurationLabel.setText(appointment.getDuration());

        if (appointment.getLocation() != null) {
            detailsLocationLabel.setText(appointment.getLocation().getFullLocation());
        } else {
            detailsLocationLabel.setText("Not specified");
        }

        detailsPurposeLabel.setText(appointment.getPurpose());
        detailsNotesArea.setText(appointment.getNotes() != null ? appointment.getNotes() : "");

        // Enable/disable buttons based on status
        boolean isActiveAppointment = !appointment.getStatus().equals("Cancelled") &&
                !appointment.getStatus().equals("Completed") &&
                !appointment.getStatus().equals("No-Show");

        editAppointmentButton.setDisable(!isActiveAppointment);
        cancelAppointmentButton.setDisable(!isActiveAppointment);
        completeAppointmentButton.setDisable(!isActiveAppointment);
    }

    @FXML
    public void handleDateChange() {
        filteredAppointments.setPredicate(createDateFilter());
    }

    @FXML
    public void handleFilterChange() {
        filteredAppointments.setPredicate(createDateFilter());
    }

    @FXML
    public void handleScheduleAppointment() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hospital/view/NewAppointment.fxml"));
            Parent newAppointmentView = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Schedule Appointment");
            stage.initModality(Modality.APPLICATION_MODAL);

            Scene scene = new Scene(newAppointmentView);
            scene.getStylesheets().add(getClass().getResource("/com/hospital/css/darkTheme.css").toExternalForm());

            stage.setScene(scene);
            stage.showAndWait();

            // Refresh appointment list
            loadAppointments();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Error", "Could not load appointment scheduling form: " + e.getMessage());
        }
    }

    @FXML
    public void handleEditAppointment() {
        Appointment selectedAppointment = appointmentTable.getSelectionModel().getSelectedItem();
        if (selectedAppointment != null) {
            handleEditAppointment(selectedAppointment);
        } else {
            AlertUtil.showWarning("Selection Required", "Please select an appointment to edit.");
        }
    }

    private void handleEditAppointment(Appointment appointment) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hospital/view/EditAppointment.fxml"));
            Parent editAppointmentView = loader.load();

            EditAppointmentController controller = loader.getController();
            controller.initData(appointment);

            Stage stage = new Stage();
            stage.setTitle("Edit Appointment");
            stage.initModality(Modality.APPLICATION_MODAL);

            Scene scene = new Scene(editAppointmentView);
            scene.getStylesheets().add(getClass().getResource("/com/hospital/css/darkTheme.css").toExternalForm());

            stage.setScene(scene);
            stage.showAndWait();

            // Refresh appointment list
            loadAppointments();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Error", "Could not load edit appointment form: " + e.getMessage());
        }
    }

    @FXML
    public void handleCancelAppointment() {
        Appointment selectedAppointment = appointmentTable.getSelectionModel().getSelectedItem();
        if (selectedAppointment != null) {
            handleCancelAppointment(selectedAppointment);
        } else {
            AlertUtil.showWarning("Selection Required", "Please select an appointment to cancel.");
        }
    }

    private void handleCancelAppointment(Appointment appointment) {
        boolean confirm = AlertUtil.showConfirmation("Confirm Cancellation",
                "Are you sure you want to cancel this appointment?");

        if (confirm) {
            try {
                appointment.setStatus("Cancelled");
                appointmentService.updateAppointment(appointment);
                loadAppointments();

                // Update the appointment details display
                showAppointmentDetails(appointment);

                AlertUtil.showInformation("Success", "Appointment cancelled successfully.");
            } catch (IOException e) {
                e.printStackTrace();
                AlertUtil.showError("Error", "Could not cancel appointment: " + e.getMessage());
            }
        }
    }

    @FXML
    public void handleCompleteAppointment() {
        Appointment selectedAppointment = appointmentTable.getSelectionModel().getSelectedItem();
        if (selectedAppointment != null) {
            boolean confirm = AlertUtil.showConfirmation("Confirm Completion",
                    "Are you sure you want to mark this appointment as completed?");

            if (confirm) {
                try {
                    selectedAppointment.setStatus("Completed");
                    appointmentService.updateAppointment(selectedAppointment);
                    loadAppointments();

                    // Update the appointment details display
                    showAppointmentDetails(selectedAppointment);

                    AlertUtil.showInformation("Success", "Appointment marked as completed.");
                } catch (IOException e) {
                    e.printStackTrace();
                    AlertUtil.showError("Error", "Could not update appointment: " + e.getMessage());
                }
            }
        } else {
            AlertUtil.showWarning("Selection Required", "Please select an appointment to mark as completed.");
        }
    }
}