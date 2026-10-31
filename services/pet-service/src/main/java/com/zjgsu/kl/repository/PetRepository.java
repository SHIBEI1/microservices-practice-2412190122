package com.zjgsu.kl.repository;

import com.zjgsu.kl.model.Pet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PetRepository extends JpaRepository<Pet, Long> {
}
