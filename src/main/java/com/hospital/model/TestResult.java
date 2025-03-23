package com.hospital.model;

import java.util.UUID;

public class TestResult {
    private String id;
    private String patientId;
    private String doctorId;
    private String testId;
    private String date;
    private String status;
    private String results;
    private String notes;

    public TestResult() {
        this.id = UUID.randomUUID().toString();
        this.status = "Pending";
    }

    public TestResult(String patientId, String doctorId, String testId, String date) {
        this.id = UUID.randomUUID().toString();
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.testId = testId;
        this.date = date;
        this.status = "Pending";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }

    public String getTestId() {
        return testId;
    }

    public void setTestId(String testId) {
        this.testId = testId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getResults() {
        return results;
    }

    public void setResults(String results) {
        this.results = results;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}