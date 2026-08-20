# BeepBox REST API Service 📦🤖

A Spring Boot REST API service built for managing and monitoring **BeepBox** autonomous camera-equipped delivery box units.

BeepBox units carry and deliver small packages to remote locations. This REST service enables clients to register boxes, check availability, load boxes with items under strict safety and capacity guardrails, query loaded items, monitor battery levels, and run automated periodic battery audit logs.

---

## 🌟 Key Features & Requirements

### Functional Capabilities
- **Register Box**: Create a box with unique tracking reference (`txref`), max weight limit (up to 500g), battery capacity percentage, and state.
- **Load Box with Items**: Load one or more items into a box while validating item name/code patterns, battery status, and weight limits.
- **Check Loaded Items**: Inspect items currently inside any registered box.
- **Check Available Boxes**: Retrieve all boxes ready for loading (`state == IDLE` AND `batteryCapacity >= 25%`).
- **Check Battery Level**: Get real-time battery percentage and battery health indicator (`OK` vs `LOW_BATTERY`).
- **Update Box State**: Manually or programmatically transition box states (`IDLE`, `LOADING`, `LOADED`, `DELIVERING`, `DELIVERED`, `RETURNING`).
- **Automated Battery Audit**: Background `@Scheduled` task logs all box battery levels every 60 seconds into an audit history database table (`battery_audit_logs`).

### Guardrails & Safety Rules
1. **Weight Limit Enforcement**: Prevents loading a box beyond its configured `weightLimit` (max 500g limit).
2. **Low Battery Loading Block**: Prevents loading a box or placing it into `LOADING` state if battery level is **below 25%**.
3. **Item Input Validation**:
   - **Item Name**: Allowed characters are letters, numbers, hyphen `-`, and underscore `_` (`^[a-zA-Z0-9\-_]+$`).
   - **Item Code**: Allowed characters are uppercase letters, numbers, and underscore `_` (`^[A-Z0-9_]+$`).
4. **Reference Code Constraint**: Box `txref` is unique and limited to a maximum of 20 characters.

---

## 🗄️ Preloaded Sample Data

Upon application boot (`./gradlew bootRun`), the database is automatically pre-populated with initial sample boxes and items:

| Box Reference | Weight Limit | Battery | State | Preloaded Items | Description |
|---|---|---|---|---|---|
| `BOX-101` | 500.0g | 100% | `IDLE` | *None* | Ready for loading |
| `BOX-102` | 450.0g | 85% | `IDLE` | *None* | Ready for loading |
| `BOX-103` | 300.0g | 15% | `IDLE` | *None* | Low battery (< 25%), loading blocked |
| `BOX-104` | 400.0g | 90% | `LOADED` | `meds-kit` (150g), `camera-lens` (100g) | Already loaded |
| `BOX-105` | 350.0g | 65% | `DELIVERING` | `drone-battery` (200g) | In transit |

---

## 🛠️ Technology Stack

- **Java**: OpenJDK 17
- **Framework**: Spring Boot 3.3.0
- **Database**: H2 In-Memory Database (`jdbc:h2:mem:beepboxdb`)
- **Persistence**: Spring Data JPA & Hibernate ORM
- **Validation**: Jakarta Bean Validation (`jakarta.validation`)
- **API Documentation**: Springdoc OpenAPI / Swagger UI
- **Testing**: JUnit 5, MockMvc, Mockito, Spring Boot Test

---

## 🚀 Quickstart & Running the Application

### 1. Prerequisites
- Java 17+ installed (`java -version`)
- Execution permissions on Gradle wrapper (`chmod +x gradlew`)

### 2. Run Tests
Execute the full unit and integration test suite:
```bash
./gradlew test
```

### 3. Run Application
Start the Spring Boot REST service:
```bash
./gradlew bootRun
```
The service will start on port **`8085`**.

---

## 📑 Interactive Documentation & Consoles

When the service is running, access the following endpoints:

