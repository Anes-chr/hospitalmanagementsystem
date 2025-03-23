package com.hospital.model;

import java.util.ArrayList;
import java.util.List;

public class Hospital {
    private String name;
    private int maxPatients;
    private String state;
    private List<HospitalBlock> blocks;

    public Hospital() {
        blocks = new ArrayList<>();
    }

    public Hospital(String name, int maxPatients, String state) {
        this.name = name;
        this.maxPatients = maxPatients;
        this.state = state;
        this.blocks = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMaxPatients() {
        return maxPatients;
    }

    public void setMaxPatients(int maxPatients) {
        this.maxPatients = maxPatients;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public List<HospitalBlock> getBlocks() {
        return blocks;
    }

    public void setBlocks(List<HospitalBlock> blocks) {
        this.blocks = blocks;
    }

    public void addBlock(HospitalBlock block) {
        this.blocks.add(block);
    }

    public String getHospitalInfo() {
        return "Hospital: " + name + "\nState: " + state + "\nMaximum Patients: " + maxPatients;
    }

    public void displayBlocksInfo() {
        for (HospitalBlock block : blocks) {
            System.out.println(block.toString());
        }
    }

    @Override
    public String toString() {
        return "Hospital{" +
                "name='" + name + '\'' +
                ", maxPatients=" + maxPatients +
                ", state='" + state + '\'' +
                ", blocksCount=" + blocks.size() +
                '}';
    }
}