package com.hospital.model;

public class Admin extends User {
    private String department;
    private String adminLevel;
    private String lastPasswordReset;

    public Admin() {
        super();
        super.setRole("ADMIN");
    }

    public Admin(String username, String password, String fullName, String email,
                 String department, String adminLevel) {
        super(username, password, fullName, email, "ADMIN");
        this.department = department;
        this.adminLevel = adminLevel;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getAdminLevel() {
        return adminLevel;
    }

    public void setAdminLevel(String adminLevel) {
        this.adminLevel = adminLevel;
    }

    public String getLastPasswordReset() {
        return lastPasswordReset;
    }

    public void setLastPasswordReset(String lastPasswordReset) {
        this.lastPasswordReset = lastPasswordReset;
    }
}