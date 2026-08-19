package com.cloudhealth.patient.service;

import com.cloudhealth.patient.api.dto.PageResponse;
import com.cloudhealth.patient.api.dto.PatientRequest;
import com.cloudhealth.patient.api.dto.PatientResponse;
import com.cloudhealth.patient.domain.Patient;
import com.cloudhealth.patient.repository.PatientRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

@Service
@Transactional
public class PatientService {

    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public PatientResponse create(PatientRequest request) {
        var patient = new Patient();
        patient.setMedicalRecordNumber(generateMedicalRecordNumber());
        apply(request, patient);
        return toResponse(patientRepository.saveAndFlush(patient));
    }

    @Transactional(readOnly = true)
    public PatientResponse findById(UUID id) {
        return toResponse(requirePatient(id));
    }

    @Transactional(readOnly = true)
    public PageResponse<PatientResponse> findAll(String query, int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by("lastName", "firstName").ascending());
        var result = query == null || query.isBlank()
                ? patientRepository.findAll(pageable)
                : patientRepository.search(query.strip(), pageable);
        return PageResponse.from(result, PatientService::toResponse);
    }

    public PatientResponse update(UUID id, PatientRequest request) {
        var patient = requirePatient(id);
        apply(request, patient);
        return toResponse(patientRepository.saveAndFlush(patient));
    }

    public void delete(UUID id) {
        patientRepository.delete(requirePatient(id));
    }

    Patient requirePatient(UUID id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient %s was not found".formatted(id)));
    }

    private void apply(PatientRequest request, Patient patient) {
        patient.setFirstName(request.firstName().strip());
        patient.setLastName(request.lastName().strip());
        patient.setDateOfBirth(request.dateOfBirth());
        patient.setGender(request.gender());
        patient.setEmail(normalizeEmail(request.email()));
        patient.setPhone(request.phone().strip());
        patient.setAddress(normalize(request.address()));
        patient.setBloodType(normalize(request.bloodType()));
        patient.setEmergencyContactName(normalize(request.emergencyContactName()));
        patient.setEmergencyContactPhone(normalize(request.emergencyContactPhone()));
        patient.setMedicalHistorySummary(normalize(request.medicalHistorySummary()));
    }

    private String generateMedicalRecordNumber() {
        return "PAT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
    }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }

    private static String normalizeEmail(String value) {
        var normalized = normalize(value);
        return normalized == null ? null : normalized.toLowerCase(Locale.ROOT);
    }

    public static PatientResponse toResponse(Patient patient) {
        return new PatientResponse(
                patient.getId(), patient.getVersion(), patient.getMedicalRecordNumber(),
                patient.getFirstName(), patient.getLastName(), patient.getDateOfBirth(),
                patient.getGender(), patient.getEmail(), patient.getPhone(), patient.getAddress(),
                patient.getBloodType(), patient.getEmergencyContactName(),
                patient.getEmergencyContactPhone(), patient.getMedicalHistorySummary(),
                patient.getCreatedAt(), patient.getUpdatedAt()
        );
    }
}
