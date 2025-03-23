package com.hospital.dao;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.model.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class PatientDao extends JsonDao<Patient> {

    public PatientDao() {
        super("data/patients.json", Patient.class);
    }

    @Override
    protected String getId(Patient patient) {
        return patient.getId();
    }

    @Override
    public List<Patient> getAllEntities() {
        File file = new File(filePath);
        if (!file.exists()) {
            return new ArrayList<>();
        }

        try {
            // Use Jackson's polymorphic deserialization
            JavaType type = objectMapper.getTypeFactory().constructCollectionType(
                    List.class, Patient.class);
            return objectMapper.readValue(file, type);
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public void save(Patient patient) throws IOException {
        if (patient.getId() == null || patient.getId().isEmpty()) {
            patient.setId(UUID.randomUUID().toString());
        }

        List<Patient> patients = getAllEntities();

        // Check if patient already exists
        boolean exists = patients.stream()
                .anyMatch(p -> p.getId().equals(patient.getId()));

        if (!exists) {
            patients.add(patient);
            saveAllEntities(patients);
        } else {
            update(patient);
        }
    }

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
                    if (patient.getLocation() != null) {
                        HospitalBlock block = patient.getLocation();
                        return block.getBlockName().toLowerCase().contains(searchLower) ||
                                block.getSpecialty().toLowerCase().contains(searchLower);
                    }
                    return false;
                })
                .collect(Collectors.toList());
    }
}