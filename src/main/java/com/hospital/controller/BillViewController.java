package com.hospital.controller;

import com.hospital.model.EmergencyPatient;
import com.hospital.model.InPatient;
import com.hospital.model.OutPatient;
import com.hospital.model.Patient;
import com.hospital.util.AlertUtil;
import com.hospital.util.DateUtil;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.print.PrinterJob;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Random;

public class BillViewController {

    @FXML
    private Label hospitalNameLabel;

    @FXML
    private Label patientNameLabel;

    @FXML
    private Label patientIdLabel;

    @FXML
    private Label patientTypeLabel;

    @FXML
    private Label billDateLabel;

    @FXML
    private Label billNumberLabel;

    @FXML
    private TableView<BillItem> billItemsTable;

    @FXML
    private TableColumn<BillItem, String> descriptionColumn;

    @FXML
    private TableColumn<BillItem, Integer> quantityColumn;

    @FXML
    private TableColumn<BillItem, Double> unitPriceColumn;

    @FXML
    private TableColumn<BillItem, Double> totalColumn;

    @FXML
    private Label subtotalLabel;

    @FXML
    private Label taxLabel;

    @FXML
    private Label totalAmountLabel;

    @FXML
    private Button printButton;

    @FXML
    private Button saveButton;

    @FXML
    private Button closeButton;

    private Patient patient;
    private double subtotal;
    private double tax;
    private double total;
    private NumberFormat currencyFormatter;
    private String billNumber;

    public void initialize() {
        currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);

