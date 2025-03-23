package com.hospital.service;

import com.hospital.dao.DoctorDao;
import com.hospital.model.Doctor;
import com.hospital.model.HospitalBlock;
import com.hospital.util.DateUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DoctorService {
    private static DoctorService instance;
    private final DoctorDao doctorDao;

    private DoctorService() {
        doctorDao = new DoctorDao();
        loadDoctors();
    }

    public static DoctorService getInstance() {
        if (instance == null) {
            instance = new DoctorService();
        }
        return instance;
    }

    private void loadDoctors() {
        List<Doctor> doctors = doctorDao.getAllEntities();
        if (doctors.isEmpty()) {
            createDefaultDoctors();
        }
    }

    private void createDefaultDoctors() {
        try {
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
            HospitalBlock firstBlock = blocks.isEmpty() ?
                    new HospitalBlock("A", 1, "General Medicine") : blocks.get(0);

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

            addDoctor(doc1);
            addDoctor(doc2);
            addDoctor(doc3);
            addDoctor(doc4);
            addDoctor(doc5);
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
        return doctorDao.getAllEntities();
    }

    public List<Doctor> getDoctorsBySpecialty(String specialty) {
        if (specialty == null || specialty.trim().isEmpty()) {
            return getAllDoctors();
        }

        return doctorDao.getAllEntities().stream()
                .filter(d -> d.getSpecialization().equalsIgnoreCase(specialty))
                .collect(Collectors.toList());
    }

    public List<Doctor> getAvailableDoctors() {
        return doctorDao.getAllEntities().stream()
                .filter(Doctor::isAvailable)
                .collect(Collectors.toList());
    }

    public Doctor getDoctorById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return null;
        }
        return doctorDao.getById(id);
    }

    public Doctor getDoctorByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }

        return doctorDao.getAllEntities().stream()
                .filter(d -> d.getUsername().equals(username))
                .findFirst()
                .orElse(null);
    }

    public List<Doctor> searchDoctors(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllDoctors();
        }

        String searchTerm = query.toLowerCase();
        return doctorDao.getAllEntities().stream()
                .filter(d -> d.getFullName().toLowerCase().contains(searchTerm) ||
                        d.getSpecialization().toLowerCase().contains(searchTerm) ||
                        d.getLicenseNumber().toLowerCase().contains(searchTerm))
                .collect(Collectors.toList());
    }

    public void addDoctor(Doctor doctor) throws IOException {
        if (doctor.getLastLogin() == null) {
            doctor.setLastLogin(DateUtil.getCurrentDateTime());
        }
        doctorDao.save(doctor);
    }

    public void updateDoctor(Doctor doctor) throws IOException {
        doctorDao.update(doctor);
    }

    public void deleteDoctor(String id) throws IOException {
        doctorDao.delete(id);
    }

    public boolean isDoctorUsed(String doctorId) {
        // Check if doctor is used in appointments or other records
        AppointmentService appointmentService = AppointmentService.getInstance();
        return appointmentService.hasDoctorAppointments(doctorId);
    }
}