# Sportclub Challenge Backend

(For a brief explanation of the implementation choices and architecture, please see [EXPLICACION_IMPLEMENTACION.md](EXPLICACION_IMPLEMENTACION.md)).

This project is a backend service implementation for the Sportclub technical challenge *(Implementación del challenge técnico de Sportclub)*.

It focuses on:
1.  **Data Migration:** Migrating `User` and `Branch` (Sede) data between two H2 databases using Spring Batch.
2.  **Paginated API:** Providing a REST endpoint to fetch users with pagination and sorting.
3.  **Access Validation:** Implementing a DNI-based access validation endpoint.

## Technology Stack

*   Java 21
*   Spring Boot 3.4.4
*   Spring Data JPA
*   Spring Batch
*   Spring Security (with JWT for authentication)
*   Spring Web
*   H2 Database (x2 - Source & Target)
*   Lombok
*   MapStruct
*   JUnit 5 / Mockito / ArchUnit (Testing)
*   Docker / Docker Compose
*   OpenAPI 3 (Swagger UI)

## Prerequisites

*   JDK 21 or later
*   Gradle (or use the included `./gradlew` wrapper)
*   Docker & Docker Compose
*   Git

## Build

Use the Gradle wrapper to build the project:

```bash
# Linux/macOS
./gradlew clean build

# Windows
.\gradlew.bat clean build
```

The executable JAR will be located in `build/libs/sportclub-challenge-*.jar`.

## Running the Application

### 1. Initializing the Source Database

The source database needs to be populated with initial data (100 branches, 100 users) before the migration can work effectively.

**Using Docker Compose (Recommended for Initialization):**

This command uses a specific compose file (`docker-compose.init.yml`) to activate the `init-source-db` profile which runs the database initializer.

```bash
docker-compose -f docker-compose.yml -f docker-compose.init.yml up --build
```

Wait for the initialization process to complete. The container might exit automatically after the `CommandLineRunner` finishes. If it stays running, you can stop it with `Ctrl+C`.

**Using Local Java Command:**

```bash
java -jar -Dspring.profiles.active=init-source-db build/libs/sportclub-challenge-*.jar
```

This command will start the application, run the initializer, and then keep running. You can stop it after initialization messages appear in the console.

### 2. Running the Main Application

**Using Docker Compose (Recommended):**

Ensure you have a `.env` file in the project root directory with the JWT secret (see **Security** section below).

```bash
docker-compose up --build
```

This command builds the image (if necessary) and starts the application container. The application will be available at `http://localhost:8080`.

**Using Local Java Command:**

After initializing the database (see step 1), run the application normally:

```bash
java -jar build/libs/sportclub-challenge-*.jar
```

The application will be available at `http://localhost:8080`.

### `.env` File

Docker Compose automatically reads environment variables from a `.env` file in the project root. Create this file for sensitive configurations like the JWT secret.

**Example `.env` file:**

```dotenv
# .env file (DO NOT COMMIT TO GIT!)
# Replace with your actual strong secret (at least 32 bytes for HS256)
APP_JWT_SECRET=YourVeryStrongAndLongSecretKeyHere_NeedsToBeSecure!

# Optional: Override other properties if needed
# APP_JWT_EXPIRATION_MS=3600000 # 1 hour
```

**Important:** Add `.env` to your `.gitignore` file to prevent committing secrets.

## API Endpoints

The main API endpoints are:

*   **Authentication (`/auth`)**
    *   `POST /auth/login`: Authenticates a user via DNI.
        *   Request Body: `{"dni": "11223344"}`
        *   Response (200 OK): `{"token": "eyJhbGciOi..."}`
        *   Response (401 Unauthorized): If DNI is invalid or user is not authorized/found.
        *   Response (400 Bad Request): If DNI format is invalid.
*   **Access Validation (`/acceso`)**
    *   `POST /acceso`: Validates if a user with the given DNI can access.
        *   Request Body: `{"dni": "11223344"}`
        *   Response (200 OK): `{"mensaje": "Acceso permitido"}`
        *   Response (403 Forbidden): If user state is `DENIED`.
        *   Response (404 Not Found): If user DNI is not found.
        *   Response (400 Bad Request): If DNI format is invalid.
*   **Users (`/usuarios`)** - Requires Authentication (JWT)
    *   `GET /usuarios`: Returns a paginated list of users.
        *   Query Parameters: `page` (0-based), `size`, `sort` (e.g., `lastName,asc`).
        *   Requires `Authorization: Bearer <token>` header.
        *   Response (200 OK): Paginated result (`PageResultDto<UserDto>`).
*   **Migration (`/migrate`)** - Public in current setup
    *   `POST /migrate`: Triggers the data migration process from the source DB to the target DB.
        *   Response (200 OK): `{"mensaje": "Data migration process triggered and completed."}`
        *   Response (500 Internal Server Error): If migration fails.

### Interactive Documentation

Swagger UI is available for exploring and testing the API endpoints:

[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

## Security (JWT)

*   Endpoints like `/usuarios` require JWT authentication.
*   Obtain a token by sending a `POST` request to `/auth/login` with a valid, authorized DNI.
*   Include the obtained token in the `Authorization` header for protected requests: `Authorization: Bearer <your_jwt_token>`.
*   The JWT secret key is configured via the `APP_JWT_SECRET` environment variable (read from the `.env` file by Docker Compose). **Ensure this is kept secure and is sufficiently long (>= 32 bytes).**

## Testing

Run unit and integration tests using the Gradle wrapper:

```bash
# Linux/macOS
./gradlew test

# Windows
.\gradlew.bat test
```

The project includes:
*   Unit tests for services and domain models.
*   Integration tests for controllers (Web Layer), persistence adapters (Data JPA), and the Spring Batch migration job.
*   Architecture tests using ArchUnit.

