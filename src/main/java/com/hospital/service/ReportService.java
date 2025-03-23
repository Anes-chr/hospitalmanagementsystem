package com.hospital.service;

import com.hospital.model.*;
import com.hospital.util.DateUtil;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class ReportService {
    private static ReportService instance;
    private final PatientService patientService;
    private final AppointmentService appointmentService;
    private final PrescriptionService prescriptionService;
    private final MedicationService medicationService;
    private final MedicalTestService medicalTestService;
    private final DoctorService doctorService;

    private ReportService() {
        patientService = new PatientService();
        appointmentService = AppointmentService.getInstance();
        prescriptionService = PrescriptionService.getInstance();
        medicationService = MedicationService.getInstance();
        medicalTestService = MedicalTestService.getInstance();
        doctorService = DoctorService.getInstance();
    }

    public static ReportService getInstance() {
        if (instance == null) {
            instance = new ReportService();
        }
        return instance;
    }

    // Patient reports
    public Map<String, Integer> getPatientCountByType() {
        List<Patient> patients = patientService.getAllPatients();
        Map<String, Integer> countMap = new HashMap<>();

        for (Patient patient : patients) {
            String type = patient.getPatientType();
            countMap.put(type, countMap.getOrDefault(type, 0) + 1);
        }

        return countMap;
    }

    public Map<String, Integer> getPatientCountByGender() {
        List<Patient> patients = patientService.getAllPatients();
        Map<String, Integer> countMap = new HashMap<>();

        for (Patient patient : patients) {
            String gender = patient.getGender();
            countMap.put(gender, countMap.getOrDefault(gender, 0) + 1);
        }

        return countMap;
    }

    public Map<String, Integer> getPatientAgeDistribution() {
        List<Patient> patients = patientService.getAllPatients();
        Map<String, Integer> ageGroups = new HashMap<>();

        for (Patient patient : patients) {
            int age = patient.getAge();
            String ageGroup;

            if (age < 10) ageGroup = "0-9";
            else if (age < 20) ageGroup = "10-19";
            else if (age < 30) ageGroup = "20-29";
            else if (age < 40) ageGroup = "30-39";
            else if (age < 50) ageGroup = "40-49";
            else if (age < 60) ageGroup = "50-59";
            else if (age < 70) ageGroup = "60-69";
            else if (age < 80) ageGroup = "70-79";
            else ageGroup = "80+";

            ageGroups.put(ageGroup, ageGroups.getOrDefault(ageGroup, 0) + 1);
        }

        return ageGroups;
    }

    // Financial reports
    public Map<String, Double> getRevenueByPatientType() {
        List<Patient> patients = patientService.getAllPatients();
        Map<String, Double> revenueMap = new HashMap<>();

        for (Patient patient : patients) {
            String type = patient.getPatientType();
            double bill = patient.calculateBill();
            revenueMap.put(type, revenueMap.getOrDefault(type, 0.0) + bill);
        }

        return revenueMap;
    }

    public Map<String, Double> getRevenueByMonth(int year) {
        List<Patient> patients = patientService.getAllPatients();
        Map<String, Double> revenueByMonth = new HashMap<>();

        // Initialize all months
        for (int i = 1; i <= 12; i++) {
            String month = String.format("%02d", i);
            revenueByMonth.put(month, 0.0);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (Patient patient : patients) {
            try {
                LocalDate date = LocalDate.parse(patient.getRegistrationDate(), formatter);
                if (date.getYear() == year) {
                    String month = String.format("%02d", date.getMonthValue());
                    double bill = patient.calculateBill();
                    revenueByMonth.put(month, revenueByMonth.getOrDefault(month, 0.0) + bill);
                }
            } catch (Exception e) {
                // Skip if date can't be parsed
                System.err.println("Error parsing date: " + patient.getRegistrationDate());
            }
        }

        return revenueByMonth;
    }

    // Medication reports
    public List<Medication> getLowStockReport() {
        return medicationService.getLowStockMedications();
    }

    public Map<String, Double> getMedicationsByCategory() {
        List<Medication> medications = medicationService.getAllMedications();
        Map<String, Double> categoryMap = new HashMap<>();

        for (Medication medication : medications) {
            String category = medication.getCategory();
            double value = medication.getUnitPrice() * medication.getStockQuantity();
            categoryMap.put(category, categoryMap.getOrDefault(category, 0.0) + value);
        }

        return categoryMap;
    }

    // Appointment reports
    public Map<String, Integer> getAppointmentsByDay(String startDate, String endDate) {
        List<Appointment> appointments = appointmentService.getAllAppointments().stream()
                .filter(a -> a.getDate().compareTo(startDate) >= 0 && a.getDate().compareTo(endDate) <= 0)
                .collect(Collectors.toList());

        Map<String, Integer> appointmentsByDay = new HashMap<>();

        for (Appointment appointment : appointments) {
            String date = appointment.getDate();
            appointmentsByDay.put(date, appointmentsByDay.getOrDefault(date, 0) + 1);
        }

        return appointmentsByDay;
    }

    public Map<String, Integer> getAppointmentsByStatus() {
        List<Appointment> appointments = appointmentService.getAllAppointments();
        Map<String, Integer> statusMap = new HashMap<>();

        for (Appointment appointment : appointments) {
            String status = appointment.getStatus();
            statusMap.put(status, statusMap.getOrDefault(status, 0) + 1);
        }

        return statusMap;
    }

    // Doctor reports
    public Map<String, Integer> getAppointmentsByDoctor() {
        List<Appointment> appointments = appointmentService.getAllAppointments();
        Map<String, Integer> doctorMap = new HashMap<>();

        for (Appointment appointment : appointments) {
            String doctorId = appointment.getDoctorId();
            doctorMap.put(doctorId, doctorMap.getOrDefault(doctorId, 0) + 1);
        }

        // Convert doctor IDs to names
        Map<String, Integer> doctorNameMap = new HashMap<>();
        for (Map.Entry<String, Integer> entry : doctorMap.entrySet()) {
            Doctor doctor = doctorService.getDoctorById(entry.getKey());
            if (doctor != null) {
                doctorNameMap.put(doctor.getFullName(), entry.getValue());
            } else {
                doctorNameMap.put("Unknown Doctor", entry.getValue());
            }
        }

        return doctorNameMap;
    }

    // Generate a comprehensive hospital report
    public String generateHospitalReport() {
        StringBuilder report = new StringBuilder();

        report.append("HOSPITAL MANAGEMENT SYSTEM REPORT\n");
        report.append("Generated on: ").append(DateUtil.getCurrentDateTime()).append("\n\n");

        // Patient statistics
        List<Patient> patients = patientService.getAllPatients();
        report.append("PATIENT STATISTICS\n");
        report.append("-----------------\n");
        report.append("Total Patients: ").append(patients.size()).append("\n");

        Map<String, Integer> patientTypes = getPatientCountByType();
        for (Map.Entry<String, Integer> entry : patientTypes.entrySet()) {
            report.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        report.append("\n");

        // Financial statistics
        report.append("FINANCIAL STATISTICS\n");
        report.append("--------------------\n");
        double totalRevenue = patients.stream().mapToDouble(Patient::calculateBill).sum();
        report.append("Total Revenue: $").append(String.format("%.2f", totalRevenue)).append("\n");

        Map<String, Double> revenueByType = getRevenueByPatientType();
        for (Map.Entry<String, Double> entry : revenueByType.entrySet()) {
            report.append(entry.getKey()).append(" Revenue: $").append(String.format("%.2f", entry.getValue())).append("\n");
        }
        report.append("\n");

        // Appointment statistics
        List<Appointment> appointments = appointmentService.getAllAppointments();
        report.append("APPOINTMENT STATISTICS\n");
        report.append("----------------------\n");
        report.append("Total Appointments: ").append(appointments.size()).append("\n");

        Map<String, Integer> appointmentStatus = getAppointmentsByStatus();
        for (Map.Entry<String, Integer> entry : appointmentStatus.entrySet()) {
            report.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        report.append("\n");

        // Medication statistics
        List<Medication> medications = medicationService.getAllMedications();
        report.append("MEDICATION STATISTICS\n");
        report.append("---------------------\n");
        report.append("Total Medications: ").append(medications.size()).append("\n");
        report.append("Low Stock Items: ").append(medicationService.getLowStockMedications().size()).append("\n");
        report.append("Out of Stock Items: ").append(medicationService.getOutOfStockMedications().size()).append("\n\n");

        // Medical test statistics
        report.append("MEDICAL TEST STATISTICS\n");
        report.append("-----------------------\n");
        report.append("Total Medical Tests: ").append(medicalTestService.getAllMedicalTests().size()).append("\n");
        report.append("Test Results: ").append(medicalTestService.getAllTestResults().size()).append("\n");
        report.append("X-Ray Examinations: ").append(medicalTestService.getAllXRayExaminations().size()).append("\n\n");

        // Doctor statistics
        List<Doctor> doctors = doctorService.getAllDoctors();
        report.append("DOCTOR STATISTICS\n");
        report.append("-----------------\n");
        report.append("Total Doctors: ").append(doctors.size()).append("\n");

        Map<String, List<Doctor>> doctorsBySpecialty = doctors.stream()
                .collect(Collectors.groupingBy(Doctor::getSpecialization));

        for (Map.Entry<String, List<Doctor>> entry : doctorsBySpecialty.entrySet()) {
            report.append(entry.getKey()).append(": ").append(entry.getValue().size()).append("\n");
        }

        return report.toString();
    }
}