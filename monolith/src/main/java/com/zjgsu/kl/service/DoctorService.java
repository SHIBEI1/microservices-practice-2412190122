package com.zjgsu.kl.service;

import java.time.LocalTime;
import java.util.List;
import java.util.Locale;
import com.zjgsu.kl.dto.DoctorDtos.*;
import com.zjgsu.kl.exception.BusinessException;
import com.zjgsu.kl.model.Doctor;
import com.zjgsu.kl.repository.DoctorRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DoctorService {
    private final DoctorRepository doctors;

    public DoctorService(DoctorRepository doctors) {
        this.doctors = doctors;
    }

    @Transactional
    public Response create(CreateRequest request) {
        if (!request.startTime().isBefore(request.endTime())
                || !isHalfHour(request.startTime()) || !isHalfHour(request.endTime())) {
            throw BusinessException.badRequest("出诊开始时间必须早于结束时间，且均为整点或半点");
        }
        String code = request.code().toUpperCase(Locale.ROOT);
        if (doctors.existsByCode(code)) {
            throw BusinessException.conflict("医生编号已存在");
        }
        Doctor doctor = new Doctor(code, request.name().strip(), request.department().strip(), request.dayOfWeek(),
            request.startTime(), request.endTime(), request.accepting());
        return Response.from(doctors.saveAndFlush(doctor));
    }

    private boolean isHalfHour(LocalTime time) {
        return time.getMinute() % 30 == 0 && time.getSecond() == 0 && time.getNano() == 0;
    }

    public Response get(Long id) {
        return Response.from(doctors.findById(id).orElseThrow(() -> BusinessException.notFound("医生不存在")));
    }

    public List<Response> list() {
        return doctors.findAll(Sort.by("id")).stream().map(Response::from).toList();
    }
}
