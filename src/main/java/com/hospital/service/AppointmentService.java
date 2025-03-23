package com.hospital.service;

import com.hospital.dao.AppointmentDao;
import com.hospital.model.Appointment;
import com.hospital.model.Doctor;
import com.hospital.util.DateUtil;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AppointmentService {
    private static AppointmentService instance;
    private final AppointmentDao appointmentDao;
    private final DoctorService doctorService;

    private AppointmentService() {
        appointmentDao = new AppointmentDao();
        doctorService = DoctorService.getInstance();
    }

    public static AppointmentService getInstance() {
        if (instance == null) {
            instance = new AppointmentService();
        }
        return instance;
    }

    public List<Appointment> getAllAppointments() {
        return appointmentDao.getAllEntities();
    }

    public List<Appointment> getAppointmentsByDoctorId(String doctorId) {
        if (doctorId == null || doctorId.isEmpty()) {
            return new ArrayList<>();
        }

        // Verify doctor exists
        Doctor doctor = doctorService.getDoctorById(doctorId);
        if (doctor == null) {
            return new ArrayList<>();
        }

        return appointmentDao.getAllEntities().stream()
                .filter(a -> a.getDoctorId().equals(doctorId))
                .collect(Collectors.toList());
    }

    public List<Appointment> getAppointmentsByPatientId(String patientId) {
        if (patientId == null || patientId.isEmpty()) {
            return new ArrayList<>();
        }

        return appointmentDao.getAllEntities().stream()
                .filter(a -> a.getPatientId().equals(patientId))
                .collect(Collectors.toList());
    }

    public List<Appointment> getTodayAppointments() {
        String today = DateUtil.formatDate(LocalDate.now());
        return appointmentDao.getAllEntities().stream()
                .filter(a -> a.getDate().equals(today))
                .collect(Collectors.toList());
    }

    public List<Appointment> getUpcomingAppointments() {
        String today = DateUtil.formatDate(LocalDate.now());
        return appointmentDao.getAllEntities().stream()
                .filter(a -> a.getDate().compareTo(today) >= 0)
                .filter(a -> !a.getStatus().equals("Cancelled") && !a.getStatus().equals("Completed"))
                .collect(Collectors.toList());
    }

    public boolean isTimeSlotAvailable(String doctorId, String date, String time) {
        // Check if doctor exists
        Doctor doctor = doctorService.getDoctorById(doctorId);
        if (doctor == null) {
            return false;
        }

        return appointmentDao.getAllEntities().stream()
                .filter(a -> a.getDoctorId().equals(doctorId))
                .filter(a -> a.getDate().equals(date))
                .filter(a -> a.getTime().equals(time))
                .filter(a -> !a.getStatus().equals("Cancelled"))
                .count() == 0;
    }

    public Appointment getAppointmentById(String id) {
        return appointmentDao.getById(id);
    }

    public void addAppointment(Appointment appointment) throws IOException {
        if (appointment.getStatus() == null || appointment.getStatus().isEmpty()) {
            appointment.setStatus("Scheduled");
        }
        appointmentDao.save(appointment);
    }

    public void updateAppointment(Appointment appointment) throws IOException {
        appointmentDao.update(appointment);
    }

    public void deleteAppointment(String id) throws IOException {
        appointmentDao.delete(id);
    }

    public boolean hasDoctorAppointments(String doctorId) {
        if (doctorId == null || doctorId.isEmpty()) {
            return false;
        }

        return appointmentDao.getAllEntities().stream()
                .anyMatch(a -> a.getDoctorId().equals(doctorId));
    }

    public void cleanupDeletedDoctorAppointments() {
        try {
            List<Appointment> appointments = appointmentDao.getAllEntities();
            List<Appointment> toUpdate = new ArrayList<>();

            for (Appointment appointment : appointments) {
                Doctor doctor = doctorService.getDoctorById(appointment.getDoctorId());
                if (doctor == null && !appointment.getStatus().equals("Cancelled")) {
                    appointment.setStatus("Cancelled");
                    toUpdate.add(appointment);
                }
            }

            for (Appointment appointment : toUpdate) {
                appointmentDao.update(appointment);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}