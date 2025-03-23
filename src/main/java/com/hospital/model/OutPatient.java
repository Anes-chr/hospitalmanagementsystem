package com.hospital.model;

public class OutPatient extends Patient {
    private String appointmentDate;
    private double consultFee;
    private String consultingDoctor;
    private String diagnosis;

    public OutPatient() {
        super();
    }

    public OutPatient(String name, int age, String gender, HospitalBlock location,
                      String registrationDate, String contactNumber, String address,
                      String bloodGroup, String appointmentDate, double consultFee,
                      String consultingDoctor, String diagnosis) {
        super(name, age, gender, location, registrationDate, contactNumber, address, bloodGroup);
        this.appointmentDate = appointmentDate;
        this.consultFee = consultFee;
        this.consultingDoctor = consultingDoctor;
        this.diagnosis = diagnosis;
    }

    public String getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(String appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public double getConsultFee() {
        return consultFee;
    }

    public void setConsultFee(double consultFee) {
        this.consultFee = consultFee;
    }

    public String getConsultingDoctor() {
        return consultingDoctor;
    }

    public void setConsultingDoctor(String consultingDoctor) {
        this.consultingDoctor = consultingDoctor;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    @Override
    public void displayInfo() {
        System.out.println("OutPatient Details:");
        System.out.println("Name: " + name);
        System.out.println("Age: " + age);
        System.out.println("Gender: " + gender);
        System.out.println("Location: " + location.getFullLocation());
        System.out.println("Appointment Date: " + appointmentDate);
        System.out.println("Consulting Doctor: " + consultingDoctor);
        System.out.println("Diagnosis: " + diagnosis);
        System.out.println("Consultation Fee: $" + consultFee);
    }

    @Override
    public double calculateBill() {
        return consultFee;
    }
}