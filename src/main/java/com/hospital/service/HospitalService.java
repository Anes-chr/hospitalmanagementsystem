package com.hospital.service;

import com.hospital.dao.HospitalBlockDao;
import com.hospital.dao.HospitalDao;
import com.hospital.model.Hospital;
import com.hospital.model.HospitalBlock;

import java.io.IOException;
import java.util.List;

public class HospitalService {
    private final HospitalDao hospitalDao;
    private final HospitalBlockDao blockDao;
    private static HospitalService instance;
    private Hospital currentHospital;

    private HospitalService() {
        this.hospitalDao = new HospitalDao();
        this.blockDao = new HospitalBlockDao();
        loadHospitalData();
    }

    public static HospitalService getInstance() {
        if (instance == null) {
            instance = new HospitalService();
        }
        return instance;
    }

    private void loadHospitalData() {
        if (hospitalDao.hospitalExists()) {
            currentHospital = hospitalDao.getById(null);
        } else {
            // Create default hospital
            currentHospital = new Hospital("Metro General Hospital", 500, "New York");

            try {
                // Add default blocks
                currentHospital.addBlock(new HospitalBlock("A", 1, "General Medicine"));
                currentHospital.addBlock(new HospitalBlock("B", 2, "Cardiology"));
                currentHospital.addBlock(new HospitalBlock("C", 1, "Pediatrics"));
                currentHospital.addBlock(new HospitalBlock("D", 3, "Surgery"));
                currentHospital.addBlock(new HospitalBlock("E", 1, "Emergency"));

                saveHospital(currentHospital);

                // Also save blocks individually
                for (HospitalBlock block : currentHospital.getBlocks()) {
                    blockDao.save(block);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Hospital getCurrentHospital() {
        return currentHospital;
    }

    public void saveHospital(Hospital hospital) throws IOException {
        hospitalDao.save(hospital);
        this.currentHospital = hospital;
    }

    public List<HospitalBlock> getAllBlocks() {
        return blockDao.getAllEntities();
    }

    public void addBlock(HospitalBlock block) throws IOException {
        blockDao.save(block);
        currentHospital.addBlock(block);
        hospitalDao.update(currentHospital);
    }

    public void updateBlock(HospitalBlock block) throws IOException {
        blockDao.update(block);

        // Update in current hospital
        List<HospitalBlock> blocks = currentHospital.getBlocks();
        for (int i = 0; i < blocks.size(); i++) {
            if (blocks.get(i).getBlockName().equals(block.getBlockName())) {
                blocks.set(i, block);
                break;
            }
        }

        hospitalDao.update(currentHospital);
    }

    public void deleteBlock(String blockName) throws IOException {
        blockDao.delete(blockName);

        // Remove from current hospital
        currentHospital.getBlocks().removeIf(block -> block.getBlockName().equals(blockName));
        hospitalDao.update(currentHospital);
    }

    public HospitalBlock getBlockByName(String blockName) {
        return blockDao.getById(blockName);
    }
}