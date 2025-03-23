package com.hospital.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.model.Appointment;
import com.hospital.model.Doctor;
import com.hospital.model.Patient;
import com.hospital.util.DateUtil;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class AppointmentService {
    private static AppointmentService instance;
    private final String APPOINTMENTS_FILE = "data/appointments.json";
    private final ObjectMapper objectMapper;
    private List<Appointment> appointments;

    private AppointmentService() {
        objectMapper = new ObjectMapper();
        loadAppointments();
    }

    public static AppointmentService getInstance() {
        if (instance == null) {
            instance = new AppointmentService();
        }
        return instance;
    }

    private void loadAppointments() {
        File file = new File(APPOINTMENTS_FILE);
        if (file.exists()) {
            try {
                appointments = objectMapper.readValue(file,
                        objectMapper.getTypeFactory().constructCollectionType(List.class, Appointment.class));
            } catch (IOException e) {
                e.printStackTrace();
                appointments = new ArrayList<>();
            }
        } else {
            appointments = new ArrayList<>();
        }
    }

    public List<Appointment> getAllAppointments() {
        return appointments;
    }

    public List<Appointment> getAppointmentsByPatientId(String patientId) {
        return appointments.stream()
                .filter(a -> a.getPatientId().equals(patientId))
                .collect(Collectors.toList());
    }

    public List<Appointment> getAppointmentsByDoctorId(String doctorId) {
        return appointments.stream()
                .filter(a -> a.getDoctorId().equals(doctorId))
                .collect(Collectors.toList());
    }

    public List<Appointment> getAppointmentsByDate(String date) {
        return appointments.stream()
                .filter(a -> a.getDate().equals(date))
                .collect(Collectors.toList());
    }

    public List<Appointment> getTodayAppointments() {
        String today = DateUtil.formatDate(LocalDate.now());
        return getAppointmentsByDate(today);
    }

    public List<Appointment> getUpcomingAppointments() {
        String today = DateUtil.formatDate(LocalDate.now());
        return appointments.stream()
                .filter(a -> a.getDate().compareTo(today) >= 0 &&
                        !a.getStatus().equals("Cancelled") &&
                        !a.getStatus().equals("Completed"))
                .collect(Collectors.toList());
    }

    public Appointment getAppointmentById(String id) {
        return appointments.stream()
                .filter(a -> a.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public void addAppointment(Appointment appointment) throws IOException {
        if (appointment.getId() == null || appointment.getId().isEmpty()) {
            appointment.setId(UUID.randomUUID().toString());
        }
        appointments.add(appointment);
        saveAppointments();
    }

    public void updateAppointment(Appointment appointment) throws IOException {
        for (int i = 0; i < appointments.size(); i++) {
            if (appointments.get(i).getId().equals(appointment.getId())) {
                appointments.set(i, appointment);
                break;
            }
        }
        saveAppointments();
    }

    public void deleteAppointment(String id) throws IOException {
        appointments.removeIf(a -> a.getId().equals(id));
        saveAppointments();
    }

    public boolean isTimeSlotAvailable(String doctorId, String date, String time) {
        return appointments.stream()
                .noneMatch(a -> a.getDoctorId().equals(doctorId) &&
                        a.getDate().equals(date) &&
                        a.getTime().equals(time) &&
                        !a.getStatus().equals("Cancelled"));
    }

    private void saveAppointments() throws IOException {
        File file = new File(APPOINTMENTS_FILE);
        file.getParentFile().mkdirs();
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, appointments);
    }
}