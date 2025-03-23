package com.hospital.dao;

import com.hospital.model.MedicalTest;

import java.io.IOException;
import java.util.List;

public class MedicalTestDao extends JsonDao<MedicalTest> {
    private static final String MEDICAL_TESTS_FILE = "data/medical_tests.json";

    public MedicalTestDao() {
        super(MEDICAL_TESTS_FILE, MedicalTest.class);
    }

    @Override
    protected String getId(MedicalTest medicalTest) {
        return medicalTest.getId();
    }

    @Override
    public List<MedicalTest> getAllEntities() {
        List<MedicalTest> medicalTests = super.getAllEntities();
        return medicalTests;
    }

    @Override
    public void save(MedicalTest medicalTest) throws IOException {
        super.save(medicalTest);
    }

    @Override
    public void update(MedicalTest updatedTest) throws IOException {
        super.update(updatedTest);
    }

    @Override
    public void delete(String testId) throws IOException {
        super.delete(testId);
    }
}