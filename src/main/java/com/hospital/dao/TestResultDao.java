package com.hospital.dao;

import com.hospital.model.TestResult;

import java.io.IOException;
import java.util.List;

public class TestResultDao extends JsonDao<TestResult> {
    private static final String TEST_RESULTS_FILE = "data/test_results.json";

    public TestResultDao() {
        super(TEST_RESULTS_FILE, TestResult.class);
    }

    @Override
    protected String getId(TestResult testResult) {
        return testResult.getId();
    }

    @Override
    public List<TestResult> getAllEntities() {
        List<TestResult> testResults = super.getAllEntities();
        return testResults;
    }

    @Override
    public void save(TestResult testResult) throws IOException {
        super.save(testResult);
    }

    @Override
    public void update(TestResult updatedResult) throws IOException {
        super.update(updatedResult);
    }

    @Override
    public void delete(String resultId) throws IOException {
        super.delete(resultId);
    }
}