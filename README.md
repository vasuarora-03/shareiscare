# ShareIsCare

ShareIsCare is CabPool — a ride-sharing backend built for daily commuters,
connecting people travelling in the same direction so they can share rides
safely and conveniently. This repository contains the V1 REST API:
authentication, vehicle and ride management, bookings, in-ride chat, and
post-ride ratings.

## Features

- **Authentication** — Phone number + OTP based signup/login (no passwords), JWT-secured sessions
- **User profiles** — Profile management, profile picture upload, driving license upload
- **Vehicles** — Owners can register and manage multiple vehicles
- **Rides** — Drivers create rides against a vehicle they own; riders search by source, destination, and date
- **Bookings** — Seat reservation with live seat-count tracking, cancellation with NORMAL/LATE classification
- **Ride completion** — Two-step confirmation (driver marks complete, passenger confirms) before a ride is considered done
- **Chat** — REST-based messaging scoped to a confirmed booking's two participants
- **Ratings** — Drivers and passengers rate each other after ride completion
- **Reports** — Users can report other users for inappropriate behaviour

## Tech Stack

| Layer | Choice |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.3.5 |
| Database | PostgreSQL |
| Persistence | Spring Data JPA (Hibernate) |
| Auth | JWT (jjwt) + Spring Security, stateless |
| Validation | Jakarta Bean Validation |
| Build | Maven |

## Architecture

The codebase is organized **by feature**, not by technical layer:

```
src/main/java/com/vasuarora/shareiscare/
├── auth/        Signup, login, OTP verification, JWT issuance
├── user/        Profile management, file uploads
├── vehicle/     Vehicle CRUD, ownership enforcement
├── ride/        Ride CRUD, search, completion
├── booking/     Seat booking, cancellation, completion confirmation
├── chat/        REST messaging scoped to a booking
├── rating/      Post-ride mutual ratings
├── report/      User reports
├── security/    JWT filter, security config, current-user resolution
└── common/      Shared response/error shapes, cross-feature utilities
```

Each feature package follows the same internal shape: `Controller` (HTTP
only) → `Service` (business rules, transactions) → `Repository` (data
access), with `Entity` and `dto/` alongside. Every error response is
normalized to a single shape via a global exception handler.

## API

All endpoints are served under the base path `/api/v1`. Endpoints marked
🔒 require a `Authorization: Bearer <token>` header obtained from
`/auth/verify-otp`; endpoints marked 🔓 are public.

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/auth/signup` | 🔓 | Create an account, triggers OTP |
| POST | `/auth/login` | 🔓 | Triggers OTP for an existing account |
| POST | `/auth/verify-otp` | 🔓 | Verifies OTP, returns JWT |
| GET | `/users/me` | 🔒 | Get own profile |
| PUT | `/users/me` | 🔒 | Update own profile |
| POST | `/users/me/profile-picture` | 🔒 | Upload profile picture |
| POST | `/users/me/license` | 🔒 | Upload driving license |
| POST | `/vehicles` | 🔒 | Add a vehicle |
| GET | `/vehicles` | 🔒 | List own vehicles |
| PUT | `/vehicles/{id}` | 🔒 | Update own vehicle |
| DELETE | `/vehicles/{id}` | 🔒 | Delete own vehicle |
| POST | `/rides` | 🔒 | Create a ride (requires uploaded license + owned vehicle) |
| GET | `/rides/me` | 🔒 | List own rides as driver |
| GET | `/rides` | 🔓 | Search rides by source, destination, date |
| GET | `/rides/{id}` | 🔓 | Get ride details |
| PUT | `/rides/{id}` | 🔒 | Edit own ride |
| DELETE | `/rides/{id}` | 🔒 | Cancel own ride |
| PATCH | `/rides/{id}/complete` | 🔒 | Driver marks ride complete |
| POST | `/rides/{id}/book` | 🔒 | Book a seat |
| GET | `/bookings/me` | 🔒 | List own bookings |
| DELETE | `/bookings/{id}` | 🔒 | Cancel own booking |
| PATCH | `/bookings/{id}/confirm-completion` | 🔒 | Passenger confirms ride completion |
| POST | `/chats/{bookingId}/messages` | 🔒 | Send a message on a booking |
| GET | `/chats/{bookingId}/messages` | 🔒 | Get a booking's message history |
| POST | `/ratings/driver` | 🔒 | Rate the driver on a completed booking |
| POST | `/ratings/passenger` | 🔒 | Rate the passenger on a completed booking |
| POST | `/reports` | 🔒 | Report a user |

Every response follows one of two shapes:

```json
// success
{ "success": true, "message": "...", "data": { ... } }

// error
{ "status": 400, "message": "...", "timestamp": "2026-07-18T18:30:00" }
```

## Getting Started

### Prerequisites

- Java 17+
- Maven (or use the included `mvnw` wrapper)
- PostgreSQL running locally, with a database created for this app

### Configuration

Database, JWT, and OTP settings live in `src/main/resources/application.yml`.
Sensitive values are read from environment variables with local-dev
defaults:

| Variable | Default | Purpose |
|---|---|---|
| `DB_USERNAME` | `postgres` | Postgres username |
| `DB_PASSWORD` | `postgres` | Postgres password |
| `JWT_SECRET` | placeholder | Signing key for JWTs — **must be overridden in any real deployment** |

By default the app expects a database named `cabpool` at
`jdbc:postgresql://localhost:5432/cabpool`.

### Running locally

```bash
./mvnw spring-boot:run
```

The API will be available at `http://localhost:8080/api/v1`.

### Testing the API

There's no bundled UI — use Postman, curl, or any HTTP client. A typical
flow:

1. `POST /auth/signup` → OTP is logged to the console (no SMS gateway in this version)
2. `POST /auth/verify-otp` with that OTP → returns a JWT
3. Use the JWT as a Bearer token for all subsequent requests

## Known Limitations (by design, for this version)

- OTPs are held in memory and are lost on restart — no SMS gateway is wired up, OTPs are logged to the console instead
- Chat is REST + client polling, not WebSockets
- No automated test suite yet
- Uploaded files are stored on local disk, not object storage

## License

Not currently licensed for reuse.
