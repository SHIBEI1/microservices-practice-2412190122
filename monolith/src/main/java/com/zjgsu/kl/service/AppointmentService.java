package com.zjgsu.kl.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import com.zjgsu.kl.dto.AppointmentDtos.*;
import com.zjgsu.kl.exception.BusinessException;
import com.zjgsu.kl.model.*;
import com.zjgsu.kl.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AppointmentService {
    private final PetRepository pets;
    private final DoctorRepository doctors;
    private final AppointmentRepository appointments;
    private final Clock clock;

    public AppointmentService(PetRepository pets, DoctorRepository doctors,
                              AppointmentRepository appointments, Clock clock) {
        this.pets = pets;
        this.doctors = doctors;
        this.appointments = appointments;
        this.clock = clock;
    }

    @Transactional
    public Response create(CreateRequest request) {
        Doctor doctor = doctors.findForUpdate(request.doctorId())
            .orElseThrow(() -> BusinessException.notFound("医生不存在"));
        Pet pet = pets.findForUpdate(request.petId())
            .orElseThrow(() -> BusinessException.notFound("宠物不存在"));
        LocalDateTime time = request.appointmentTime();
        validateTime(doctor, time);
        if (appointments.existsByDoctorIdAndActiveSlot(doctor.getId(), time)) {
            throw BusinessException.conflict("该医生的预约时段已被占用");
        }
        if (appointments.existsByPetIdAndActiveSlot(pet.getId(), time)) {
            throw BusinessException.conflict("该宠物在此时段已有预约");
        }
        Appointment appointment = new Appointment(pet, doctor, time, request.reason().strip(), LocalDateTime.now(clock));
        return Response.from(appointments.saveAndFlush(appointment));
    }

    private void validateTime(Doctor doctor, LocalDateTime time) {
        if (!doctor.isAccepting()) {
            throw BusinessException.conflict("该医生当前不接受预约");
        }
        if (!time.isAfter(LocalDateTime.now(clock))) {
            throw BusinessException.conflict("只能预约未来的就诊时间");
        }
        if (time.getMinute() % 30 != 0 || time.getSecond() != 0 || time.getNano() != 0) {
            throw BusinessException.conflict("每次就诊占用30分钟，预约时间必须为整点或半点");
        }
        LocalDateTime end = time.plusMinutes(30);
        if (time.getDayOfWeek() != doctor.getDayOfWeek()
                || time.toLocalTime().isBefore(doctor.getStartTime())
                || !end.toLocalDate().equals(time.toLocalDate())
                || end.toLocalTime().isAfter(doctor.getEndTime())) {
            throw BusinessException.conflict("预约时间不在医生的出诊范围内");
        }
    }

    public Response get(Long id) {
        return Response.from(appointments.findById(id)
            .orElseThrow(() -> BusinessException.notFound("预约不存在")));
    }

    public List<Response> list(Long petId) {
        if (petId == null) {
            return appointments.findAllByOrderByAppointmentTimeAsc().stream().map(Response::from).toList();
        }
        if (!pets.existsById(petId)) {
            throw BusinessException.notFound("宠物不存在");
        }
        return appointments.findByPetIdOrderByAppointmentTimeAsc(petId).stream().map(Response::from).toList();
    }

    @Transactional
    public Response confirm(Long id) {
        Appointment appointment = getForUpdate(id);
        if (appointment.getStatus() != AppointmentStatus.PENDING) {
            throw BusinessException.conflict("只有待确认预约可以确认");
        }
        requireFuture(appointment);
        appointment.confirm();
        return Response.from(appointment);
    }

    @Transactional
    public Response complete(Long id) {
        Appointment appointment = getForUpdate(id);
        if (appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw BusinessException.conflict("只有已确认预约可以完成");
        }
        if (LocalDateTime.now(clock).isBefore(appointment.getAppointmentTime())) {
            throw BusinessException.conflict("尚未到预约时间，不能完成就诊");
        }
        appointment.complete();
        return Response.from(appointment);
    }

    @Transactional
    public Response cancel(Long id) {
        Appointment appointment = getForUpdate(id);
        if (appointment.getStatus() != AppointmentStatus.PENDING
                && appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw BusinessException.conflict("只有待确认或已确认预约可以取消");
        }
        requireFuture(appointment);
        appointment.cancel();
        return Response.from(appointment);
    }

    private Appointment getForUpdate(Long id) {
        return appointments.findForUpdate(id).orElseThrow(() -> BusinessException.notFound("预约不存在"));
    }

    private void requireFuture(Appointment appointment) {
        if (!appointment.getAppointmentTime().isAfter(LocalDateTime.now(clock))) {
            throw BusinessException.conflict("预约已到就诊时间，不能再确认或取消");
        }
    }
}
