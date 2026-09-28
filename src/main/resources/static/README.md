# GarageDesk Frontend

A simple frontend for the GarageDesk Vehicle Service Job Card and Bay Scheduling System.

## Technology

- HTML5
- CSS3
- Vanilla JavaScript
- REST API / Fetch
- No frontend framework required

## Backend URL

Serve the frontend from Spring Boot and open the same host and port. By default:

`http://localhost:8080`

The frontend calls the API on the page's origin, so it follows the Spring Boot port automatically.

## Expected backend endpoints

- GET/POST `/api/vehicles`
- GET/POST `/api/job-cards` (also available as `/api/jobcards`)
- PUT `/api/job-cards/{id}/assign`
- PUT `/api/job-cards/{id}/status`
- GET/POST `/api/bays`
- GET/POST `/api/mechanics`
- POST `/api/job-cards/{id}/bill`
- GET `/api/bills`

Vehicle JSON uses `registrationNumber`, `ownerName`, `phoneNumber`, `vehicleModel`, and `vehicleType`.

## Running

From the `garagedesk` project directory, run `mvn spring-boot:run`, then open `http://localhost:8080`. The HTML, JavaScript, and CSS are served from Spring Boot's `src/main/resources/static` directory; opening `index.html` through a separate preview server can send API requests to the wrong origin.

## Main screens

- Dashboard
- Job Cards
- Vehicles
- Service Bays
- Mechanics
- Bills

The UI also demonstrates the two important GarageDesk rules:

1. A bay cannot be assigned to another unfinished job.
2. A job should pass QUALITY_CHECK before COMPLETED.

Those rules must still be enforced by the Spring Boot service layer. The frontend is only the user interface.
