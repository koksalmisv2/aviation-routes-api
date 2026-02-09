# Aviation Routes API

A comprehensive Spring Boot REST API for managing aviation routes, locations, and transportations with intelligent route calculation.

## 🚀 Features

- **Location Management**: CRUD operations for airports and other locations
- **Transportation Management**: Manage flights, buses, subways, and uber connections
- **Smart Route Calculation**: Find all valid routes between locations with various transportation combinations
- **Authentication & Authorization**: JWT-based security with ADMIN and AGENCY roles
- **Caching**: Redis-based caching for high-performance route queries
- **API Documentation**: Interactive Swagger UI
- **Dockerized**: Complete Docker and Docker Compose setup

## 🛠️ Tech Stack

- **Java 21**
- **Spring Boot 3.2.1**
- **Spring Data JPA** (Hibernate)
- **Spring Security** with JWT
- **PostgreSQL** (production) / H2 (development)
- **Redis** (caching)
- **Swagger/OpenAPI 3**
- **Maven**
- **Docker & Docker Compose**
- **JUnit 5** & Mockito

## 📋 Prerequisites

- Java 21 or higher
- Maven 3.6+
- Docker & Docker Compose (for containerized deployment)
- PostgreSQL (if running without Docker)
- Redis (if running without Docker)

## 🏃 Running the Application

### Option 1: Quick Start with H2 (No Setup Required)

```bash
# Build the project
mvn clean package

# Run the application (H2 database is default)
mvn spring-boot:run

# Or run the jar
java -jar target/routes-api-1.0.0.jar
```

The application will start with:
- **In-memory H2 Database** (no installation needed)
- **Simple in-memory cache** (no Redis needed)
- **API**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **H2 Console**: http://localhost:8080/h2-console

**H2 Console Connection:**
- JDBC URL: `jdbc:h2:mem:aviation_db`
- Username: `sa`
- Password: (leave empty)

### Option 2: Using Docker Compose with PostgreSQL & Redis

```bash
# Build and start all services
docker-compose up --build

# Or run in detached mode
docker-compose up -d --build

# Stop all services
docker-compose down

# Stop and remove volumes
docker-compose down -v
```

The application will be available at:
- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html

### Option 3: Running Locally with PostgreSQL & Redis

1. Start PostgreSQL and Redis:
```bash
docker run -d -p 5432:5432 -e POSTGRES_DB=aviation_db -e POSTGRES_PASSWORD=postgres postgres:16-alpine
docker run -d -p 6379:6379 redis:7-alpine
```

2. Run with PostgreSQL profile:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=postgres
# Or
java -jar target/routes-api-1.0.0.jar --spring.profiles.active=postgres
```

## 🔐 Authentication

The application comes with two default users:

**Admin User:**
- Username: `admin`
- Password: `admin123`
- Role: `ADMIN`
- Access: All endpoints

**Agency User:**
- Username: `agency`
- Password: `agency123`
- Role: `AGENCY`
- Access: Only route search endpoint

### Getting a JWT Token

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

Response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "admin",
  "role": "ADMIN"
}
```

Use the token in subsequent requests:
```bash
curl -X GET http://localhost:8080/api/locations \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

## 📚 API Endpoints

### Authentication
- `POST /api/auth/login` - Login and get JWT token

### Locations (Admin Only)
- `GET /api/locations` - Get all locations
- `GET /api/locations/{id}` - Get location by ID
- `POST /api/locations` - Create new location
- `PUT /api/locations/{id}` - Update location
- `DELETE /api/locations/{id}` - Delete location

### Transportations (Admin Only)
- `GET /api/transportations` - Get all transportations
- `GET /api/transportations/{id}` - Get transportation by ID
- `POST /api/transportations` - Create new transportation
- `PUT /api/transportations/{id}` - Update transportation
- `DELETE /api/transportations/{id}` - Delete transportation

### Routes (Admin & Agency)
- `GET /api/routes?originId={id}&destinationId={id}&date={YYYY-MM-DD}` - Find all valid routes

## 🧪 Testing

Run all tests:
```bash
mvn test
```

Run specific test class:
```bash
mvn test -Dtest=RouteServiceTest
```

## 📖 API Documentation

Access the interactive Swagger UI at:
```
http://localhost:8080/swagger-ui.html
```

OpenAPI JSON specification:
```
http://localhost:8080/v3/api-docs
```

## 💡 Usage Examples

### 1. Create Locations

```bash
# Create Taksim Square
curl -X POST http://localhost:8080/api/locations \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Taksim Square",
    "country": "Turkey",
    "city": "Istanbul",
    "locationCode": "CCIST"
  }'

