package com.hospital.util;

import com.hospital.model.Patient;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ChartUtil {

    public static void populatePatientTypeChart(PieChart chart, List<Patient> patients) {
        Map<String, Long> patientCountByType = patients.stream()
                .collect(Collectors.groupingBy(Patient::getPatientType, Collectors.counting()));

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        patientCountByType.forEach((type, count) -> {
            pieChartData.add(new PieChart.Data(type, count));
        });

        chart.setData(pieChartData);
        chart.setTitle("Patient Distribution by Type");
    }

    public static void populateGenderDistributionChart(PieChart chart, List<Patient> patients) {
        Map<String, Long> genderCount = patients.stream()
                .collect(Collectors.groupingBy(Patient::getGender, Collectors.counting()));

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        genderCount.forEach((gender, count) -> {
            pieChartData.add(new PieChart.Data(gender, count));
        });

        chart.setData(pieChartData);
        chart.setTitle("Patient Distribution by Gender");
    }

    public static void populateAgeDistributionChart(BarChart<String, Number> chart, List<Patient> patients) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Age Distribution");

        // Age groups
        Map<String, Long> ageGroups = patients.stream()
                .collect(Collectors.groupingBy(p -> {
                    int age = p.getAge();
                    if (age < 10) return "0-9";
                    else if (age < 20) return "10-19";
                    else if (age < 30) return "20-29";
                    else if (age < 40) return "30-39";
                    else if (age < 50) return "40-49";
                    else if (age < 60) return "50-59";
                    else if (age < 70) return "60-69";
                    else if (age < 80) return "70-79";
                    else return "80+";
                }, Collectors.counting()));

        // Ensure all age groups are represented even if count is 0
        String[] allAgeGroups = {"0-9", "10-19", "20-29", "30-39", "40-49", "50-59", "60-69", "70-79", "80+"};
        for (String ageGroup : allAgeGroups) {
            series.getData().add(new XYChart.Data<>(ageGroup, ageGroups.getOrDefault(ageGroup, 0L)));
        }

        chart.getData().add(series);
        chart.setTitle("Patient Age Distribution");
    }

    public static void populateRevenueChart(BarChart<String, Number> chart, List<Patient> patients) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Revenue by Patient Type");

        Map<String, Double> revenueByType = patients.stream()
                .collect(Collectors.groupingBy(
                        Patient::getPatientType,
                        Collectors.summingDouble(Patient::calculateBill)
                ));

        revenueByType.forEach((type, revenue) -> {
            series.getData().add(new XYChart.Data<>(type, revenue));
        });

        chart.getData().add(series);
        chart.setTitle("Revenue by Patient Type");
    }
}