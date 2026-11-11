package com.zjgsu.kl.client;

import com.zjgsu.kl.client.ParticipantClient.RemoteDoctor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "doctor-service")
public interface DoctorFeignClient {
    @GetMapping("/api/doctors/{id}")
    RemoteDoctor getById(@PathVariable("id") Long id);
}
