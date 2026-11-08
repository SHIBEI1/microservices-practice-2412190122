package com.zjgsu.kl;

import java.net.SocketTimeoutException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import com.jayway.jsonpath.JsonPath;
import com.zjgsu.kl.repository.AppointmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(AppointmentServiceTests.TimeConfiguration.class)
class AppointmentServiceTests {
    @MockitoBean DiscoveryClient discoveryClient;
    @Autowired MockMvc mvc;
    @Autowired RestTemplate restTemplate;
    @Autowired AppointmentRepository repository;
    @Autowired MutableClock clock;
    MockRestServiceServer remote;

    @BeforeEach
    void prepare() {
        remote = MockRestServiceServer.bindTo(restTemplate).ignoreExpectOrder(true).build();
        clock.current = Instant.parse("2030-01-07T00:00:00Z");
        when(discoveryClient.getInstances("pet-service")).thenReturn(List.of(
            new DefaultServiceInstance("pet-test", "pet-service", "192.0.2.11", 8080, false)));
        when(discoveryClient.getInstances("doctor-service")).thenReturn(List.of(
            new DefaultServiceInstance("doctor-test", "doctor-service", "192.0.2.12", 8080, false)));
    }

    void pet(long id, int count) {
        remote.expect(ExpectedCount.times(count), requestTo("http://192.0.2.11:8080/api/pets/" + id))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess("{\"id\":" + id + ",\"name\":\"团团\"}", MediaType.APPLICATION_JSON));
    }

    void doctor(long id, int count) {
        remote.expect(ExpectedCount.times(count), requestTo("http://192.0.2.12:8080/api/doctors/" + id))
            .andExpect(method(HttpMethod.GET)).andRespond(withSuccess("""
                {"id":%d,"code":"DOC001","dayOfWeek":"MONDAY","startTime":"09:00:00",
                 "endTime":"17:00:00","accepting":true}
                """.formatted(id), MediaType.APPLICATION_JSON));
    }

    String body(long petId, long doctorId, String time) {
        return """
            {"petId":%d,"doctorId":%d,"appointmentTime":"%s","reason":"常规体检"}
            """.formatted(petId, doctorId, time);
    }

    ResultActions create(long petId, long doctorId, String time) throws Exception {
        return mvc.perform(post("/api/appointments").contentType(MediaType.APPLICATION_JSON)
            .content(body(petId, doctorId, time)));
    }

    long create() throws Exception {
        String response = create(11, 21, "2030-01-07T10:00:00").andExpect(status().isCreated())
            .andExpect(header().exists("Location")).andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(response, "$.id")).longValue();
    }

    @Test
    void healthIsAvailable() throws Exception {
        mvc.perform(get("/actuator/health")).andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void creationValidatesBothParticipantsOverHttpBeforeWriting() throws Exception {
        pet(11, 1);
        doctor(21, 1);
        long id = create();
        assertEquals(11L, repository.findById(id).orElseThrow().getPetId());
        mvc.perform(get("/api/appointments/{id}", id)).andExpect(status().isOk())
            .andExpect(jsonPath("$.petId").value(11)).andExpect(jsonPath("$.doctorId").value(21))
            .andExpect(jsonPath("$.status").value("PENDING"));
        remote.verify();
    }

    @Test
    void missingPetRejectsCreationWithoutWritingOrCallingDoctor() throws Exception {
        remote.expect(requestTo("http://192.0.2.11:8080/api/pets/11")).andRespond(withStatus(HttpStatus.NOT_FOUND));
        create(11, 21, "2030-01-07T10:00:00").andExpect(status().isNotFound())
            .andExpect(jsonPath("$.detail").value("宠物不存在"));
        assertEquals(0, repository.count());
        remote.verify();
    }

    @Test
    void missingDoctorRejectsCreationWithoutWriting() throws Exception {
        pet(11, 1);
        remote.expect(requestTo("http://192.0.2.12:8080/api/doctors/21"))
            .andRespond(withStatus(HttpStatus.NOT_FOUND));
        create(11, 21, "2030-01-07T10:00:00").andExpect(status().isNotFound());
        assertEquals(0, repository.count());
        remote.verify();
    }

    @Test
    void unavailablePetReturns503WithoutWriting() throws Exception {
        remote.expect(requestTo("http://192.0.2.11:8080/api/pets/11"))
            .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));
        create(11, 21, "2030-01-07T10:00:00").andExpect(status().isServiceUnavailable());
        assertEquals(0, repository.count());
        remote.verify();
    }

    @Test
    void unavailableDoctorReturns503WithoutWriting() throws Exception {
        pet(11, 1);
        remote.expect(requestTo("http://192.0.2.12:8080/api/doctors/21"))
            .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));
        create(11, 21, "2030-01-07T10:00:00").andExpect(status().isServiceUnavailable());
        assertEquals(0, repository.count());
        remote.verify();
    }

    @Test
    void timeoutReturns503WithoutSkippingValidation() throws Exception {
        remote.expect(requestTo("http://192.0.2.11:8080/api/pets/11"))
            .andRespond(withException(new SocketTimeoutException("read timed out")));
        create(11, 21, "2030-01-07T10:00:00").andExpect(status().isServiceUnavailable());
        assertEquals(0, repository.count());
        remote.verify();
    }

    @Test
    void invalidRemoteResponseReturns502WithoutWriting() throws Exception {
        remote.expect(requestTo("http://192.0.2.11:8080/api/pets/11"))
            .andRespond(withSuccess("{\"id\":99}", MediaType.APPLICATION_JSON));
        create(11, 21, "2030-01-07T10:00:00").andExpect(status().isBadGateway());
        assertEquals(0, repository.count());
        remote.verify();
    }

    @Test
    void doctorSlotCannotBeBookedTwice() throws Exception {
        pet(11, 1);
        pet(12, 1);
        doctor(21, 2);
        create();
        create(12, 21, "2030-01-07T10:00:00").andExpect(status().isConflict());
        assertEquals(1, repository.count());
        remote.verify();
    }

    @Test
    void petCannotBookTwoDoctorsAtTheSameTime() throws Exception {
        pet(11, 2);
        doctor(21, 1);
        doctor(22, 1);
        create();
        create(11, 22, "2030-01-07T10:00:00").andExpect(status().isConflict());
        assertEquals(1, repository.count());
        remote.verify();
    }

    @Test
    void timeMustBeFutureAlignedAndWithinWorkingHours() throws Exception {
        pet(11, 3);
        doctor(21, 3);
        for (String time : new String[]{"2029-12-31T10:00:00", "2030-01-07T10:15:00", "2030-01-07T17:00:00"}) {
            create(11, 21, time).andExpect(status().isConflict());
        }
        assertEquals(0, repository.count());
        remote.verify();
    }

    @Test
    void confirmedAppointmentCanOnlyBeCompletedAtItsStartTime() throws Exception {
        pet(11, 1);
        doctor(21, 1);
        long id = create();
        mvc.perform(post("/api/appointments/{id}/confirm", id)).andExpect(status().isOk());
        mvc.perform(post("/api/appointments/{id}/complete", id)).andExpect(status().isConflict());
        clock.current = Instant.parse("2030-01-07T02:00:00Z");
        mvc.perform(post("/api/appointments/{id}/complete", id)).andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("COMPLETED"));
        mvc.perform(post("/api/appointments/{id}/cancel", id)).andExpect(status().isConflict());
        remote.verify();
    }

    @Test
    void cancellationRetainsHistoryAndReleasesSlot() throws Exception {
        pet(11, 2);
        doctor(21, 2);
        long id = create();
        mvc.perform(post("/api/appointments/{id}/cancel", id)).andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("CANCELLED"));
        create();
        assertEquals(2, repository.count());
        assertEquals(null, repository.findById(id).orElseThrow().getActiveSlot());
        remote.verify();
    }

    @Test
    void historicalReadsDoNotDependOnParticipantServices() throws Exception {
        pet(11, 1);
        doctor(21, 1);
        long id = create();
        remote.verify();
        remote.reset();
        mvc.perform(get("/api/appointments/{id}", id)).andExpect(status().isOk());
        mvc.perform(get("/api/appointments").param("petId", "11")).andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1));
        remote.verify();
    }

    @Test
    void invalidInputAndMissingAppointmentHaveClearErrors() throws Exception {
        mvc.perform(post("/api/appointments").contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isBadRequest());
        mvc.perform(get("/api/appointments/999999")).andExpect(status().isNotFound());
        assertEquals(0, repository.count());
        remote.verify();
    }

    @Test
    void missingPetInstancesReturn503WithoutWritingOrCallingDoctor() throws Exception {
        when(discoveryClient.getInstances("pet-service")).thenReturn(List.of());
        create(11, 21, "2030-01-07T10:00:00").andExpect(status().isServiceUnavailable())
            .andExpect(jsonPath("$.detail").value("宠物服务没有可用实例，请稍后重试"));
        assertEquals(0, repository.count());
        verify(discoveryClient, never()).getInstances("doctor-service");
        remote.verify();
    }

    @Test
    void missingDoctorInstancesReturn503WithoutWriting() throws Exception {
        pet(11, 1);
        when(discoveryClient.getInstances("doctor-service")).thenReturn(List.of());
        create(11, 21, "2030-01-07T10:00:00").andExpect(status().isServiceUnavailable())
            .andExpect(jsonPath("$.detail").value("医生服务没有可用实例，请稍后重试"));
        assertEquals(0, repository.count());
        remote.verify();
    }

    @Test
    void discoveryFailureReturns503WithoutWriting() throws Exception {
        when(discoveryClient.getInstances("pet-service")).thenThrow(new IllegalStateException("registry unavailable"));
        create(11, 21, "2030-01-07T10:00:00").andExpect(status().isServiceUnavailable())
            .andExpect(jsonPath("$.detail").value("宠物服务发现失败，请稍后重试"));
        assertEquals(0, repository.count());
        remote.verify();
    }

    @Test
    void eachCreationUsesTheCurrentDiscoveredInstance() throws Exception {
        pet(11, 1);
        doctor(21, 1);
        create();
        remote.verify();
        remote.reset();

        when(discoveryClient.getInstances("pet-service")).thenReturn(List.of(
            new DefaultServiceInstance("pet-new", "pet-service", "192.0.2.13", 9090, false)));
        remote.expect(requestTo("http://192.0.2.13:9090/api/pets/11"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess("{\"id\":11}", MediaType.APPLICATION_JSON));
        doctor(21, 1);
        create(11, 21, "2030-01-07T10:30:00").andExpect(status().isCreated());
        assertEquals(2, repository.count());
        verify(discoveryClient, times(2)).getInstances("pet-service");
        verify(discoveryClient, times(2)).getInstances("doctor-service");
        remote.verify();
    }

    @TestConfiguration
    static class TimeConfiguration {
        @Bean @Primary
        MutableClock testClock() { return new MutableClock(); }
    }

    static class MutableClock extends Clock {
        Instant current = Instant.parse("2030-01-07T00:00:00Z");
        public ZoneId getZone() { return ZoneId.of("Asia/Shanghai"); }
        public Clock withZone(ZoneId zone) { return Clock.fixed(current, zone); }
        public Instant instant() { return current; }
    }
}
