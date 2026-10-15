package com.zjgsu.kl.service;

import java.util.List;
import com.zjgsu.kl.dto.PetDtos.*;
import com.zjgsu.kl.exception.BusinessException;
import com.zjgsu.kl.model.Pet;
import com.zjgsu.kl.repository.PetRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PetService {
    private final PetRepository pets;

    public PetService(PetRepository pets) {
        this.pets = pets;
    }

    @Transactional
    public Response create(CreateRequest request) {
        return Response.from(pets.save(new Pet(request.name().strip(), request.species().strip(), request.age(),
            request.ownerName().strip(), request.ownerPhone(), request.healthNotes())));
    }

    public Response get(Long id) {
        return Response.from(pets.findById(id).orElseThrow(() -> BusinessException.notFound("宠物不存在")));
    }

    public List<Response> list() {
        return pets.findAll(Sort.by("id")).stream().map(Response::from).toList();
    }
}
