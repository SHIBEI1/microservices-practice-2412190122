package com.zjgsu.kl;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.Instant;
import java.time.ZoneId;
import com.jayway.jsonpath.JsonPath;
import com.zjgsu.kl.client.DoctorFeignClient;
import com.zjgsu.kl.client.PetFeignClient;
import com.zjgsu.kl.client.ParticipantClient.RemoteDoctor;
import com.zjgsu.kl.client.ParticipantClient.RemotePet;
import feign.FeignException;
import feign.RetryableException;
import feign.codec.DecodeException;
import com.zjgsu.kl.repository.AppointmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(AppointmentServiceTests.TimeConfiguration.class)
class AppointmentServiceTests {
    @MockitoBean PetFeignClient pets;
    @MockitoBean DoctorFeignClient doctors;
    @Autowired MockMvc mvc;
    @Autowired AppointmentRepository repository;
    @Autowired MutableClock clock;

    @BeforeEach
    void prepare() {
        clock.current = Instant.parse("2030-01-07T00:00:00Z");
    }

    void pet(long id) {
        when(pets.getById(id)).thenReturn(new RemotePet(id));
    }

    void doctor(long id) {
        when(doctors.getById(id)).thenReturn(new RemoteDoctor(
            id, DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(17, 0), true));
    }

    FeignException upstreamFailure(int status) {
        FeignException exception = mock(FeignException.class);
        when(exception.status()).thenReturn(status);
        return exception;
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
    void creationValidatesBothParticipantsBeforeWriting() throws Exception {
        pet(11);
        doctor(21);
        long id = create();
        assertEquals(11L, repository.findById(id).orElseThrow().getPetId());
        mvc.perform(get("/api/appointments/{id}", id)).andExpect(status().isOk())
            .andExpect(jsonPath("$.petId").value(11)).andExpect(jsonPath("$.doctorId").value(21))
            .andExpect(jsonPath("$.status").value("PENDING"));
        var calls = inOrder(pets, doctors);
        calls.verify(pets).getById(11L);
        calls.verify(doctors).getById(21L);
    }

    @Test
    void missingPetRejectsCreationWithoutWritingOrCallingDoctor() throws Exception {
        when(pets.getById(11L)).thenThrow(mock(FeignException.NotFound.class));
        create(11, 21, "2030-01-07T10:00:00").andExpect(status().isNotFound())
            .andExpect(jsonPath("$.detail").value("宠物不存在"));
        assertEquals(0, repository.count());
        verifyNoInteractions(doctors);
    }

    @Test
    void missingDoctorRejectsCreationWithoutWriting() throws Exception {
        pet(11);
        when(doctors.getById(21L)).thenThrow(mock(FeignException.NotFound.class));
        create(11, 21, "2030-01-07T10:00:00").andExpect(status().isNotFound());
        assertEquals(0, repository.count());
    }

    @Test
    void unavailablePetReturns503WithoutWriting() throws Exception {
        doThrow(upstreamFailure(503)).when(pets).getById(11L);
        create(11, 21, "2030-01-07T10:00:00").andExpect(status().isServiceUnavailable());
        assertEquals(0, repository.count());
    }

    @Test
    void unavailableDoctorReturns503WithoutWriting() throws Exception {
        pet(11);
        doThrow(upstreamFailure(503)).when(doctors).getById(21L);
        create(11, 21, "2030-01-07T10:00:00").andExpect(status().isServiceUnavailable());
        assertEquals(0, repository.count());
    }

    @Test
    void timeoutReturns503WithoutSkippingValidation() throws Exception {
        when(pets.getById(11L)).thenThrow(mock(RetryableException.class));
        create(11, 21, "2030-01-07T10:00:00").andExpect(status().isServiceUnavailable());
        assertEquals(0, repository.count());
    }

    @Test
    void invalidRemoteResponseReturns502WithoutWriting() throws Exception {
        when(pets.getById(11L)).thenReturn(new RemotePet(99L));
        create(11, 21, "2030-01-07T10:00:00").andExpect(status().isBadGateway());
        assertEquals(0, repository.count());
    }

    @Test
    void doctorSlotCannotBeBookedTwice() throws Exception {
        pet(11);
        pet(12);
        doctor(21);
        create();
        create(12, 21, "2030-01-07T10:00:00").andExpect(status().isConflict());
        assertEquals(1, repository.count());
    }

    @Test
    void petCannotBookTwoDoctorsAtTheSameTime() throws Exception {
        pet(11);
        doctor(21);
        doctor(22);
        create();
        create(11, 22, "2030-01-07T10:00:00").andExpect(status().isConflict());
        assertEquals(1, repository.count());
    }

    @Test
    void timeMustBeFutureAlignedAndWithinWorkingHours() throws Exception {
        pet(11);
        doctor(21);
        for (String time : new String[]{"2029-12-31T10:00:00", "2030-01-07T10:15:00", "2030-01-07T17:00:00"}) {
            create(11, 21, time).andExpect(status().isConflict());
        }
        assertEquals(0, repository.count());
    }

    @Test
    void confirmedAppointmentCanOnlyBeCompletedAtItsStartTime() throws Exception {
        pet(11);
        doctor(21);
        long id = create();
        mvc.perform(post("/api/appointments/{id}/confirm", id)).andExpect(status().isOk());
        mvc.perform(post("/api/appointments/{id}/complete", id)).andExpect(status().isConflict());
        clock.current = Instant.parse("2030-01-07T02:00:00Z");
        mvc.perform(post("/api/appointments/{id}/complete", id)).andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("COMPLETED"));
        mvc.perform(post("/api/appointments/{id}/cancel", id)).andExpect(status().isConflict());
    }

