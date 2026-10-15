package com.zjgsu.kl.dto;

import java.time.LocalDateTime;
import com.zjgsu.kl.model.Appointment;
import com.zjgsu.kl.model.AppointmentStatus;
import jakarta.validation.constraints.*;

public final class AppointmentDtos {
    private AppointmentDtos() {}

    public record CreateRequest(
        @NotNull @Positive Long petId,
        @NotNull @Positive Long doctorId,
        @NotNull LocalDateTime appointmentTime,
        @NotBlank @Size(max = 500) String reason
    ) {}

    public record Response(Long id, Long petId, Long doctorId, LocalDateTime appointmentTime,
                           String reason, AppointmentStatus status, LocalDateTime createdAt) {
        public static Response from(Appointment appointment) {
            return new Response(appointment.getId(), appointment.getPet().getId(), appointment.getDoctor().getId(),
                appointment.getAppointmentTime(), appointment.getReason(), appointment.getStatus(), appointment.getCreatedAt());
        }
    }
}
