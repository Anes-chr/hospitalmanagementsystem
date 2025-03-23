package com.hospital.model;

import java.util.UUID;

public class XRayExamination {
    private String id;
    private String patientId;
    private String requestedByDoctorId;
    private String date;
    private String bodyPart;
    private String reason;
    private String status;
    private String results;
    private String imagePath;
    private String technician;
    private String scheduledTime;

    public XRayExamination() {
        this.id = UUID.randomUUID().toString();
        this.status = "Requested";
    }

    public XRayExamination(String patientId, String requestedByDoctorId, String date,
                           String bodyPart, String reason) {
        this.id = UUID.randomUUID().toString();
        this.patientId = patientId;
        this.requestedByDoctorId = requestedByDoctorId;
        this.date = date;
        this.bodyPart = bodyPart;
        this.reason = reason;
        this.status = "Requested";
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

    public String getRequestedByDoctorId() {
        return requestedByDoctorId;
    }

    public void setRequestedByDoctorId(String requestedByDoctorId) {
        this.requestedByDoctorId = requestedByDoctorId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getBodyPart() {
        return bodyPart;
    }

    public void setBodyPart(String bodyPart) {
        this.bodyPart = bodyPart;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Missing methods that are needed by XRayUpdateFormController
    public String getResults() {
        return results;
    }

    public void setResults(String results) {
        this.results = results;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getTechnician() {
        return technician;
    }

    public void setTechnician(String technician) {
        this.technician = technician;
    }

    public String getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(String scheduledTime) {
        this.scheduledTime = scheduledTime;
    }
}