package com.hospital.controller;

import com.hospital.model.Hospital;
import com.hospital.model.HospitalBlock;
import com.hospital.service.HospitalService;
import com.hospital.util.AlertUtil;
import com.hospital.util.ValidationUtil;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class HospitalSettingsController implements Initializable {

    @FXML
    private TextField hospitalNameField;

    @FXML
    private TextField hospitalStateField;

    @FXML
    private TextField maxPatientsField;

    @FXML
    private TableView<HospitalBlock> blocksTable;

    @FXML
    private TableColumn<HospitalBlock, String> blockNameColumn;

    @FXML
    private TableColumn<HospitalBlock, Integer> floorNumberColumn;

    @FXML
    private TableColumn<HospitalBlock, String> specialtyColumn;

    @FXML
    private TableColumn<HospitalBlock, String> blockActionColumn;

    @FXML
    private Button addBlockButton;

    @FXML
    private Button saveHospitalButton;

    @FXML
    private TextField blockNameField;

    @FXML
    private TextField floorNumberField;

    @FXML
    private TextField specialtyField;

    @FXML
    private Button clearBlockFormButton;

    @FXML
    private Button saveBlockButton;

    @FXML
    private TableView<HospitalBlock> allBlocksTable;

    @FXML
    private TableColumn<HospitalBlock, String> allBlockNameColumn;

    @FXML
    private TableColumn<HospitalBlock, Integer> allFloorNumberColumn;

    @FXML
    private TableColumn<HospitalBlock, String> allSpecialtyColumn;

    @FXML
    private TableColumn<HospitalBlock, String> allBlockActionColumn;

    private HospitalService hospitalService;
    private Hospital currentHospital;
    private HospitalBlock selectedBlock;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        hospitalService = HospitalService.getInstance();
        currentHospital = hospitalService.getCurrentHospital();

        // Set up table columns for hospital blocks
        setupBlocksTable();

        // Set up table columns for all blocks
        setupAllBlocksTable();

        // Load current hospital data
        loadHospitalData();

        // Load all hospital blocks
        loadAllBlocks();
    }

    private void setupBlocksTable() {
        blockNameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getBlockName()));
        floorNumberColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getFloorNumber()).asObject());
        specialtyColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSpecialty()));

        // Set up action column
        blockActionColumn.setCellFactory(createBlockActionCellFactory(true));
    }

    private void setupAllBlocksTable() {
        allBlockNameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getBlockName()));
        allFloorNumberColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getFloorNumber()).asObject());
        allSpecialtyColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSpecialty()));

        // Set up action column
        allBlockActionColumn.setCellFactory(createBlockActionCellFactory(false));

        // Add selection listener
        allBlocksTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedBlock = newSelection;
                populateBlockForm(newSelection);
            }
        });
    }

    private Callback<TableColumn<HospitalBlock, String>, TableCell<HospitalBlock, String>> createBlockActionCellFactory(boolean forHospitalBlocks) {
        return new Callback<>() {
            @Override
            public TableCell<HospitalBlock, String> call(TableColumn<HospitalBlock, String> param) {
                return new TableCell<>() {
                    private final Button editBtn = new Button("Edit");
                    private final Button deleteBtn = new Button("Delete");

                    {
                        editBtn.setOnAction(event -> {
                            HospitalBlock block = getTableRow().getItem();
                            if (block != null) {
                                selectedBlock = block;
                                populateBlockForm(block);
                            }
                        });

                        deleteBtn.setOnAction(event -> {
                            HospitalBlock block = getTableRow().getItem();
                            if (block != null) {
                                handleDeleteBlock(block);
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
                            buttons.getChildren().addAll(editBtn, deleteBtn);
                            setGraphic(buttons);
                        }
                    }
                };
            }
        };
    }

    private void loadHospitalData() {
        if (currentHospital != null) {
            hospitalNameField.setText(currentHospital.getName());
            hospitalStateField.setText(currentHospital.getState());
            maxPatientsField.setText(String.valueOf(currentHospital.getMaxPatients()));

            // Load blocks into table
            blocksTable.setItems(FXCollections.observableArrayList(currentHospital.getBlocks()));
        }
    }

    private void loadAllBlocks() {
        List<HospitalBlock> blocks = hospitalService.getAllBlocks();
        allBlocksTable.setItems(FXCollections.observableArrayList(blocks));
    }

    private void populateBlockForm(HospitalBlock block) {
        blockNameField.setText(block.getBlockName());
        floorNumberField.setText(String.valueOf(block.getFloorNumber()));
        specialtyField.setText(block.getSpecialty());
    }

    @FXML
    public void handleAddBlock() {
        clearBlockForm();
        blockNameField.requestFocus();
    }

    @FXML
    public void handleClearBlockForm() {
        clearBlockForm();
    }

    private void clearBlockForm() {
        blockNameField.clear();
        floorNumberField.clear();
        specialtyField.clear();
        selectedBlock = null;
    }

    @FXML
    public void handleSaveBlock() {
        // Validate input
        if (!validateBlockInput()) {
            return;
        }

        try {
            String blockName = blockNameField.getText();
            int floorNumber = Integer.parseInt(floorNumberField.getText());
            String specialty = specialtyField.getText();

            HospitalBlock block;
            if (selectedBlock != null) {
                // Update existing block
                block = selectedBlock;
                block.setBlockName(blockName);
                block.setFloorNumber(floorNumber);
                block.setSpecialty(specialty);

                hospitalService.updateBlock(block);
                AlertUtil.showInformation("Success", "Hospital block updated successfully.");
            } else {
                // Create new block
                block = new HospitalBlock(blockName, floorNumber, specialty);

                hospitalService.addBlock(block);
                AlertUtil.showInformation("Success", "Hospital block added successfully.");
            }

            // Refresh data
            loadHospitalData();
            loadAllBlocks();
            clearBlockForm();

        } catch (NumberFormatException e) {
            AlertUtil.showError("Input Error", "Floor number must be a valid integer.");
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Save Error", "Could not save hospital block: " + e.getMessage());
        }
    }

    private void handleDeleteBlock(HospitalBlock block) {
        boolean confirm = AlertUtil.showConfirmation("Confirm Deletion",
                "Are you sure you want to delete block " + block.getBlockName() + "?");

        if (confirm) {
            try {
                hospitalService.deleteBlock(block.getBlockName());

                // Refresh data
                loadHospitalData();
                loadAllBlocks();

                AlertUtil.showInformation("Success", "Hospital block deleted successfully.");
            } catch (IOException e) {
                e.printStackTrace();
                AlertUtil.showError("Delete Error", "Could not delete hospital block: " + e.getMessage());
            }
        }
    }

    @FXML
    public void handleSaveHospital() {
        // Validate input
        if (!validateHospitalInput()) {
            return;
        }

        try {
            String name = hospitalNameField.getText();
            String state = hospitalStateField.getText();
            int maxPatients = Integer.parseInt(maxPatientsField.getText());

            // Update hospital
            currentHospital.setName(name);
            currentHospital.setState(state);
            currentHospital.setMaxPatients(maxPatients);

            hospitalService.saveHospital(currentHospital);

            AlertUtil.showInformation("Success", "Hospital information saved successfully.");

        } catch (NumberFormatException e) {
            AlertUtil.showError("Input Error", "Maximum patients must be a valid integer.");
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Save Error", "Could not save hospital information: " + e.getMessage());
        }
    }

    private boolean validateHospitalInput() {
        if (!ValidationUtil.isNotEmpty(hospitalNameField.getText())) {
            AlertUtil.showError("Validation Error", "Hospital name is required.");
            return false;
        }

        if (!ValidationUtil.isNotEmpty(hospitalStateField.getText())) {
            AlertUtil.showError("Validation Error", "Hospital state/location is required.");
            return false;
        }

        if (!ValidationUtil.isValidNumber(maxPatientsField.getText())) {
            AlertUtil.showError("Validation Error", "Maximum patients must be a valid number.");
            return false;
        }

        return true;
    }

    private boolean validateBlockInput() {
        if (!ValidationUtil.isNotEmpty(blockNameField.getText())) {
            AlertUtil.showError("Validation Error", "Block name is required.");
            return false;
        }

        if (!ValidationUtil.isValidNumber(floorNumberField.getText())) {
            AlertUtil.showError("Validation Error", "Floor number must be a valid number.");
            return false;
        }

        if (!ValidationUtil.isNotEmpty(specialtyField.getText())) {
            AlertUtil.showError("Validation Error", "Specialty is required.");
            return false;
        }

        return true;
    }
}