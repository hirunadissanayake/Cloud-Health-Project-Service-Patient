package com.cloudhealth.patient.api.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

public record AppointmentRequest(
        @NotNull @Future OffsetDateTime scheduledAt,
        @Min(15) @Max(480) int durationMinutes,
        @NotBlank @Size(max = 200) String practitionerName,
        @NotBlank @Size(max = 150) String department,
        @NotBlank @Size(max = 1000) String reason,
        @Size(max = 2000) String notes
) {
}

