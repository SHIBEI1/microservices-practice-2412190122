package com.zjgsu.kl;

import com.jayway.jsonpath.JsonPath;
import jakarta.persistence.EntityManager;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(PetHospitalApiTests.TimeConfiguration.class)
class PetHospitalApiTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private MutableClock clock;

    @BeforeEach
    void resetClock() {
        clock.setInstant(Instant.parse("2030-01-07T00:00:00Z"));
    }

    @Test
    void petCanBeCreatedAndReadFromDatabase() throws Exception {
        long petId = pet("Mimi");
        entityManager.flush();
        entityManager.clear();

        mvc.perform(get("/api/pets/{id}", petId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(petId))
                .andExpect(jsonPath("$.name").value("Mimi"))
                .andExpect(jsonPath("$.species").value("CAT"))
                .andExpect(jsonPath("$.ownerPhone").value("13800138000"));
        mvc.perform(get("/api/pets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void doctorCanBeCreatedAndReadFromDatabase() throws Exception {
        long doctorId = doctor("D001");
        entityManager.flush();
        entityManager.clear();

        mvc.perform(get("/api/doctors/{id}", doctorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("D001"))
                .andExpect(jsonPath("$.dayOfWeek").value("MONDAY"))
                .andExpect(jsonPath("$.accepting").value(true));
        mvc.perform(get("/api/doctors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void invalidPetReturnsValidationProblem() throws Exception {
        postJson("/api/pets", """
                {"name":"","species":"CAT","age":-1,
                 "ownerName":"Owner","ownerPhone":"123","healthNotes":""}
                """)
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400));
        mvc.perform(get("/api/pets"))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void missingResourcesReturnNotFound() throws Exception {
        for (String resource : new String[]{"pets", "doctors", "appointments"}) {
            mvc.perform(get("/api/" + resource + "/999999"))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.status").value(404));
        }
    }

    @Test
    void doctorCodeMustBeUnique() throws Exception {
        doctor("D001");
        postJson("/api/doctors", doctorJson("D001", "09:00", "17:00", true))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
        mvc.perform(get("/api/doctors"))
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void doctorScheduleRequiresAnOrderedHalfHourInterval() throws Exception {
        for (String[] interval : new String[][]{
                {"17:00", "09:00"},
                {"09:00", "09:00"},
                {"09:15", "17:00"},
                {"09:00", "17:15"},
                {"09:00:01", "17:00"}
        }) {
            postJson("/api/doctors", doctorJson("D001", interval[0], interval[1], true))
                    .andExpect(status().isBadRequest());
        }
        mvc.perform(get("/api/doctors"))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void appointmentCanBeConfirmedAndCompletedWithPersistentState() throws Exception {
        long petId = pet("Mimi");
        long doctorId = doctor("D001");
        long appointmentId = appointment(petId, doctorId, "2030-01-07T10:00:00");

        mvc.perform(get("/api/appointments/{id}", appointmentId))
                .andExpect(jsonPath("$.status").value("PENDING"));
        postJson("/api/appointments/" + appointmentId + "/confirm", "")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
        postJson("/api/appointments/" + appointmentId + "/complete", "")
                .andExpect(status().isConflict());

        clock.setInstant(Instant.parse("2030-01-07T02:00:00Z"));
        postJson("/api/appointments/" + appointmentId + "/complete", "")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
        entityManager.flush();
        entityManager.clear();

        mvc.perform(get("/api/appointments/{id}", appointmentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.petId").value(petId))
                .andExpect(jsonPath("$.doctorId").value(doctorId))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
        postJson("/api/appointments/" + appointmentId + "/cancel", "")
                .andExpect(status().isConflict());
    }

    @Test
    void doctorCannotAcceptTwoPetsInTheSameSlot() throws Exception {
        long firstPetId = pet("Mimi");
        long secondPetId = pet("Lucky");
        long doctorId = doctor("D001");
        appointment(firstPetId, doctorId, "2030-01-07T10:00:00");

        book(secondPetId, doctorId, "2030-01-07T10:00:00")
                .andExpect(status().isConflict());
        mvc.perform(get("/api/appointments"))
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void petCannotBookTwoDoctorsInTheSameSlot() throws Exception {
        long petId = pet("Mimi");
        long firstDoctorId = doctor("D001");
        long secondDoctorId = doctor("D002");
        appointment(petId, firstDoctorId, "2030-01-07T10:00:00");

        book(petId, secondDoctorId, "2030-01-07T10:00:00")
                .andExpect(status().isConflict());
    }

    @Test
    void cancellationRetainsHistoryAndReleasesTheSlot() throws Exception {
        long petId = pet("Mimi");
        long doctorId = doctor("D001");
        long originalId = appointment(petId, doctorId, "2030-01-07T10:00:00");

        postJson("/api/appointments/" + originalId + "/cancel", "")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
        long replacementId = appointment(petId, doctorId, "2030-01-07T10:00:00");
        entityManager.flush();
        entityManager.clear();

        mvc.perform(get("/api/appointments/{id}", originalId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
        mvc.perform(get("/api/appointments/{id}", replacementId))
                .andExpect(jsonPath("$.status").value("PENDING"));
        mvc.perform(get("/api/appointments").param("petId", Long.toString(petId)))
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void confirmedAppointmentCanBeCancelledBeforeItsStart() throws Exception {
        long appointmentId = appointment(pet("Mimi"), doctor("D001"), "2030-01-07T10:00:00");
        postJson("/api/appointments/" + appointmentId + "/confirm", "")
                .andExpect(status().isOk());
        postJson("/api/appointments/" + appointmentId + "/cancel", "")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void invalidStateTransitionsAreRejected() throws Exception {
        long appointmentId = appointment(pet("Mimi"), doctor("D001"), "2030-01-07T10:00:00");
        postJson("/api/appointments/" + appointmentId + "/complete", "")
                .andExpect(status().isConflict());
        postJson("/api/appointments/" + appointmentId + "/confirm", "")
                .andExpect(status().isOk());
        postJson("/api/appointments/" + appointmentId + "/confirm", "")
                .andExpect(status().isConflict());
        postJson("/api/appointments/" + appointmentId + "/cancel", "")
                .andExpect(status().isOk());
        postJson("/api/appointments/" + appointmentId + "/cancel", "")
                .andExpect(status().isConflict());
        postJson("/api/appointments/" + appointmentId + "/confirm", "")
                .andExpect(status().isConflict());
    }

    @Test
    void unavailableDoctorCannotBeBooked() throws Exception {
        long petId = pet("Mimi");
        long doctorId = created("/api/doctors", doctorJson("D001", "09:00", "17:00", false));
        book(petId, doctorId, "2030-01-07T10:00:00")
                .andExpect(status().isConflict());
    }

    @Test
    void missingAppointmentParticipantsAreRejected() throws Exception {
        long petId = pet("Mimi");
        long doctorId = doctor("D001");
        book(999999, doctorId, "2030-01-07T10:00:00")
                .andExpect(status().isNotFound());
        book(petId, 999999, "2030-01-07T10:00:00")
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/appointments"))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void appointmentRequestRequiresParticipantsTimeAndReason() throws Exception {
        postJson("/api/appointments", """
                {"petId":null,"doctorId":null,"appointmentTime":null,"reason":""}
                """)
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
        postJson("/api/appointments", """
                {"petId":1,"doctorId":1,"appointmentTime":"not-a-date","reason":"Checkup"}
                """)
                .andExpect(status().isBadRequest());
    }

    @Test
    void startedAppointmentCannotBeCancelled() throws Exception {
        long appointmentId = appointment(pet("Mimi"), doctor("D001"), "2030-01-07T10:00:00");
        postJson("/api/appointments/" + appointmentId + "/confirm", "")
                .andExpect(status().isOk());
        clock.setInstant(Instant.parse("2030-01-07T02:00:00Z"));
        postJson("/api/appointments/" + appointmentId + "/cancel", "")
                .andExpect(status().isConflict());
    }

    @Test
    void expiredPendingAppointmentCannotBeConfirmed() throws Exception {
        long appointmentId = appointment(pet("Mimi"), doctor("D001"), "2030-01-07T10:00:00");
        clock.setInstant(Instant.parse("2030-01-07T02:00:00Z"));
        postJson("/api/appointments/" + appointmentId + "/confirm", "")
                .andExpect(status().isConflict());
    }

    @Test
    void lastHalfHourBeforeClosingCanBeBooked() throws Exception {
        long petId = pet("Mimi");
        long doctorId = doctor("D001");
        book(petId, doctorId, "2030-01-07T16:30:00")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void appointmentMustFitTheDoctorWeekdayAndWorkingHours() throws Exception {
        long petId = pet("Mimi");
        long doctorId = doctor("D001");
        for (String time : new String[]{
                "2030-01-08T10:00:00",
                "2030-01-07T08:30:00",
                "2030-01-07T17:00:00"
        }) {
            book(petId, doctorId, time).andExpect(status().isConflict());
        }
        mvc.perform(get("/api/appointments"))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void appointmentMustBeFutureAndStartAtAnExactHalfHour() throws Exception {
        long petId = pet("Mimi");
        long doctorId = doctor("D001");
        for (String time : new String[]{
                "2029-12-31T10:00:00",
                "2030-01-07T08:00:00",
                "2030-01-07T10:15:00",
                "2030-01-07T10:00:01",
                "2030-01-07T10:00:00.001"
        }) {
            book(petId, doctorId, time).andExpect(status().isConflict());
        }
    }

    @Test
    void appointmentsCanBeFilteredByPet() throws Exception {
        long firstPetId = pet("Mimi");
        long secondPetId = pet("Lucky");
        long doctorId = doctor("D001");
        long firstAppointmentId = appointment(firstPetId, doctorId, "2030-01-07T10:00:00");
        appointment(secondPetId, doctorId, "2030-01-07T10:30:00");

        mvc.perform(get("/api/appointments").param("petId", Long.toString(firstPetId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(firstAppointmentId));
        mvc.perform(get("/api/appointments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    private long pet(String name) throws Exception {
        return created("/api/pets", """
                {"name":"%s","species":"CAT","age":2,"ownerName":"Owner",
                 "ownerPhone":"13800138000","healthNotes":"No known allergies"}
                """.formatted(name));
    }

    private long doctor(String code) throws Exception {
        return created("/api/doctors", doctorJson(code, "09:00", "17:00", true));
    }

    private String doctorJson(String code, String start, String end, boolean accepting) {
        return """
                {"code":"%s","name":"Doctor Li","department":"General",
                 "dayOfWeek":"MONDAY","startTime":"%s","endTime":"%s","accepting":%s}
                """.formatted(code, start, end, accepting);
    }

    private long appointment(long petId, long doctorId, String time) throws Exception {
        return created("/api/appointments", appointmentJson(petId, doctorId, time));
    }

    private ResultActions book(long petId, long doctorId, String time) throws Exception {
        return postJson("/api/appointments", appointmentJson(petId, doctorId, time));
    }

    private String appointmentJson(long petId, long doctorId, String time) {
        return """
                {"petId":%d,"doctorId":%d,"appointmentTime":"%s","reason":"Annual checkup"}
                """.formatted(petId, doctorId, time);
    }

    private long created(String path, String body) throws Exception {
        ResultActions response = postJson(path, body).andExpect(status().isCreated());
        String json = response.andReturn().getResponse().getContentAsString();
        long id = ((Number) JsonPath.read(json, "$.id")).longValue();
        response.andExpect(header().string("Location", endsWith(path + "/" + id)));
        return id;
    }

    private ResultActions postJson(String path, String body) throws Exception {
        return mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content(body));
    }

    @TestConfiguration
    static class TimeConfiguration {
        @Bean
        @Primary
        MutableClock testClock() {
            return new MutableClock(Instant.parse("2030-01-07T00:00:00Z"), ZoneId.of("Asia/Shanghai"));
        }
    }

    static class MutableClock extends Clock {
        private volatile Instant instant;
        private final ZoneId zone;

        MutableClock(Instant instant, ZoneId zone) {
            this.instant = instant;
            this.zone = zone;
        }

        void setInstant(Instant instant) {
            this.instant = instant;
        }

        @Override
        public ZoneId getZone() {
            return zone;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return new MutableClock(instant, zone);
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}
