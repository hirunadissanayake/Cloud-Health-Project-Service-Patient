package com.cloudhealth.patient.service;

import com.cloudhealth.patient.api.dto.AppointmentRequest;
import com.cloudhealth.patient.api.dto.AppointmentResponse;
import com.cloudhealth.patient.api.dto.AppointmentStatusRequest;
import com.cloudhealth.patient.api.dto.PageResponse;
import com.cloudhealth.patient.domain.Appointment;
import com.cloudhealth.patient.domain.AppointmentStatus;
import com.cloudhealth.patient.repository.AppointmentRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class AppointmentService {

    private static final Map<AppointmentStatus, Set<AppointmentStatus>> ALLOWED_TRANSITIONS = Map.of(
            AppointmentStatus.SCHEDULED, EnumSet.of(AppointmentStatus.CONFIRMED, AppointmentStatus.CANCELLED),
            AppointmentStatus.CONFIRMED, EnumSet.of(
                    AppointmentStatus.COMPLETED,
                    AppointmentStatus.CANCELLED,
                    AppointmentStatus.NO_SHOW
            ),
            AppointmentStatus.COMPLETED, EnumSet.noneOf(AppointmentStatus.class),
            AppointmentStatus.CANCELLED, EnumSet.noneOf(AppointmentStatus.class),
            AppointmentStatus.NO_SHOW, EnumSet.noneOf(AppointmentStatus.class)
    );

    private final AppointmentRepository appointmentRepository;
    private final PatientService patientService;

    public AppointmentService(AppointmentRepository appointmentRepository, PatientService patientService) {
        this.appointmentRepository = appointmentRepository;
        this.patientService = patientService;
    }

    public AppointmentResponse schedule(UUID patientId, AppointmentRequest request) {
        var patient = patientService.requirePatient(patientId);
        ensureSlotAvailable(patientId, request);

        var appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setScheduledAt(request.scheduledAt());
        appointment.setDurationMinutes(request.durationMinutes());
        appointment.setPractitionerName(request.practitionerName().strip());
        appointment.setDepartment(request.department().strip());
        appointment.setReason(request.reason().strip());
        appointment.setNotes(normalize(request.notes()));
        appointment.setStatus(AppointmentStatus.SCHEDULED);

        return toResponse(appointmentRepository.saveAndFlush(appointment));
    }

    @Transactional(readOnly = true)
    public AppointmentResponse findById(UUID id) {
        return toResponse(requireAppointment(id));
    }

    @Transactional(readOnly = true)
    public PageResponse<AppointmentResponse> findForPatient(UUID patientId, int page, int size) {
        patientService.requirePatient(patientId);
        var pageable = PageRequest.of(page, size, Sort.by("scheduledAt").descending());
        return PageResponse.from(
                appointmentRepository.findByPatientId(patientId, pageable),
                AppointmentService::toResponse
        );
    }

    public AppointmentResponse changeStatus(UUID id, AppointmentStatusRequest request) {
        var appointment = requireAppointment(id);
        var current = appointment.getStatus();
        var target = request.status();

        if (current != target && !ALLOWED_TRANSITIONS.get(current).contains(target)) {
            throw new ConflictException(
                    "Appointment status cannot change from %s to %s".formatted(current, target)
            );
        }

        appointment.setStatus(target);
        return toResponse(appointmentRepository.saveAndFlush(appointment));
    }

    private Appointment requireAppointment(UUID id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment %s was not found".formatted(id)));
    }

    private void ensureSlotAvailable(UUID patientId, AppointmentRequest request) {
        var cancelled = AppointmentStatus.CANCELLED;
        if (appointmentRepository.existsByPatientIdAndScheduledAtAndStatusNot(
                patientId, request.scheduledAt(), cancelled)) {
            throw new ConflictException("The patient already has an appointment at that time");
        }
        if (appointmentRepository.existsByPractitionerNameIgnoreCaseAndScheduledAtAndStatusNot(
                request.practitionerName().strip(), request.scheduledAt(), cancelled)) {
            throw new ConflictException("The practitioner already has an appointment at that time");
        }
    }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }

    public static AppointmentResponse toResponse(Appointment appointment) {
        return new AppointmentResponse(
                appointment.getId(), appointment.getVersion(), appointment.getPatient().getId(),
                appointment.getScheduledAt(), appointment.getDurationMinutes(),
                appointment.getPractitionerName(), appointment.getDepartment(), appointment.getReason(),
                appointment.getStatus(), appointment.getNotes(), appointment.getCreatedAt(),
                appointment.getUpdatedAt()
        );
    }
}
