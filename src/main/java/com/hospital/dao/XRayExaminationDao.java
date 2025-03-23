package com.hospital.dao;

import com.hospital.model.XRayExamination;

import java.io.IOException;
import java.util.List;

public class XRayExaminationDao extends JsonDao<XRayExamination> {
    private static final String XRAY_EXAMINATIONS_FILE = "data/xray_examinations.json";

    public XRayExaminationDao() {
        super(XRAY_EXAMINATIONS_FILE, XRayExamination.class);
    }

    @Override
    protected String getId(XRayExamination xRayExamination) {
        return xRayExamination.getId();
    }

    @Override
    public List<XRayExamination> getAllEntities() {
        List<XRayExamination> xRayExaminations = super.getAllEntities();
        return xRayExaminations;
    }

    @Override
    public void save(XRayExamination xRayExamination) throws IOException {
        super.save(xRayExamination);
    }

    @Override
    public void update(XRayExamination updatedXRay) throws IOException {
        super.update(updatedXRay);
    }

    @Override
    public void delete(String xRayId) throws IOException {
        super.delete(xRayId);
    }
}