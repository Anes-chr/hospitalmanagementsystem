package com.hospital.controller;

import com.hospital.model.Medication;
import com.hospital.service.ReportService;
import com.hospital.util.AlertUtil;
import com.hospital.util.DateUtil;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.List; // Add this import
import java.util.Map;
import java.util.ResourceBundle;

public class ReportManagementController implements Initializable {

    @FXML
    private DatePicker fromDatePicker;

    @FXML
    private DatePicker toDatePicker;

    @FXML
    private ComboBox<String> reportTypeComboBox;

    @FXML
    private Button generateReportButton;

    @FXML
    private Button exportReportButton;

    @FXML
    private TabPane chartTabPane;

    @FXML
    private PieChart patientTypeChart;

    @FXML
    private PieChart patientGenderChart;

    @FXML
    private BarChart<String, Number> patientAgeChart;

    @FXML
    private BarChart<String, Number> revenueByTypeChart;

    @FXML
    private LineChart<String, Number> revenueByMonthChart;

    @FXML
    private PieChart medicationCategoryChart;

    @FXML
    private TableView<Medication> lowStockTable;

    @FXML
    private TableColumn<Medication, String> lowStockNameColumn;

    @FXML
    private TableColumn<Medication, String> lowStockCategoryColumn;

    @FXML
    private TableColumn<Medication, Integer> lowStockQuantityColumn;

    @FXML
    private LineChart<String, Number> appointmentByDayChart;

    @FXML
    private PieChart appointmentStatusChart;

    @FXML
    private BarChart<String, Number> appointmentByDoctorChart;

    @FXML
    private TextArea reportTextArea;

    private ReportService reportService;
    private String currentReport;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        reportService = ReportService.getInstance();

        // Initialize date pickers
        LocalDate today = LocalDate.now();
        LocalDate monthAgo = today.minusMonths(1);

        fromDatePicker.setValue(monthAgo);
        toDatePicker.setValue(today);

        // Initialize report type combo box
        reportTypeComboBox.getItems().addAll(
                "Patient Statistics",
                "Financial Statistics",
                "Pharmacy Statistics",
                "Appointment Statistics",
                "Doctor Statistics",
                "Comprehensive Report"
        );
        reportTypeComboBox.getSelectionModel().select("Comprehensive Report");

