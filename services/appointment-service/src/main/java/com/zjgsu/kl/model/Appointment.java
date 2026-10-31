package com.zjgsu.kl.model;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "appointments", uniqueConstraints = {
    @UniqueConstraint(name = "uk_doctor_active_slot", columnNames = {"doctor_id", "active_slot"}),
    @UniqueConstraint(name = "uk_pet_active_slot", columnNames = {"pet_id", "active_slot"})
}, check = @CheckConstraint(name = "ck_appointment_slot", constraint = "(status = 'CANCELLED' and active_slot is null) or (status <> 'CANCELLED' and active_slot is not null and active_slot = appointment_time)"))
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "pet_id", nullable = false)
    private Long petId;
    @Column(name = "doctor_id", nullable = false)
    private Long doctorId;
    @Column(name = "appointment_time", nullable = false)
    private LocalDateTime appointmentTime;
    @Column(name = "active_slot")
    private LocalDateTime activeSlot;
    @Column(nullable = false, length = 500)
    private String reason;
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 16)
    private AppointmentStatus status;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected Appointment() {}

    public Appointment(Long petId, Long doctorId, LocalDateTime time, String reason, LocalDateTime createdAt) {
        this.petId = petId;
        this.doctorId = doctorId;
        this.appointmentTime = time;
        this.activeSlot = time;
        this.reason = reason;
        this.createdAt = createdAt.truncatedTo(ChronoUnit.MICROS);
        this.status = AppointmentStatus.PENDING;
    }

    public void confirm() { status = AppointmentStatus.CONFIRMED; }
    public void complete() { status = AppointmentStatus.COMPLETED; }
    public void cancel() { status = AppointmentStatus.CANCELLED; activeSlot = null; }

    public Long getId() { return id; }
    public Long getPetId() { return petId; }
    public Long getDoctorId() { return doctorId; }
    public LocalDateTime getAppointmentTime() { return appointmentTime; }
    public LocalDateTime getActiveSlot() { return activeSlot; }
    public String getReason() { return reason; }
    public AppointmentStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
