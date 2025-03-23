package com.hospital.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.model.User;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AuthService {
    private static AuthService instance;
    private final String USERS_FILE = "data/users.json";
    private final ObjectMapper objectMapper;
    private User currentUser;

    private AuthService() {
        objectMapper = new ObjectMapper();
        initializeUsers();
    }

    public static AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
        }
        return instance;
    }

    private void initializeUsers() {
        File file = new File(USERS_FILE);
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                List<User> defaultUsers = new ArrayList<>();

                // Create default admin
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword("admin");  // In a real app, this should be hashed
                admin.setFullName("System Administrator");
                admin.setEmail("admin@hospital.com");
                admin.setRole("ADMIN");

                // Create default doctor
                User doctor = new User();
                doctor.setUsername("doctor");
                doctor.setPassword("doctor");  // In a real app, this should be hashed
                doctor.setFullName("Dr. John Smith");
                doctor.setEmail("doctor@hospital.com");
                doctor.setRole("DOCTOR");

                // Create default patient
                User patient = new User();
                patient.setUsername("patient");
                patient.setPassword("patient");  // In a real app, this should be hashed
                patient.setFullName("James Patient");
                patient.setEmail("patient@example.com");
                patient.setRole("PATIENT");

                defaultUsers.add(admin);
                defaultUsers.add(doctor);
                defaultUsers.add(patient);

                objectMapper.writeValue(file, defaultUsers);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean login(String username, String password) {
        try {
            File file = new File(USERS_FILE);
            List<User> users = objectMapper.readValue(file,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, User.class));

            Optional<User> userOpt = users.stream()
                    .filter(u -> u.getUsername().equals(username) && u.getPassword().equals(password))
                    .findFirst();

            if (userOpt.isPresent()) {
                currentUser = userOpt.get();

                // Update last login time
                LocalDateTime now = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                currentUser.setLastLogin(now.format(formatter));

                updateUser(currentUser);

                return true;
            }

            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void logout() {
        currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public String getCurrentUserRole() {
        return currentUser != null ? currentUser.getRole() : null;
    }

    private void updateUser(User user) {
        try {
            File file = new File(USERS_FILE);
            List<User> users = objectMapper.readValue(file,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, User.class));

            for (int i = 0; i < users.size(); i++) {
                if (users.get(i).getId().equals(user.getId())) {
                    users.set(i, user);
                    break;
                }
            }

            objectMapper.writeValue(file, users);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<User> getAllUsers() {
        try {
            File file = new File(USERS_FILE);
            return objectMapper.readValue(file,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, User.class));
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}