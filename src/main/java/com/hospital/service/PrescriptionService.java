package com.hospital.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.model.Prescription;
import com.hospital.util.DateUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class PrescriptionService {
    private static PrescriptionService instance;
    private final String PRESCRIPTIONS_FILE = "data/prescriptions.json";
    private final ObjectMapper objectMapper;
    private List<Prescription> prescriptions;

    private PrescriptionService() {
        objectMapper = new ObjectMapper();
        loadPrescriptions();
    }

    public static PrescriptionService getInstance() {
        if (instance == null) {
            instance = new PrescriptionService();
        }
        return instance;
    }

    private void loadPrescriptions() {
        File file = new File(PRESCRIPTIONS_FILE);
        if (file.exists()) {
            try {
                prescriptions = objectMapper.readValue(file,
                        objectMapper.getTypeFactory().constructCollectionType(List.class, Prescription.class));
            } catch (IOException e) {
                e.printStackTrace();
                prescriptions = new ArrayList<>();
            }
        } else {
            prescriptions = new ArrayList<>();
        }
    }

    public List<Prescription> getAllPrescriptions() {
        return prescriptions;
    }

    public List<Prescription> getPrescriptionsByPatientId(String patientId) {
        return prescriptions.stream()
                .filter(p -> p.getPatientId().equals(patientId))
                .collect(Collectors.toList());
    }

    public List<Prescription> getPrescriptionsByDoctorId(String doctorId) {
        return prescriptions.stream()
                .filter(p -> p.getDoctorId().equals(doctorId))
                .collect(Collectors.toList());
    }

    public List<Prescription> getUnfulfilledPrescriptions() {
        return prescriptions.stream()
                .filter(p -> !p.isFulfilled())
                .collect(Collectors.toList());
    }

    public Prescription getPrescriptionById(String id) {
        return prescriptions.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public void addPrescription(Prescription prescription) throws IOException {
        if (prescription.getId() == null || prescription.getId().isEmpty()) {
            prescription.setId(UUID.randomUUID().toString());
        }
        prescriptions.add(prescription);
        savePrescriptions();
    }

    public void updatePrescription(Prescription prescription) throws IOException {
        for (int i = 0; i < prescriptions.size(); i++) {
            if (prescriptions.get(i).getId().equals(prescription.getId())) {
                prescriptions.set(i, prescription);
                break;
            }
        }
        savePrescriptions();
    }

    public void fulfillPrescription(String id) throws IOException {
        Prescription prescription = getPrescriptionById(id);
        if (prescription != null) {
            prescription.setFulfilled(true);
            prescription.setFulfilledDate(DateUtil.getCurrentDateTime());
            updatePrescription(prescription);

            // Update medication stock
            MedicationService medicationService = MedicationService.getInstance();
            for (Prescription.PrescriptionItem item : prescription.getMedications()) {
                medicationService.updateStock(item.getMedication().getId(), -item.getQuantity());
            }
        }
    }

    public void deletePrescription(String id) throws IOException {
        prescriptions.removeIf(p -> p.getId().equals(id));
        savePrescriptions();
    }

    private void savePrescriptions() throws IOException {
        File file = new File(PRESCRIPTIONS_FILE);
        file.getParentFile().mkdirs();
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, prescriptions);
    }
}