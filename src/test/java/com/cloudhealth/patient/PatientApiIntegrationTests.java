package com.cloudhealth.patient;

import com.cloudhealth.patient.repository.AppointmentRepository;
import com.cloudhealth.patient.repository.PatientRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.cloud.config.enabled=false",
        "eureka.client.enabled=false",
        "spring.datasource.url=jdbc:h2:mem:patients;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.jpa.open-in-view=false"
})
class PatientApiIntegrationTests {

    @Autowired
    private WebApplicationContext applicationContext;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private PatientRepository patientRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext).build();
        appointmentRepository.deleteAll();
        patientRepository.deleteAll();
    }

    @Test
    void patientCrudAndSearchLifecycle() throws Exception {
        var id = createPatient("Nimal", "Silva");

        mockMvc.perform(get("/api/patients/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Nimal"))
                .andExpect(jsonPath("$.medicalRecordNumber", startsWith("PAT-")));

        mockMvc.perform(put("/api/patients/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patientJson("Nimal", "Perera")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastName").value("Perera"))
                .andExpect(jsonPath("$.version").value(1));

        mockMvc.perform(get("/api/patients").param("q", "Perera"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements").value(1));

        mockMvc.perform(delete("/api/patients/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/patients/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource not found"));
    }

    @Test
    void appointmentSchedulingAndStatusLifecycle() throws Exception {
        var patientId = createPatient("Amara", "Fernando");
        var scheduledAt = OffsetDateTime.now().plusDays(2).withNano(0);
        var request = appointmentJson(scheduledAt);

        var result = mockMvc.perform(post("/api/patients/{patientId}/appointments", patientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SCHEDULED"))
                .andReturn();
        String appointmentId = JsonPath.read(result.getResponse().getContentAsString(), "$.id");

        mockMvc.perform(post("/api/patients/{patientId}/appointments", patientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Data conflict"));

        mockMvc.perform(patch("/api/appointments/{id}/status", appointmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"CONFIRMED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        mockMvc.perform(patch("/api/appointments/{id}/status", appointmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"SCHEDULED\"}"))
                .andExpect(status().isConflict());

        mockMvc.perform(get("/api/patients/{patientId}/appointments", patientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(appointmentId));
    }

    @Test
    void invalidPatientReturnsFieldErrors() throws Exception {
        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "",
                                  "lastName": "",
                                  "dateOfBirth": "2999-01-01",
                                  "gender": "FEMALE",
                                  "email": "not-an-email",
                                  "phone": "12",
                                  "bloodType": "X"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.firstName").exists())
                .andExpect(jsonPath("$.fieldErrors.dateOfBirth").exists())
                .andExpect(jsonPath("$.fieldErrors.email").exists());
    }

    @Test
    void unknownAppointmentReturnsNotFoundProblem() throws Exception {
        mockMvc.perform(get("/api/appointments/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    private UUID createPatient(String firstName, String lastName) throws Exception {
        var result = mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patientJson(firstName, lastName)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", startsWith("/api/patients/")))
                .andReturn();
        return UUID.fromString(JsonPath.read(result.getResponse().getContentAsString(), "$.id"));
    }

    private String patientJson(String firstName, String lastName) {
        return """
                {
                  "firstName": "%s",
                  "lastName": "%s",
                  "dateOfBirth": "1992-06-15",
                  "gender": "MALE",
                  "email": "patient@example.com",
                  "phone": "+94 77 123 4567",
                  "address": "Colombo",
                  "bloodType": "O+",
                  "emergencyContactName": "Emergency Contact",
                  "emergencyContactPhone": "+94 71 111 2222",
                  "medicalHistorySummary": "No known chronic conditions"
                }
                """.formatted(firstName, lastName);
    }

    private String appointmentJson(OffsetDateTime scheduledAt) {
        return """
                {
                  "scheduledAt": "%s",
                  "durationMinutes": 30,
                  "practitionerName": "Dr. Anjali Perera",
                  "department": "General Medicine",
                  "reason": "Routine consultation",
                  "notes": "First visit"
                }
                """.formatted(scheduledAt);
    }
}
