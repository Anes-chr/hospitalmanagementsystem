package com.hospital.model;

public class Doctor extends User {
    private String specialization;
    private String licenseNumber;
    private HospitalBlock assignedBlock;
    private String contactNumber;
    private String workingHours;
    private boolean available;

    public Doctor() {
        super();
        super.setRole("DOCTOR");
    }

    public Doctor(String username, String password, String fullName, String email,
                  String specialization, String licenseNumber, HospitalBlock assignedBlock,
                  String contactNumber, String workingHours) {
        super(username, password, fullName, email, "DOCTOR");
        this.specialization = specialization;
        this.licenseNumber = licenseNumber;
        this.assignedBlock = assignedBlock;
        this.contactNumber = contactNumber;
        this.workingHours = workingHours;
        this.available = true;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public HospitalBlock getAssignedBlock() {
        return assignedBlock;
    }

    public void setAssignedBlock(HospitalBlock assignedBlock) {
        this.assignedBlock = assignedBlock;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getWorkingHours() {
        return workingHours;
    }

    public void setWorkingHours(String workingHours) {
        this.workingHours = workingHours;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}