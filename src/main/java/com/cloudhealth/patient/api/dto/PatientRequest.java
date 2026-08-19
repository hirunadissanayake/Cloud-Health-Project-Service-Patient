package com.cloudhealth.patient.api.dto;

import com.cloudhealth.patient.domain.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record PatientRequest(
        @NotBlank @Size(max = 100) String firstName,
        @NotBlank @Size(max = 100) String lastName,
        @NotNull @Past LocalDate dateOfBirth,
        @NotNull Gender gender,
        @Email @Size(max = 254) String email,
        @NotBlank @Size(max = 30)
        @Pattern(regexp = "^[+0-9() .-]{7,30}$", message = "must be a valid phone number") String phone,
        @Size(max = 500) String address,
        @Pattern(regexp = "^(A|B|AB|O)[+-]$", message = "must be one of A+, A-, B+, B-, AB+, AB-, O+, O-") String bloodType,
        @Size(max = 200) String emergencyContactName,
        @Size(max = 30) String emergencyContactPhone,
        @Size(max = 4000) String medicalHistorySummary
) {
}