        // Initialize low stock table columns
        lowStockNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        lowStockCategoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        lowStockQuantityColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getStockQuantity()).asObject());

        // Load initial charts
        loadInitialCharts();

        // Set export button to disabled initially
        exportReportButton.setDisable(true);
    }

    private void loadInitialCharts() {
        // Load patient type distribution
        Map<String, Integer> patientCountByType = reportService.getPatientCountByType();
        loadPieChart(patientTypeChart, patientCountByType);

        // Load patient gender distribution
        Map<String, Integer> patientCountByGender = reportService.getPatientCountByGender();
        loadPieChart(patientGenderChart, patientCountByGender);

        // Load patient age distribution
        Map<String, Integer> patientAgeDistribution = reportService.getPatientAgeDistribution();
        loadBarChart(patientAgeChart, patientAgeDistribution);

        // Load revenue by patient type
        Map<String, Double> revenueByType = reportService.getRevenueByPatientType();
        loadBarChartDouble(revenueByTypeChart, revenueByType);

        // Load revenue by month (current year)
        Map<String, Double> revenueByMonth = reportService.getRevenueByMonth(LocalDate.now().getYear());
        loadLineChart(revenueByMonthChart, revenueByMonth);

        // Load medication by category
        Map<String, Double> medicationsByCategory = reportService.getMedicationsByCategory();
        loadPieChartDouble(medicationCategoryChart, medicationsByCategory);

        // Load low stock items
        lowStockTable.setItems(FXCollections.observableArrayList(reportService.getLowStockReport()));

        // Load appointments by day
        String fromDate = DateUtil.formatDate(fromDatePicker.getValue());
        String toDate = DateUtil.formatDate(toDatePicker.getValue());
        Map<String, Integer> appointmentsByDay = reportService.getAppointmentsByDay(fromDate, toDate);
        loadLineChart(appointmentByDayChart, appointmentsByDay);

        // Load appointments by status
        Map<String, Integer> appointmentsByStatus = reportService.getAppointmentsByStatus();
        loadPieChart(appointmentStatusChart, appointmentsByStatus);

        // Load appointments by doctor
        Map<String, Integer> appointmentsByDoctor = reportService.getAppointmentsByDoctor();
        loadBarChart(appointmentByDoctorChart, appointmentsByDoctor);
    }

    private <T> void loadPieChart(PieChart chart, Map<String, T> data) {
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        for (Map.Entry<String, T> entry : data.entrySet()) {
            if (entry.getValue() instanceof Number) {
                Number value = (Number) entry.getValue();
                pieChartData.add(new PieChart.Data(entry.getKey(), value.doubleValue()));
            }
        }

        chart.setData(pieChartData);
    }

    private void loadPieChartDouble(PieChart chart, Map<String, Double> data) {
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        for (Map.Entry<String, Double> entry : data.entrySet()) {
            pieChartData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
        }

        chart.setData(pieChartData);
    }

    private void loadBarChart(BarChart<String, Number> chart, Map<String, Integer> data) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();

        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        chart.getData().clear();
        chart.getData().add(series);
    }

    private void loadBarChartDouble(BarChart<String, Number> chart, Map<String, Double> data) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();

        for (Map.Entry<String, Double> entry : data.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        chart.getData().clear();
        chart.getData().add(series);
    }

    private void loadLineChart(LineChart<String, Number> chart, Map<String, ? extends Number> data) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();

        for (Map.Entry<String, ? extends Number> entry : data.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        chart.getData().clear();
        chart.getData().add(series);
    }

    @FXML
    public void handleGenerateReport() {
        String selectedReportType = reportTypeComboBox.getValue();

        // Check if dates are valid
        if (fromDatePicker.getValue() == null || toDatePicker.getValue() == null) {
            AlertUtil.showWarning("Invalid Date Range", "Please select valid from and to dates.");
            return;
        }

        if (fromDatePicker.getValue().isAfter(toDatePicker.getValue())) {
            AlertUtil.showWarning("Invalid Date Range", "From date must be before or equal to to date.");
            return;
        }

        // Generate report based on selection
        switch (selectedReportType) {
            case "Patient Statistics":
                generatePatientStatisticsReport();
                // Show patient statistics tab
                chartTabPane.getSelectionModel().select(0);
                break;
            case "Financial Statistics":
                generateFinancialStatisticsReport();
                // Show financial statistics tab
                chartTabPane.getSelectionModel().select(1);
                break;
            case "Pharmacy Statistics":
                generatePharmacyStatisticsReport();
                // Show pharmacy statistics tab
                chartTabPane.getSelectionModel().select(2);
                break;
            case "Appointment Statistics":
                generateAppointmentStatisticsReport();
                // Show appointment statistics tab
                chartTabPane.getSelectionModel().select(3);
                break;
            case "Doctor Statistics":
                generateDoctorStatisticsReport();
                // Show doctor statistics tab
                chartTabPane.getSelectionModel().select(4);
                break;
            case "Comprehensive Report":
                generateComprehensiveReport();
                break;
            default:
                AlertUtil.showWarning("Invalid Selection", "Please select a valid report type.");
                return;
        }

        // Enable export button now that a report has been generated
        exportReportButton.setDisable(false);
    }

    private void generatePatientStatisticsReport() {
        StringBuilder report = new StringBuilder();
        report.append("PATIENT STATISTICS REPORT\n");
        report.append("-------------------------\n");
        report.append("Generated on: ").append(DateUtil.getCurrentDateTime()).append("\n\n");

        // Add patient counts by type
        report.append("Patient Distribution by Type:\n");
        Map<String, Integer> patientCountByType = reportService.getPatientCountByType();
        for (Map.Entry<String, Integer> entry : patientCountByType.entrySet()) {
            report.append("- ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        report.append("\n");

        // Add patient counts by gender
        report.append("Patient Distribution by Gender:\n");
        Map<String, Integer> patientCountByGender = reportService.getPatientCountByGender();
        for (Map.Entry<String, Integer> entry : patientCountByGender.entrySet()) {
            report.append("- ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        report.append("\n");

        // Add patient age distribution
        report.append("Patient Age Distribution:\n");
        Map<String, Integer> patientAgeDistribution = reportService.getPatientAgeDistribution();
        for (Map.Entry<String, Integer> entry : patientAgeDistribution.entrySet()) {
            report.append("- ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }

        // Update the report text area
        reportTextArea.setText(report.toString());
        currentReport = report.toString();
    }

    private void generateFinancialStatisticsReport() {
        StringBuilder report = new StringBuilder();
        report.append("FINANCIAL STATISTICS REPORT\n");
        report.append("---------------------------\n");
        report.append("Generated on: ").append(DateUtil.getCurrentDateTime()).append("\n\n");

        // Add revenue by patient type
        report.append("Revenue by Patient Type:\n");
        Map<String, Double> revenueByType = reportService.getRevenueByPatientType();
        for (Map.Entry<String, Double> entry : revenueByType.entrySet()) {
            report.append("- ").append(entry.getKey()).append(": $").append(String.format("%.2f", entry.getValue())).append("\n");
        }
        report.append("\n");

        // Add revenue by month
        report.append("Revenue by Month (").append(LocalDate.now().getYear()).append("):\n");
        Map<String, Double> revenueByMonth = reportService.getRevenueByMonth(LocalDate.now().getYear());
        for (Map.Entry<String, Double> entry : revenueByMonth.entrySet()) {
            String monthName = getMonthName(Integer.parseInt(entry.getKey()));
            report.append("- ").append(monthName).append(": $").append(String.format("%.2f", entry.getValue())).append("\n");
        }

        // Update the report text area
        reportTextArea.setText(report.toString());
        currentReport = report.toString();
    }

    private void generatePharmacyStatisticsReport() {
        StringBuilder report = new StringBuilder();
        report.append("PHARMACY STATISTICS REPORT\n");
        report.append("--------------------------\n");
        report.append("Generated on: ").append(DateUtil.getCurrentDateTime()).append("\n\n");

        // Add medication value by category
        report.append("Medication Stock Value by Category:\n");
        Map<String, Double> medicationsByCategory = reportService.getMedicationsByCategory();
        for (Map.Entry<String, Double> entry : medicationsByCategory.entrySet()) {
            report.append("- ").append(entry.getKey()).append(": $").append(String.format("%.2f", entry.getValue())).append("\n");
        }
        report.append("\n");

        // Add low stock items
        report.append("Low Stock Medications:\n");
        List<Medication> lowStockItems = reportService.getLowStockReport();
        for (Medication medication : lowStockItems) {
            report.append("- ").append(medication.getName())
                    .append(" (").append(medication.getCategory()).append(")")
                    .append(": ").append(medication.getStockQuantity()).append(" units\n");
        }

        // Update the report text area
        reportTextArea.setText(report.toString());
        currentReport = report.toString();
    }

    private void generateAppointmentStatisticsReport() {
        StringBuilder report = new StringBuilder();
        report.append("APPOINTMENT STATISTICS REPORT\n");
        report.append("-----------------------------\n");
        report.append("Generated on: ").append(DateUtil.getCurrentDateTime()).append("\n\n");

        // Add date range
        String fromDate = DateUtil.formatDate(fromDatePicker.getValue());
        String toDate = DateUtil.formatDate(toDatePicker.getValue());
        report.append("Date Range: ").append(fromDate).append(" to ").append(toDate).append("\n\n");

        // Add appointments by day
        report.append("Appointments by Day:\n");
        Map<String, Integer> appointmentsByDay = reportService.getAppointmentsByDay(fromDate, toDate);
        for (Map.Entry<String, Integer> entry : appointmentsByDay.entrySet()) {
            report.append("- ").append(entry.getKey()).append(": ").append(entry.getValue()).append(" appointments\n");
        }
        report.append("\n");

        // Add appointments by status
        report.append("Appointments by Status:\n");
        Map<String, Integer> appointmentsByStatus = reportService.getAppointmentsByStatus();
        for (Map.Entry<String, Integer> entry : appointmentsByStatus.entrySet()) {
            report.append("- ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }

        // Update the report text area
        reportTextArea.setText(report.toString());
        currentReport = report.toString();
    }

    private void generateDoctorStatisticsReport() {
        StringBuilder report = new StringBuilder();
        report.append("DOCTOR STATISTICS REPORT\n");
        report.append("------------------------\n");
        report.append("Generated on: ").append(DateUtil.getCurrentDateTime()).append("\n\n");

        // Add appointments by doctor
        report.append("Appointments by Doctor:\n");
        Map<String, Integer> appointmentsByDoctor = reportService.getAppointmentsByDoctor();
        for (Map.Entry<String, Integer> entry : appointmentsByDoctor.entrySet()) {
            report.append("- ").append(entry.getKey()).append(": ").append(entry.getValue()).append(" appointments\n");
        }

        // Update the report text area
        reportTextArea.setText(report.toString());
        currentReport = report.toString();
    }

    private void generateComprehensiveReport() {
        // Generate comprehensive hospital report
        String report = reportService.generateHospitalReport();

        // Update the report text area
        reportTextArea.setText(report);
        currentReport = report;
    }

    @FXML
    public void handleExportReport() {
        if (currentReport == null || currentReport.isEmpty()) {
            AlertUtil.showWarning("No Report", "Please generate a report first before exporting.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Report");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Text Files", "*.txt")
        );
        fileChooser.setInitialFileName("Report_" + DateUtil.getCurrentDate() + ".txt");

        File file = fileChooser.showSaveDialog(exportReportButton.getScene().getWindow());
        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(currentReport);
                AlertUtil.showInformation("Export Successful", "Report has been exported successfully.");
            } catch (IOException e) {
                e.printStackTrace();
                AlertUtil.showError("Export Error", "Could not export report: " + e.getMessage());
            }
        }
    }

    private String getMonthName(int month) {
        switch(month) {
            case 1: return "January";
            case 2: return "February";
            case 3: return "March";
            case 4: return "April";
            case 5: return "May";
            case 6: return "June";
            case 7: return "July";
            case 8: return "August";
            case 9: return "September";
            case 10: return "October";
            case 11: return "November";
            case 12: return "December";
            default: return "Unknown";
        }
    }
}