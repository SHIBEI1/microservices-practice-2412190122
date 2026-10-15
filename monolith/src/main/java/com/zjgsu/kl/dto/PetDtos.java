package com.zjgsu.kl.dto;

import com.zjgsu.kl.model.Pet;
import jakarta.validation.constraints.*;

public final class PetDtos {
    private PetDtos() {}

    public record CreateRequest(
        @NotBlank @Size(max = 60) String name,
        @NotBlank @Size(max = 30) String species,
        @NotNull @Min(0) @Max(50) Integer age,
        @NotBlank @Size(max = 60) String ownerName,
        @NotBlank @Pattern(regexp = "1[0-9]{10}") String ownerPhone,
        @Size(max = 500) String healthNotes
    ) {}

    public record Response(Long id, String name, String species, int age,
                           String ownerName, String ownerPhone, String healthNotes) {
        public static Response from(Pet pet) {
            return new Response(pet.getId(), pet.getName(), pet.getSpecies(), pet.getAge(),
                pet.getOwnerName(), pet.getOwnerPhone(), pet.getHealthNotes());
        }
    }
}
