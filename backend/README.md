# Modern Library System Backend

This is the backend application for the Modern Library Management System, built with Quarkus.

## Prerequisites

- Java 17 or higher
- Gradle 7.0 or higher

## Running the Application

### Development Mode

To run the application in development mode (with hot reload):

```bash
./gradlew quarkusDev
```

### Production Mode

To build and run the application for production:

```bash
# Build the application
./gradlew build

# Run the native executable (requires GraalVM)
./gradlew buildNative
./build/backend-1.0-SNAPSHOT-runner
```

## API Endpoints

The application provides the following REST API endpoints:

### Health Check

- `GET /api/health` - Health check endpoint

### Books API

- `GET /api/books` - Get all books
- `GET /api/books/{id}` - Get book by ID
- `POST /api/books` - Create a new book
- `PUT /api/books/{id}` - Update an existing book
- `DELETE /api/books/{id}` - Delete a book
- `GET /api/books/search?q={query}` - Search books

### Users API

- `GET /api/users` - Get all users
- `GET /api/users/{id}` - Get user by ID
- `POST /api/users` - Create a new user
- `PUT /api/users/{id}` - Update an existing user
- `DELETE /api/users/{id}` - Delete a user
- `GET /api/users/search?q={query}` - Search users

## API Documentation

When the application is running, you can access:

- Swagger UI: http://localhost:8080/q/swagger-ui/
- OpenAPI Schema: http://localhost:8080/q/openapi

## Configuration

The application configuration is located in `src/main/resources/application.yml`.

## Testing

Run tests with:

```bash
./gradlew test
```

## Code Quality

Run linting and static analysis:

```bash
./gradlew lint
```
