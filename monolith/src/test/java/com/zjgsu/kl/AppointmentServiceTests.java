package com.zjgsu.kl;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import com.zjgsu.kl.dto.AppointmentDtos;
import com.zjgsu.kl.exception.BusinessException;
import com.zjgsu.kl.model.Appointment;
import com.zjgsu.kl.model.AppointmentStatus;
import com.zjgsu.kl.model.Doctor;
import com.zjgsu.kl.model.Pet;
import com.zjgsu.kl.repository.AppointmentRepository;
import com.zjgsu.kl.repository.DoctorRepository;
import com.zjgsu.kl.repository.PetRepository;
import com.zjgsu.kl.service.AppointmentService;
import com.zjgsu.kl.support.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTests {
    @Mock
    private PetRepository pets;
    @Mock
    private DoctorRepository doctors;
    @Mock
    private AppointmentRepository appointments;
    private AppointmentService service;
    private Pet pet;
    private Doctor doctor;

    @BeforeEach
    void setUp() {
        pet = TestData.pet("Mimi");
        doctor = TestData.doctor("SERVICE001");
        ReflectionTestUtils.setField(pet, "id", 1L);
        ReflectionTestUtils.setField(doctor, "id", 2L);
        Clock clock = Clock.fixed(Instant.parse("2030-01-07T00:00:00Z"), ZoneId.of("Asia/Shanghai"));
        service = new AppointmentService(pets, doctors, appointments, clock);
    }

    @Test
    void validAppointmentIsCreatedWithPendingStatus() {
        prepareCreation();
        when(appointments.saveAndFlush(any(Appointment.class))).thenAnswer(invocation -> {
            Appointment appointment = invocation.getArgument(0);
            ReflectionTestUtils.setField(appointment, "id", 3L);
            return appointment;
        });

        AppointmentDtos.Response response = service.create(request());

        assertEquals(3L, response.id());
        assertEquals(1L, response.petId());
        assertEquals(2L, response.doctorId());
        assertEquals(AppointmentStatus.PENDING, response.status());
        assertEquals(TestData.APPOINTMENT_TIME, response.appointmentTime());
        verify(appointments).saveAndFlush(argThat(appointment ->
            appointment.getActiveSlot().equals(TestData.APPOINTMENT_TIME)));
    }

    @Test
    void doctorConflictIsRejectedBeforeWriting() {
        prepareCreation();
        when(appointments.existsByDoctorIdAndActiveSlot(2L, TestData.APPOINTMENT_TIME)).thenReturn(true);

        BusinessException error = assertThrows(BusinessException.class, () -> service.create(request()));

        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEquals("该医生的预约时段已被占用", error.getMessage());
        verify(appointments, never()).saveAndFlush(any());
    }

    @Test
    void petConflictIsRejectedBeforeWriting() {
        prepareCreation();
        when(appointments.existsByPetIdAndActiveSlot(1L, TestData.APPOINTMENT_TIME)).thenReturn(true);

        BusinessException error = assertThrows(BusinessException.class, () -> service.create(request()));

        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEquals("该宠物在此时段已有预约", error.getMessage());
        verify(appointments, never()).saveAndFlush(any());
    }

    @Test
    void appointmentOutsideDoctorHoursIsRejected() {
        prepareCreation();
        AppointmentDtos.CreateRequest outside = new AppointmentDtos.CreateRequest(
            1L, 2L, TestData.APPOINTMENT_TIME.withHour(17), "Checkup");

        BusinessException error = assertThrows(BusinessException.class, () -> service.create(outside));

        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        verifyNoInteractions(appointments);
    }

    @Test
    void pendingAppointmentCannotBeCompletedDirectly() {
        Appointment appointment = savedAppointment();
        when(appointments.findForUpdate(3L)).thenReturn(Optional.of(appointment));

        BusinessException error = assertThrows(BusinessException.class, () -> service.complete(3L));

        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEquals(AppointmentStatus.PENDING, appointment.getStatus());
    }

    @Test
    void cancellationRetainsTheRecordAndReleasesItsSlot() {
        Appointment appointment = savedAppointment();
        when(appointments.findForUpdate(3L)).thenReturn(Optional.of(appointment));

        AppointmentDtos.Response response = service.cancel(3L);

        assertEquals(3L, response.id());
        assertEquals(AppointmentStatus.CANCELLED, response.status());
        assertNull(appointment.getActiveSlot());
        verify(appointments, never()).deleteById(anyLong());
    }

    private void prepareCreation() {
        when(doctors.findForUpdate(2L)).thenReturn(Optional.of(doctor));
        when(pets.findForUpdate(1L)).thenReturn(Optional.of(pet));
    }

    private AppointmentDtos.CreateRequest request() {
        return new AppointmentDtos.CreateRequest(1L, 2L, TestData.APPOINTMENT_TIME, "Checkup");
    }

    private Appointment savedAppointment() {
        Appointment appointment = TestData.appointment(pet, doctor);
        ReflectionTestUtils.setField(appointment, "id", 3L);
        return appointment;
    }
}
