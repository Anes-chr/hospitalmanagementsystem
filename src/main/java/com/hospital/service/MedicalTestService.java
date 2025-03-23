package com.hospital.service;

import com.hospital.dao.MedicalTestDao;
import javafx.scene.layout.HBox;
import com.hospital.dao.TestResultDao;
import com.hospital.dao.XRayExaminationDao;
import com.hospital.model.MedicalTest;
import com.hospital.model.TestResult;
import com.hospital.model.XRayExamination;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class MedicalTestService {
    private static MedicalTestService instance;
    private final MedicalTestDao medicalTestDao;
    private final TestResultDao testResultDao;
    private final XRayExaminationDao xRayExaminationDao;

    private MedicalTestService() {
        medicalTestDao = new MedicalTestDao();
        testResultDao = new TestResultDao();
        xRayExaminationDao = new XRayExaminationDao();
        initializeDefaultTests();
    }

    public static MedicalTestService getInstance() {
        if (instance == null) {
            instance = new MedicalTestService();
        }
        return instance;
    }

    private void initializeDefaultTests() {
        List<MedicalTest> tests = getAllMedicalTests();
        if (tests.isEmpty()) {
            try {
                // Add some default medical tests
                MedicalTest cbc = new MedicalTest(
                        "Complete Blood Count",
                        "Measures various components of the blood including red and white blood cells, platelets, etc.",
                        "Hematology",
                        45.0,
                        "No special preparation required.",
                        "15-30 minutes",
                        true
                );

                MedicalTest urinalysis = new MedicalTest(
                        "Urinalysis",
                        "Analyzes urine characteristics and components.",
                        "Urology",
                        35.0,
                        "Collect midstream urine sample.",
                        "10-15 minutes",
                        false
                );

                MedicalTest lipidPanel = new MedicalTest(
                        "Lipid Panel",
                        "Measures cholesterol and triglycerides.",
                        "Chemistry",
                        65.0,
                        "Fast for 12 hours before the test.",
                        "30 minutes",
                        true
                );

                MedicalTest ecg = new MedicalTest(
                        "Electrocardiogram (ECG)",
                        "Records electrical activity of the heart.",
                        "Cardiology",
                        120.0,
                        "Wear loose-fitting clothing.",
                        "15-20 minutes",
                        false
                );

                addMedicalTest(cbc);
                addMedicalTest(urinalysis);
                addMedicalTest(lipidPanel);
                addMedicalTest(ecg);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public List<MedicalTest> getAllMedicalTests() {
        return medicalTestDao.getAllEntities();
    }

    public MedicalTest getMedicalTestById(String id) {
        return medicalTestDao.getById(id);
    }

    public List<MedicalTest> getMedicalTestsByCategory(String category) {
        return getAllMedicalTests().stream()
                .filter(test -> test.getCategory().equalsIgnoreCase(category))
                .collect(Collectors.toList());
    }

    public void addMedicalTest(MedicalTest medicalTest) throws IOException {
        medicalTestDao.save(medicalTest);
    }

    public void updateMedicalTest(MedicalTest medicalTest) throws IOException {
        medicalTestDao.update(medicalTest);
    }

    public void deleteMedicalTest(String id) throws IOException {
        medicalTestDao.delete(id);
    }

    // Test Results methods
    public List<TestResult> getAllTestResults() {
        return testResultDao.getAllEntities();
    }

    public TestResult getTestResultById(String id) {
        return testResultDao.getById(id);
    }

    public List<TestResult> getTestResultsByPatientId(String patientId) {
        return getAllTestResults().stream()
                .filter(result -> result.getPatientId().equals(patientId))
                .collect(Collectors.toList());
    }

    public List<TestResult> getTestResultsByDoctorId(String doctorId) {
        return getAllTestResults().stream()
                .filter(result -> result.getDoctorId().equals(doctorId))
                .collect(Collectors.toList());
    }

    public void addTestResult(TestResult testResult) throws IOException {
        testResultDao.save(testResult);
    }

    public void updateTestResult(TestResult testResult) throws IOException {
        testResultDao.update(testResult);
    }

    public void deleteTestResult(String id) throws IOException {
        testResultDao.delete(id);
    }

    // X-Ray Examination methods
    public List<XRayExamination> getAllXRayExaminations() {
        return xRayExaminationDao.getAllEntities();
    }

    public XRayExamination getXRayExaminationById(String id) {
        return xRayExaminationDao.getById(id);
    }

    public List<XRayExamination> getXRayExaminationsByPatientId(String patientId) {
        return getAllXRayExaminations().stream()
                .filter(xray -> xray.getPatientId().equals(patientId))
                .collect(Collectors.toList());
    }

    public List<XRayExamination> getXRayExaminationsByDoctorId(String doctorId) {
        return getAllXRayExaminations().stream()
                .filter(xray -> xray.getRequestedByDoctorId().equals(doctorId))
                .collect(Collectors.toList());
    }

    public void addXRayExamination(XRayExamination xRayExamination) throws IOException {
        xRayExaminationDao.save(xRayExamination);
    }

    public void updateXRayExamination(XRayExamination xRayExamination) throws IOException {
        xRayExaminationDao.update(xRayExamination);
    }

    public void deleteXRayExamination(String id) throws IOException {
        xRayExaminationDao.delete(id);
    }
}