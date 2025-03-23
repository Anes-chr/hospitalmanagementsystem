package com.hospital.dao;

import com.hospital.model.Doctor;

import java.io.IOException;
import java.util.List;

public class DoctorDao extends JsonDao<Doctor> {
    private static final String DOCTORS_FILE = "data/doctors.json";

    public DoctorDao() {
        super(DOCTORS_FILE, Doctor.class);
    }

    @Override
    protected String getId(Doctor doctor) {
        return doctor.getId();
    }

    @Override
    public List<Doctor> getAllEntities() {
        List<Doctor> doctors = super.getAllEntities();
        return doctors;
    }

    @Override
    public void save(Doctor doctor) throws IOException {
        super.save(doctor);
    }

    @Override
    public void update(Doctor updatedDoctor) throws IOException {
        super.update(updatedDoctor);
    }

    @Override
    public void delete(String doctorId) throws IOException {
        super.delete(doctorId);
    }
}