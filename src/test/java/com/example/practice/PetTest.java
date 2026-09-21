package com.example.practice;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PetTest {

    @Test
    @DisplayName("边界场景：宠物年龄为 0 时可以正常创建")
    void shouldAcceptZeroAsBoundaryAge() {
        System.out.println("[边界场景] 宠物年龄为 0 时可以正常创建");
        Pet newborn = new Pet(101L, "小白", PetType.DOG, 0, 1L, null);

        assertEquals(0, newborn.getAge());
        assertEquals("", newborn.getHealthNotes());
    }

    @Test
    @DisplayName("异常或非法输入：拒绝负数年龄")
    void shouldRejectNegativeAge() {
        System.out.println("[异常或非法输入] 宠物年龄为负数时抛出异常");
        assertThrows(IllegalArgumentException.class,
                () -> new Pet(101L, "小白", PetType.DOG, -1, 1L, ""));
    }

    @Test
    @DisplayName("异常或非法输入：拒绝空白宠物名称")
    void shouldRejectBlankPetName() {
        System.out.println("[异常或非法输入] 宠物名称为空白时抛出异常");
        assertThrows(IllegalArgumentException.class,
                () -> new Pet(101L, "  ", PetType.DOG, 2, 1L, ""));
    }
}
