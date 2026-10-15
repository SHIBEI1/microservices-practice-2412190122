package com.zjgsu.kl.controller;

import java.net.URI;
import java.util.List;
import com.zjgsu.kl.dto.PetDtos.*;
import com.zjgsu.kl.service.PetService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pets")
public class PetController {
    private final PetService service;

    public PetController(PetService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<Response> create(@Valid @RequestBody CreateRequest request) {
        Response response = service.create(request);
        return ResponseEntity.created(URI.create("/api/pets/" + response.id())).body(response);
    }

    @GetMapping("/{id}")
    public Response get(@PathVariable Long id) { return service.get(id); }

    @GetMapping
    public List<Response> list() { return service.list(); }
}
