package com.hospital.dao;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hospital.model.EmergencyPatient;
import com.hospital.model.InPatient;
import com.hospital.model.OutPatient;
import com.hospital.model.Patient;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PatientDao extends JsonDao<Patient> {
    private static final String FILE_PATH = "data/patients.json";
    private final ObjectMapper mapper;

    public PatientDao() {
        super(FILE_PATH, Patient.class);
        this.mapper = new ObjectMapper();
    }

    @Override
    public List<Patient> getAllEntities() {
        createFileIfNotExists();
        List<Patient> patients = new ArrayList<>();

        try {
            File file = new File(filePath);
            JsonNode rootNode = mapper.readTree(file);

            for (JsonNode node : rootNode) {
                String type = node.get("patientType").asText();
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
                }

                if (patient != null) {
                    patients.add(patient);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return patients;
    }

    @Override
    public Patient getById(String id) {
        List<Patient> patients = getAllEntities();
        return patients.stream()
                .filter(patient -> patient.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void save(Patient patient) throws IOException {
        List<Patient> patients = getAllEntities();

        // Check if patient already exists
        boolean exists = patients.stream()
                .anyMatch(p -> p.getId().equals(patient.getId()));

        if (!exists) {
            patients.add(patient);

            List<ObjectNode> jsonNodes = new ArrayList<>();
            for (Patient p : patients) {
                ObjectNode node = mapper.valueToTree(p);
                node.put("patientType", p.getPatientType());
                jsonNodes.add(node);
            }

            File file = new File(filePath);
            mapper.writeValue(file, jsonNodes);
        } else {
            update(patient);
        }
    }

    @Override
    public void update(Patient patient) throws IOException {
        List<Patient> patients = getAllEntities();

        for (int i = 0; i < patients.size(); i++) {
            if (patients.get(i).getId().equals(patient.getId())) {
                patients.set(i, patient);
                break;
            }
        }

        List<ObjectNode> jsonNodes = new ArrayList<>();
        for (Patient p : patients) {
            ObjectNode node = mapper.valueToTree(p);
            node.put("patientType", p.getPatientType());
            jsonNodes.add(node);
        }

        File file = new File(filePath);
        mapper.writeValue(file, jsonNodes);
    }

    @Override
    public void delete(String id) throws IOException {
        List<Patient> patients = getAllEntities();
        List<Patient> updatedPatients = patients.stream()
                .filter(patient -> !patient.getId().equals(id))
                .collect(Collectors.toList());

        List<ObjectNode> jsonNodes = new ArrayList<>();
        for (Patient p : updatedPatients) {
            ObjectNode node = mapper.valueToTree(p);
            node.put("patientType", p.getPatientType());
            jsonNodes.add(node);
        }

        File file = new File(filePath);
        mapper.writeValue(file, jsonNodes);
    }

    public List<Patient> searchPatientsByName(String name) {
        List<Patient> patients = getAllEntities();
        return patients.stream()
                .filter(patient -> patient.getName().toLowerCase().contains(name.toLowerCase()))
                .collect(Collectors.toList());
    }

    public List<Patient> getPatientsByType(String type) {
        List<Patient> patients = getAllEntities();
        return patients.stream()
                .filter(patient -> patient.getPatientType().equals(type))
                .collect(Collectors.toList());
    }

    public List<Patient> getPatientsByLocation(String blockName) {
        List<Patient> patients = getAllEntities();
        return patients.stream()
                .filter(patient -> patient.getLocation() != null &&
                        patient.getLocation().getBlockName().equals(blockName))
                .collect(Collectors.toList());
    }
}