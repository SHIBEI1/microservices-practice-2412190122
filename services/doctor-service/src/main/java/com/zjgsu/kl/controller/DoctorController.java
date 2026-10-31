package com.zjgsu.kl.controller;

import java.net.URI;
import java.util.List;
import com.zjgsu.kl.dto.DoctorDtos.*;
import com.zjgsu.kl.service.DoctorService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {
    private final DoctorService service;

    public DoctorController(DoctorService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<Response> create(@Valid @RequestBody CreateRequest request) {
        Response response = service.create(request);
        return ResponseEntity.created(URI.create("/api/doctors/" + response.id())).body(response);
    }

    @GetMapping("/{id}")
    public Response get(@PathVariable Long id) { return service.get(id); }

    @GetMapping
    public List<Response> list() { return service.list(); }
}