    @Test
    void cancellationRetainsHistoryAndReleasesSlot() throws Exception {
        pet(11);
        doctor(21);
        long id = create();
        mvc.perform(post("/api/appointments/{id}/cancel", id)).andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("CANCELLED"));
        create();
        assertEquals(2, repository.count());
        assertEquals(null, repository.findById(id).orElseThrow().getActiveSlot());
    }

    @Test
    void historicalReadsDoNotDependOnParticipantServices() throws Exception {
        pet(11);
        doctor(21);
        long id = create();
        clearInvocations(pets, doctors);
        mvc.perform(get("/api/appointments/{id}", id)).andExpect(status().isOk());
        mvc.perform(get("/api/appointments").param("petId", "11")).andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1));
        verifyNoInteractions(pets, doctors);
    }

    @Test
    void invalidInputAndMissingAppointmentHaveClearErrors() throws Exception {
        mvc.perform(post("/api/appointments").contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isBadRequest());
        mvc.perform(get("/api/appointments/999999")).andExpect(status().isNotFound());
        assertEquals(0, repository.count());
    }

    @Test
    void undecodablePetReturns502WithoutWriting() throws Exception {
        when(pets.getById(11L)).thenThrow(mock(DecodeException.class));
        create(11, 21, "2030-01-07T10:00:00").andExpect(status().isBadGateway())
            .andExpect(jsonPath("$.detail").value("宠物服务返回了无效数据"));
        assertEquals(0, repository.count());
        verifyNoInteractions(doctors);
    }

    @Test
    void unexpectedUpstreamClientErrorReturns502WithoutWriting() throws Exception {
        doThrow(upstreamFailure(401)).when(pets).getById(11L);
        create(11, 21, "2030-01-07T10:00:00").andExpect(status().isBadGateway());
        assertEquals(0, repository.count());
        verifyNoInteractions(doctors);
    }

    @Test
    void invalidDoctorScheduleReturns502WithoutWriting() throws Exception {
        pet(11);
        when(doctors.getById(21L)).thenReturn(new RemoteDoctor(
            21L, DayOfWeek.MONDAY, LocalTime.of(17, 0), LocalTime.of(9, 0), true));
        create(11, 21, "2030-01-07T10:00:00").andExpect(status().isBadGateway());
        assertEquals(0, repository.count());
    }

    @Test
    void emptyPetResponseReturns502WithoutWriting() throws Exception {
        create(11, 21, "2030-01-07T10:00:00").andExpect(status().isBadGateway());
        assertEquals(0, repository.count());
        verifyNoInteractions(doctors);
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
