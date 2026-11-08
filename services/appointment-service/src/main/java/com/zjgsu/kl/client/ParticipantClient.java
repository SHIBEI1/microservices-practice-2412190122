package com.zjgsu.kl.client;

import java.net.URI;
import java.time.DayOfWeek;
import java.time.LocalTime;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import com.zjgsu.kl.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class ParticipantClient {
    private static final Logger log = LoggerFactory.getLogger(ParticipantClient.class);
    private final RestTemplate restTemplate;
    private final DiscoveryClient discoveryClient;

    public ParticipantClient(RestTemplate restTemplate, DiscoveryClient discoveryClient) {
        this.restTemplate = restTemplate;
        this.discoveryClient = discoveryClient;
    }

    public RemotePet requirePet(Long id) {
        RemotePet pet = fetch("pet-service", "/api/pets/{id}", id, RemotePet.class, "宠物");
        if (pet == null || !id.equals(pet.id())) {
            throw invalidResponse("宠物");
        }
        return pet;
    }

    public RemoteDoctor requireDoctor(Long id) {
        RemoteDoctor doctor = fetch("doctor-service", "/api/doctors/{id}", id, RemoteDoctor.class, "医生");
        if (doctor == null || !id.equals(doctor.id()) || doctor.dayOfWeek() == null
                || doctor.startTime() == null || doctor.endTime() == null || doctor.accepting() == null
                || !doctor.startTime().isBefore(doctor.endTime())) {
            throw invalidResponse("医生");
        }
        return doctor;
    }

    private URI instanceUri(String serviceName, String resource) {
        List<ServiceInstance> instances;
        try {
            instances = discoveryClient.getInstances(serviceName);
        } catch (RuntimeException exception) {
            throw new BusinessException(HttpStatus.SERVICE_UNAVAILABLE, resource + "服务发现失败，请稍后重试");
        }
        if (instances.isEmpty()) {
            throw new BusinessException(HttpStatus.SERVICE_UNAVAILABLE, resource + "服务没有可用实例，请稍后重试");
        }
        URI uri = instances.getFirst().getUri();
        log.info("Nacos服务发现 {} -> {}", serviceName, uri);
        return uri;
    }

    private <T> T fetch(String serviceName, String path, Long id, Class<T> type, String resource) {
        String url = instanceUri(serviceName, resource) + path;
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
