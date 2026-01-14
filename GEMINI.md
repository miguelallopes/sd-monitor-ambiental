# GEMINI.md - Context & Instructions for the Environmental Monitoring System

This document provides a comprehensive overview of the **Environmental Monitoring System (Sistema de Monitorização Ambiental)** project. Use this context to understand the architecture, run the application, and assist with development tasks.

## 1. Project Overview

**Purpose:** A distributed system for monitoring environmental metrics (temperature, humidity) across the **University of Évora** campus. The system ensures comfort and equipment preservation by collecting data from various locations (Rooms, Floors, Buildings, Departments).

**Architecture:** Microservices-based, containerized with Docker.
*   **Core:**
    *   **Server (`server`):** The central Spring Boot application that ingests data, validates devices, manages the PostgreSQL database, and exposes REST APIs.
    *   **Database (`database`):** PostgreSQL for persistent storage.
    *   **Message Broker (`messagebroker`):** Eclipse Mosquitto for MQTT communication.
*   **Clients (Simulators):**
    *   **gRPC Client (`client-grpc`):** Simulates Edge/Gateway devices (High performance, Sync, Protobuf).
    *   **MQTT Client (`client-mqtt`):** Simulates simple low-power IoT sensors (Async, Pub/Sub, Battery-operated).
    *   **REST Client (`client-rest`):** Simulates HTTP-capable devices (Simple, Sync, JSON).
*   **Management:**
    *   **Admin CLI (`admin-cli`):** A command-line interface for administrators to manage devices and query aggregated metrics.

**Key Technologies:**
*   **Language:** Java
*   **Framework:** Spring Boot (Server, Admin CLI, REST Client)
*   **Build System:** Gradle (Multi-project build)
*   **Containerization:** Docker & Docker Compose
*   **Protocols:** gRPC (Protobuf), MQTT, REST (JSON/HTTP)
*   **Persistence:** JPA / Hibernate / PostgreSQL

## 2. System Requirements & Business Logic

### Data Generation (Simulators)
Simulators must generate realistic synthetic data:
*   **Temperature:** 15°C to 30°C (Gradual variation).
*   **Humidity:** 30% to 80% (Gradual variation).
*   **Timestamp:** ISO 8601 format.

### Ingestion Logic (Server)
*   **Multi-Protocol:** The server must accept data via MQTT (Topic subscription), gRPC (Service call), and REST (POST endpoint).
*   **Validation:** **CRITICAL:** Data from any protocol must be validated. If the **Device ID is not registered** in the system, the metrics **MUST be discarded**. Only valid devices can store metrics.
*   **Storage:** Metrics are stored in a denormalized format (referencing Room, Floor, Building directly) to optimize read performance for aggregations.

## 3. API Reference (Server)

The server exposes the following REST endpoints for management and ingestion:

### Device Management (`/api/devices`)
*   `POST /`: Register a new device (Fields: ID, Protocol, Location, Status).
*   `GET /`: List all registered devices.
*   `GET /{id}`: Get details of a specific device.
*   `PUT /{id}`: Update device information.
*   `DELETE /{id}`: Remove a device.

### Metrics Ingestion
*   `POST /api/metrics/ingest`: Endpoint for REST clients to submit readings.

### Metrics Consultation
*   `GET /api/metrics/average`: Returns average Temp/Humidity.
    *   Params: `level` (sala, departamento, piso, edificio), `id` (entity name), `from`, `to`.
*   `GET /api/metrics/raw`: Returns raw metrics for a specific device.
    *   Params: `deviceId`, `from`, `to`.

## 4. Admin CLI Features
The CLI provides a menu-driven interface for:
1.  **Device Management:** List, Add, Update, Remove, View Details.
2.  **Metrics Query:** Consult averages by Room, Department, Floor, or Building (with date filtering) and view raw data.
3.  **System Statistics:** (Optional/Extra).

## 5. Infrastructure & Setup

The entire system is orchestrated using `docker-compose.yml`.

### Key Ports
| Service | Port | Protocol | Description |
| :--- | :--- | :--- | :--- |
| **Server** | `8080` | HTTP/REST | Web API endpoint |
| **Server** | `50051`| gRPC | High-performance RPC endpoint |
| **Broker** | `1883` | MQTT | Message broker for IoT devices |
| **DB** | `5432` | TCP | PostgreSQL Database |

