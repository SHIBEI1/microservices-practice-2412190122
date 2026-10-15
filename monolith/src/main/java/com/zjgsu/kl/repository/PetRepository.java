package com.zjgsu.kl.repository;

import java.util.Optional;
import com.zjgsu.kl.model.Pet;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface PetRepository extends JpaRepository<Pet, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Pet p where p.id = :id")
    Optional<Pet> findForUpdate(@Param("id") Long id);
}
