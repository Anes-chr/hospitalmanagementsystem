package com.hospital.dao;

import com.hospital.model.Hospital;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HospitalDao extends JsonDao<Hospital> {
    private static final String FILE_PATH = "data/hospital.json";

    public HospitalDao() {
        super(FILE_PATH, Hospital.class);
    }

    @Override
    public Hospital getById(String id) {
        // For a hospital, we typically only have one, so we'll just return the first one
        List<Hospital> hospitals = getAllEntities();
        return hospitals.isEmpty() ? null : hospitals.get(0);
    }

    @Override
    public void save(Hospital hospital) throws IOException {
        List<Hospital> hospitals = getAllEntities();

        // If hospital already exists, update it
        if (!hospitals.isEmpty()) {
            hospitals.set(0, hospital);
        } else {
            hospitals.add(hospital);
        }

        saveEntities(hospitals);
    }

    @Override
    public void update(Hospital hospital) throws IOException {
        save(hospital);
    }

    @Override
    public void delete(String id) throws IOException {
        // Typically not needed for a hospital, but we'll implement it anyway
        List<Hospital> hospitals = getAllEntities();
        hospitals.clear();
        saveEntities(hospitals);
    }

    // Check if a hospital exists
    public boolean hospitalExists() {
        List<Hospital> hospitals = getAllEntities();
        return !hospitals.isEmpty();
    }
}