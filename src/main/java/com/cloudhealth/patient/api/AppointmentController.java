package com.cloudhealth.patient.api;

import com.cloudhealth.patient.api.dto.AppointmentRequest;
import com.cloudhealth.patient.api.dto.AppointmentResponse;
import com.cloudhealth.patient.api.dto.AppointmentStatusRequest;
import com.cloudhealth.patient.api.dto.PageResponse;
import com.cloudhealth.patient.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping("/patients/{patientId}/appointments")
    public ResponseEntity<AppointmentResponse> schedule(
            @PathVariable UUID patientId,
            @Valid @RequestBody AppointmentRequest request
    ) {
        var appointment = appointmentService.schedule(patientId, request);
        return ResponseEntity.created(URI.create("/api/appointments/" + appointment.id())).body(appointment);
    }

    @GetMapping("/patients/{patientId}/appointments")
    public PageResponse<AppointmentResponse> findForPatient(
            @PathVariable UUID patientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return appointmentService.findForPatient(patientId, page, size);
    }

    @GetMapping("/appointments/{id}")
    public AppointmentResponse findById(@PathVariable UUID id) {
        return appointmentService.findById(id);
    }

    @PatchMapping("/appointments/{id}/status")
    public AppointmentResponse changeStatus(
            @PathVariable UUID id,
            @Valid @RequestBody AppointmentStatusRequest request
    ) {
        return appointmentService.changeStatus(id, request);
    }
}

