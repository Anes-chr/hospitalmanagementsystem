package com.hospital.dao;

import com.hospital.model.Appointment;

import java.io.IOException;
import java.util.List;

public class AppointmentDao extends JsonDao<Appointment> {
    private static final String APPOINTMENTS_FILE = "data/appointments.json";

    public AppointmentDao() {
        super(APPOINTMENTS_FILE, Appointment.class);
    }

    @Override
    protected String getId(Appointment appointment) {
        return appointment.getId();
    }

    @Override
    public List<Appointment> getAllEntities() {
        List<Appointment> appointments = super.getAllEntities();
        return appointments;
    }

    @Override
    public void save(Appointment appointment) throws IOException {
        super.save(appointment);
    }

    @Override
    public void update(Appointment updatedAppointment) throws IOException {
        super.update(updatedAppointment);
    }

    @Override
    public void delete(String appointmentId) throws IOException {
        super.delete(appointmentId);
    }
}