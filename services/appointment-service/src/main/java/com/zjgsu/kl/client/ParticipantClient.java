package com.zjgsu.kl.client;

import java.time.DayOfWeek;
import java.time.LocalTime;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.zjgsu.kl.config.ClientProperties;
import com.zjgsu.kl.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class ParticipantClient {
    private final RestTemplate restTemplate;
    private final ClientProperties properties;

    public ParticipantClient(RestTemplate restTemplate, ClientProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    public RemotePet requirePet(Long id) {
        RemotePet pet = fetch(properties.petBaseUrl() + "/api/pets/{id}", id, RemotePet.class, "宠物");
        if (pet == null || !id.equals(pet.id())) {
            throw invalidResponse("宠物");
        }
        return pet;
    }

    public RemoteDoctor requireDoctor(Long id) {
        RemoteDoctor doctor = fetch(properties.doctorBaseUrl() + "/api/doctors/{id}", id, RemoteDoctor.class, "医生");
        if (doctor == null || !id.equals(doctor.id()) || doctor.dayOfWeek() == null
                || doctor.startTime() == null || doctor.endTime() == null || doctor.accepting() == null
                || !doctor.startTime().isBefore(doctor.endTime())) {
            throw invalidResponse("医生");
        }
        return doctor;
    }

    private <T> T fetch(String url, Long id, Class<T> type, String resource) {
        try {
            return restTemplate.getForObject(url, type, id);
        } catch (HttpClientErrorException.NotFound exception) {
            throw BusinessException.notFound(resource + "不存在");
        } catch (ResourceAccessException exception) {
            throw new BusinessException(HttpStatus.SERVICE_UNAVAILABLE, resource + "服务连接或读取失败，请稍后重试");
        } catch (HttpStatusCodeException exception) {
            HttpStatus status = exception.getStatusCode().is5xxServerError()
                ? HttpStatus.SERVICE_UNAVAILABLE : HttpStatus.BAD_GATEWAY;
            throw new BusinessException(status, resource + "服务暂不可用");
        } catch (RestClientException exception) {
            throw invalidResponse(resource);
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
