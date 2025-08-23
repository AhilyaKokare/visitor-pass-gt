# Visitor Pass Management System v1.0.0


**1. Project Overview**

The Visitor Pass Management System is a full-stack, enterprise-grade application designed to modernize and secure the process of managing visitors across multiple corporate locations. It replaces inefficient, paper-based logs with a secure, auditable, and professional digital platform built on a modern microservice architecture.
This repository is a monorepo containing the complete source code for the project:

**/backend:** Contains all backend Java microservices.

**/frontend:** Contains the Angular single-page application.

**2. Architecture**

The system is architected for scalability and separation of concerns using a microservice pattern.

**- Frontend (/frontend/visitor-pass-frontend):** An **Angular 18** standalone application provides a dynamic and responsive user interface for all roles.

**- Core Backend API (/backend/visitor-pass-service):** A **Spring Boot 3** service acting as the primary API gateway. It handles all business logic for user management, multi-tenancy, and the complete visitor pass lifecycle.

**- Notification Service (/backend/notification-service):** A dedicated **Spring Boot 3** microservice for handling all asynchronous communications. It listens for events from the core service and sends real-time email notifications.

**- Database:** MySQL provides robust data persistence. Each microservice connects to its own isolated database schema.

**- Message Broker:** RabbitMQ facilitates reliable, asynchronous communication between the backend services.


**3. Technology Stack**

**Domain** **:**	**Technology & Frameworks**

**- Backend :**	Java 21, Spring Boot 3, Spring Security (JWT), Spring Data JPA, RabbitMQ

**- Frontend :**	Angular 18, TypeScript, Bootstrap, SCSS

**- Database :**	MySQL 8

**- API Docs :**	SpringDoc OpenAPI 3 (Swagger UI)

**- Build Tools :**	Maven (Backend), Angular CLI (Frontend)

**- Version Control :**	Git, GitHub

**4. Getting Started: Local Development Setup**

This guide details how to set up and run the entire application stack locally on your machine. Please follow the steps in the specified order.

**Prerequisites**

Before you begin, ensure you have the following installed and configured on your system:

**- Java JDK 21** or higher.

**- Node.js 22.x** (LTS version recommended).

**- Angular CLI 18.x**(npm install -g @angular/cli).

**- Maven 3.8** or higher.

**- MySQL Server 8.x** (must be running).

**- RabbitMQ Server** (must be running).

**- An API Client like Postman** (for testing).

**- A Git client.**

**Step 1: Clone the Repository**

Clone the entire project monorepo to your local machine.

git clone https://github.com/AhilyaKokare/visitor-pass-service.git

cd visitor-pass-service

**Step 2: Configure Environment Variables (Crucial for Security)**

This project uses .env files to manage sensitive credentials safely. You must create these files before running the applications.

**1.Backend - Visitor Pass Service:**

Navigate to backend/visitor-pass-service/.

Create a new file named .env.

Add the following content, replacing the placeholder values with your actual secrets:

.env

DB_PASSWORD=your_mysql_password

JWT_SECRET=a-very-long-and-secure-random-string-for-jwt

INTERNAL_API_KEY=another-long-random-string-for-service-communication

**Backend - Notification Service:**

Navigate to backend/notification-service/.

Create a new file named .env.

Add the following content, replacing the placeholders:

.env

DB_PASSWORD=your_mysql_password

MAIL_USERNAME=your.email@gmail.com

MAIL_PASSWORD=your-16-character-gmail-app-password

Note: These .env files are correctly listed in the .gitignore and should never be committed to the repository.

**Step 3: Set Up the Databases**

Before starting the backend, the databases must exist.

Connect to your local MySQL server as the root user.

Execute the following SQL commands:

SQL

CREATE DATABASE visitor_pass_db;

CREATE DATABASE notification_db;

**Step 4: Build and Run the Backend Services**

You will need two separate terminal windows for this.

**Terminal 1 - Start visitor-pass-service (MUST BE FIRST):**

**Navigate to the service directory**

cd backend/visitor-pass-service

**Build the project**

mvn clean install

**Run the application**

mvn spring-boot:run

Wait for this service to start up completely. You should see a log message saying "Started VisitorPassServiceApplication". This service will also create the necessary RabbitMQ queues.

**Terminal 2 - Start notification-service:**

**Navigate to the service directory**

cd backend/notification-service

**Build the project**

mvn clean install

**Run the application**

mvn spring-boot:run

Wait for this service to start up completely. It will connect to the RabbitMQ queues created by the first service.

**Step 5: Build and Run the Frontend Application**

You will need a third terminal window for the frontend.

**Terminal 3 - Start the Angular UI:**

**Navigate to the frontend directory**

cd frontend/visitor-pass-frontend

**Install all dependencies (only needs to be done once)**

npm install

**Start the local development server**

ng serve

**Step 6: Access the Application**

Your complete system is now running!

**Frontend UI:** http://localhost:4200

**Interactive API Docs:** http://localhost:8080/swagger-ui.html

**RabbitMQ Management:** http://localhost:15672 (User: guest / Pass: guest)

**5. Default Login Credentials**

After the visitor-pass-service starts for the first time, it automatically creates a Super Admin account.

**Role:** Super Admin

**Username:** superadmin@system.com

**Password:** superadmin123

You can use this account to log in and begin creating tenants and tenant administrators.