- **Swagger UI (Interactive API Docs)**: [http://localhost:8085/swagger-ui.html](http://localhost:8085/swagger-ui.html)
- **OpenAPI v3 JSON**: [http://localhost:8085/v3/api-docs](http://localhost:8085/v3/api-docs)
- **H2 Database Console**: [http://localhost:8085/h2-console](http://localhost:8085/h2-console)
  - **JDBC URL**: `jdbc:h2:mem:beepboxdb`
  - **User**: `sa`
  - **Password**: *(leave blank)*

---

## 📡 REST API Specification

### Base URL
`http://localhost:8085/api/v1/boxes`

### Summary of Endpoints

| HTTP Method | Endpoint Path | Description |
|---|---|---|
| `POST` | `/api/v1/boxes` | Register a new box |
| `GET` | `/api/v1/boxes` | List all registered boxes |
| `GET` | `/api/v1/boxes/available` | List boxes available for loading |
| `GET` | `/api/v1/boxes/{txref}` | Get box details by reference |
| `POST` | `/api/v1/boxes/{txref}/items` | Load items into box |
| `GET` | `/api/v1/boxes/{txref}/items` | Get loaded items for box |
| `GET` | `/api/v1/boxes/{txref}/battery` | Get box battery level & status |
| `PATCH` | `/api/v1/boxes/{txref}/state` | Update box state |

---

### Request & Response Examples

#### 1. Register a Box (`POST /api/v1/boxes`)
**Request:**
```json
{
  "txref": "BOX-A100",
  "weightLimit": 450.0,
  "batteryCapacity": 95,
  "state": "IDLE"
}
```
**Response (`201 Created`):**
```json
{
  "success": true,
  "message": "Box created successfully",
  "data": {
    "id": 6,
    "txref": "BOX-A100",
    "weightLimit": 450.0,
    "batteryCapacity": 95,
    "state": "IDLE",
    "currentWeight": 0.0,
    "items": []
  },
  "timestamp": "2026-08-20T10:00:00"
}
```

#### 2. Query Available Boxes for Loading (`GET /api/v1/boxes/available`)
Returns only boxes with state `IDLE` and `batteryCapacity >= 25%`.

**Response (`200 OK`):**
```json
{
  "success": true,
  "message": "Available boxes for loading retrieved successfully",
  "data": [
    {
      "id": 1,
      "txref": "BOX-101",
      "weightLimit": 500.0,
      "batteryCapacity": 100,
      "state": "IDLE",
      "currentWeight": 0.0,
      "items": []
    },
    {
      "id": 2,
      "txref": "BOX-102",
      "weightLimit": 450.0,
      "batteryCapacity": 85,
      "state": "IDLE",
      "currentWeight": 0.0,
      "items": []
    }
  ],
  "timestamp": "2026-08-20T10:00:00"
}
```

#### 3. Load Box with Items (`POST /api/v1/boxes/{txref}/items`)
**Request:**
```json
{
  "items": [
    {
      "name": "first-aid-kit",
      "weight": 120.0,
      "code": "AID_100"
    },
    {
      "name": "insulin_vial",
      "weight": 50.0,
      "code": "MED_200"
    }
  ]
}
```
**Response (`200 OK`):**
```json
{
  "success": true,
  "message": "Items loaded into box successfully",
  "data": {
    "id": 1,
    "txref": "BOX-101",
    "weightLimit": 500.0,
    "batteryCapacity": 100,
    "state": "LOADED",
    "currentWeight": 170.0,
    "items": [
      {
        "id": 1,
        "name": "first-aid-kit",
        "weight": 120.0,
        "code": "AID_100"
      },
      {
        "id": 2,
        "name": "insulin_vial",
        "weight": 50.0,
        "code": "MED_200"
      }
    ]
  },
  "timestamp": "2026-08-20T10:00:00"
}
```

#### 4. Error Case: Low Battery Rejection (< 25%)
**Request:** `POST /api/v1/boxes/BOX-103/items`
**Response (`400 Bad Request`):**
```json
{
  "success": false,
  "message": "Cannot load box or set state to LOADING: Battery level is below 25% (Current: 15%)",
  "data": null,
  "timestamp": "2026-08-20T10:00:00"
}
```

#### 5. Error Case: Weight Limit Exceeded
**Request:** `POST /api/v1/boxes/BOX-102/items` (total weight > 450.0g)
**Response (`400 Bad Request`):**
```json
{
  "success": false,
  "message": "Weight limit exceeded: Total items weight (480.00g) exceeds box limit (450.00g)",
  "data": null,
  "timestamp": "2026-08-20T10:00:00"
}
```

#### 6. Check Battery Level (`GET /api/v1/boxes/{txref}/battery`)
**Response (`200 OK`):**
```json
{
  "success": true,
  "message": "Operation successful",
  "data": {
    "txref": "BOX-101",
    "batteryCapacity": 100,
    "status": "OK"
  },
  "timestamp": "2026-08-20T10:00:00"
}
```

---

## 🏗️ Project Architecture

```
src/main/java/com/beepbox/
├── BeepBoxApplication.java       # Application Main Class & @EnableScheduling
├── config/
│   ├── DataInitializer.java      # Preloads initial sample boxes & items
│   └── OpenApiConfig.java        # Swagger/OpenAPI v3 configuration
├── controller/
│   └── BoxController.java        # REST Controller exposing endpoints
├── dto/
│   ├── ApiResponse.java          # Universal JSON response wrapper
│   ├── BatteryLevelResponse.java # Battery check response DTO
│   ├── BoxDto.java               # Box DTO
│   ├── BoxStateUpdateRequest.java# State change request DTO
│   ├── ItemDto.java              # Item DTO
│   └── LoadBoxRequest.java       # Load request DTO with items list
├── exception/
│   ├── BoxNotFoundException.java
│   ├── BoxWeightLimitExceededException.java
│   ├── DuplicateTxrefException.java
│   ├── GlobalExceptionHandler.java # Unified @RestControllerAdvice error handler
│   ├── InvalidBoxStateException.java
│   └── LowBatteryException.java
├── model/
│   ├── BatteryAuditLog.java     # Audit history JPA entity
│   ├── Box.java                 # Box JPA entity
│   ├── BoxState.java            # IDLE, LOADING, LOADED, DELIVERING, DELIVERED, RETURNING
│   └── Item.java                # Item JPA entity
├── repository/
│   ├── BatteryAuditLogRepository.java
│   ├── BoxRepository.java
│   └── ItemRepository.java
└── service/
    ├── BatteryAuditScheduler.java# Scheduled background battery logger
    ├── BoxService.java          # Service interface
    └── BoxServiceImpl.java      # Core business logic implementation
```

---

## 🧪 Testing

The codebase includes comprehensive test coverage:
- **Unit Tests**: `BoxServiceTest` tests business logic, weight calculations, battery checks, and exceptions.
- **Integration Tests**: `BoxControllerTest` tests end-to-end REST HTTP endpoints, JSON serialization, bean validation, and HTTP status codes.

Run tests:
```bash
./gradlew test
```
