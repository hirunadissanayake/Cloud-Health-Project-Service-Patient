package com.cloudhealth.patient.repository;

import com.cloudhealth.patient.domain.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface PatientRepository extends JpaRepository<Patient, UUID> {

    @Query("""
            select p from Patient p
            where lower(p.firstName) like lower(concat('%', :query, '%'))
               or lower(p.lastName) like lower(concat('%', :query, '%'))
               or lower(p.medicalRecordNumber) like lower(concat('%', :query, '%'))
            """)
    Page<Patient> search(@Param("query") String query, Pageable pageable);
}

