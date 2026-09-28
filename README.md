# GarageDesk – Vehicle Service Job Card and Bay Scheduling System (Backend)

## 1. Project description

A vehicle service garage used to give jobs to mechanics and service bays verbally every morning.
This caused bay conflicts and nobody knew the real status of a job.

GarageDesk is a **REST API backend only** (no frontend). It can:

- create a job card for a vehicle with the requested services
- assign the job card to a free service bay and a mechanic
- track the job: `WAITING → IN_PROGRESS → QUALITY_CHECK → COMPLETED`
- stop a bay from being given to a second active job
- generate a final bill (parts + labour) for a completed job

## 2. Technologies used

- Java 17 (newer JDKs such as 21 also work)
- Spring Boot 3.3.5, Maven
- Spring Web (REST), Spring Data JPA (Hibernate)
- Jakarta Bean Validation
- MySQL 8
- JUnit 5 + Mockito (a few unit tests for the business rules)

## 3. Database setup

1. Install MySQL and make sure it is running on port 3306.
2. That is all. The connection URL contains `createDatabaseIfNotExist=true`, so the
   database `garagedesk` is created automatically on the first run, and Hibernate
   (`ddl-auto=update`) creates the tables.

If you prefer to create it by hand:

```sql
CREATE DATABASE garagedesk;
```

Tables created: `vehicle`, `job_card`, `bay`, `mechanic`, `bill`.

Relationships:

- `Vehicle` 1 ─── * `JobCard` (`@OneToMany` / `@ManyToOne`)
- `Bay` 1 ─── * `JobCard` (`@ManyToOne`) – a bay is reused by many jobs over time, but only one can be *active*
- `Mechanic` 1 ─── * `JobCard` (`@ManyToOne`)
- `JobCard` 1 ─── 1 `Bill` (`@OneToOne`)

The "back" sides (`Vehicle.jobCards`, `JobCard.bill`) use `@JsonIgnore`, so there is no infinite JSON loop.

## 4. Configure application.properties

Open `src/main/resources/application.properties` and change the username and password:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/garagedesk?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD
spring.jpa.hibernate.ddl-auto=update
```

Replace `YOUR_PASSWORD` with your own MySQL password. (Do not commit real passwords.)

## 5. Run in IntelliJ IDEA

1. Unzip the project.
2. **File → Open** and select the `garagedesk` folder (the one that contains `pom.xml`).
   Choose **Open as Project** and trust the project if asked.
3. Wait for Maven to download dependencies (needs internet the first time).
4. Check **File → Project Structure → Project SDK** is a JDK 17 or newer.
5. Edit the password in `application.properties` (section 4).
6. Open `GarageDeskApplication.java` and click the green ▶ next to `main`.
7. When you see `Started GarageDeskApplication`, the API is running at `http://localhost:8080`.

Command line alternative (needs Maven installed): `mvn spring-boot:run`

Run the unit tests (no database needed): `mvn test`, or right-click `JobCardServiceTest` → Run.

## 6. API endpoints

| Method | URL | Purpose | Success code |
|---|---|---|---|
| POST | `/api/vehicles` | Add a vehicle | 201 |
| GET | `/api/vehicles` | List vehicles | 200 |
| GET | `/api/vehicles/{id}` | Get one vehicle | 200 |
| POST | `/api/bays` | Add a bay | 201 |
| GET | `/api/bays` | List bays (shows `available`) | 200 |
| GET | `/api/bays/{id}` | Get one bay | 200 |
| POST | `/api/mechanics` | Add a mechanic | 201 |
| GET | `/api/mechanics` | List mechanics | 200 |
| GET | `/api/mechanics/{id}` | Get one mechanic | 200 |
| POST | `/api/jobcards` | Create a job card (status starts as `WAITING`) | 201 |
| GET | `/api/jobcards` | List job cards (optional `?status=WAITING`) | 200 |
| GET | `/api/jobcards/{id}` | Get one job card | 200 |
| PUT | `/api/jobcards/{id}/assign` | Assign bay + mechanic | 200 |
| PUT | `/api/jobcards/{id}/status` | Move to next status | 200 |
| POST | `/api/bills` | Generate bill for a completed job | 201 |
| GET | `/api/bills` | List bills | 200 |
| GET | `/api/bills/{id}` | Get one bill | 200 |

Error codes: `400` bad request / broken rule, `404` not found, `409` conflict (bay occupied, duplicates).

## 7. Sample JSON requests

**POST /api/vehicles**
```json
{
  "registrationNumber": "TN38AB1234",
  "ownerName": "Arun",
  "vehicleModel": "Hyundai i20",
  "vehicleType": "Hatchback"
}
```

**POST /api/bays**
```json
{ "bayNumber": 1, "bayName": "Bay 1 - General Service" }
```

