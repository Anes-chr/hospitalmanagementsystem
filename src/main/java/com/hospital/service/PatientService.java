package com.hospital.service;

import com.hospital.dao.PatientDao;
import com.hospital.model.*;
import com.hospital.util.DateUtil;

import java.io.IOException;
import java.util.List;

public class PatientService {
    private final PatientDao patientDao;
    private final HospitalService hospitalService;

    public PatientService() {
        this.patientDao = new PatientDao();
        this.hospitalService = HospitalService.getInstance();
    }

    public List<Patient> getAllPatients() {
        return patientDao.getAllEntities();
    }

    public Patient getPatientById(String id) {
        return patientDao.getById(id);
    }

    public List<Patient> searchPatientsByName(String name) {
        return patientDao.searchPatientsByName(name);
    }

    public List<Patient> getPatientsByType(String type) {
        return patientDao.getPatientsByType(type);
    }

    public List<Patient> getPatientsByLocation(String location) {
        return patientDao.getPatientsByLocation(location);
    }

    public void addPatient(Patient patient) throws IOException {
        patient.setRegistrationDate(DateUtil.getCurrentDate());
        patientDao.save(patient);
    }

    // Add this method - it was missing
    public void savePatient(Patient patient) throws IOException {
        if (patient.getRegistrationDate() == null || patient.getRegistrationDate().isEmpty()) {
            patient.setRegistrationDate(DateUtil.getCurrentDate());
        }
        patientDao.save(patient);
    }

    public void updatePatient(Patient patient) throws IOException {
        patientDao.update(patient);
    }

    public void deletePatient(String id) throws IOException {
        patientDao.delete(id);
    }

    private HospitalBlock getBlockBySpecialty(String specialty) {
        List<HospitalBlock> blocks = hospitalService.getAllBlocks();

        if (blocks == null || blocks.isEmpty()) {
            // Create a default block if none exists
            return new HospitalBlock("A", 1, "General");
        }

        return blocks.stream()
                .filter(b -> b.getSpecialty().equals(specialty))
                .findFirst()
                .orElse(blocks.get(0)); // Default to first block if specialty not found
    }

    public void initializeWithSampleData() {
        // Check if we already have patients
        List<Patient> existingPatients = getAllPatients();
        if (!existingPatients.isEmpty()) {
            return; // Skip initialization if data already exists
        }

        try {
            // Get hospital blocks for assigning patients
            List<HospitalBlock> blocks = hospitalService.getAllBlocks();

            // Create default blocks if none exist
            if (blocks == null || blocks.isEmpty()) {
                HospitalBlock generalBlock = new HospitalBlock("A", 1, "General Medicine");
                HospitalBlock emergencyBlock = new HospitalBlock("B", 1, "Emergency");
                HospitalBlock pediatricsBlock = new HospitalBlock("C", 2, "Pediatrics");

                hospitalService.addBlock(generalBlock);
                hospitalService.addBlock(emergencyBlock);
                hospitalService.addBlock(pediatricsBlock);

                blocks = hospitalService.getAllBlocks();
            }

            // Get blocks by specialty or use first available
            HospitalBlock generalBlock = blocks.stream()
                    .filter(b -> b.getSpecialty().contains("General"))
                    .findFirst()
                    .orElse(blocks.get(0));

            HospitalBlock emergencyBlock = blocks.stream()
                    .filter(b -> b.getSpecialty().contains("Emergency"))
                    .findFirst()
                    .orElse(blocks.get(0));

            // Create and save sample patients
            // InPatient
            InPatient inPatient1 = new InPatient(
                    "John Doe", 45, "Male", generalBlock,
                    DateUtil.getCurrentDate(), "5551234567", "123 Main St",
                    "A+", "101", DateUtil.getCurrentDate(), 250.0, 5, "Dr. Smith"
            );
            patientDao.save(inPatient1);

            InPatient inPatient2 = new InPatient(
                    "Sarah Johnson", 35, "Female", generalBlock,
                    DateUtil.getCurrentDate(), "5552345678", "456 Elm St",
                    "O-", "102", DateUtil.getCurrentDate(), 300.0, 3, "Dr. Wilson"
            );
            patientDao.save(inPatient2);

            // OutPatient
            OutPatient outPatient1 = new OutPatient(
                    "David Brown", 52, "Male", generalBlock,
                    DateUtil.getCurrentDate(), "5553456789", "789 Pine St",
                    "B-", DateUtil.getCurrentDate(), 150.0, "Dr. Jones", "Regular check-up"
            );
            patientDao.save(outPatient1);

            OutPatient outPatient2 = new OutPatient(
                    "Emily Clark", 28, "Female", generalBlock,
                    DateUtil.getCurrentDate(), "5554567890", "101 Oak St",
                    "AB+", DateUtil.getCurrentDate(), 120.0, "Dr. Garcia", "Allergies"
            );
            patientDao.save(outPatient2);

            // EmergencyPatient
            EmergencyPatient emergencyPatient1 = new EmergencyPatient(
                    "Mike Johnson", 28, "Male", emergencyBlock,
                    DateUtil.getCurrentDate(), "5551234567", "789 Oak Rd",
                    "B+", "High", 350.0, "5559876543", "Fractured arm", "15:30"
            );
            patientDao.save(emergencyPatient1);

        } catch (IOException e) {
            System.err.println("Error initializing patient data: " + e.getMessage());
            e.printStackTrace();
        }
    }
}