# Budget Tracker API

A REST API built with Kotlin and Spring Boot.

## Technologies

- Kotlin
- Spring Boot
- Spring Data JPA
- MySQL
- Gradle
- Swagger / OpenAPI
- JUnit 5 and MockK

## Prerequisites

Make sure the following are installed:

- JDK 17
- MySQL Server
- Git

## Database Setup

The application uses MySQL to store categories, transactions, and budgets.

1. Start your MySQL server.
2. Create the database if it does not already exist:

   ```sql
   CREATE DATABASE budget_tracker;
   ```

3. Configure the database connection in `src/main/resources/application.properties`:

   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/budget_tracker
   spring.datasource.username=${DB_USERNAME:root}
   spring.datasource.password=${DB_PASSWORD:1234}

   spring.jpa.hibernate.ddl-auto=update
   ```

The default configuration uses the local MySQL server, the `root` user, and the password `1234`. 


## Running the Application

Clone the repository:

```bash
git clone <repository-url>
cd budget_tracker_api
```

Make sure MySQL is running and the database is configured.

Run the application on Windows:

```bash
gradlew.bat bootRun
```

Run the application on Linux or macOS:

```bash
./gradlew bootRun
```

The application will start on the configured port, usually `8080`.

Base API URL:

`http://localhost:8080/api`

## Running the Tests

Run all tests on Windows:

```bash
gradlew.bat test
```

Run all tests on Linux or macOS:

```bash
./gradlew test
```

To view the generated HTML test report, open:

`build/reports/tests/test/index.html`

The tests cover application functionality, including controller and service behavior.

## API Documentation

The API is documented using OpenAPI and Swagger UI.

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
