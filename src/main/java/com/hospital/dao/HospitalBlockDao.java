package com.hospital.dao;

import com.hospital.model.HospitalBlock;

import java.io.IOException;
import java.util.List;

public class HospitalBlockDao extends JsonDao<HospitalBlock> {
    private static final String BLOCKS_FILE = "data/hospital_blocks.json";

    public HospitalBlockDao() {
        super(BLOCKS_FILE, HospitalBlock.class);
    }

    @Override
    protected String getId(HospitalBlock block) {
        return block.getBlockName(); // Using blockName as the ID
    }

    @Override
    public HospitalBlock getById(String blockName) {
        List<HospitalBlock> blocks = getAllEntities();
        return blocks.stream()
                .filter(b -> b.getBlockName().equals(blockName))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void save(HospitalBlock block) throws IOException {
        List<HospitalBlock> blocks = getAllEntities();

        // Check if the block already exists
        boolean exists = blocks.stream()
                .anyMatch(b -> b.getBlockName().equals(block.getBlockName()));

        if (!exists) {
            blocks.add(block);
            saveAllEntities(blocks);
        } else {
            // Update the existing block
            update(block);
        }
    }

    @Override
    public void update(HospitalBlock updatedBlock) throws IOException {
        List<HospitalBlock> blocks = getAllEntities();

        boolean found = false;
        for (int i = 0; i < blocks.size(); i++) {
            if (blocks.get(i).getBlockName().equals(updatedBlock.getBlockName())) {
                blocks.set(i, updatedBlock);
                found = true;
                break;
            }
        }

        if (found) {
            saveAllEntities(blocks);
        }
    }

    @Override
    public void delete(String blockName) throws IOException {
        List<HospitalBlock> blocks = getAllEntities();
        blocks.removeIf(block -> block.getBlockName().equals(blockName));
        saveAllEntities(blocks);
    }
}