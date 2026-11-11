package com.zjgsu.kl.client;

import com.zjgsu.kl.client.ParticipantClient.RemotePet;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "pet-service")
public interface PetFeignClient {
    @GetMapping("/api/pets/{id}")
    RemotePet getById(@PathVariable("id") Long id);
}
