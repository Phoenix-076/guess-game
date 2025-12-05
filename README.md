# Emoji Country Guess

Full-stack game where players guess countries from emoji clues. Angular frontend, Spring Boot backend, in-memory H2 database seeded with countries by difficulty.

## Features
- Difficulty modes (easy/medium/hard/legend) with scoring rules.
- Leaderboard (top 10 players) and full leaderboard for admins.
- Admin dashboard to add new questions per difficulty.
- Username/password auth (stored in memory for demo).

## Tech
- Backend: Java 21, Spring Boot 3, Spring Data JPA, H2 (in-memory).
- Frontend: Angular, RxJS, TypeScript.

## Run it locally
From repo root `Guess/`:

### Backend (port 8080)
```bash
cd backend
./mvnw spring-boot:run
```
If `mvnw` is not executable on your OS, use `mvn spring-boot:run`.

### Frontend (port 4200)
```bash
cd frontend
npm install
npm start
```

## Accounts
- Default admin: username `admin`, password `admin123` (configurable in `backend/src/main/resources/application.properties`).
- Demo players are seeded in `data.sql`; create more via the app.

Admin accounts cannot play; they can add questions and view the full leaderboard.

## API base URLs
- Game: `http://localhost:8080/api/game`
- Score: `http://localhost:8080/api/score`
- Admin: `http://localhost:8080/api/admin`

## Tests
- Backend: `./mvnw test`
- Frontend: `npm test`
