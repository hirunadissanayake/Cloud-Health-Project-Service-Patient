package com.cloudhealth.patient.api.dto;

import com.cloudhealth.patient.domain.AppointmentStatus;
import jakarta.validation.constraints.NotNull;

public record AppointmentStatusRequest(@NotNull AppointmentStatus status) {
}

