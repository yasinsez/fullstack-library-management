# MSSQL Database Setup Guide

This guide explains how to connect your Java backend to Microsoft SQL Server and use the DAO layer.

## 1. Database Configuration

### Environment Variables
You can configure the database connection using environment variables:

```bash
export DB_USERNAME=sa
export DB_PASSWORD=YourPassword123
export DB_URL=jdbc:sqlserver://localhost:1433;databaseName=ModernLibrary;encrypt=false;trustServerCertificate=true
```

### Application Configuration
The database is configured in `src/main/resources/application.yml`:

```yaml
quarkus:
  datasource:
    db-kind: mssql
    username: ${DB_USERNAME:sa}
    password: ${DB_PASSWORD:YourPassword123}
    jdbc:
      url: ${DB_URL:jdbc:sqlserver://localhost:1433;databaseName=ModernLibrary;encrypt=false;trustServerCertificate=true}
      driver: com.microsoft.sqlserver.jdbc.SQLServerDriver
```

## 2. Dependencies Added

The following dependencies have been added to `build.gradle`:

```gradle
// Database dependencies
implementation 'io.quarkus:quarkus-agroal'
implementation 'io.quarkus:quarkus-jdbc-mssql'
implementation 'com.microsoft.sqlserver:mssql-jdbc:12.4.2.jre11'
```

## 3. Architecture Overview

### Key Components

1. **DatabaseConnectionManager** - Manages MSSQL connections using Quarkus Agroal connection pooling
2. **DAO Implementations** - MSSQL-specific implementations of DAO interfaces
3. **Entity Mapping** - Maps database result sets to Java objects

### DAO Layer Structure

```
dao/
├── BaseDAO.java              # Base DAO interface
├── UserDAO.java              # User-specific DAO interface
├── BookDAO.java              # Book-specific DAO interface
└── impl/
    ├── MSSQLUserDAOImpl.java # MSSQL implementation of UserDAO
    └── MSSQLBookDAOImpl.java # MSSQL implementation of BookDAO
```

## 4. Usage Examples

### Using UserDAO

```java
@Inject
UserDAO userDAO;

// Save a new user
User user = Member.builder()
    .username("johndoe")
    .email("john@example.com")
    .fullName("John Doe")
    .active(true)
    .status(UserStatus.ACTIVE)
    .build();

User savedUser = userDAO.save(user);

// Find user by username
Optional<User> foundUser = userDAO.findByUsername("johndoe");

// Find all active users
List<User> activeUsers = userDAO.findActiveUsers();
```

### Using BookDAO

```java
@Inject
BookDAO bookDAO;

// Save a new book
Book book = Book.builder()
    .title("Clean Code")
    .ISBN("9780132350884")
    .publicationYear(2008)
    .description("A handbook of agile software craftsmanship")
    .build();

Book savedBook = bookDAO.save(book);

// Find book by ISBN
Optional<Book> foundBook = bookDAO.findByISBN("9780132350884");

// Search books by title
List<Book> books = bookDAO.findByTitle("Clean Code");
```

## 5. Database Schema

The application uses the schema defined in `database/mssql/01_schema_creation.sql`. Make sure to run this script to create the necessary tables:

```sql
-- Create the database
CREATE DATABASE ModernLibrary;
GO

USE ModernLibrary;
GO

-- Run the schema creation script
-- (Content from 01_schema_creation.sql)
```

## 6. Connection Pooling

The application uses Quarkus Agroal for connection pooling with the following configuration:

- **Min Pool Size**: 5 connections
- **Max Pool Size**: 20 connections
- **Acquisition Timeout**: 5 seconds
- **Max Lifetime**: 5 minutes

## 7. Health Checks

A database health check is available at:
- **Health endpoint**: `/q/health`
- **Database status**: `/api/database/status`
- **Database info**: `/api/database/info`

## 8. Error Handling

All DAO operations throw `DAOException` for database-related errors. These exceptions wrap the underlying `SQLException` and provide meaningful error messages.

## 9. Testing the Connection

1. Start your MSSQL server
2. Create the database and run the schema script
3. Start the Quarkus application: `./gradlew quarkusDev`
4. Check the health endpoint: `curl http://localhost:8080/q/health`
5. Test database status: `curl http://localhost:8080/api/database/status`

## 10. Next Steps

To fully implement the DAO layer:

1. Complete the remaining DAO implementations (LoanDAO, FineDAO, etc.)
2. Add transaction management using `@Transactional`
3. Implement proper error handling and logging
4. Add integration tests for DAO operations
5. Consider adding database migration scripts
