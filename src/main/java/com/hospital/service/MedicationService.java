package com.hospital.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.model.Medication;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class MedicationService {
    private static MedicationService instance;
    private final String MEDICATIONS_FILE = "data/medications.json";
    private final ObjectMapper objectMapper;
    private List<Medication> medications;

    private MedicationService() {
        objectMapper = new ObjectMapper();
        loadMedications();
    }

    public static MedicationService getInstance() {
        if (instance == null) {
            instance = new MedicationService();
        }
        return instance;
    }

    private void loadMedications() {
        File file = new File(MEDICATIONS_FILE);
        if (file.exists()) {
            try {
                medications = objectMapper.readValue(file,
                        objectMapper.getTypeFactory().constructCollectionType(List.class, Medication.class));
            } catch (IOException e) {
                e.printStackTrace();
                createDefaultMedications();
            }
        } else {
            createDefaultMedications();
        }
    }

    private void createDefaultMedications() {
        medications = new ArrayList<>();

        try {
            // Create some default medications
            addMedication(new Medication("Paracetamol", "Pain relief and fever reduction",
                    "Pain Relief", 5.99, 100, "Pharma Inc.", "2026-12-31", "Tablets", false));

            addMedication(new Medication("Ibuprofen", "Non-steroidal anti-inflammatory drug",
                    "Pain Relief", 7.50, 85, "MediCorp", "2026-10-15", "Tablets", false));

            addMedication(new Medication("Amoxicillin", "Antibiotic used to treat bacterial infections",
                    "Antibiotics", 12.99, 50, "BioPharm", "2025-08-20", "Capsules", true));

            addMedication(new Medication("Lisinopril", "ACE inhibitor for high blood pressure",
                    "Cardiovascular", 15.75, 40, "HeartCare", "2026-05-10", "Tablets", true));

            addMedication(new Medication("Loratadine", "Antihistamine for allergies",
                    "Allergy", 9.25, 75, "AllergyRelief", "2027-01-15", "Tablets", false));

            addMedication(new Medication("Insulin", "Hormone for diabetes management",
                    "Diabetes", 45.50, 30, "DiabCare", "2024-12-01", "Injection", true));

            addMedication(new Medication("Salbutamol", "Bronchodilator for asthma",
                    "Respiratory", 18.99, 25, "BreathEasy", "2025-11-30", "Inhaler", true));

            addMedication(new Medication("Omeprazole", "Proton pump inhibitor for acid reflux",
                    "Gastrointestinal", 11.49, 60, "GutHealth", "2026-08-15", "Capsules", true));

            addMedication(new Medication("Simvastatin", "Statin for cholesterol management",
                    "Cardiovascular", 14.25, 45, "HeartCare", "2026-03-20", "Tablets", true));

            addMedication(new Medication("Diazepam", "Benzodiazepine for anxiety",
                    "Mental Health", 22.75, 20, "MindCare", "2025-04-10", "Tablets", true));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Medication> getAllMedications() {
        return medications;
    }

    public List<Medication> getMedicationsByCategory(String category) {
        return medications.stream()
                .filter(m -> m.getCategory().equalsIgnoreCase(category))
                .collect(Collectors.toList());
    }

    public List<Medication> getLowStockMedications() {
        return medications.stream()
                .filter(Medication::isLowStock)
                .collect(Collectors.toList());
    }

    public List<Medication> getOutOfStockMedications() {
        return medications.stream()
                .filter(Medication::isOutOfStock)
                .collect(Collectors.toList());
    }

    public Medication getMedicationById(String id) {
        return medications.stream()
                .filter(m -> m.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public void addMedication(Medication medication) throws IOException {
        if (medication.getId() == null || medication.getId().isEmpty()) {
            medication.setId(UUID.randomUUID().toString());
        }
        medications.add(medication);
        saveMedications();
    }

    public void updateMedication(Medication medication) throws IOException {
        for (int i = 0; i < medications.size(); i++) {
            if (medications.get(i).getId().equals(medication.getId())) {
                medications.set(i, medication);
                break;
            }
        }
        saveMedications();
    }

    public void updateStock(String medicationId, int quantity) throws IOException {
        Medication medication = getMedicationById(medicationId);
        if (medication != null) {
            medication.setStockQuantity(medication.getStockQuantity() + quantity);
            updateMedication(medication);
        }
    }

    public void deleteMedication(String id) throws IOException {
        medications.removeIf(m -> m.getId().equals(id));
        saveMedications();
    }

    private void saveMedications() throws IOException {
        File file = new File(MEDICATIONS_FILE);
        file.getParentFile().mkdirs();
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, medications);
    }
}