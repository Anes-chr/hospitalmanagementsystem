package com.hospital.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.model.MedicalTest;
import com.hospital.model.TestResult;
import com.hospital.model.XRayExamination;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class MedicalTestService {
    private static MedicalTestService instance;
    private final String MEDICAL_TESTS_FILE = "data/medical_tests.json";
    private final String TEST_RESULTS_FILE = "data/test_results.json";
    private final String XRAY_EXAMINATIONS_FILE = "data/xray_examinations.json";
    private final ObjectMapper objectMapper;
    private List<MedicalTest> medicalTests;
    private List<TestResult> testResults;
    private List<XRayExamination> xRayExaminations;

    private MedicalTestService() {
        objectMapper = new ObjectMapper();
        loadMedicalTests();
        loadTestResults();
        loadXRayExaminations();
    }

    public static MedicalTestService getInstance() {
        if (instance == null) {
            instance = new MedicalTestService();
        }
        return instance;
    }

    private void loadMedicalTests() {
        File file = new File(MEDICAL_TESTS_FILE);
        if (file.exists()) {
            try {
                medicalTests = objectMapper.readValue(file,
                        objectMapper.getTypeFactory().constructCollectionType(List.class, MedicalTest.class));
            } catch (IOException e) {
                e.printStackTrace();
                createDefaultMedicalTests();
            }
        } else {
            createDefaultMedicalTests();
        }
    }

    private void loadTestResults() {
        File file = new File(TEST_RESULTS_FILE);
        if (file.exists()) {
            try {
                testResults = objectMapper.readValue(file,
                        objectMapper.getTypeFactory().constructCollectionType(List.class, TestResult.class));
            } catch (IOException e) {
                e.printStackTrace();
                testResults = new ArrayList<>();
            }
        } else {
            testResults = new ArrayList<>();
        }
    }

    private void loadXRayExaminations() {
        File file = new File(XRAY_EXAMINATIONS_FILE);
        if (file.exists()) {
            try {
                xRayExaminations = objectMapper.readValue(file,
                        objectMapper.getTypeFactory().constructCollectionType(List.class, XRayExamination.class));
            } catch (IOException e) {
                e.printStackTrace();
                xRayExaminations = new ArrayList<>();
            }
        } else {
            xRayExaminations = new ArrayList<>();
        }
    }

    private void createDefaultMedicalTests() {
        medicalTests = new ArrayList<>();

        try {
            // Create default medical tests
            MedicalTest test1 = new MedicalTest("Complete Blood Count (CBC)",
                    "Measures different components of blood", "Hematology",
                    75.0, "No food or drink for 8 hours before test", "30 minutes", true);

            MedicalTest test2 = new MedicalTest("Basic Metabolic Panel",
                    "Measures glucose, electrolytes, and kidney function", "Chemistry",
                    85.0, "No food or drink for 8 hours before test", "1 hour", true);

            MedicalTest test3 = new MedicalTest("Lipid Profile",
                    "Measures cholesterol levels", "Chemistry",
                    95.0, "No food or drink for 12 hours before test", "1 hour", true);

            MedicalTest test4 = new MedicalTest("Urinalysis",
                    "Analyzes urine composition", "Urology",
                    50.0, "Clean catch sample required", "45 minutes", false);

            MedicalTest test5 = new MedicalTest("Thyroid Function Test",
                    "Measures thyroid hormone levels", "Endocrinology",
                    120.0, "No preparation needed", "2 hours", false);

            MedicalTest test6 = new MedicalTest("COVID-19 PCR",
                    "Detects viral RNA in respiratory samples", "Infectious Disease",
                    100.0, "No preparation needed", "24-48 hours", false);

            medicalTests.add(test1);
            medicalTests.add(test2);
            medicalTests.add(test3);
            medicalTests.add(test4);
            medicalTests.add(test5);
            medicalTests.add(test6);

            saveMedicalTests();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Medical Tests methods
    public List<MedicalTest> getAllMedicalTests() {
        return medicalTests;
    }

    public List<MedicalTest> getMedicalTestsByCategory(String category) {
        return medicalTests.stream()
                .filter(t -> t.getCategory().equalsIgnoreCase(category))
                .collect(Collectors.toList());
    }

    public MedicalTest getMedicalTestById(String id) {
        return medicalTests.stream()
                .filter(t -> t.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public void addMedicalTest(MedicalTest test) throws IOException {
        if (test.getId() == null || test.getId().isEmpty()) {
            test.setId(UUID.randomUUID().toString());
        }
        medicalTests.add(test);
        saveMedicalTests();
    }

    public void updateMedicalTest(MedicalTest test) throws IOException {
        for (int i = 0; i < medicalTests.size(); i++) {
            if (medicalTests.get(i).getId().equals(test.getId())) {
                medicalTests.set(i, test);
                break;
            }
        }
        saveMedicalTests();
    }

    public void deleteMedicalTest(String id) throws IOException {
        medicalTests.removeIf(t -> t.getId().equals(id));
        saveMedicalTests();
    }

    // Test Results methods
    public List<TestResult> getAllTestResults() {
        return testResults;
    }

    public List<TestResult> getTestResultsByPatientId(String patientId) {
        return testResults.stream()
                .filter(r -> r.getPatientId().equals(patientId))
                .collect(Collectors.toList());
    }

    public List<TestResult> getTestResultsByDoctorId(String doctorId) {
        return testResults.stream()
                .filter(r -> r.getDoctorId().equals(doctorId))
                .collect(Collectors.toList());
    }

    public List<TestResult> getTestResultsByStatus(String status) {
        return testResults.stream()
                .filter(r -> r.getStatus().equals(status))
                .collect(Collectors.toList());
    }

    public TestResult getTestResultById(String id) {
        return testResults.stream()
                .filter(r -> r.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public void addTestResult(TestResult result) throws IOException {
        if (result.getId() == null || result.getId().isEmpty()) {
            result.setId(UUID.randomUUID().toString());
        }
        testResults.add(result);
        saveTestResults();
    }

    public void updateTestResult(TestResult result) throws IOException {
        for (int i = 0; i < testResults.size(); i++) {
            if (testResults.get(i).getId().equals(result.getId())) {
                testResults.set(i, result);
                break;
            }
        }
        saveTestResults();
    }

    public void deleteTestResult(String id) throws IOException {
        testResults.removeIf(r -> r.getId().equals(id));
        saveTestResults();
    }

    // X-Ray Examinations methods
    public List<XRayExamination> getAllXRayExaminations() {
        return xRayExaminations;
    }

    public List<XRayExamination> getXRayExaminationsByPatientId(String patientId) {
        return xRayExaminations.stream()
                .filter(x -> x.getPatientId().equals(patientId))
                .collect(Collectors.toList());
    }

    public List<XRayExamination> getXRayExaminationsByDoctorId(String doctorId) {
        return xRayExaminations.stream()
                .filter(x -> x.getRequestedByDoctorId().equals(doctorId))
                .collect(Collectors.toList());
    }

    public List<XRayExamination> getXRayExaminationsByStatus(String status) {
        return xRayExaminations.stream()
                .filter(x -> x.getStatus().equals(status))
                .collect(Collectors.toList());
    }

    public XRayExamination getXRayExaminationById(String id) {
        return xRayExaminations.stream()
                .filter(x -> x.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public void addXRayExamination(XRayExamination examination) throws IOException {
        if (examination.getId() == null || examination.getId().isEmpty()) {
            examination.setId(UUID.randomUUID().toString());
        }
        xRayExaminations.add(examination);
        saveXRayExaminations();
    }

    public void updateXRayExamination(XRayExamination examination) throws IOException {
        for (int i = 0; i < xRayExaminations.size(); i++) {
            if (xRayExaminations.get(i).getId().equals(examination.getId())) {
                xRayExaminations.set(i, examination);
                break;
            }
        }
        saveXRayExaminations();
    }

    public void deleteXRayExamination(String id) throws IOException {
        xRayExaminations.removeIf(x -> x.getId().equals(id));
        saveXRayExaminations();
    }

    // Save methods
    private void saveMedicalTests() throws IOException {
        File file = new File(MEDICAL_TESTS_FILE);
        file.getParentFile().mkdirs();
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, medicalTests);
    }

    private void saveTestResults() throws IOException {
        File file = new File(TEST_RESULTS_FILE);
        file.getParentFile().mkdirs();
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, testResults);
    }

    private void saveXRayExaminations() throws IOException {
        File file = new File(XRAY_EXAMINATIONS_FILE);
        file.getParentFile().mkdirs();
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, xRayExaminations);
    }
}