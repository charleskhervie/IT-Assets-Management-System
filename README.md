# IT Asset Management System

[![Java](https://img.shields.io/badge/Java-21-orange)](https://openjdk.java.net/)
[![Maven](https://img.shields.io/badge/Maven-3.6+-blue)](https://maven.apache.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0+-blue)](https://www.mysql.com/)
[![JavaFX](https://img.shields.io/badge/JavaFX-23-green)](https://openjfx.io/)
[![License](https://img.shields.io/badge/License-Educational-red)](#license)

A project made in compliance with the requirements for **CMSC 127 - File Processing and Database Systems**.

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Project Metadata](#project-metadata)
- [Folder Structure](#folder-structure)
- [Prerequisites](#prerequisites)
- [Setup Instructions](#setup-instructions)
- [How to Run](#how-to-run)
- [Usage](#usage)
- [Dependencies](#dependencies)
- [Testing](#testing)
- [Contributing](#contributing)
- [License](#license)

## 📖 Overview

The **IT Assets Management System (ITAMS)** is a Java-based desktop application designed to manage IT assets within an organization. It provides functionality for tracking equipment, employees, departments, and asset transactions such as check-in and check-out operations. The system features a user-friendly graphical interface built with JavaFX and FXML, and it uses a MySQL database for data persistence.

## ✨ Features

- 🔐 **User Authentication** and role-based access (Admin/Staff)
- 🏢 **Department Management** - Add, edit, and manage organizational departments
- 👥 **Employee Management** - Handle employee records and assignments
- 📦 **Equipment Tracking** - Monitor IT assets with detailed information
- 🏷️ **Category Management** - Organize equipment by categories
- 📥📤 **Check-in/Check-out** - Track asset borrowing and returns
- 📊 **Transaction History** - View complete audit trail of asset movements
- 📄 **Reporting** - Generate PDF reports for assets and transactions
- 📥📤 **Import/Export** - Data migration capabilities

## 📋 Project Metadata

| Property | Value |
|----------|-------|
| **Group ID** | `itams` |
| **Artifact ID** | `it-assets-manager` |
| **Version** | `0.1` |
| **Java Version** | 21 |
| **Build Tool** | Maven |
| **UI Framework** | JavaFX |
| **Database** | MySQL |

## 📁 Folder Structure

```
IT-Assets-Management-System/
├── app.env                          # 🔧 Environment configuration file
├── pom.xml                          # 📦 Maven project configuration
├── README.md                        # 📖 This file
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── dao/
│   │   │   │   ├── dao_util/
│   │   │   │   │   ├── CredentialManager.java  # 🔑 Handles environment variables
│   │   │   │   │   └── DBUtil.java             # 🗄️ Database connection utility
│   │   │   │   ├── handler/                     # 🔄 Data access handlers
│   │   │   │   │   ├── CategoryHandler.java
│   │   │   │   │   ├── DepartmentHandler.java
│   │   │   │   │   ├── EmployeeHandler.java
│   │   │   │   │   ├── EquipmentHandler.java
│   │   │   │   │   └── ... (other handlers)
│   │   │   │   ├── impl/                        # ⚙️ Implementation classes
│   │   │   │   ├── intfc/                       # 🔌 Interfaces
│   │   │   │   └── model/                       # 📋 Data model classes
│   │   │   └── itams/
│   │   │       └── App.java                     # 🚀 Main application class
│   │   └── resources/
│   │       ├── css/                             # 🎨 Stylesheets
│   │       │   ├── dashboard.css
│   │       │   └── unitsList.css
│   │       ├── fxml/                            # 🖼️ UI layout files
│   │       │   ├── addAsset.fxml
│   │       │   ├── AddCategory.fxml
│   │       │   ├── addEmployee.fxml
│   │       │   ├── AddEquipment.fxml
│   │       │   ├── AddRemarks.fxml
│   │       │   ├── categoryList.fxml
│   │       │   ├── Check-in.fxml
│   │       │   ├── Check-out.fxml
│   │       │   ├── Dashboard.fxml
│   │       │   ├── editAsset.fxml
│   │       │   ├── editCategory.fxml
│   │       │   ├── editEquipment.fxml
│   │       │   ├── Employee.fxml
│   │       │   ├── equipmentList.fxml
│   │       │   ├── importExport.fxml
│   │       │   ├── login.fxml
│   │       │   ├── report.fxml
│   │       │   ├── Transaction.fxml
│   │       │   ├── unitsList.fxml
│   │       │   └── viewTransaction.fxml
│   │       └── sql/
│   │           └── itams_db.sql                  # 🗃️ Database schema and sample data
│   └── test/
│       └── java/
│           └── itams/
│               └── AppTest.java                 # 🧪 Unit tests
└── target/                                      # 🏗️ Build output directory
    ├── classes/                                 # 📂 Compiled classes
    ├── generated-sources/
    ├── maven-status/
    └── test-classes/
```

## 📋 Prerequisites

Before running the application, ensure you have the following installed:

- ☕ **Java 21** or higher
- 📦 **Maven 3.6+**
- 🗄️ **MySQL 8.0+**
- 🎨 **JavaFX** (included as dependency)

## 🛠️ Setup Instructions

### 1. Database Setup

1. 📥 Install MySQL Server on your system.
2. 🗃️ Create a new database named `itams_db`:
   ```sql
   CREATE DATABASE itams_db;
   ```
3. 📤 Import the database schema and sample data:
   ```bash
   mysql -u root -p itams_db < src/main/resources/sql/itams_db.sql
   ```

### 2. Environment Configuration

1. ✏️ Edit the `app.env` file to configure database credentials:
   ```env
   user=root
   password=your_mysql_password
   ```
   Replace `your_mysql_password` with your actual MySQL root password.

## 🚀 How to Run

1. **Clone or navigate to the project directory:**
   ```bash
   cd IT-Assets-Management-System/ITAMS/IT-Assets-Management-System
   ```

2. **Compile the project:**
   ```bash
   mvn clean compile
   ```

3. **Run the application:**
   ```bash
   mvn javafx:run
   ```

   > **Alternative:** Run directly with Java (after compilation):
   ```bash
   java --module-path "path/to/javafx/lib" --add-modules javafx.controls,javafx.fxml -cp target/classes itams.App
   ```
   Replace `path/to/javafx/lib` with the actual path to your JavaFX library.

## 📖 Usage

### Login Credentials

Use the credentials from the sample data in `itams_db.sql`:

| Role | Username | Password |
|------|----------|----------|
| Admin | `admin_xeon` | `pass123` |
| Staff | `staff_andrei` | `pass456` |

### Application Workflow

1. **🔐 Login:** Enter your credentials on the login screen
2. **🏠 Dashboard:** Access the main dashboard to manage assets, employees, and transactions
3. **📋 Features:**
   - ➕ Add/Edit/Delete equipment, categories, departments, and employees
   - 📥📤 Check-in/Check-out assets
   - 📊 View transaction history
   - 📄 Generate reports
   - 📥📤 Import/Export data

## 📦 Dependencies

| Dependency | Purpose |
|------------|---------|
| **JavaFX** | GUI framework for desktop application |
| **MySQL Connector/J** | Database connectivity |
| **OpenPDF** | PDF report generation |
| **JUnit** | Unit testing framework |

## 🧪 Testing

Run the tests using Maven:
```bash
mvn test
```

## 🤝 Contributing

This is a course project for **CMSC 127**. For modifications:

- ✅ Ensure changes are tested
- 📝 Follow the existing code structure
- 🔄 Maintain database schema integrity
- 🎨 Keep UI consistent with JavaFX design

## 📄 License

This project is for **educational purposes** as part of CMSC 127 coursework.

---

⭐ **Star this repo** if you find it helpful!