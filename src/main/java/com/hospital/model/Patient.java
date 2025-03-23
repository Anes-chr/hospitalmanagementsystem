package com.hospital.model;

import java.util.UUID;

public abstract class Patient {
    protected String id;
    protected String name;
    protected int age;
    protected String gender;
    protected HospitalBlock location;
    protected String registrationDate;
    protected String contactNumber;
    protected String address;
    protected String bloodGroup;

    public Patient() {
        this.id = UUID.randomUUID().toString();
    }

    public Patient(String name, int age, String gender, HospitalBlock location,
                   String registrationDate, String contactNumber, String address, String bloodGroup) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.location = location;
        this.registrationDate = registrationDate;
        this.contactNumber = contactNumber;
        this.address = address;
        this.bloodGroup = bloodGroup;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public HospitalBlock getLocation() {
        return location;
    }

    public void setLocation(HospitalBlock location) {
        this.location = location;
    }

    public String getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(String registrationDate) {
        this.registrationDate = registrationDate;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public String getPatientType() {
        return this.getClass().getSimpleName();
    }

    public abstract void displayInfo();
    public abstract double calculateBill();
}