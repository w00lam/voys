# Tech Stack

## Frontend

React + Vite web frontend.

Initial choices:

- React for the browser UI.
- Vite for frontend development and build tooling.
- Browser-side recording through MediaRecorder.
- Client-side handling for recording state, upload progress, transcription status polling, and search interactions.
- Chrome/Edge-first recording with `audio/webm;codecs=opus`.

The frontend should consume the Spring Boot backend through REST APIs and authenticate with Spring Security session cookies. API requests that require authentication should include browser credentials.

## Backend

Spring Boot with Java.

Preferred initial choices:

- Java 21 LTS
- Spring Boot 3.x
- Gradle
- Spring Web for REST APIs
- Spring Validation for request validation
- Spring Security for session-based authentication and user-specific data access
- Spring Data JPA when persistence is introduced

## Database

PostgreSQL is the preferred default for production.

Use H2 only for lightweight local tests if needed, not as the main development database.

## Other

- Gradle for Spring project build and dependency management.
- Vite for frontend development and build tooling.
- Docker Compose may be used for local infrastructure such as PostgreSQL.
- GitHub Actions may be added once the first application code exists.
- Local open-source Whisper transcription for the MVP.
- Python runtime for running Whisper through a CLI process.
- Local filesystem storage for MVP audio files, behind a storage adapter.
- HttpOnly, Secure, SameSite cookies for browser authentication.
