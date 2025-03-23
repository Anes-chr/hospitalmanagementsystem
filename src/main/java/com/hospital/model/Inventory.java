package com.hospital.model;

import java.util.UUID;

public class Inventory {
    private String id;
    private String itemName;
    private String category;
    private int quantity;
    private double unitCost;
    private String supplier;
    private String purchaseDate;
    private String expiryDate;
    private String storageLocation;
    private int minStockLevel;
    private int maxStockLevel;

    public Inventory() {
        this.id = UUID.randomUUID().toString();
    }

    public Inventory(String itemName, String category, int quantity, double unitCost,
                     String supplier, String purchaseDate, String expiryDate,
                     String storageLocation, int minStockLevel, int maxStockLevel) {
        this.id = UUID.randomUUID().toString();
        this.itemName = itemName;
        this.category = category;
        this.quantity = quantity;
        this.unitCost = unitCost;
        this.supplier = supplier;
        this.purchaseDate = purchaseDate;
        this.expiryDate = expiryDate;
        this.storageLocation = storageLocation;
        this.minStockLevel = minStockLevel;
        this.maxStockLevel = maxStockLevel;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(double unitCost) {
        this.unitCost = unitCost;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public String getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(String purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getStorageLocation() {
        return storageLocation;
    }

    public void setStorageLocation(String storageLocation) {
        this.storageLocation = storageLocation;
    }

    public int getMinStockLevel() {
        return minStockLevel;
    }

    public void setMinStockLevel(int minStockLevel) {
        this.minStockLevel = minStockLevel;
    }

    public int getMaxStockLevel() {
        return maxStockLevel;
    }

    public void setMaxStockLevel(int maxStockLevel) {
        this.maxStockLevel = maxStockLevel;
    }

    public boolean isLowStock() {
        return quantity <= minStockLevel;
    }

    public boolean isOverStocked() {
        return quantity >= maxStockLevel;
    }

    public double getTotalValue() {
        return quantity * unitCost;
    }
}