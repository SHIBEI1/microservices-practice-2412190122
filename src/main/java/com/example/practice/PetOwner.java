package com.example.practice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** 宠物主人及其名下宠物集合。 */
public class PetOwner {
    private final Long id;
    private String name;
    private String phone;
    private String address;
    private final List<Pet> pets = new ArrayList<>();

    public PetOwner(Long id, String name, String phone, String address) {
        this.id = Pet.requirePositiveId(id, "主人ID");
        this.name = Pet.requireText(name, "主人姓名");
        setPhone(phone);
        this.address = Pet.requireText(address, "家庭地址");
    }

    /** 添加宠物，并拒绝归属错误或ID重复的宠物档案。 */
    public void addPet(Pet pet) {
        Objects.requireNonNull(pet, "宠物不能为空");
        if (!id.equals(pet.getOwnerId())) {
            throw new IllegalArgumentException("宠物不属于当前主人");
        }
        if (findPetById(pet.getId()).isPresent()) {
            throw new IllegalArgumentException("同一主人名下不能存在重复的宠物ID");
        }
        pets.add(pet);
    }

    /** 集合查询操作：按宠物ID查找档案。 */
    public Optional<Pet> findPetById(Long petId) {
        return pets.stream().filter(pet -> pet.getId().equals(petId)).findFirst();
    }

    /** 集合筛选操作：按宠物种类筛选。 */
    public List<Pet> findPetsByType(PetType type) {
        Objects.requireNonNull(type, "宠物种类不能为空");
        return pets.stream().filter(pet -> pet.getType() == type).toList();
    }

    /** 集合统计操作：统计主人名下宠物数量。 */
    public int countPets() {
        return pets.size();
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
    public List<Pet> getPets() { return Collections.unmodifiableList(pets); }

    public void setName(String name) { this.name = Pet.requireText(name, "主人姓名"); }

    public void setPhone(String phone) {
        String normalized = Pet.requireText(phone, "联系电话");
        if (!normalized.matches("1\\d{10}")) {
            throw new IllegalArgumentException("联系电话必须是11位手机号");
        }
        this.phone = normalized;
    }

    public void setAddress(String address) { this.address = Pet.requireText(address, "家庭地址"); }
}
