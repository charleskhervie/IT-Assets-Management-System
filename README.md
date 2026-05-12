# IT Asset Management System

[![Java](https://img.shields.io/badge/Java-21-orange)](https://openjdk.java.net/)
[![Maven](https://img.shields.io/badge/Maven-3.6%2B-blue)](https://maven.apache.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0%2B-blue)](https://www.mysql.com/)
[![JavaFX](https://img.shields.io/badge/JavaFX-23-green)](https://openjfx.io/)
[![License](https://img.shields.io/badge/License-Educational-red)](#license)

Course project for **CMSC 127 - File Processing and Database Systems**.

## Overview

The **IT Asset Management System (ITAMS)** is a JavaFX desktop application for managing IT equipment, employees, categories, departments, and asset transactions. It stores data in MySQL and includes first-run database setup, role-based login, reporting, and import/export features.

## Features

- User authentication with Admin and Employee access modes
- Department, employee, category, equipment, and unit management
- Check-in and check-out transaction tracking
- Transaction history and reporting
- Import and export tools
- First-run database setup with automatic `.env` generation

## Project Metadata

| Property | Value |
|----------|-------|
| Group ID | `itams` |
| Artifact ID | `it-assets-manager` |
| Version | `0.1` |
| Java Version | `21` |
| Build Tool | `Maven` |
| UI Framework | `JavaFX 23` |
| Database | `MySQL 8+` |

## Folder Structure

```text
IT-Assets-Management-System/
|-- pom.xml
|-- README.md
|-- ITAMS-Standalone-PORTABLE-FIXED.exe
|-- DEBUG_DIST/                  # Portable packaging files
|-- src/
|   |-- main/
|   |   |-- java/
|   |   |   |-- dao/
|   |   |   |   |-- dao_util/
|   |   |   |   |   |-- CredentialManager.java
|   |   |   |   |   |-- DatabaseSetup.java
|   |   |   |   |   `-- DBUtil.java
|   |   |   |   |-- handler/
|   |   |   |   |-- impl/
|   |   |   |   |-- intfc/
|   |   |   |   `-- model/
|   |   |   |-- itams/
|   |   |   |   |-- App.java
|   |   |   |   `-- AppConfig.java
|   |   |   `-- ui/
|   |   |       |-- controller/
|   |   |       |-- service/
|   |   |       `-- util/
|   |   `-- resources/
|   |       |-- css/
|   |       |-- fxml/
|   |       `-- sql/
|   `-- test/
|       `-- java/
`-- target/                      # Maven build output
```

Notes:

- `.env` is not meant to be committed. It is generated automatically on first successful setup.
- `target/` and some files in `DEBUG_DIST/` are generated build artifacts.

## Prerequisites

Before running the project from source, install:

- Java 21
- Maven 3.6 or newer
- MySQL Server 8.0 or newer

JavaFX is already declared in `pom.xml` as a project dependency.

## First-Run Setup

The application now handles first-run setup directly.

1. Start the app.
2. If no valid `.env` file exists, the **Database Configuration** dialog appears.
3. Enter your MySQL host, port, username, and password.
4. The app tests the connection, creates the `itams_db` database if needed, and loads the SQL schema from `src/main/resources/sql/itams_db.sql`.
5. A `.env` file is then created automatically.

Behavior of `.env`:

- When running the portable executable, `.env` is created beside the `.exe`.
- When running from source, `.env` is created in the current working directory.
- If `.env` is deleted, the app treats the next launch as a first run and asks for MySQL credentials again.

## How to Run

### Run from source

From the project root:

```bash
mvn clean compile
mvn javafx:run
```

### Run the portable build

You can also run:

```text
ITAMS-Standalone-PORTABLE-FIXED.exe
```

This version stores its `.env` file in the same folder as the executable.

## Usage

### Sample login credentials

The seeded SQL file includes these sample accounts:

| Access in app | Username | Password |
|---------------|----------|----------|
| Admin | `admin_xeon` | `pass123` |
| Employee | `staff_andrei` | `pass456` |

Note: the login screen uses the role label **Employee**, even though the seed data stores that user role as `Staff`.

### Typical workflow

1. Complete database setup on first launch if prompted.
2. Log in with an Admin or Employee account.
3. Use the dashboard to manage assets, employees, transactions, reports, and imports/exports.

## Dependencies

| Dependency | Purpose |
|------------|---------|
| JavaFX | Desktop UI |
| MySQL Connector/J | Database connectivity |
| OpenPDF | PDF report generation |
| JUnit | Test support |

## Testing

Run tests with:

```bash
mvn test
```

## Packaging Notes

The repository includes a portable Windows build workflow under `DEBUG_DIST/`. The current final portable artifact is:

```text
ITAMS-Standalone-PORTABLE-FIXED.exe
```

## Contributing

This is a course project. If you modify it:

- Keep the JavaFX and FXML structure consistent
- Preserve database compatibility with `itams_db.sql`
- Test first-run setup and login flow after credential or database changes
- Avoid committing generated `.env` files or disposable build artifacts

## License

This project is for educational use as part of CMSC 127 coursework.
