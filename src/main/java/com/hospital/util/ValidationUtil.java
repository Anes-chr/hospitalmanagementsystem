package com.hospital.util;

import java.util.regex.Pattern;

public class ValidationUtil {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{10}$");
    private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-z ]{2,30}$");
    private static final Pattern NUMBER_PATTERN = Pattern.compile("^[0-9]+$");

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }

    public static boolean isValidName(String name) {
        return name != null && NAME_PATTERN.matcher(name).matches();
    }

    public static boolean isValidNumber(String number) {
        return number != null && NUMBER_PATTERN.matcher(number).matches();
    }

    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public static boolean isValidAge(int age) {
        return age > 0 && age < 120;
    }

    public static boolean isValidPositiveDouble(double value) {
        return value >= 0;
    }

    public static boolean isValidDate(String date) {
        try {
            if (date == null || date.trim().isEmpty()) {
                return false;
            }

            // Simple check for YYYY-MM-DD format
            String[] parts = date.split("-");
            if (parts.length != 3) {
                return false;
            }

            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            int day = Integer.parseInt(parts[2]);

            return year >= 1900 && year <= 2100 &&
                    month >= 1 && month <= 12 &&
                    day >= 1 && day <= 31;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }
}