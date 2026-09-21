package com.example.practice;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AppointmentTest {

    private final Pet pet = new Pet(101L, "团子", PetType.CAT, 2, 1L, "食欲下降");

    @Test
    @DisplayName("正常场景：预约按待确认、已确认、已完成顺序流转")
    void shouldFollowNormalAppointmentStatusFlow() {
        System.out.println("[正常场景] 预约状态从待确认依次流转为已确认和已完成");
        Doctor doctor = new Doctor(201L, "李医生", "内科", "消化系统疾病", true);
        Appointment appointment = new Appointment(
                301L, pet, doctor, LocalDateTime.of(2026, 9, 22, 10, 0), "首次就诊");

        assertEquals(AppointmentStatus.PENDING, appointment.getStatus());
        appointment.confirm();
        assertEquals(AppointmentStatus.CONFIRMED, appointment.getStatus());
        appointment.complete();
        assertEquals(AppointmentStatus.COMPLETED, appointment.getStatus());
    }

    @Test
    @DisplayName("异常或非法输入：拒绝预约不可接诊的医生")
    void shouldRejectUnavailableDoctor() {
        System.out.println("[异常或非法输入] 为不可接诊的医生创建预约时抛出异常");
        Doctor unavailableDoctor = new Doctor(201L, "李医生", "内科", "消化系统疾病", false);

        assertThrows(IllegalStateException.class,
                () -> new Appointment(301L, pet, unavailableDoctor,
                        LocalDateTime.of(2026, 9, 22, 10, 0), ""));
    }

    @Test
    @DisplayName("异常或非法输入：拒绝直接完成未确认的预约")
    void shouldRejectCompletingUnconfirmedAppointment() {
        System.out.println("[异常或非法输入] 未确认的预约直接完成时抛出异常");
        Doctor doctor = new Doctor(201L, "李医生", "内科", "消化系统疾病", true);
        Appointment appointment = new Appointment(
                301L, pet, doctor, LocalDateTime.of(2026, 9, 22, 10, 0), "");

        assertThrows(IllegalStateException.class, appointment::complete);
    }
}
