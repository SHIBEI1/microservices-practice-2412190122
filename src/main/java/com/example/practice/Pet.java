package com.example.practice;

import java.util.Objects;

/** 宠物档案。 */
public class Pet {
    private final Long id;
    private String name;
    private PetType type;
    private int age;
    private final Long ownerId;
    private String healthNotes;

    public Pet(Long id, String name, PetType type, int age, Long ownerId, String healthNotes) {
        this.id = requirePositiveId(id, "宠物ID");
        this.name = requireText(name, "宠物名字");
        this.type = Objects.requireNonNull(type, "宠物种类不能为空");
        setAge(age);
        this.ownerId = requirePositiveId(ownerId, "主人ID");
        this.healthNotes = healthNotes == null ? "" : healthNotes.trim();
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public PetType getType() { return type; }
    public int getAge() { return age; }
    public Long getOwnerId() { return ownerId; }
    public String getHealthNotes() { return healthNotes; }

    public void setName(String name) { this.name = requireText(name, "宠物名字"); }
    public void setType(PetType type) { this.type = Objects.requireNonNull(type, "宠物种类不能为空"); }

    public void setAge(int age) {
        if (age < 0) {
            throw new IllegalArgumentException("宠物年龄不能小于0");
        }
        this.age = age;
    }

    public void setHealthNotes(String healthNotes) {
        this.healthNotes = healthNotes == null ? "" : healthNotes.trim();
    }

    static Long requirePositiveId(Long id, String fieldName) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(fieldName + "必须为正整数");
        }
        return id;
    }

    static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + "不能为空");
        }
        return value.trim();
    }
}
