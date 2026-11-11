package com.zjgsu.kl.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.zjgsu.kl.exception.BusinessException;
import feign.FeignException;
import feign.RetryableException;
import feign.codec.DecodeException;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.function.Supplier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class ParticipantClient {
    private final PetFeignClient pets;
    private final DoctorFeignClient doctors;

    public ParticipantClient(PetFeignClient pets, DoctorFeignClient doctors) {
        this.pets = pets;
        this.doctors = doctors;
    }

    public RemotePet requirePet(Long id) {
        RemotePet pet = fetch(() -> pets.getById(id), "宠物");
        if (pet == null || !id.equals(pet.id())) {
            throw invalidResponse("宠物");
        }
        return pet;
    }

    public RemoteDoctor requireDoctor(Long id) {
        RemoteDoctor doctor = fetch(() -> doctors.getById(id), "医生");
        if (doctor == null || !id.equals(doctor.id()) || doctor.dayOfWeek() == null
                || doctor.startTime() == null || doctor.endTime() == null || doctor.accepting() == null
                || !doctor.startTime().isBefore(doctor.endTime())) {
            throw invalidResponse("医生");
        }
        return doctor;
    }

    private <T> T fetch(Supplier<T> request, String resource) {
        try {
            return request.get();
        } catch (DecodeException exception) {
            throw invalidResponse(resource);
        } catch (RetryableException exception) {
            throw new BusinessException(HttpStatus.SERVICE_UNAVAILABLE, resource + "服务连接或读取失败，请稍后重试");
        } catch (FeignException.NotFound exception) {
            throw BusinessException.notFound(resource + "不存在");
        } catch (FeignException exception) {
            if (exception.status() >= 500) {
                throw new BusinessException(HttpStatus.SERVICE_UNAVAILABLE, resource + "服务暂不可用，请稍后重试");
            }
            throw new BusinessException(HttpStatus.BAD_GATEWAY, resource + "服务返回了异常响应");
        }
    }

    private BusinessException invalidResponse(String resource) {
        return new BusinessException(HttpStatus.BAD_GATEWAY, resource + "服务返回了无效数据");
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RemotePet(Long id) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RemoteDoctor(Long id, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime,
                               Boolean accepting) {}
}
