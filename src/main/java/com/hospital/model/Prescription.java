package com.hospital.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Prescription {
    private String id;
    private String patientId;
    private String doctorId;
    private String date;
    private List<PrescriptionItem> medications;
    private String notes;
    private boolean fulfilled;
    private String fulfilledDate;

    public Prescription() {
        this.id = UUID.randomUUID().toString();
        this.medications = new ArrayList<>();
        this.fulfilled = false;
    }

    public Prescription(String patientId, String doctorId, String date, String notes) {
        this.id = UUID.randomUUID().toString();
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.date = date;
        this.medications = new ArrayList<>();
        this.notes = notes;
        this.fulfilled = false;
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

    public List<PrescriptionItem> getMedications() {
        return medications;
    }

    public void setMedications(List<PrescriptionItem> medications) {
        this.medications = medications;
    }

    public void addMedication(Medication medication, int quantity, String dosage, String instructions) {
        medications.add(new PrescriptionItem(medication, quantity, dosage, instructions));
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isFulfilled() {
        return fulfilled;
    }

    public void setFulfilled(boolean fulfilled) {
        this.fulfilled = fulfilled;
    }

    public String getFulfilledDate() {
        return fulfilledDate;
    }

    public void setFulfilledDate(String fulfilledDate) {
        this.fulfilledDate = fulfilledDate;
    }

    public double calculateTotalCost() {
        double total = 0;
        for (PrescriptionItem item : medications) {
            total += item.getMedication().getUnitPrice() * item.getQuantity();
        }
        return total;
    }

    public static class PrescriptionItem {
        private Medication medication;
        private int quantity;
        private String dosage;
        private String instructions;

        public PrescriptionItem(Medication medication, int quantity, String dosage, String instructions) {
            this.medication = medication;
            this.quantity = quantity;
            this.dosage = dosage;
            this.instructions = instructions;
        }

        public Medication getMedication() {
            return medication;
        }

        public void setMedication(Medication medication) {
            this.medication = medication;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public String getDosage() {
            return dosage;
        }

        public void setDosage(String dosage) {
            this.dosage = dosage;
        }

        public String getInstructions() {
            return instructions;
        }

        public void setInstructions(String instructions) {
            this.instructions = instructions;
        }

        @Override
        public String toString() {
            return medication.getName() + " - " + dosage + " - " + instructions;
        }
    }
}