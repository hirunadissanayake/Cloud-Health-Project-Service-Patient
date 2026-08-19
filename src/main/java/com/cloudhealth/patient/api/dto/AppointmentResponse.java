package com.cloudhealth.patient.api.dto;

import com.cloudhealth.patient.domain.AppointmentStatus;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

public record AppointmentResponse(
        UUID id,
        long version,
        UUID patientId,
        OffsetDateTime scheduledAt,
        int durationMinutes,
        String practitionerName,
        String department,
        String reason,
        AppointmentStatus status,
        String notes,
        Instant createdAt,
        Instant updatedAt
) {
}

