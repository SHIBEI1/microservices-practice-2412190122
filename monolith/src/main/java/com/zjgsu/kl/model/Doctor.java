package com.zjgsu.kl.model;

import java.time.DayOfWeek;
import java.time.LocalTime;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "doctors", uniqueConstraints = @UniqueConstraint(name = "uk_doctor_code", columnNames = "code"),
    check = @CheckConstraint(name = "ck_doctor_hours", constraint = "start_time < end_time"))
public class Doctor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 30)
    private String code;
    @Column(nullable = false, length = 60)
    private String name;
    @Column(nullable = false, length = 60)
    private String department;
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "day_of_week", nullable = false, length = 9)
    private DayOfWeek dayOfWeek;
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;
    @Column(nullable = false)
    private boolean accepting;

    protected Doctor() {}

    public Doctor(String code, String name, String department, DayOfWeek dayOfWeek,
                  LocalTime startTime, LocalTime endTime, boolean accepting) {
        this.code = code;
        this.name = name;
        this.department = department;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.accepting = accepting;
    }

    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getDepartment() { return department; }
    public DayOfWeek getDayOfWeek() { return dayOfWeek; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    public boolean isAccepting() { return accepting; }
}
