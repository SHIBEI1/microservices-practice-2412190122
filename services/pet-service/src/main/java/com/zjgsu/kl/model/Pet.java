package com.zjgsu.kl.model;

import jakarta.persistence.*;

@Entity
@Table(name = "pets", check = @CheckConstraint(name = "ck_pet_age", constraint = "age between 0 and 50"))
public class Pet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 60)
    private String name;
    @Column(nullable = false, length = 30)
    private String species;
    @Column(nullable = false)
    private int age;
    @Column(name = "owner_name", nullable = false, length = 60)
    private String ownerName;
    @Column(name = "owner_phone", nullable = false, length = 11)
    private String ownerPhone;
    @Column(name = "health_notes", length = 500)
    private String healthNotes;

    protected Pet() {}

    public Pet(String name, String species, int age, String ownerName, String ownerPhone, String healthNotes) {
        this.name = name;
        this.species = species;
        this.age = age;
        this.ownerName = ownerName;
        this.ownerPhone = ownerPhone;
        this.healthNotes = healthNotes;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getSpecies() { return species; }
    public int getAge() { return age; }
    public String getOwnerName() { return ownerName; }
    public String getOwnerPhone() { return ownerPhone; }
    public String getHealthNotes() { return healthNotes; }
}