**POST /api/mechanics**
```json
{ "name": "Ravi", "specialization": "Engine and brakes" }
```

**POST /api/jobcards**
```json
{ "vehicleId": 1, "requestedServices": "Oil change, brake pad replacement" }
```

**PUT /api/jobcards/1/assign**
```json
{ "bayId": 1, "mechanicId": 1 }
```

**PUT /api/jobcards/1/status**
```json
{ "status": "IN_PROGRESS" }
```

**POST /api/bills**
```json
{ "jobCardId": 1, "partsAmount": 2500.00, "labourCharge": 800.00 }
```

## 8. Sample JSON responses

**Vehicle created (201)**
```json
{
  "id": 1,
  "registrationNumber": "TN38AB1234",
  "ownerName": "Arun",
  "vehicleModel": "Hyundai i20",
  "vehicleType": "Hatchback"
}
```

**Bay created (201)**
```json
{ "id": 1, "bayNumber": 1, "bayName": "Bay 1 - General Service", "available": true }
```

**Job card created (201)**
```json
{
  "id": 1,
  "vehicle": {
    "id": 1,
    "registrationNumber": "TN38AB1234",
    "ownerName": "Arun",
    "vehicleModel": "Hyundai i20",
    "vehicleType": "Hatchback"
  },
  "requestedServices": "Oil change, brake pad replacement",
  "status": "WAITING",
  "bay": null,
  "mechanic": null,
  "createdAt": "2026-09-28T09:15:30.123456"
}
```

**After assigning (200)** – the bay and the mechanic are now marked `available: false`
```json
{
  "id": 1,
  "vehicle": { "id": 1, "registrationNumber": "TN38AB1234", "ownerName": "Arun", "vehicleModel": "Hyundai i20", "vehicleType": "Hatchback" },
  "requestedServices": "Oil change, brake pad replacement",
  "status": "WAITING",
  "bay": { "id": 1, "bayNumber": 1, "bayName": "Bay 1 - General Service", "available": false },
  "mechanic": { "id": 1, "name": "Ravi", "specialization": "Engine and brakes", "available": false },
  "createdAt": "2026-09-28T09:15:30.123456"
}
```

**Bill created (201)**
```json
{
  "id": 1,
  "jobCard": {
    "id": 1,
    "vehicle": { "id": 1, "registrationNumber": "TN38AB1234", "ownerName": "Arun", "vehicleModel": "Hyundai i20", "vehicleType": "Hatchback" },
    "requestedServices": "Oil change, brake pad replacement",
    "status": "COMPLETED",
    "bay": { "id": 1, "bayNumber": 1, "bayName": "Bay 1 - General Service", "available": true },
    "mechanic": { "id": 1, "name": "Ravi", "specialization": "Engine and brakes", "available": true },
    "createdAt": "2026-09-28T09:15:30.123456"
  },
  "partsAmount": 2500.00,
  "labourCharge": 800.00,
  "totalAmount": 3300.00,
  "generatedAt": "2026-09-28T11:40:02.456789"
}
```

**Every error looks like this**
```json
{ "message": "Bay 1 is already occupied by an active job" }
```

Other examples:

| Situation | Code | Message |
|---|---|---|
| Bay already has an active job | 409 | `Bay 1 is already occupied by an active job` |
| IN_PROGRESS → COMPLETED | 400 | `Job must pass quality-check before it can be completed` |
| WAITING → COMPLETED | 400 | `Job must pass quality-check before it can be completed` |
| WAITING → QUALITY_CHECK | 400 | `Invalid status change from WAITING to QUALITY_CHECK. Allowed order: ...` |
| Start job with no bay/mechanic | 400 | `Assign a bay and a mechanic before starting the job` |
| Mechanic is busy | 409 | `Mechanic Ravi is not available` |
| Bill for unfinished job | 400 | `Bill can only be generated for a COMPLETED job` |
| Second bill for same job | 409 | `A bill already exists for job card 1` |
| Duplicate vehicle number | 409 | `Vehicle TN38AB1234 is already registered` |
| Blank field | 400 | `ownerName: must not be blank` |
| Negative bill amount | 400 | `labourCharge: must not be negative` |
| Unknown id | 404 | `Job card with id 99 not found` |
| Wrong status word (e.g. `DONE`) | 400 | `Request body is missing or invalid. Check the JSON format and values (...)` |

## 9. Important business rules

1. **One active job per bay.** A bay is occupied while a job using it is `WAITING`, `IN_PROGRESS` or `QUALITY_CHECK`.
   Assigning an occupied bay returns **409** and nothing is overwritten.
   The check is done in `JobCardService.assignBayAndMechanic`. The bay row is also locked
   during the assignment, so two simultaneous requests cannot both get the same bay.
