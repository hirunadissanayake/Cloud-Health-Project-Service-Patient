package com.cloudhealth.patient.repository;

import com.cloudhealth.patient.domain.Appointment;
import com.cloudhealth.patient.domain.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    Page<Appointment> findByPatientId(UUID patientId, Pageable pageable);

    boolean existsByPatientIdAndScheduledAtAndStatusNot(
            UUID patientId,
            OffsetDateTime scheduledAt,
            AppointmentStatus excludedStatus
    );

    boolean existsByPractitionerNameIgnoreCaseAndScheduledAtAndStatusNot(
            String practitionerName,
            OffsetDateTime scheduledAt,
            AppointmentStatus excludedStatus
    );
}

