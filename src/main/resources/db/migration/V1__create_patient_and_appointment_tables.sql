CREATE TABLE patients (
    id UUID PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    medical_record_number VARCHAR(30) NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    date_of_birth DATE NOT NULL,
    gender VARCHAR(30) NOT NULL,
    email VARCHAR(254),
    phone VARCHAR(30) NOT NULL,
    address VARCHAR(500),
    blood_type VARCHAR(5),
    emergency_contact_name VARCHAR(200),
    emergency_contact_phone VARCHAR(30),
    medical_history_summary VARCHAR(4000),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_patients_name ON patients (last_name, first_name);

CREATE TABLE appointments (
    id UUID PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    patient_id UUID NOT NULL,
    scheduled_at TIMESTAMP WITH TIME ZONE NOT NULL,
    duration_minutes INTEGER NOT NULL,
    practitioner_name VARCHAR(200) NOT NULL,
    department VARCHAR(150) NOT NULL,
    reason VARCHAR(1000) NOT NULL,
    status VARCHAR(30) NOT NULL,
    notes VARCHAR(2000),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_appointments_patient
        FOREIGN KEY (patient_id) REFERENCES patients (id) ON DELETE CASCADE
);

CREATE INDEX idx_appointments_patient ON appointments (patient_id);
CREATE INDEX idx_appointments_schedule ON appointments (scheduled_at);

