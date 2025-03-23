package com.hospital.model;

import java.util.UUID;

public class MedicalTest {
    private String id;
    private String name;
    private String description;
    private String category;
    private double cost;
    private String preparation;
    private String duration;
    private boolean fasting;

    public MedicalTest() {
        this.id = UUID.randomUUID().toString();
    }

    public MedicalTest(String name, String description, String category, double cost,
                       String preparation, String duration, boolean fasting) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.description = description;
        this.category = category;
        this.cost = cost;
        this.preparation = preparation;
        this.duration = duration;
        this.fasting = fasting;
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

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public String getPreparation() {
        return preparation;
    }

    public void setPreparation(String preparation) {
        this.preparation = preparation;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public boolean isFasting() {
        return fasting;
    }

    public void setFasting(boolean fasting) {
        this.fasting = fasting;
    }
}