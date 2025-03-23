package com.hospital.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.model.Doctor;
import com.hospital.model.HospitalBlock;
import com.hospital.util.DateUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class DoctorService {
    private static DoctorService instance;
    private final String DOCTORS_FILE = "data/doctors.json";
    private final ObjectMapper objectMapper;
    private List<Doctor> doctors;

    private DoctorService() {
        objectMapper = new ObjectMapper();
        loadDoctors();
    }

    public static DoctorService getInstance() {
        if (instance == null) {
            instance = new DoctorService();
        }
        return instance;
    }

    private void loadDoctors() {
        File file = new File(DOCTORS_FILE);
        if (file.exists()) {
            try {
                doctors = objectMapper.readValue(file,
                        objectMapper.getTypeFactory().constructCollectionType(List.class, Doctor.class));
            } catch (IOException e) {
                e.printStackTrace();
                createDefaultDoctors();
            }
        } else {
            createDefaultDoctors();
        }
    }

    private void createDefaultDoctors() {
        doctors = new ArrayList<>();
        HospitalService hospitalService = HospitalService.getInstance();
        List<HospitalBlock> blocks = hospitalService.getAllBlocks();

        // Safety check - if blocks list is empty, create a default block
        if (blocks == null || blocks.isEmpty()) {
            try {
                // Create a default block
                HospitalBlock defaultBlock = new HospitalBlock("A", 1, "General Medicine");
                hospitalService.addBlock(defaultBlock);
                blocks = hospitalService.getAllBlocks(); // Get updated list
            } catch (Exception e) {
                e.printStackTrace();
                // If we still can't create a block, create an empty list
                blocks = new ArrayList<>();
                blocks.add(new HospitalBlock("A", 1, "General Medicine"));
            }
        }

        // Get the first block or create a default one if list is still empty
        HospitalBlock firstBlock = blocks.isEmpty() ? new HospitalBlock("A", 1, "General Medicine") : blocks.get(0);

        try {
            // Create default doctors
            Doctor doc1 = new Doctor("john.smith", "password", "Dr. John Smith", "jsmith@hospital.com",
                    "Cardiology", "MD12345",
                    findBlockBySpecialty(blocks, "Cardiology", firstBlock),
                    "555-1234", "9:00 AM - 5:00 PM");

            Doctor doc2 = new Doctor("emily.johnson", "password", "Dr. Emily Johnson", "ejohnson@hospital.com",
                    "Pediatrics", "MD67890",
                    findBlockBySpecialty(blocks, "Pediatrics", firstBlock),
                    "555-5678", "8:00 AM - 4:00 PM");

            Doctor doc3 = new Doctor("michael.williams", "password", "Dr. Michael Williams", "mwilliams@hospital.com",
                    "General Medicine", "MD24680",
                    findBlockBySpecialty(blocks, "General Medicine", firstBlock),
                    "555-9012", "10:00 AM - 6:00 PM");

            Doctor doc4 = new Doctor("sarah.brown", "password", "Dr. Sarah Brown", "sbrown@hospital.com",
                    "Surgery", "MD13579",
                    findBlockBySpecialty(blocks, "Surgery", firstBlock),
                    "555-3456", "7:00 AM - 3:00 PM");

            Doctor doc5 = new Doctor("david.miller", "password", "Dr. David Miller", "dmiller@hospital.com",
                    "Emergency", "MD97531",
                    findBlockBySpecialty(blocks, "Emergency", firstBlock),
                    "555-7890", "All shifts (rotating)");

            doctors.add(doc1);
            doctors.add(doc2);
            doctors.add(doc3);
            doctors.add(doc4);
            doctors.add(doc5);

            saveDoctors();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private HospitalBlock findBlockBySpecialty(List<HospitalBlock> blocks, String specialty, HospitalBlock defaultBlock) {
        if (blocks == null || blocks.isEmpty()) return defaultBlock;
        return blocks.stream()
                .filter(b -> b.getSpecialty().equalsIgnoreCase(specialty))
                .findFirst()
                .orElse(defaultBlock);
    }

    public List<Doctor> getAllDoctors() {
        return doctors;
    }

    public List<Doctor> getDoctorsBySpecialty(String specialty) {
        return doctors.stream()
                .filter(d -> d.getSpecialization().equalsIgnoreCase(specialty))
                .collect(Collectors.toList());
    }

    public List<Doctor> getAvailableDoctors() {
        return doctors.stream()
                .filter(Doctor::isAvailable)
                .collect(Collectors.toList());
    }

    public Doctor getDoctorById(String id) {
        return doctors.stream()
                .filter(d -> d.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public Doctor getDoctorByUsername(String username) {
        return doctors.stream()
                .filter(d -> d.getUsername().equals(username))
                .findFirst()
                .orElse(null);
    }

    public void addDoctor(Doctor doctor) throws IOException {
        if (doctor.getId() == null || doctor.getId().isEmpty()) {
            doctor.setId(UUID.randomUUID().toString());
        }
        doctors.add(doctor);
        saveDoctors();
    }

    public void updateDoctor(Doctor doctor) throws IOException {
        for (int i = 0; i < doctors.size(); i++) {
            if (doctors.get(i).getId().equals(doctor.getId())) {
                doctors.set(i, doctor);
                break;
            }
        }
        saveDoctors();
    }

    public void deleteDoctor(String id) throws IOException {
        doctors.removeIf(d -> d.getId().equals(id));
        saveDoctors();
    }

    private void saveDoctors() throws IOException {
        File file = new File(DOCTORS_FILE);
        file.getParentFile().mkdirs();
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, doctors);
    }
}