2. **Status order is fixed:** `WAITING → IN_PROGRESS → QUALITY_CHECK → COMPLETED`.
   Skipping a step (for example `IN_PROGRESS → COMPLETED`) returns **400**. A completed job cannot be changed again.
   The check is done in `JobCardService.updateStatus`.
3. When a job becomes `COMPLETED`, its bay and mechanic become `available` again.
4. A mechanic can only be assigned if `available` is true (one active job at a time).
5. Bay and mechanic can only be assigned while the job is `WAITING` (re-assigning while waiting is allowed and frees the old bay/mechanic).
   A job needs a bay and a mechanic before it can go to `IN_PROGRESS`.
6. A bill can only be created for a `COMPLETED` job, only once per job.
   `totalAmount = partsAmount + labourCharge`; negative amounts are rejected.

## 10. Postman testing sequence

Use a fresh database so the ids below are 1, 2, ... (or use the ids that the responses give you).
Set header `Content-Type: application/json` for POST/PUT (Postman does this when you choose Body → raw → JSON).
Base URL: `http://localhost:8080`

**Set up data**

| # | Request | Body | Expect |
|---|---|---|---|
| 1 | POST `/api/bays` | `{"bayNumber":1,"bayName":"Bay 1 - General Service"}` | 201 |
| 2 | POST `/api/bays` | `{"bayNumber":2,"bayName":"Bay 2 - Alignment"}` | 201 |
| 3 | POST `/api/mechanics` | `{"name":"Ravi","specialization":"Engine and brakes"}` | 201 |
| 4 | POST `/api/mechanics` | `{"name":"Suresh","specialization":"Electrical and AC"}` | 201 |
| 5 | POST `/api/vehicles` | `{"registrationNumber":"TN38AB1234","ownerName":"Arun","vehicleModel":"Hyundai i20","vehicleType":"Hatchback"}` | 201 |
| 6 | POST `/api/vehicles` | `{"registrationNumber":"TN52CD5678","ownerName":"Meena","vehicleModel":"Maruti Swift","vehicleType":"Hatchback"}` | 201 |

**Normal flow**

| # | Request | Body | Expect |
|---|---|---|---|
| 7 | POST `/api/jobcards` | `{"vehicleId":1,"requestedServices":"Oil change, brake pad replacement"}` | 201, status `WAITING` |
| 8 | POST `/api/jobcards` | `{"vehicleId":2,"requestedServices":"AC service, battery check"}` | 201 |
| 9 | PUT `/api/jobcards/1/assign` | `{"bayId":1,"mechanicId":1}` | 200, bay 1 `available:false` |

**Business rule: occupied bay**

| # | Request | Body | Expect |
|---|---|---|---|
| 10 | PUT `/api/jobcards/2/assign` | `{"bayId":1,"mechanicId":2}` | **409** `Bay 1 is already occupied by an active job` |
| 11 | PUT `/api/jobcards/2/assign` | `{"bayId":2,"mechanicId":2}` | 200 (a free bay works) |

**Status flow and the invalid transition**

| # | Request | Body | Expect |
|---|---|---|---|
| 12 | PUT `/api/jobcards/1/status` | `{"status":"IN_PROGRESS"}` | 200 |
| 13 | PUT `/api/jobcards/1/status` | `{"status":"COMPLETED"}` | **400** `Job must pass quality-check before it can be completed` |
| 14 | PUT `/api/jobcards/1/status` | `{"status":"QUALITY_CHECK"}` | 200 |
| 15 | PUT `/api/jobcards/1/status` | `{"status":"COMPLETED"}` | 200, bay 1 and Ravi free again |

**Billing**

| # | Request | Body | Expect |
|---|---|---|---|
| 16 | POST `/api/bills` | `{"jobCardId":1,"partsAmount":2500.00,"labourCharge":800.00}` | 201, `totalAmount` 3300.00 |
| 17 | POST `/api/bills` | same body again | **409** bill already exists |
| 18 | POST `/api/bills` | `{"jobCardId":2,"partsAmount":100,"labourCharge":50}` | **400** job 2 not completed |
| 19 | POST `/api/bills` | `{"jobCardId":1,"partsAmount":-5,"labourCharge":800}` | **400** `partsAmount: must not be negative` |

**Check the result**

| # | Request | Expect |
|---|---|---|
| 20 | GET `/api/bays` | Bay 1 `available:true`, Bay 2 `available:false` |
| 21 | GET `/api/jobcards?status=WAITING` | job card 2 |
| 22 | PUT `/api/jobcards/3/assign` (body `{"bayId":1,"mechanicId":1}`) | **404** `Job card with id 3 not found` |

Bay 1 is free again after step 15, so a new job card can now be assigned to it.
