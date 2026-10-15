package com.zjgsu.kl;

import com.zjgsu.kl.model.Appointment;
import com.zjgsu.kl.model.AppointmentStatus;
import com.zjgsu.kl.model.Doctor;
import com.zjgsu.kl.model.Pet;
import com.zjgsu.kl.repository.AppointmentRepository;
import com.zjgsu.kl.repository.DoctorRepository;
import com.zjgsu.kl.repository.PetRepository;
import com.zjgsu.kl.support.TestData;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class RepositoryPersistenceTests {
    @Autowired
    private PetRepository pets;
    @Autowired
    private DoctorRepository doctors;
    @Autowired
    private AppointmentRepository appointments;
    @Autowired
    private EntityManager entityManager;

    @Test
    void petCanBeWrittenReloadedAndDeleted() {
        Pet saved = pets.saveAndFlush(TestData.pet("Mimi"));
        Long id = saved.getId();
        entityManager.clear();

        Pet reloaded = pets.findById(id).orElseThrow();
        assertEquals("Mimi", reloaded.getName());
        assertEquals("13800138000", reloaded.getOwnerPhone());

        pets.deleteById(id);
        pets.flush();
        entityManager.clear();
        assertFalse(pets.existsById(id));
    }

    @Test
    void appointmentCanBeWrittenAndQueriedByPetAndSlot() {
        Pet pet = pets.saveAndFlush(TestData.pet("Mimi"));
        Doctor doctor = doctors.saveAndFlush(TestData.doctor("REPO001"));
        Appointment saved = appointments.saveAndFlush(TestData.appointment(pet, doctor));
        Long id = saved.getId();
        Long petId = pet.getId();
        Long doctorId = doctor.getId();
        entityManager.clear();

        Appointment reloaded = appointments.findById(id).orElseThrow();
        assertEquals(petId, reloaded.getPet().getId());
        assertEquals(doctorId, reloaded.getDoctor().getId());
        assertEquals(AppointmentStatus.PENDING, reloaded.getStatus());
        assertEquals(TestData.APPOINTMENT_TIME, reloaded.getAppointmentTime());
        assertTrue(appointments.existsByDoctorIdAndActiveSlot(doctorId, TestData.APPOINTMENT_TIME));
        assertTrue(appointments.existsByPetIdAndActiveSlot(petId, TestData.APPOINTMENT_TIME));
        assertEquals(1, appointments.findByPetIdOrderByAppointmentTimeAsc(petId).size());
    }

    @Test
    void cancellationIsPersistedAndAllowsAnotherAppointmentInTheSameSlot() {
        Pet pet = pets.saveAndFlush(TestData.pet("Mimi"));
        Doctor doctor = doctors.saveAndFlush(TestData.doctor("REPO002"));
        Appointment original = appointments.saveAndFlush(TestData.appointment(pet, doctor));
        Long originalId = original.getId();
        Long petId = pet.getId();
        Long doctorId = doctor.getId();

        original.cancel();
        appointments.flush();
        entityManager.clear();

        Appointment cancelled = appointments.findById(originalId).orElseThrow();
        assertEquals(AppointmentStatus.CANCELLED, cancelled.getStatus());
        assertNull(cancelled.getActiveSlot());
        assertFalse(appointments.existsByDoctorIdAndActiveSlot(doctorId, TestData.APPOINTMENT_TIME));

        Pet samePet = pets.findById(petId).orElseThrow();
        Doctor sameDoctor = doctors.findById(doctorId).orElseThrow();
        Appointment replacement = appointments.saveAndFlush(TestData.appointment(samePet, sameDoctor));

        assertNotEquals(originalId, replacement.getId());
        assertEquals(2, appointments.findByPetIdOrderByAppointmentTimeAsc(petId).size());
        assertTrue(appointments.existsByDoctorIdAndActiveSlot(doctorId, TestData.APPOINTMENT_TIME));
    }
}
