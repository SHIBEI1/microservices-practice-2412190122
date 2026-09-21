package com.example.practice;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PetOwnerTest {

    @Test
    @DisplayName("正常场景：添加、查询、筛选并统计宠物")
    void shouldAddSearchFilterAndCountPets() {
        System.out.println("[正常场景] 添加宠物后完成查询、类型筛选和数量统计");
        PetOwner owner = new PetOwner(1L, "张明", "13800138000", "南京市玄武区");
        Pet cat = new Pet(101L, "团子", PetType.CAT, 2, 1L, "已接种疫苗");
        Pet dog = new Pet(102L, "旺财", PetType.DOG, 4, 1L, "无");

        owner.addPet(cat);
        owner.addPet(dog);

        assertEquals(2, owner.countPets());
        assertEquals(cat, owner.findPetById(101L).orElseThrow());
        assertEquals(1, owner.findPetsByType(PetType.CAT).size());
    }

    @Test
    @DisplayName("边界场景：主人没有宠物时返回空结果")
    void shouldReturnEmptyResultsWhenOwnerHasNoPets() {
        System.out.println("[边界场景] 主人没有宠物时数量为 0，查询和筛选返回空结果");
        PetOwner owner = new PetOwner(1L, "张明", "13800138000", "南京市玄武区");

        assertEquals(0, owner.countPets());
        assertTrue(owner.findPetById(999L).isEmpty());
        assertTrue(owner.findPetsByType(PetType.DOG).isEmpty());
    }

    @Test
    @DisplayName("异常或非法输入：拒绝添加属于其他主人的宠物")
    void shouldRejectPetBelongingToAnotherOwner() {
        System.out.println("[异常或非法输入] 添加属于其他主人的宠物时抛出异常");
        PetOwner owner = new PetOwner(1L, "张明", "13800138000", "南京市玄武区");
        Pet anotherOwnersPet = new Pet(101L, "团子", PetType.CAT, 2, 2L, "");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> owner.addPet(anotherOwnersPet)
        );
        assertEquals("宠物不属于当前主人", exception.getMessage());
    }

    @Test
    @DisplayName("异常或非法输入：拒绝重复的宠物编号")
    void shouldRejectDuplicatePetId() {
        System.out.println("[异常或非法输入] 添加重复宠物编号时抛出异常");
        PetOwner owner = new PetOwner(1L, "张明", "13800138000", "南京市玄武区");
        owner.addPet(new Pet(101L, "团子", PetType.CAT, 2, 1L, ""));

        assertThrows(IllegalArgumentException.class,
                () -> owner.addPet(new Pet(101L, "小花", PetType.CAT, 1, 1L, "")));
    }
}
