package com.hospital.dao;

import com.hospital.model.Hospital;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HospitalDao extends JsonDao<Hospital> {
    private static final String HOSPITAL_FILE = "data/hospital.json";

    public HospitalDao() {
        super(HOSPITAL_FILE, Hospital.class);
    }

    @Override
    protected String getId(Hospital hospital) {
        return hospital.getName(); // Using name as the ID since Hospital might not have an ID field
    }

    @Override
    public Hospital getById(String id) {
        List<Hospital> hospitals = getAllEntities();

        // If no id specified, return the first hospital
        if (id == null) {
            return hospitals.isEmpty() ? null : hospitals.get(0);
        }

        // Otherwise find by name
        return hospitals.stream()
                .filter(h -> h.getName().equals(id))
                .findFirst()
                .orElse(null);
    }

    public boolean hospitalExists() {
        return new File(HOSPITAL_FILE).exists();
    }

    @Override
    public void save(Hospital hospital) throws IOException {
        List<Hospital> hospitals = getAllEntities();

        // Replace existing hospital if any (we'll just have one hospital)
        if (!hospitals.isEmpty()) {
            hospitals.clear();
        }

        hospitals.add(hospital);
        saveAllEntities(hospitals);
    }

    @Override
    public void update(Hospital hospital) throws IOException {
        save(hospital); // For hospital, update is the same as save
    }
}