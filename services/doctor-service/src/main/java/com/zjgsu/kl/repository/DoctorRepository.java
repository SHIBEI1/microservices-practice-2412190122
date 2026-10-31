package com.zjgsu.kl.repository;

import com.zjgsu.kl.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    boolean existsByCode(String code);
}