### Running the System
To start all services (Server, DB, Broker) in background:
```bash
docker-compose up --build -d
```
*   **Database initialization:** Automatic. The server uses JPA/Hibernate to create the schema on the first run.
*   **Credentials:** Defined in `.env` and `db.env`. Default user/pass: `sd-monitor-ambiental`.

## 6. Development & Execution Commands

Each component is a Gradle subproject. Use the Gradle wrapper (`./gradlew`) from the root or the specific directory.

### A. Admin CLI (`admin-cli`)
*   **Run:**
    ```bash
    ./gradlew -p admin-cli bootRun
    ```
*   **Run with Custom Server URL:**
    ```bash
    ./gradlew -p admin-cli bootRun --args="--ambiente.server.url=http://<host>:<port>"
    ```

### B. gRPC Client (`client-grpc`)
*   **Single Submission:**
    ```bash
    ./gradlew -p client-grpc run --args="<host> <port> <device_id>"
    ```
*   **Continuous Submission:**
    ```bash
    ./gradlew -p client-grpc run --args="<host> <port> <device_id> <temp> <humidity>"
    ```

### C. MQTT Client (`client-mqtt`)
Topic: `ambiente`
*   **Single Submission:**
    ```bash
    ./gradlew -p client-mqtt run --args="<host> <port> <device_id>"
    ```
*   **Continuous Submission:**
    ```bash
    ./gradlew -p client-mqtt run --args="<host> <port> <device_id> <temp> <humidity>"
    ```

### D. REST Client (`client-rest`)
*   **Single Submission:**
    ```bash
    ./gradlew -p client-rest bootRun --args="<device_id> --ambiente.server.url=<endpoint>"
    ```
*   **Continuous Submission:**
    ```bash
    ./gradlew -p client-rest bootRun --args="<device_id> <temp> <humidity> --ambiente.server.url=<endpoint>"
    ```

## 7. Data Structures & Conventions

### Protocol Buffers (`proto/ambiente.proto`)
Defines the contract for gRPC communication.
*   **Service:** `AmbienteService`
*   **RPC:** `submeterDadosAmbiente`
*   **Message:** `AmbienteServiceRequest` (deviceId, temperatura, humidade, timestamp)

### Database Schema
*   **Fact Table:** `Metricas` (Denormalized with references to Building, Floor, Room for query performance).
*   **Dimension Tables:** `Edificio`, `Piso`, `Sala`, `Departamento`, `Dispositivo`.

### Coding Style
*   **Package Structure:** `pt.ue.ambiente...`
*   **Spring Boot:** Heavy use of `@Service`, `@Autowired`, `@Configuration`.
*   **Error Handling:** `spring-retry` used in REST client for robustness.

## 8. Final Delivery & Report Requirements

The final submission must include the source code, configuration files, execution scripts, and a PDF report (`relatório.pdf`).

### Report Content
The report **MUST** include the following sections and be written in Portuguese Portugal for a university project of Sistemas Distribuidos:
1.  **Student Identification:** Names and student numbers.
    *   Miguel Pinto, l58122
    *   Miguel Lopes, l58540
2.  **Justification of Choices:**
    *   Explanation of the database structure (tables, relationships).
    *   Rationale for communication methods (why MQTT for simple sensors vs gRPC for gateways).
    *   Architectural decisions.
3.  **Detailed Setup Instructions:**
    *   Steps to configure the PostgreSQL database.
    *   Commands to create the database, tables, users, and permissions (SQL scripts or JPA explanation).
    *   How to execute the system.
4.  **Development Observations:**
    *   Challenges faced during implementation.
    *   Notes on the development process.
5.  **Performance Analysis:**
    *   **CRITICAL:** A detailed comparison of performance between the three protocols (**MQTT**, **gRPC**, **REST**).
    *   This analysis should be based on the results obtained from the project (likely using `resultados.csv`).
    *   Metrics to compare: latency, throughput, resource usage, overhead.

### Submission Format
*   **File:** `.zip` file named `sd-t02-YYYYY-ZZZZZ.zip` (replacing YYYYY and ZZZZZ with student numbers).
*   **Structure:** Each module in its own folder (`server`, `client-mqtt`, etc.) as currently organized.

## 9. Troubleshooting

*   **Logs:**
    *   Docker: `docker-compose logs -f [service_name]`
    *   Application: Check `application.properties` to adjust logging levels (e.g., `logging.level.pt.ue.ambiente.client.admin.api=DEBUG`).
*   **Database:** Access via any Postgres client on port `5432` (if mapped in docker-compose).