        // Set up table columns
        descriptionColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDescription()));
        quantityColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getQuantity()).asObject());
        unitPriceColumn.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getUnitPrice()).asObject());
        totalColumn.setCellFactory(column -> new javafx.scene.control.TableCell<BillItem, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(currencyFormatter.format(price));
                }
            }
        });

        // Format currency columns
        unitPriceColumn.setCellFactory(column -> new javafx.scene.control.TableCell<BillItem, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(currencyFormatter.format(price));
                }
            }
        });    }

    public void initData(Patient patient) {
        this.patient = patient;

        // Generate a random bill number
        billNumber = "B" + (10000 + new Random().nextInt(90000));

        // Set labels
        patientNameLabel.setText("Patient Name: " + patient.getName());
        patientIdLabel.setText("Patient ID: " + patient.getId());
        patientTypeLabel.setText("Patient Type: " + patient.getPatientType());
        billDateLabel.setText("Bill Date: " + DateUtil.getCurrentDate());
        billNumberLabel.setText("Bill #: " + billNumber);

        // Populate bill items
        populateBillItems();

        // Calculate totals
        calculateTotals();
    }

    private void populateBillItems() {
        ObservableList<BillItem> items = FXCollections.observableArrayList();

        // Add items based on patient type
        if (patient instanceof InPatient) {
            InPatient inPatient = (InPatient) patient;
            int days = inPatient.getNumberOfDaysAdmitted();
            double rate = inPatient.getDailyRate();

            items.add(new BillItem("Room Charges", days, rate, days * rate));
            items.add(new BillItem("Nursing Care", days, 75.0, days * 75.0));
            items.add(new BillItem("Meals", days * 3, 15.0, days * 3 * 15.0));
            items.add(new BillItem("Medication", 1, 120.0, 120.0));
            items.add(new BillItem("Doctor Visits", Math.max(1, days / 2), 200.0, Math.max(1, days / 2) * 200.0));

        } else if (patient instanceof OutPatient) {
            OutPatient outPatient = (OutPatient) patient;

            items.add(new BillItem("Consultation Fee", 1, outPatient.getConsultFee(), outPatient.getConsultFee()));
            items.add(new BillItem("Registration Fee", 1, 50.0, 50.0));
            items.add(new BillItem("Medication", 1, 75.0, 75.0));

        } else if (patient instanceof EmergencyPatient) {
            EmergencyPatient emergencyPatient = (EmergencyPatient) patient;

            items.add(new BillItem("Emergency Treatment", 1, emergencyPatient.getEmergencyTreatmentCost(),
                    emergencyPatient.getEmergencyTreatmentCost()));
            items.add(new BillItem("Emergency Room Fee", 1, 300.0, 300.0));
            items.add(new BillItem("Medication", 1, 150.0, 150.0));
            items.add(new BillItem("Medical Supplies", 1, 200.0, 200.0));
        }

        // Add common items
        items.add(new BillItem("Administrative Fee", 1, 25.0, 25.0));

        billItemsTable.setItems(items);
    }

    private void calculateTotals() {
        subtotal = billItemsTable.getItems().stream()
                .mapToDouble(BillItem::getTotal)
                .sum();

        tax = subtotal * 0.1; // 10% tax
        total = subtotal + tax;

        subtotalLabel.setText(currencyFormatter.format(subtotal));
        taxLabel.setText(currencyFormatter.format(tax));
        totalAmountLabel.setText(currencyFormatter.format(total));
    }

    @FXML
    public void handlePrint(ActionEvent event) {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null) {
            boolean printed = job.printPage(billItemsTable.getParent().getParent());
            if (printed) {
                job.endJob();
                AlertUtil.showInformation("Print", "Bill printed successfully.");
            } else {
                AlertUtil.showError("Print Error", "Printing failed.");
            }
        } else {
            AlertUtil.showError("Print Error", "Could not create printer job.");
        }
    }

    @FXML
    public void handleSave(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Bill");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Text Files", "*.txt")
        );
        fileChooser.setInitialFileName("Bill_" + billNumber + ".txt");

        File file = fileChooser.showSaveDialog(((Button)event.getSource()).getScene().getWindow());
        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                // Write bill header
                writer.write("=========================================\n");
                writer.write("             HOSPITAL BILL               \n");
                writer.write("=========================================\n");
                writer.write("Hospital: Metro General Hospital\n");
                writer.write("-----------------------------------------\n");
                writer.write("Patient Name: " + patient.getName() + "\n");
                writer.write("Patient ID: " + patient.getId() + "\n");
                writer.write("Patient Type: " + patient.getPatientType() + "\n");
                writer.write("Bill Date: " + DateUtil.getCurrentDate() + "\n");
                writer.write("Bill #: " + billNumber + "\n");
                writer.write("-----------------------------------------\n");
                writer.write("BILLING DETAILS:\n");
                writer.write("-----------------------------------------\n");

                // Write bill items
                String itemFormat = "%-30s %5s %12s %12s\n";
                writer.write(String.format(itemFormat, "Description", "Qty", "Unit Price", "Total"));
                writer.write("-----------------------------------------\n");

                for (BillItem item : billItemsTable.getItems()) {
                    writer.write(String.format(itemFormat,
                            item.getDescription(),
                            item.getQuantity(),
                            currencyFormatter.format(item.getUnitPrice()),
                            currencyFormatter.format(item.getTotal())
                    ));
                }

                writer.write("-----------------------------------------\n");
                writer.write(String.format("Subtotal: %32s\n", currencyFormatter.format(subtotal)));
                writer.write(String.format("Tax (10%%): %31s\n", currencyFormatter.format(tax)));
                writer.write(String.format("TOTAL AMOUNT: %28s\n", currencyFormatter.format(total)));
                writer.write("-----------------------------------------\n");
                writer.write("Payment Information:\n");
                writer.write("Please make payment within 30 days of receipt.\n");
                writer.write("For questions regarding this bill, please contact\n");
                writer.write("billing department at 555-123-4567.\n");
                writer.write("=========================================\n");

                AlertUtil.showInformation("Save", "Bill saved successfully.");
            } catch (IOException e) {
                e.printStackTrace();
                AlertUtil.showError("Save Error", "Could not save bill: " + e.getMessage());
            }
        }
    }

    @FXML
    public void handleClose(ActionEvent event) {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    public static class BillItem {
        private final String description;
        private final int quantity;
        private final double unitPrice;
        private final double total;

        public BillItem(String description, int quantity, double unitPrice, double total) {
            this.description = description;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.total = total;
        }

        public String getDescription() {
            return description;
        }

        public int getQuantity() {
            return quantity;
        }

        public double getUnitPrice() {
            return unitPrice;
        }

        public double getTotal() {
            return total;
        }
    }
}