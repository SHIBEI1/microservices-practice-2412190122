package com.example.practice;

/** 宠物医生。 */
public class Doctor {
    private final Long id;
    private String name;
    private String department;
    private String specialty;
    private boolean available;

    public Doctor(Long id, String name, String department, String specialty, boolean available) {
        this.id = Pet.requirePositiveId(id, "医生ID");
        this.name = Pet.requireText(name, "医生姓名");
        this.department = Pet.requireText(department, "所属科室");
        this.specialty = Pet.requireText(specialty, "擅长病种");
        this.available = available;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDepartment() { return department; }
    public String getSpecialty() { return specialty; }
    public boolean isAvailable() { return available; }

    public void setName(String name) { this.name = Pet.requireText(name, "医生姓名"); }
    public void setDepartment(String department) { this.department = Pet.requireText(department, "所属科室"); }
    public void setSpecialty(String specialty) { this.specialty = Pet.requireText(specialty, "擅长病种"); }
    public void setAvailable(boolean available) { this.available = available; }
}
