package com.cloudhealth.patient.api.dto;

import com.cloudhealth.patient.domain.Gender;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record PatientResponse(
        UUID id,
        long version,
        String medicalRecordNumber,
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        Gender gender,
        String email,
        String phone,
        String address,
        String bloodType,
        String emergencyContactName,
        String emergencyContactPhone,
        String medicalHistorySummary,
        Instant createdAt,
        Instant updatedAt
) {
}

