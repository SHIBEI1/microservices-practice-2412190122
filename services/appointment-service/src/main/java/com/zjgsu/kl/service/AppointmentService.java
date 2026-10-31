package com.zjgsu.kl.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import com.zjgsu.kl.client.ParticipantClient;
import com.zjgsu.kl.client.ParticipantClient.RemoteDoctor;
import com.zjgsu.kl.dto.AppointmentDtos.*;
import com.zjgsu.kl.exception.BusinessException;
import com.zjgsu.kl.model.Appointment;
import com.zjgsu.kl.model.AppointmentStatus;
import com.zjgsu.kl.repository.AppointmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AppointmentService {
    private final AppointmentRepository appointments;
    private final ParticipantClient participants;
    private final Clock clock;

    public AppointmentService(AppointmentRepository appointments, ParticipantClient participants, Clock clock) {
        this.appointments = appointments;
        this.participants = participants;
        this.clock = clock;
    }

    @Transactional
    public Response create(CreateRequest request) {
        participants.requirePet(request.petId());
        RemoteDoctor doctor = participants.requireDoctor(request.doctorId());
        validateTime(doctor, request.appointmentTime());
        if (appointments.existsByDoctorIdAndActiveSlot(request.doctorId(), request.appointmentTime())) {
            throw BusinessException.conflict("该医生的预约时段已被占用");
        }
        if (appointments.existsByPetIdAndActiveSlot(request.petId(), request.appointmentTime())) {
            throw BusinessException.conflict("该宠物在此时段已有预约");
        }
        Appointment appointment = new Appointment(request.petId(), request.doctorId(), request.appointmentTime(),
            request.reason().strip(), LocalDateTime.now(clock));
        return Response.from(appointments.saveAndFlush(appointment));
    }

    private void validateTime(RemoteDoctor doctor, LocalDateTime time) {
        if (!doctor.accepting()) {
            throw BusinessException.conflict("该医生当前不接受预约");
        }
        if (!time.isAfter(LocalDateTime.now(clock))) {
            throw BusinessException.conflict("只能预约未来的就诊时间");
        }
        if (time.getMinute() % 30 != 0 || time.getSecond() != 0 || time.getNano() != 0) {
            throw BusinessException.conflict("每次就诊占用30分钟，预约时间必须为整点或半点");
        }
        LocalDateTime end = time.plusMinutes(30);
        if (time.getDayOfWeek() != doctor.dayOfWeek()
                || time.toLocalTime().isBefore(doctor.startTime())
                || !end.toLocalDate().equals(time.toLocalDate())
                || end.toLocalTime().isAfter(doctor.endTime())) {
            throw BusinessException.conflict("预约时间不在医生的出诊范围内");
        }
    }

    public Response get(Long id) {
        return Response.from(appointments.findById(id)
            .orElseThrow(() -> BusinessException.notFound("预约不存在")));
    }

    public List<Response> list(Long petId) {
        if (petId != null && petId <= 0) {
            throw BusinessException.badRequest("宠物编号必须为正数");
        }
        List<Appointment> result = petId == null
            ? appointments.findAllByOrderByAppointmentTimeAsc()
            : appointments.findByPetIdOrderByAppointmentTimeAsc(petId);
        return result.stream().map(Response::from).toList();
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
