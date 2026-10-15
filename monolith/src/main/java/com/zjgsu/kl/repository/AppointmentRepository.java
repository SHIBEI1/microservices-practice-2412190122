package com.zjgsu.kl.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import com.zjgsu.kl.model.Appointment;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    boolean existsByDoctorIdAndActiveSlot(Long doctorId, LocalDateTime activeSlot);
    boolean existsByPetIdAndActiveSlot(Long petId, LocalDateTime activeSlot);
    List<Appointment> findByPetIdOrderByAppointmentTimeAsc(Long petId);
    List<Appointment> findAllByOrderByAppointmentTimeAsc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Appointment a where a.id = :id")
    Optional<Appointment> findForUpdate(@Param("id") Long id);
}
