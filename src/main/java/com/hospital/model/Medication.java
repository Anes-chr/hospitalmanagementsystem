package com.hospital.model;

import java.util.UUID;

public class Medication {
    private String id;
    private String name;
    private String description;
    private String category;
    private double unitPrice;
    private int stockQuantity;
    private String manufacturer;
    private String expiryDate;
    private String dosageForm; // tablets, liquid, injection, etc.
    private boolean prescription; // requires prescription or not

    public Medication() {
        this.id = UUID.randomUUID().toString();
    }

    public Medication(String name, String description, String category, double unitPrice,
                      int stockQuantity, String manufacturer, String expiryDate,
                      String dosageForm, boolean prescription) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.description = description;
        this.category = category;
        this.unitPrice = unitPrice;
        this.stockQuantity = stockQuantity;
        this.manufacturer = manufacturer;
        this.expiryDate = expiryDate;
        this.dosageForm = dosageForm;
        this.prescription = prescription;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getDosageForm() {
        return dosageForm;
    }

    public void setDosageForm(String dosageForm) {
        this.dosageForm = dosageForm;
    }

    public boolean isPrescription() {
        return prescription;
    }

    public void setPrescription(boolean prescription) {
        this.prescription = prescription;
    }

    public boolean isLowStock() {
        return stockQuantity < 10;
    }

    public boolean isOutOfStock() {
        return stockQuantity <= 0;
    }

    @Override
    public String toString() {
        return name + " (" + dosageForm + ") - $" + unitPrice;
    }
}