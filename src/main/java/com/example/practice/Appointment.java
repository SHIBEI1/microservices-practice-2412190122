package com.example.practice;

import java.time.LocalDateTime;
import java.util.Objects;

/** 宠物就诊预约。 */
public class Appointment {
    private final Long id;
    private final Pet pet;
    private final Doctor doctor;
    private final LocalDateTime appointmentTime;
    private AppointmentStatus status;
    private String notes;

    public Appointment(Long id, Pet pet, Doctor doctor, LocalDateTime appointmentTime, String notes) {
        this.id = Pet.requirePositiveId(id, "预约ID");
        this.pet = Objects.requireNonNull(pet, "预约宠物不能为空");
        this.doctor = Objects.requireNonNull(doctor, "预约医生不能为空");
        this.appointmentTime = Objects.requireNonNull(appointmentTime, "预约时间不能为空");
        if (!doctor.isAvailable()) {
            throw new IllegalStateException("当前医生不可预约");
        }
        this.status = AppointmentStatus.PENDING;
        this.notes = notes == null ? "" : notes.trim();
    }

    public void confirm() {
        requireStatus(AppointmentStatus.PENDING, "只有待确认预约可以确认");
        status = AppointmentStatus.CONFIRMED;
    }

    public void complete() {
        requireStatus(AppointmentStatus.CONFIRMED, "只有已确认预约可以完成");
        status = AppointmentStatus.COMPLETED;
    }

    public void cancel() {
        if (status == AppointmentStatus.COMPLETED || status == AppointmentStatus.CANCELLED) {
            throw new IllegalStateException("已完成或已取消预约不能再次取消");
        }
        status = AppointmentStatus.CANCELLED;
    }

    private void requireStatus(AppointmentStatus expected, String message) {
        if (status != expected) {
            throw new IllegalStateException(message);
        }
    }

    public Long getId() { return id; }
    public Pet getPet() { return pet; }
    public Doctor getDoctor() { return doctor; }
    public LocalDateTime getAppointmentTime() { return appointmentTime; }
    public AppointmentStatus getStatus() { return status; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes == null ? "" : notes.trim(); }
}
