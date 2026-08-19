package com.cloudhealth.patient.api;

import com.cloudhealth.patient.api.dto.PageResponse;
import com.cloudhealth.patient.api.dto.PatientRequest;
import com.cloudhealth.patient.api.dto.PatientResponse;
import com.cloudhealth.patient.service.PatientService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @PostMapping
    public ResponseEntity<PatientResponse> create(@Valid @RequestBody PatientRequest request) {
        var patient = patientService.create(request);
        return ResponseEntity.created(URI.create("/api/patients/" + patient.id())).body(patient);
    }

    @GetMapping("/{id}")
    public PatientResponse findById(@PathVariable UUID id) {
        return patientService.findById(id);
    }

    @GetMapping
    public PageResponse<PatientResponse> findAll(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return patientService.findAll(q, requirePage(page), requireSize(size));
    }

    @PutMapping("/{id}")
    public PatientResponse update(@PathVariable UUID id, @Valid @RequestBody PatientRequest request) {
        return patientService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        patientService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private int requirePage(int page) {
        if (page < 0) {
            throw new IllegalArgumentException("page must be zero or greater");
        }
        return page;
    }

    private int requireSize(int size) {
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("size must be between 1 and 100");
        }
        return size;
    }
}

