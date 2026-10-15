package com.zjgsu.kl.controller;

import java.net.URI;
import java.util.List;
import com.zjgsu.kl.dto.AppointmentDtos.*;
import com.zjgsu.kl.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {
    private final AppointmentService service;

    public AppointmentController(AppointmentService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<Response> create(@Valid @RequestBody CreateRequest request) {
        Response response = service.create(request);
        return ResponseEntity.created(URI.create("/api/appointments/" + response.id())).body(response);
    }

    @GetMapping("/{id}")
    public Response get(@PathVariable Long id) { return service.get(id); }

    @GetMapping
    public List<Response> list(@RequestParam(required = false) Long petId) { return service.list(petId); }

    @PostMapping("/{id}/confirm")
    public Response confirm(@PathVariable Long id) { return service.confirm(id); }

    @PostMapping("/{id}/complete")
    public Response complete(@PathVariable Long id) { return service.complete(id); }

    @PostMapping("/{id}/cancel")
    public Response cancel(@PathVariable Long id) { return service.cancel(id); }
}
