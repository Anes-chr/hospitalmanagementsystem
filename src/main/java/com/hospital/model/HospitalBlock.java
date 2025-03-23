package com.hospital.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HospitalBlock {
    private String blockName;
    private int floorNumber;
    private String specialty;
    // This field isn't a property but calculated on-demand
    private transient String fullLocation;

    public HospitalBlock() {
        // Default constructor needed for Jackson
    }

    public HospitalBlock(String blockName, int floorNumber, String specialty) {
        this.blockName = blockName;
        this.floorNumber = floorNumber;
        this.specialty = specialty;
    }

    public String getBlockName() {
        return blockName;
    }

    public void setBlockName(String blockName) {
        this.blockName = blockName;
    }

    public int getFloorNumber() {
        return floorNumber;
    }

    public void setFloorNumber(int floorNumber) {
        this.floorNumber = floorNumber;
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public String getFullLocation() {
        return "Block " + blockName + ", Floor " + floorNumber + ", " + specialty;
    }

    @Override
    public String toString() {
        return getFullLocation();
    }
}