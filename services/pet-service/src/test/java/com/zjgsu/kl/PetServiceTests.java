package com.zjgsu.kl;

import com.jayway.jsonpath.JsonPath;
import com.zjgsu.kl.repository.PetRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PetServiceTests {
    @Autowired MockMvc mvc;
    @Autowired PetRepository repository;

    @Test
    void healthIsAvailable() throws Exception {
        mvc.perform(get("/actuator/health")).andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void petCanBeCreatedAndReadFromOwnRepository() throws Exception {
        String response = mvc.perform(post("/api/pets").contentType(MediaType.APPLICATION_JSON).content("""
            {"name":"团团","species":"猫","age":2,"ownerName":"孔磊","ownerPhone":"13800000000","healthNotes":"已接种疫苗"}
            """)).andExpect(status().isCreated()).andExpect(header().exists("Location"))
            .andReturn().getResponse().getContentAsString();
        long id = ((Number) JsonPath.read(response, "$.id")).longValue();
        assertEquals("团团", repository.findById(id).orElseThrow().getName());
        mvc.perform(get("/api/pets/{id}", id)).andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("团团"));
        mvc.perform(get("/api/pets")).andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void invalidInputIsRejectedWithoutWriting() throws Exception {
        mvc.perform(post("/api/pets").contentType(MediaType.APPLICATION_JSON).content("""
            {"name":"","species":"猫","age":-1,"ownerName":"孔磊","ownerPhone":"123"}
            """)).andExpect(status().isBadRequest());
        assertEquals(0, repository.count());
    }

    @Test
    void missingPetReturnsNotFound() throws Exception {
        mvc.perform(get("/api/pets/999999")).andExpect(status().isNotFound())
            .andExpect(jsonPath("$.detail").value("宠物不存在"));
    }
}
