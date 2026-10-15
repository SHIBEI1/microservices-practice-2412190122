package com.zjgsu.kl.support;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import com.zjgsu.kl.model.Appointment;
import com.zjgsu.kl.model.Doctor;
import com.zjgsu.kl.model.Pet;

public final class TestData {
    public static final LocalDateTime APPOINTMENT_TIME = LocalDateTime.of(2030, 1, 7, 10, 0);

    private TestData() {}

    public static Pet pet(String name) {
        return new Pet(name, "CAT", 2, "Owner", "13800138000", "Healthy");
    }

    public static Doctor doctor(String code) {
        return new Doctor(code, "Doctor Li", "General", DayOfWeek.MONDAY,
            LocalTime.of(9, 0), LocalTime.of(17, 0), true);
    }

    public static Appointment appointment(Pet pet, Doctor doctor) {
        return new Appointment(pet, doctor, APPOINTMENT_TIME, "Checkup",
            LocalDateTime.of(2030, 1, 7, 8, 0));
    }
}
