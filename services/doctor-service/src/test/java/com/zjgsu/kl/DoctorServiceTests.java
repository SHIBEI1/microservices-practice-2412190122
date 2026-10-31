package com.zjgsu.kl;

import com.jayway.jsonpath.JsonPath;
import com.zjgsu.kl.repository.DoctorRepository;
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
class DoctorServiceTests {
    @Autowired MockMvc mvc;
    @Autowired DoctorRepository repository;

    private String request(String code, String start, String end) {
        return """
            {"code":"%s","name":"林医生","department":"内科","dayOfWeek":"MONDAY",
             "startTime":"%s","endTime":"%s","accepting":true}
            """.formatted(code, start, end);
    }

    @Test
    void healthIsAvailable() throws Exception {
        mvc.perform(get("/actuator/health")).andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void doctorCanBeCreatedAndReadFromOwnRepository() throws Exception {
        String response = mvc.perform(post("/api/doctors").contentType(MediaType.APPLICATION_JSON)
            .content(request("doc001", "09:00", "17:00"))).andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        long id = ((Number) JsonPath.read(response, "$.id")).longValue();
        assertEquals("DOC001", repository.findById(id).orElseThrow().getCode());
        mvc.perform(get("/api/doctors/{id}", id)).andExpect(status().isOk())
            .andExpect(jsonPath("$.dayOfWeek").value("MONDAY"));
        mvc.perform(get("/api/doctors")).andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void doctorCodeMustBeUniqueIgnoringCase() throws Exception {
        mvc.perform(post("/api/doctors").contentType(MediaType.APPLICATION_JSON)
            .content(request("DOC001", "09:00", "17:00"))).andExpect(status().isCreated());
        mvc.perform(post("/api/doctors").contentType(MediaType.APPLICATION_JSON)
            .content(request("doc001", "09:00", "17:00"))).andExpect(status().isConflict());
        assertEquals(1, repository.count());
    }

    @Test
    void invalidWorkingHoursAreRejected() throws Exception {
        for (String[] hours : new String[][]{{"17:00", "09:00"}, {"09:15", "17:00"}}) {
            mvc.perform(post("/api/doctors").contentType(MediaType.APPLICATION_JSON)
                .content(request("DOC001", hours[0], hours[1]))).andExpect(status().isBadRequest());
        }
        assertEquals(0, repository.count());
    }

    @Test
    void missingDoctorReturnsNotFound() throws Exception {
        mvc.perform(get("/api/doctors/999999")).andExpect(status().isNotFound());
    }
}
