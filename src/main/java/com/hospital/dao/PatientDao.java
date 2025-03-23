package com.hospital.dao;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.hospital.model.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class PatientDao extends JsonDao<Patient> {
    private final String PATIENTS_FILE = "data/patients.json";
    private final ObjectMapper mapper;

    public PatientDao() {
        super("data/patients.json", Patient.class);
        this.mapper = new ObjectMapper();
    }

    @Override
    protected String getId(Patient patient) {
        return patient.getId();
    }

    @Override
    public List<Patient> getAllEntities() {
        File file = new File(PATIENTS_FILE);
        if (!file.exists()) {
            return new ArrayList<>();
        }

        List<Patient> patients = new ArrayList<>();

        try {
            // Special handling for polymorphic deserialization
            JsonNode rootNode = mapper.readTree(file);
            if (rootNode.isArray()) {
                for (JsonNode node : rootNode) {
                    String type = node.has("patientType") ? node.get("patientType").asText() : "Unknown";

                    Patient patient = null;
                    switch (type) {
                        case "InPatient":
                            patient = mapper.treeToValue(node, InPatient.class);
                            break;
                        case "OutPatient":
                            patient = mapper.treeToValue(node, OutPatient.class);
                            break;
                        case "EmergencyPatient":
                            patient = mapper.treeToValue(node, EmergencyPatient.class);
                            break;
                        default:
                            patient = mapper.treeToValue(node, Patient.class);
                            break;
                    }

                    if (patient != null) {
                        patients.add(patient);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return patients;
    }

    @Override
    public void save(Patient patient) throws IOException {
        if (patient.getId() == null || patient.getId().isEmpty()) {
            patient.setId(UUID.randomUUID().toString());
        }

        List<Patient> patients = getAllEntities();
        patients.add(patient);
        saveAllEntities(patients);
    }

    @Override
    public void update(Patient updatedPatient) throws IOException {
        List<Patient> patients = getAllEntities();

        boolean found = false;
        for (int i = 0; i < patients.size(); i++) {
            if (patients.get(i).getId().equals(updatedPatient.getId())) {
                patients.set(i, updatedPatient);
                found = true;
                break;
            }
        }

        if (found) {
            saveAllEntities(patients);
        }
    }

    @Override
    public void delete(String id) throws IOException {
        List<Patient> patients = getAllEntities();
        patients.removeIf(patient -> patient.getId().equals(id));
        saveAllEntities(patients);
    }

    @Override
    protected void saveAllEntities(List<Patient> patients) throws IOException {
        File file = new File(PATIENTS_FILE);
        file.getParentFile().mkdirs();

        // Create a JSON array for polymorphic serialization
        ArrayNode arrayNode = mapper.createArrayNode();

        for (Patient patient : patients) {
            JsonNode patientNode = mapper.valueToTree(patient);
            arrayNode.add(patientNode);
        }

        mapper.writerWithDefaultPrettyPrinter().writeValue(file, arrayNode);
    }

    // Add these missing methods that are called from PatientService

    /**
     * Search for patients by name
     * @param name Search string for patient name
     * @return List of patients whose name contains the search string
     */
    public List<Patient> searchPatientsByName(String name) {
        List<Patient> allPatients = getAllEntities();
        if (name == null || name.isEmpty()) {
            return allPatients;
        }

        String searchLower = name.toLowerCase();
        return allPatients.stream()
                .filter(patient -> patient.getName().toLowerCase().contains(searchLower))
                .collect(Collectors.toList());
    }

    /**
     * Get patients by type
     * @param type Patient type (InPatient, OutPatient, EmergencyPatient)
     * @return List of patients of the specified type
     */
    public List<Patient> getPatientsByType(String type) {
        List<Patient> allPatients = getAllEntities();
        if (type == null || type.isEmpty()) {
            return allPatients;
        }

        return allPatients.stream()
                .filter(patient -> {
                    if (type.equalsIgnoreCase("InPatient") && patient instanceof InPatient) {
                        return true;
                    } else if (type.equalsIgnoreCase("OutPatient") && patient instanceof OutPatient) {
                        return true;
                    } else if (type.equalsIgnoreCase("EmergencyPatient") && patient instanceof EmergencyPatient) {
                        return true;
                    }
                    return false;
                })
                .collect(Collectors.toList());
    }

    /**
     * Get patients by location
     * @param location Location string to search for
     * @return List of patients in the specified location
     */
    public List<Patient> getPatientsByLocation(String location) {
        List<Patient> allPatients = getAllEntities();
        if (location == null || location.isEmpty()) {
            return allPatients;
        }

        String searchLower = location.toLowerCase();
        return allPatients.stream()
                .filter(patient -> {
                    if (patient instanceof InPatient) {
                        InPatient inPatient = (InPatient) patient;
                        HospitalBlock block = inPatient.getLocation();
                        return block != null &&
                                (block.getBlockName().toLowerCase().contains(searchLower) ||
                                        block.getSpecialty().toLowerCase().contains(searchLower));
                    } else if (patient instanceof OutPatient) {
                        OutPatient outPatient = (OutPatient) patient;
                        HospitalBlock block = outPatient.getLocation();
                        return block != null &&
                                (block.getBlockName().toLowerCase().contains(searchLower) ||
                                        block.getSpecialty().toLowerCase().contains(searchLower));
                    }
                    return false;
                })
                .collect(Collectors.toList());
    }
}