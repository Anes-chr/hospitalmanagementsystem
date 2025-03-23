package com.hospital.model;

public class EmergencyPatient extends Patient {
    private String severityLevel;
    private double emergencyTreatmentCost;
    private String emergencyContact;
    private String treatmentDetails;
    private String admissionTime;

    public EmergencyPatient() {
        super();
    }

    public EmergencyPatient(String name, int age, String gender, HospitalBlock location,
                            String registrationDate, String contactNumber, String address,
                            String bloodGroup, String severityLevel, double emergencyTreatmentCost,
                            String emergencyContact, String treatmentDetails, String admissionTime) {
        super(name, age, gender, location, registrationDate, contactNumber, address, bloodGroup);
        this.severityLevel = severityLevel;
        this.emergencyTreatmentCost = emergencyTreatmentCost;
        this.emergencyContact = emergencyContact;
        this.treatmentDetails = treatmentDetails;
        this.admissionTime = admissionTime;
    }

    public String getSeverityLevel() {
        return severityLevel;
    }

    public void setSeverityLevel(String severityLevel) {
        this.severityLevel = severityLevel;
    }

    public double getEmergencyTreatmentCost() {
        return emergencyTreatmentCost;
    }

    public void setEmergencyTreatmentCost(double emergencyTreatmentCost) {
        this.emergencyTreatmentCost = emergencyTreatmentCost;
    }

    public String getEmergencyContact() {
        return emergencyContact;
    }

    public void setEmergencyContact(String emergencyContact) {
        this.emergencyContact = emergencyContact;
    }

    public String getTreatmentDetails() {
        return treatmentDetails;
    }

    public void setTreatmentDetails(String treatmentDetails) {
        this.treatmentDetails = treatmentDetails;
    }

    public String getAdmissionTime() {
        return admissionTime;
    }

    public void setAdmissionTime(String admissionTime) {
        this.admissionTime = admissionTime;
    }

    @Override
    public void displayInfo() {
        System.out.println("Emergency Patient Details:");
        System.out.println("Name: " + name);
        System.out.println("Age: " + age);
        System.out.println("Gender: " + gender);
        System.out.println("Location: " + location.getFullLocation());
        System.out.println("Severity Level: " + severityLevel);
        System.out.println("Admission Time: " + admissionTime);
        System.out.println("Treatment Details: " + treatmentDetails);
        System.out.println("Emergency Contact: " + emergencyContact);
        System.out.println("Treatment Cost: $" + emergencyTreatmentCost);
    }

    @Override
    public double calculateBill() {
        // Apply additional charges based on severity
        double additionalCharge = 0;
        switch (severityLevel.toLowerCase()) {
            case "critical":
                additionalCharge = 500;
                break;
            case "high":
                additionalCharge = 300;
                break;
            case "medium":
                additionalCharge = 150;
                break;
            case "low":
                additionalCharge = 50;
                break;
        }
        return emergencyTreatmentCost + additionalCharge;
    }
}