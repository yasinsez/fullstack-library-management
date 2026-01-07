# Modern Library Management System

A comprehensive Java backend and Next.js frontend for managing library operations, including book management, inventory tracking, user management, and loan processing.

## Project Structure

```
library-system/
├── backend/              # Java backend code
│   ├── src/main/java/    # Source code
│   │   └── com/yasinsez/library/
│   │       ├── config/   # Application configuration
│   │       ├── controller/ # API endpoints
│   │       ├── dto/      # Data Transfer Objects
│   │       ├── exception/ # Custom exceptions
│   │       ├── model/    # Entity classes
│   │       ├── repository/ # Data access implementations
│   │       └── service/  # Business logic
│   └── src/test/java/    # Unit tests
│       └── com/yasinsez/library/
│           ├── controller/ # Controller tests
│           ├── repository/ # Repository tests
│           └── service/  # Service tests
├── frontend/             # Next.js frontend code
│   ├── app/              # Application pages
│   ├── components/       # Reusable components
│   ├── lib/              # Utility functions
│   └── public/           # Static assets
└── docs/                 # Documentation
    ├── ClassDesign.md    # Class design documentation
    ├── CursorFeatures.md # Cursor IDE features
    └── README.md         # This file
```

## Getting Started

### Prerequisites

- **Java JDK 21**: [Download from Adoptium](https://adoptium.net/temurin/releases/?version=21)
- **Maven 3.9+**: For building the backend
- **Node.js 18+**: For running the frontend
- **MS SQL Server**: Local or remote instance for the database

### 1. Database Setup

The system uses MS SQL Server. You can set it up using Docker or a local installation:

1.  **Create Database**: Create a database named `library`.
2.  **Initialize Schema**: Run the script located at `database/mssql/01_schema_creation.sql`.
3.  **Seed Data**: (Optional) Run `database/mssql/02_sample_data.sql` for initial records.

Update `backend/src/main/resources/application.properties` with your credentials:
```properties
quarkus.datasource.username=your_username
quarkus.datasource.password=your_password
quarkus.datasource.jdbc.url=jdbc:sqlserver://localhost:1433;databaseName=library;...
```

### 2. Running the Backend

1.  Navigate to the backend directory:
    ```bash
    cd backend
    ```
2.  Run in development mode:
    ```bash
    mvn quarkus:dev
    ```
    - **API Base URL**: `http://localhost:8083`
    - **Swagger UI**: `http://localhost:8083/q/swagger-ui`
    - **Health Check**: `http://localhost:8083/q/health`

### 3. Running the Frontend

1.  Navigate to the frontend directory:
    ```bash
    cd frontend
    ```
2.  Install dependencies:
    ```bash
    npm install
    ```
3.  Run the development server:
    ```bash
    npm run dev
    ```
    - **Frontend URL**: `http://localhost:3000`

### 4. Default Credentials (Seed Data)

If you used the sample data script, you can log in with:
- **Admin**: `admin` / `Admin123!`
- **Librarian**: `librarian` / `Lib123!`
- **Member**: `member` / `Mem123!`

---

## Features

- **Book Management**: Add, update, delete, and search for books
- **User Management**: User registration and login
- **Inventory Tracking**: Manage book copies and their availability
- **Loan Processing**: Manage book borrowing, returns, and extensions
- **Data Validation**: Enforce business rules and validate data integrity

## Technology Stack

### Backend

- **Java**: Core programming language
- **Quarkus**: Supersonic, subatomic Java framework
- **Maven**: Build system and dependency management
- **JUnit 5**: Testing framework
- **Mockito**: Mocking framework for testing
- **Lombok**: Reduces boilerplate code
- **SLF4J**: Logging framework

### Frontend

- **Next.js**: React framework for production
- **React**: A JavaScript library for building user interfaces
- **TypeScript**: Typed JavaScript at scale
- **Tailwind CSS**: A utility-first CSS framework

## Design Patterns

- **Repository Pattern**: For data access abstraction
- **DTO Pattern**: For API request/response separation
- **Builder Pattern**: For object construction (via Lombok)
- **Dependency Injection**: For component composition
