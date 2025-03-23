package com.hospital.model;

import java.util.UUID;

public class Appointment {
    private String id;
    private String patientId;
    private String doctorId;
    private String date;
    private String time;
    private String duration;
    private String purpose;
    private HospitalBlock location;
    private String notes;
    private String status;

    public Appointment() {
        this.id = UUID.randomUUID().toString();
        this.status = "Scheduled";
    }

    public Appointment(String patientId, String doctorId, String date, String time,
                       String duration, String purpose, HospitalBlock location) {
        this(patientId, doctorId, date, time, duration, purpose, location, "");
    }

    public Appointment(String patientId, String doctorId, String date, String time,
                       String duration, String purpose, HospitalBlock location, String notes) {
        this.id = UUID.randomUUID().toString();
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.date = date;
        this.time = time;
        this.duration = duration;
        this.purpose = purpose;
        this.location = location;
        this.notes = notes;
        this.status = "Scheduled";
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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public HospitalBlock getLocation() {
        return location;
    }

    public void setLocation(HospitalBlock location) {
        this.location = location;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}