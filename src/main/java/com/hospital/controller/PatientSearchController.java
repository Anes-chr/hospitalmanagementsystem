package com.hospital.controller;

import com.hospital.model.Patient;
import com.hospital.service.PatientService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class PatientSearchController implements Initializable {

    @FXML
    private TextField searchField;

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
    private Button selectButton;

    @FXML
    private Button cancelButton;

    private PatientService patientService;
    private ObservableList<Patient> allPatients;
    private FilteredList<Patient> filteredPatients;
    private Consumer<Patient> onPatientSelected;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        patientService = new PatientService();

        // Initialize table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        ageColumn.setCellValueFactory(new PropertyValueFactory<>("age"));
        genderColumn.setCellValueFactory(new PropertyValueFactory<>("gender"));
        contactColumn.setCellValueFactory(new PropertyValueFactory<>("contactNumber"));

        // Load patients
        loadPatients();

        // Set up search functionality
        searchField.textProperty().addListener((obs, oldVal, newVal) -> handleSearch());

        // Set up double-click selection
        patientTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                handleSelect();
            }
        });
    }

    public void initData(Consumer<Patient> onPatientSelected) {
        this.onPatientSelected = onPatientSelected;
    }

    private void loadPatients() {
        List<Patient> patients = patientService.getAllPatients();
        allPatients = FXCollections.observableArrayList(patients);

        // Create filtered list
        filteredPatients = new FilteredList<>(allPatients, p -> true);
        patientTable.setItems(filteredPatients);
    }

    @FXML
    public void handleSearch() {
        String searchText = searchField.getText().toLowerCase();

        if (searchText.isEmpty()) {
            filteredPatients.setPredicate(p -> true);
        } else {
            filteredPatients.setPredicate(p ->
                    p.getName().toLowerCase().contains(searchText) ||
                            p.getContactNumber().toLowerCase().contains(searchText) ||
                            (p.getAddress() != null && p.getAddress().toLowerCase().contains(searchText)));
        }
    }

    @FXML
    public void handleSelect() {
        Patient selectedPatient = patientTable.getSelectionModel().getSelectedItem();
        if (selectedPatient != null && onPatientSelected != null) {
            onPatientSelected.accept(selectedPatient);
            closeWindow();
        }
    }

    @FXML
    public void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}