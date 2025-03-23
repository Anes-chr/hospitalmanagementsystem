package com.hospital.model;

public class InPatient extends Patient {
    private String roomNumber;
    private String admissionDate;
    private double dailyRate;
    private int numberOfDaysAdmitted;
    private String dischargeDate;
    private String attendingDoctor;

    public InPatient() {
        super();
    }

    public InPatient(String name, int age, String gender, HospitalBlock location,
                     String registrationDate, String contactNumber, String address,
                     String bloodGroup, String roomNumber, String admissionDate,
                     double dailyRate, int numberOfDaysAdmitted, String attendingDoctor) {
        super(name, age, gender, location, registrationDate, contactNumber, address, bloodGroup);
        this.roomNumber = roomNumber;
        this.admissionDate = admissionDate;
        this.dailyRate = dailyRate;
        this.numberOfDaysAdmitted = numberOfDaysAdmitted;
        this.attendingDoctor = attendingDoctor;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getAdmissionDate() {
        return admissionDate;
    }

    public void setAdmissionDate(String admissionDate) {
        this.admissionDate = admissionDate;
    }

    public double getDailyRate() {
        return dailyRate;
    }

    public void setDailyRate(double dailyRate) {
        this.dailyRate = dailyRate;
    }

    public int getNumberOfDaysAdmitted() {
        return numberOfDaysAdmitted;
    }

    public void setNumberOfDaysAdmitted(int numberOfDaysAdmitted) {
        this.numberOfDaysAdmitted = numberOfDaysAdmitted;
    }

    public String getDischargeDate() {
        return dischargeDate;
    }

    public void setDischargeDate(String dischargeDate) {
        this.dischargeDate = dischargeDate;
    }

    public String getAttendingDoctor() {
        return attendingDoctor;
    }

    public void setAttendingDoctor(String attendingDoctor) {
        this.attendingDoctor = attendingDoctor;
    }

    @Override
    public void displayInfo() {
        System.out.println("InPatient Details:");
        System.out.println("Name: " + name);
        System.out.println("Age: " + age);
        System.out.println("Gender: " + gender);
        System.out.println("Location: " + location.getFullLocation());
        System.out.println("Room Number: " + roomNumber);
        System.out.println("Admission Date: " + admissionDate);
        System.out.println("Attending Doctor: " + attendingDoctor);
        System.out.println("Daily Rate: $" + dailyRate);
        System.out.println("Days Admitted: " + numberOfDaysAdmitted);
        System.out.println("Total Bill: $" + calculateBill());
    }

    @Override
    public double calculateBill() {
        return dailyRate * numberOfDaysAdmitted;
    }
}