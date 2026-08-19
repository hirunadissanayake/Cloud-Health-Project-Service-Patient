# Patient-Service

A microservice responsible for patient profiles, medical-history summaries, and appointment lifecycle management.

## About

This project is part of the Cloud Health Project for ITS 2130 Enterprise Cloud Architecture. It exposes REST APIs through the API Gateway and stores relational healthcare data in PostgreSQL locally or Cloud SQL on Google Cloud.

## Tech Stack

| Technology | Details |
|---|---|
| Java | 25 |
| Spring Boot | 4.1.0 |
| Spring Cloud | 2025.1.2 |
| Spring Data JPA / Hibernate | ORM and persistence |
| PostgreSQL / Cloud SQL | Relational database |
| Flyway | Versioned schema migrations |
| Netflix Eureka Client | Service registration |
| Spring Cloud Config Client | External configuration |
| Spring Validation | Request validation |
| H2 | Isolated integration tests |

## Service Details

| Property | Value |
|---|---|
| Port | `8081` |
| Artifact ID | `patient-service` |
| Group ID | `com.cloudhealth` |
| Database | `healthcare_patients` |
| Gateway paths | `/api/patients/**`, `/api/appointments/**` |
| Repository | `Cloud-Health-Project-Service-Patient` |

## API Endpoints

| Method | Path | Description | Content-Type |
|---|---|---|---|
| `POST` | `/api/patients` | Register a patient | `application/json` |
| `GET` | `/api/patients` | Search and page patients | — |
| `GET` | `/api/patients/{id}` | Get a patient | — |
| `PUT` | `/api/patients/{id}` | Update a patient | `application/json` |
| `DELETE` | `/api/patients/{id}` | Delete a patient and appointments | — |
| `POST` | `/api/patients/{id}/appointments` | Schedule an appointment | `application/json` |
| `GET` | `/api/patients/{id}/appointments` | List patient appointments | — |
| `GET` | `/api/appointments/{id}` | Get an appointment | — |
| `PATCH` | `/api/appointments/{id}/status` | Change appointment status | `application/json` |

Appointment lifecycle:

```text
SCHEDULED -> CONFIRMED or CANCELLED
CONFIRMED -> COMPLETED, CANCELLED, or NO_SHOW
```

Errors use `application/problem+json`. Validation failures contain field-level details, missing resources return `404`, and invalid scheduling transitions return `409`.

## Getting Started

> **Prerequisites:** PostgreSQL, Config-Server, and Discovery-Server must be running.

```bash
export PATIENT_DB_URL=jdbc:postgresql://localhost:5432/healthcare_patients
export PATIENT_DB_USERNAME=healthcare
export PATIENT_DB_PASSWORD=your-local-password
./mvnw spring-boot:run
```

Flyway creates and updates the database schema. Hibernate validates the schema without modifying production tables.

Health endpoint: `http://localhost:8081/actuator/health`

## Testing

```bash
./mvnw test
```

The integration suite uses an isolated H2 database and does not require Cloud SQL.

## Project Details

| Property | Value |
|---|---|
| Student | Hiruna Dissanayake |
| Student number | `241711024` |
| GCP project | `cloud-health-506015-hiruna` |
