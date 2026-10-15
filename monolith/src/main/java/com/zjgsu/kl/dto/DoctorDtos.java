package com.zjgsu.kl.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;
import com.zjgsu.kl.model.Doctor;
import jakarta.validation.constraints.*;

public final class DoctorDtos {
    private DoctorDtos() {}

    public record CreateRequest(
        @NotBlank @Pattern(regexp = "[A-Za-z0-9_-]{1,30}") String code,
        @NotBlank @Size(max = 60) String name,
        @NotBlank @Size(max = 60) String department,
        @NotNull DayOfWeek dayOfWeek,
        @NotNull LocalTime startTime,
        @NotNull LocalTime endTime,
        @NotNull Boolean accepting
    ) {}

    public record Response(Long id, String code, String name, String department, DayOfWeek dayOfWeek,
                           LocalTime startTime, LocalTime endTime, boolean accepting) {
        public static Response from(Doctor doctor) {
            return new Response(doctor.getId(), doctor.getCode(), doctor.getName(), doctor.getDepartment(),
                doctor.getDayOfWeek(), doctor.getStartTime(), doctor.getEndTime(), doctor.isAccepting());
        }
    }
}