# Create Istanbul Airport
curl -X POST http://localhost:8080/api/locations \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Istanbul Airport",
    "country": "Turkey",
    "city": "Istanbul",
    "locationCode": "IST"
  }'

# Create London Heathrow
curl -X POST http://localhost:8080/api/locations \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "London Heathrow",
    "country": "UK",
    "city": "London",
    "locationCode": "LHR"
  }'
```

### 2. Create Transportations

```bash
# Bus from Taksim to Istanbul Airport (operates every day)
curl -X POST http://localhost:8080/api/transportations \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "originLocationId": 1,
    "destinationLocationId": 2,
    "transportationType": "BUS",
    "operatingDays": [1, 2, 3, 4, 5, 6, 7]
  }'

# Flight from Istanbul to London (Mon, Wed, Fri)
curl -X POST http://localhost:8080/api/transportations \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "originLocationId": 2,
    "destinationLocationId": 3,
    "transportationType": "FLIGHT",
    "operatingDays": [1, 3, 5]
  }'
```

### 3. Search Routes

```bash
# Find routes from Taksim to London on March 10, 2025 (Monday)
curl -X GET "http://localhost:8080/api/routes?originId=1&destinationId=3&date=2025-03-10" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

## 🏗️ Project Structure

```
src/
├── main/
│   ├── java/com/aviation/
│   │   ├── config/          # Configuration classes
│   │   ├── controller/      # REST controllers
│   │   ├── dto/            # Data Transfer Objects
│   │   ├── entity/         # JPA entities
│   │   ├── exception/      # Exception handlers
│   │   ├── repository/     # JPA repositories
│   │   ├── security/       # Security components (JWT)
│   │   └── service/        # Business logic
│   └── resources/
│       ├── application.properties
│       └── application-h2.properties
└── test/
    └── java/com/aviation/
        └── service/        # Service tests
```

## 🔄 Route Calculation Logic

The system supports the following valid route patterns:

1. **Direct Flight**: `FLIGHT`
2. **Before Flight Transfer**: `BUS/SUBWAY/UBER → FLIGHT`
3. **After Flight Transfer**: `FLIGHT → BUS/SUBWAY/UBER`
4. **Complete Route**: `BUS/SUBWAY/UBER → FLIGHT → BUS/SUBWAY/UBER`

### Rules:
- Maximum 3 transportations per route
- Exactly 1 flight required
- Maximum 1 before-flight transfer
- Maximum 1 after-flight transfer
- All transportations must be available on the selected date
- Connections must be valid (destination of one = origin of next)

## 🎯 Operating Days

Operating days are represented as integers:
- 1 = Monday
- 2 = Tuesday
- 3 = Wednesday
- 4 = Thursday
- 5 = Friday
- 6 = Saturday
- 7 = Sunday

Example: `[1, 3, 5]` means the transportation operates on Monday, Wednesday, and Friday.

## 🔧 Configuration

### Default Configuration (H2 + Simple Cache)

The application runs out-of-the-box with H2 in-memory database and simple caching. No additional setup required!

Key configuration properties in `application.properties`:

```properties
# Server
server.port=8080

# H2 Database (default)
spring.datasource.url=jdbc:h2:mem:aviation_db
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# Simple Cache (default)
spring.cache.type=simple

# JWT
jwt.secret=your-secret-key
jwt.expiration=86400000

# Hibernate
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
```

### PostgreSQL & Redis Configuration

To use PostgreSQL and Redis, run with the `postgres` profile:

```bash
java -jar target/routes-api-1.0.0.jar --spring.profiles.active=postgres
```

This activates `application-postgres.properties`:

```properties
# PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/aviation_db
spring.datasource.username=postgres
spring.datasource.password=postgres

# Redis Cache
spring.cache.type=redis
spring.data.redis.host=localhost
spring.data.redis.port=6379
```

## 🐳 Docker Commands

```bash
# Build the image
docker build -t aviation-routes-api .

# Run PostgreSQL
docker run -d --name aviation-postgres \
  -e POSTGRES_DB=aviation_db \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 postgres:16-alpine

# Run Redis
docker run -d --name aviation-redis -p 6379:6379 redis:7-alpine

# Run the application
docker run -d --name aviation-app \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/aviation_db \
  aviation-routes-api
```

## 📝 License

This project is created for educational/interview purposes.

## 👥 Authors

Full Stack Developer Candidate

## 🤝 Contributing

This is a case study project. For production use, consider adding:
- Database migration tool (Liquibase/Flyway)
- More comprehensive validation
- Rate limiting
- API versioning
- Monitoring and logging (ELK stack)
- CI/CD pipeline
