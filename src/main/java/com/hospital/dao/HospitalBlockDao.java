package com.hospital.dao;

import com.hospital.model.HospitalBlock;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class HospitalBlockDao extends JsonDao<HospitalBlock> {
    private static final String FILE_PATH = "data/blocks.json";

    public HospitalBlockDao() {
        super(FILE_PATH, HospitalBlock.class);
    }

    @Override
    public HospitalBlock getById(String blockName) {
        List<HospitalBlock> blocks = getAllEntities();
        return blocks.stream()
                .filter(block -> block.getBlockName().equals(blockName))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void save(HospitalBlock block) throws IOException {
        List<HospitalBlock> blocks = getAllEntities();

        // Check if block already exists
        boolean exists = blocks.stream()
                .anyMatch(b -> b.getBlockName().equals(block.getBlockName()));

        if (!exists) {
            blocks.add(block);
            saveEntities(blocks);
        } else {
            update(block);
        }
    }

    @Override
    public void update(HospitalBlock block) throws IOException {
        List<HospitalBlock> blocks = getAllEntities();

        for (int i = 0; i < blocks.size(); i++) {
            if (blocks.get(i).getBlockName().equals(block.getBlockName())) {
                blocks.set(i, block);
                break;
            }
        }

        saveEntities(blocks);
    }

    @Override
    public void delete(String blockName) throws IOException {
        List<HospitalBlock> blocks = getAllEntities();
        List<HospitalBlock> updatedBlocks = blocks.stream()
                .filter(block -> !block.getBlockName().equals(blockName))
                .collect(Collectors.toList());

        saveEntities(updatedBlocks);
    }

    public List<HospitalBlock> getBlocksBySpecialty(String specialty) {
        List<HospitalBlock> blocks = getAllEntities();
        return blocks.stream()
                .filter(block -> block.getSpecialty().equalsIgnoreCase(specialty))
                .collect(Collectors.toList());
    }
}