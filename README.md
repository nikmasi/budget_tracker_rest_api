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

- IntelliJ IDEA  (optional)
- MySQL Workbench (optional)

## Database Setup

The application uses MySQL as its database. Before running the application, create the database and execute the SQL scripts to create the tables and insert the initial data.

### 1. Create the database

Connect to your MySQL server using MySQL Workbench or a MySQL terminal and execute:

```sql
CREATE DATABASE budget_tracker;
```

### 2. Execute the SQL scripts

The project contains SQL scripts for creating the database tables and inserting initial data. Execute the table creation script first, followed by the data insertion script.

#### Option A: Using MySQL Workbench

1. Open MySQL Workbench and connect to your MySQL server.
2. Open `skripta.sql` using **File → Open SQL Script**.
3. Select the `budget_tracker` schema as the default schema.
4. Execute the script by clicking the lightning bolt icon.
5. Open `insert_skripta.sql` and execute it in the same way.

#### Option B: Using the command line

Open CMD, navigate to the directory containing the SQL scripts, and execute:

```cmd
mysql -u root -p budget_tracker < skripta.sql
```

Then insert the initial data:

```cmd
mysql -u root -p budget_tracker < insert_skripta.sql
```

Enter your MySQL password when prompted.

If the `mysql` command is not recognized, use the full path to the MySQL client or add the MySQL `bin` directory to your system's `PATH`.

For example, if MySQL Server 8.0 is installed in the default location:

```cmd
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p budget_tracker < skripta.sql
```

### 3. Configure the database connection

Configure the database connection in `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/budget_tracker
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD}
```

Set the `DB_PASSWORD` environment variable to your MySQL password before starting the application. You can also configure `DB_USERNAME` if you use a different MySQL user.

## Running the Application

Clone the repository:

```bash
git clone <repository-url>
cd budget_tracker_api
```

Make sure MySQL is running and the database is configured.

Run the application on Windows:

```bash
./gradlew bootRun
```

Run the application on Linux or macOS:

```bash
./gradlew bootRun
```

Or using IntelliJ IDEA

1. Open the project in IntelliJ IDEA.
2. Wait for Gradle to finish syncing the project.
3. Make sure the JDK is set to Java 17.
4. Open `BudgetTrackerApiApplication.kt`.
5. Click the green **Run** button.

The application will start on the configured port, usually `8080`.

Base API URL:

`http://localhost:8080/api`

## Running the Tests

Run all tests on Windows:

```bash
./gradlew.bat test
```

Run all tests on Linux or macOS:

```bash
./gradlew test
```

Or using IntelliJ IDEA

1. Open the project in IntelliJ IDEA.
2. Wait for Gradle to finish syncing.
3. Open the `src/test/kotlin` directory.
4. Right-click the test directory or an individual test class.
5. Select **Run Tests** or **Run 'All Tests'**, depending on the selected item.

To view the generated HTML test report, open:

`build/reports/tests/test/index.html`

The tests cover application functionality, including controller and service behavior.

## API Documentation

The API is documented using OpenAPI and Swagger UI.

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
