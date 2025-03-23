package com.hospital.service;

import com.hospital.dao.PatientDao;
import com.hospital.model.EmergencyPatient;
import com.hospital.model.HospitalBlock;
import com.hospital.model.InPatient;
import com.hospital.model.OutPatient;
import com.hospital.model.Patient;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class PatientService {
    private final PatientDao patientDao;

    public PatientService() {
        this.patientDao = new PatientDao();
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

    public List<Patient> getPatientsByLocation(String blockName) {
        return patientDao.getPatientsByLocation(blockName);
    }

    public void savePatient(Patient patient) throws IOException {
        patientDao.save(patient);
    }

    public void updatePatient(Patient patient) throws IOException {
        patientDao.update(patient);
    }

    public void deletePatient(String id) throws IOException {
        patientDao.delete(id);
    }

    // Factory method to create a new patient based on type
    public Patient createPatient(String type, String name, int age, String gender, HospitalBlock location,
                                 String registrationDate, String contactNumber, String address, String bloodGroup,
                                 Object... additionalParams) {
        Patient patient = null;

        switch (type) {
            case "InPatient":
                if (additionalParams.length >= 4) {
                    String roomNumber = (String) additionalParams[0];
                    String admissionDate = (String) additionalParams[1];
                    double dailyRate = (double) additionalParams[2];
                    int numberOfDaysAdmitted = (int) additionalParams[3];
                    String attendingDoctor = additionalParams.length > 4 ? (String) additionalParams[4] : "";

                    patient = new InPatient(name, age, gender, location, registrationDate, contactNumber, address,
                            bloodGroup, roomNumber, admissionDate, dailyRate, numberOfDaysAdmitted, attendingDoctor);
                }
                break;

            case "OutPatient":
                if (additionalParams.length >= 3) {
                    String appointmentDate = (String) additionalParams[0];
                    double consultFee = (double) additionalParams[1];
                    String consultingDoctor = (String) additionalParams[2];
                    String diagnosis = additionalParams.length > 3 ? (String) additionalParams[3] : "";

                    patient = new OutPatient(name, age, gender, location, registrationDate, contactNumber, address,
                            bloodGroup, appointmentDate, consultFee, consultingDoctor, diagnosis);
                }
                break;

            case "EmergencyPatient":
                if (additionalParams.length >= 4) {
                    String severityLevel = (String) additionalParams[0];
                    double emergencyTreatmentCost = (double) additionalParams[1];
                    String emergencyContact = (String) additionalParams[2];
                    String treatmentDetails = (String) additionalParams[3];
                    String admissionTime = additionalParams.length > 4 ? (String) additionalParams[4] : "";

                    patient = new EmergencyPatient(name, age, gender, location, registrationDate, contactNumber, address,
                            bloodGroup, severityLevel, emergencyTreatmentCost, emergencyContact, treatmentDetails, admissionTime);
                }
                break;
        }

        return patient;
    }

    // Get total bill amount for all patients
    public double getTotalBilling() {
        List<Patient> patients = getAllPatients();
        return patients.stream()
                .mapToDouble(Patient::calculateBill)
                .sum();
    }

    // Get total patients by gender
    public long getPatientCountByGender(String gender) {
        List<Patient> patients = getAllPatients();
        return patients.stream()
                .filter(p -> p.getGender().equalsIgnoreCase(gender))
                .count();
    }
    private HospitalBlock getBlockBySpecialty(String specialty) {
        HospitalService hospitalService = HospitalService.getInstance();
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

    // Initialize with some sample data for testing
    public void initializeWithSampleData() {
        if (getAllPatients().isEmpty()) {
            try {
                // Create a hospital block
                HospitalBlock block1 = new HospitalBlock("A", 1, "General Medicine");
                HospitalBlock block2 = new HospitalBlock("B", 2, "Cardiology");
                HospitalBlock block3 = new HospitalBlock("C", 1, "Emergency");

                // Create sample patients
                InPatient inPatient = new InPatient(
                        "John Smith", 45, "Male", block1,
                        "2023-05-20", "1234567890", "123 Main St", "O+",
                        "A-101", "2023-05-20", 150.0, 5, "Dr. Wilson");

                OutPatient outPatient = new OutPatient(
                        "Jane Doe", 32, "Female", block2,
                        "2023-05-21", "9876543210", "456 Elm St", "A-",
                        "2023-05-23", 75.0, "Dr. Johnson", "Hypertension");

                EmergencyPatient emergencyPatient = new EmergencyPatient(
                        "Mike Johnson", 28, "Male", block3,
                        "2023-05-22", "5551234567", "789 Oak Rd", "B+",
                        "High", 350.0, "5559876543", "Fractured arm", "2023-05-22 15:30");

                // Save the patients
                patientDao.save(inPatient);
                patientDao.save(outPatient);
                patientDao.save(emergencyPatient);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}