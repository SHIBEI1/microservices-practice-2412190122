package com.zjgsu.kl;

import com.zjgsu.kl.model.Appointment;
import com.zjgsu.kl.model.Doctor;
import com.zjgsu.kl.model.Pet;
import com.zjgsu.kl.repository.AppointmentRepository;
import com.zjgsu.kl.repository.DoctorRepository;
import com.zjgsu.kl.repository.PetRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class DatabaseConstraintTests {

    @Autowired
    private PetRepository pets;

    @Autowired
    private DoctorRepository doctors;

    @Autowired
    private AppointmentRepository appointments;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void databaseRejectsDuplicateDoctorActiveSlot() {
        Pet firstPet = pet("Mimi");
        Pet secondPet = pet("Lucky");
        Doctor doctor = doctor("D001");
        appointments.saveAndFlush(appointment(firstPet, doctor));

        assertThrows(DataIntegrityViolationException.class,
                () -> appointments.saveAndFlush(appointment(secondPet, doctor)));
    }

    @Test
    void databaseRejectsDuplicatePetActiveSlot() {
        Pet pet = pet("Mimi");
        Doctor firstDoctor = doctor("D001");
        Doctor secondDoctor = doctor("D002");
        appointments.saveAndFlush(appointment(pet, firstDoctor));

        assertThrows(DataIntegrityViolationException.class,
                () -> appointments.saveAndFlush(appointment(pet, secondDoctor)));
    }

    @Test
    void databaseRejectsAppointmentWithMissingPet() {
        Doctor doctor = doctor("D001");
        Pet missingPet = pets.getReferenceById(999999L);

        assertThrows(DataIntegrityViolationException.class,
                () -> appointments.saveAndFlush(appointment(missingPet, doctor)));
    }

    @Test
    void databaseRequiresCancelledAppointmentToReleaseActiveSlot() {
        Appointment appointment = appointments.saveAndFlush(appointment(pet("Mimi"), doctor("D001")));

        DataAccessException error = assertThrows(DataAccessException.class,
                () -> jdbc.update("update appointments set status = 'CANCELLED' where id = ?", appointment.getId()));
        assertTrue(error.getMostSpecificCause().getMessage().toLowerCase(Locale.ROOT)
                .contains("ck_appointment_slot"));
    }

    private Pet pet(String name) {
        return pets.saveAndFlush(new Pet(name, "CAT", 2, "Owner", "13800138000", "Healthy"));
    }

    private Doctor doctor(String code) {
        return doctors.saveAndFlush(new Doctor(code, "Doctor Li", "General", DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(17, 0), true));
    }

    private Appointment appointment(Pet pet, Doctor doctor) {
        return new Appointment(pet, doctor, LocalDateTime.of(2030, 1, 7, 10, 0), "Checkup",
                LocalDateTime.of(2030, 1, 7, 8, 0));
    }
}
