# 🎰 Blackjack Application

A production-ready Blackjack game implementation built with **Spring Boot 3.2**, **Java 21**, and **reactive programming**. This project demonstrates **Domain-Driven Design (DDD)** and **Hexagonal Architecture** (Ports & Adapters) principles with a strong focus on SOLID design.

> **Educational Project**: Perfect for developers learning DDD, Hexagonal Architecture, and SOLID principles in a real-world context.

---

## 📋 Table of Contents

- [Project Overview](#-project-overview)
- [Features](#-features)
- [Architecture](#-architecture-ddd--hexagonal)
  - [DDD Concepts](#ddd-concepts)
  - [Hexagonal Architecture](#hexagonal-architecture-ports--adapters)
  - [SOLID Principles](#solid-principles-applied)
- [Tech Stack](#-tech-stack)
- [Project Structure](#-project-structure)
- [Quick Start](#-quick-start)
- [Docker Setup](#-docker-setup)
- [API Documentation](#-api-documentation)
- [Testing](#-testing)
- [Contributing Guidelines](#-contributing-guidelines)

---

## 🎯 Project Overview

This is a full-stack Blackjack game application that allows players to:

- **Create player profiles** with statistics tracking
- **Start new games** with automatic card dealing
- **Make game decisions**: Hit, Stand, Double Down
- **View game history** and player rankings
- **Experience real-time gameplay** with reactive APIs

### Why This Architecture?

This project serves as a **learning reference** for implementing:

1. **Domain-Driven Design** - Organizing code around business domain concepts
2. **Hexagonal Architecture** - Decoupling core business logic from infrastructure
3. **SOLID Principles** - Writing maintainable, testable, and extensible code
4. **Reactive Programming** - Building non-blocking, event-driven systems

---

## ✨ Features

### Core Game Mechanics
| Feature | Description |
|---------|-------------|
| **Card Dealing** | Automatic dealing of initial cards to player and crupier |
| **Hit/Stand** | Player can request additional cards or end their turn |
| **Blackjack Detection** | Automatic detection of natural blackjack (21 with first 2 cards) |
| **Scoring System** | Proper ace handling (1 or 11) and bust detection |
| **Crupier AI** | Crupier follows standard casino rules (hit on 16, stand on 17) |
| **Win Determination** | Complete win/lose/push logic with statistics updates |

### Player Management
| Feature | Description |
|---------|-------------|
| **Profile Creation** | Create new player profiles with unique names |
| **Statistics Tracking** | Tracks wins, losses, pushes, and blackjack count |
| **Game History** | Complete history of all games played |
| **Leaderboard** | Player rankings based on win rate |

### Technical Features
| Feature | Description |
|---------|-------------|
| **Reactive API** | Non-blocking WebFlux endpoints |
| **Multiple Databases** | MongoDB for games, MySQL for players |
| **Docker Support** | Full containerization with docker-compose |
| **OpenAPI Docs** | Interactive API documentation |
| **Testcontainers** | Integration testing with real databases |

---

## 🏗️ Architecture

### DDD Concepts

This project implements several Domain-Driven Design patterns:

#### 1. **Bounded Contexts**
The application is divided into clear bounded contexts:

```
┌─────────────────────────────────────────────────────────────┐
│                    BLACKJACK APP                            │
├─────────────────┬─────────────────┬─────────────────────────┤
│  Game Context   │ Player Context  │      Deck Context       │
│                 │                 │                         │
│ • Game          │ • Player        │ • Card                  │
│ • Crupier       │ • PlayerStats   │ • Deck                  │
│ • Hand          │                 │ • ScoringService        │
│ • GameHistory   │                 │ • CardRank, Suit        │
└─────────────────┴─────────────────┴─────────────────────────┘
```

**Why Bounded Contexts Matter:**
- Each context has its own Ubiquitous Language (business terminology)
- Clear boundaries prevent domain concepts from bleeding into each other
- Teams can work on different contexts independently

#### 2. **Aggregate Roots**

```java
// Game Aggregate Root - owns player and crupier
public class Game {
    private Player player;      // Owned by Game aggregate
    private Crupier crupier;    // Owned by Game aggregate
    private Deck deck;          // Owned by Game aggregate
    // ...
}
```

**Aggregate Root Principles:**
- Entry point to the aggregate
- Maintains consistency within the aggregate
- External references only by ID

#### 3. **Value Objects**

```java
// Immutable value object - no identity
public record Card(CardRank rank, Suit suit) {
    public int getNumericValue() { ... }
}

// Another value object
public record Hand(List<Card> cards) {
    public int calculateScore() { ... }
}
```

**Value Object Characteristics:**
- No identity (two cards with same rank/suit are equal)
- Immutable (once created, cannot be changed)
- Self-validating

#### 4. **Domain Services**

When logic doesn't belong to a single entity:

```java
// Domain service for scoring logic
public class ScoringService {
    public static int calculateHandScore(List<Card> cards) {
        // Ace handling, score calculation
    }
}
```

#### 5. **Repository Pattern (Abstraction)**

```java
// Domain layer defines the interface
public interface GameRepository {
    Mono<Game> save(Game game);
    Mono<Game> findById(UUID id);
    Flux<Game> findByPlayerId(UUID playerId);
}

// Infrastructure implements it
@Repository
public class GameRepositoryImpl implements GameRepository {
    // MongoDB implementation
}
```

---

### Hexagonal Architecture (Ports & Adapters)

The application follows the Hexagonal Architecture pattern, also known as **Ports & Adapters**:

```
                    ┌─────────────────────────────────────────┐
                    │           CORE DOMAIN                   │
                    │  ┌─────────────────────────────────┐    │
                    │  │         Domain Layer            │    │
                    │  │  • Entities (Game, Player)      │    │
                    │  │  • Value Objects (Card, Hand)   │    │
                    │  │  • Domain Services              │    │
                    │  │  • Repository Interfaces        │    │
                    │  └─────────────────────────────────┘    │
                    │                                         │
                    │  ┌─────────────────────────────────┐    │
                    │  │      Application Layer          │    │
                    │  │  • Use Cases (GameService)      │    │
                    │  │  • DTOs/Response Objects        │    │
                    │  │  • Ports (Inbound Interfaces)   │    │
                    │  └─────────────────────────────────┘    │
                    └──────────────┬──────────────────────────┘
                                   │
              ┌────────────────────┴────────────────────┐
              │                                         │
     ┌────────▼────────┐                    ┌──────────▼──────────┐
     │  INBOUND ADAPTER │                   │ OUTBOUND ADAPTERS   │
     │  (Driving Ports)  │                   │  (Driven Ports)     │
     │                   │                   │                      │
     │  • GameController │                   │  • MongoRepository  │
     │  • PlayerController│                  │  • MySQLRepository  │
     │                   │                   │  • OpenApiConfig    │
     └────────┬────────┘                    └──────────┬──────────┘
              │                                         │
              └────────────────────┬────────────────────┘
                                   │
                    ┌──────────────▼──────────────┐
                    │    EXTERNAL SYSTEMS         │
                    │  • Web Clients (HTTP)       │
                    │  • MongoDB Database         │
                    │  • MySQL Database           │
                    └─────────────────────────────┘
```

#### **Inbound Ports (Driving)**
Define how external actors drive the application:
- `GameController` - HTTP endpoint for game operations
- `PlayerController` - HTTP endpoint for player operations

#### **Outbound Ports (Driven)**
Define what external systems the application can call:
- `GameRepository` - Port for persisting games
- `PlayerRepository` - Port for persisting players

#### **Adapters**
Implement the ports:
- **Inbound**: WebFlux controllers handling HTTP requests
- **Outbound**: MongoDB and R2DBC implementations of repositories

---

### SOLID Principles Applied

| Principle | Implementation | Benefit |
|-----------|---------------|---------|
| **Single Responsibility** | Each class has one reason to change | Easier maintenance and testing |
| **Open/Closed** | Open for extension, closed for modification | New features without breaking existing code |
| **Liskov Substitution** | Interfaces implemented consistently | Flexible polymorphic behavior |
| **Interface Segregation** | Focused interfaces (no fat interfaces) | Clients depend only on what they use |
| **Dependency Inversion** | Domain depends on abstractions, not concretions | Swappable implementations |

#### Example: Dependency Inversion

```java
// ❌ BAD: Application depends on concrete implementation
public class GameService {
    private final GameRepositoryImpl repository;  // Direct dependency
}

// ✅ GOOD: Application depends on abstraction
public class GameService {
    private final GameRepository repository;      // Interface dependency
    
    public GameService(GameRepository repository) {
        this.repository = repository;  // Injected, can be any implementation
    }
}
```

#### Example: Single Responsibility

```java
// ❌ BAD: God class with too many responsibilities
public class Game {
    public void saveToDatabase() { ... }      // Persistence
    public void calculateScore() { ... }       // Business logic
    public void sendEmail() { ... }            // Communication
}

// ✅ GOOD: Each class has one responsibility
public class Game {
    // Only game state and rules
}

public class GameService {
    // Orchestrates game flow
}

public class GameRepository {
    // Handles persistence
}
```

---

## 🛠️ Tech Stack

| Category | Technology | Version | Purpose |
|----------|------------|---------|---------|
| **Language** | Java | 21 | Modern Java with pattern matching, records |
| **Framework** | Spring Boot | 3.2.0 | Application framework |
| **Reactive** | Project Reactor | - | Non-blocking reactive streams |
| **Web** | Spring WebFlux | - | Reactive web framework |
| **Database (Games)** | MongoDB | Latest | Document store for game data |
| **Database (Players)** | MySQL + R2DBC | Latest | Relational data with reactive driver |
| **API Docs** | SpringDoc OpenAPI | 2.3.0 | OpenAPI/Swagger documentation |
| **Build** | Maven | - | Project build and dependency management |
| **Testing** | JUnit 5 | - | Testing framework |
| **Mocking** | Mockito | 5.11.0 | Mocking framework for tests |
| **Containers** | Docker | - | Containerization |
| **Containers (Test)** | Testcontainers | 1.19.8 | Integration testing |

---

## 📁 Project Structure

```
blackjack/
├── src/
│   ├── main/
│   │   ├── java/com/itacademy/blackjack/
│   │   │   ├── BlackjackApplication.java
│   │   │   ├── config/
│   │   │   │   ├── OpenApiConfig.java          # Swagger configuration
│   │   │   │   └── R2dbcConfig.java            # Reactive configuration
│   │   │   ├── deck/                           # Deck Bounded Context
│   │   │   │   ├── domain/
│   │   │   │   │   └── model/
│   │   │   │   │       └── CardData.java       # Domain value object
│   │   │   │   ├── infrastructure/
│   │   │   │   │   └── CardMapper.java         # ACL (Anti-Corruption Layer)
│   │   │   │   └── model/
│   │   │   │       ├── Card.java               # Domain value object
│   │   │   │       ├── CardRank.java           # Enum (A, K, Q, J, 10-2)
│   │   │   │       ├── Deck.java               # Domain entity
│   │   │   │       ├── ScoringService.java     # Domain service
│   │   │   │       └── Suit.java               # Enum (♠, ♥, ♦, ♣)
│   │   │   ├── game/                           # Game Bounded Context
│   │   │   │   ├── application/                # Application Layer
│   │   │   │   │   ├── GameService.java        # Use cases
│   │   │   │   │   └── dto/                    # Data Transfer Objects
│   │   │   │   │       ├── GameResponse.java
│   │   │   │   │       ├── GameRequest.java
│   │   │   │   │       └── PlayerResponse.java
│   │   │   │   ├── domain/                     # Domain Layer
│   │   │   │   │   ├── model/
│   │   │   │   │   │   ├── Game.java           # Aggregate Root
│   │   │   │   │   ├── Crupier.java            # Domain entity
│   │   │   │   │   ├── Hand.java               # Value object
│   │   │   │   │   ├── GameStatus.java         # Enum
│   │   │   │   │   ├── GameResult.java         # Enum
│   │   │   │   │   └── exception/              # Domain exceptions
│   │   │   │   │       ├── ResourceNotFoundException.java
│   │   │   │   │       └── NotPlayerTurnException.java
│   │   │   │   └── repository/
│   │   │   │       └── GameRepository.java     # Port (Interface)
│   │   │   └── infrastructure/                 # Infrastructure Layer
│   │   │       ├── persistence/
│   │   │       │   └── mongo/
│   │   │       │       ├── document/           # MongoDB documents
│   │   │       │       ├── mapper/             # Domain-Model mappers
│   │   │       │       └── repository/         # Adapter implementations
│   │   │       └── web/
│   │   │           └── GameController.java     # Inbound Adapter
│   │   │   ├── player/                         # Player Bounded Context
│   │   │   │   ├── application/                # Application Layer
│   │   │   │   │   ├── PlayerService.java      # Use cases
│   │   │   │   │   └── dto/
│   │   │   │   │       ├── CreatePlayerRequest.java
│   │   │   │   │       └── PlayerProfileResponse.java
│   │   │   │   ├── domain/                     # Domain Layer
│   │   │   │   │   ├── model/
│   │   │   │   │   │   ├── Player.java         # Aggregate Root
│   │   │   │   │   │   └── PlayerStats.java    # Value object
│   │   │   │   │   └── repository/
│   │   │   │   │       └── PlayerRepository.java # Port (Interface)
│   │   │   │   └── infrastructure/             # Infrastructure Layer
│   │   │   │       ├── persistence/
│   │   │   │       │   └── r2dbc/              # R2DBC adapter
│   │   │   │       │       ├── PlayerEntity.java
│   │   │   │       │       ├── PlayerMapper.java
│   │   │   │       │       └── PlayerRepositoryImpl.java
│   │   │   │       └── web/
│   │   │   │           └── PlayerController.java # Inbound Adapter
│   │   │   └── exception/                      # Cross-cutting
│   │   │       ├── ErrorResponse.java
│   │   │       └── GlobalExceptionHandler.java
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── schema.sql                      # MySQL schema
│   │       └── static/                         # Frontend assets
│   │           ├── index.html
│   │           ├── css/styles.css
│   │           └── js/app.js
│   └── test/
│       ├── java/com/itacademy/blackjack/
│       │   ├── BlackjackApplicationTests.java
│       │   ├── GameServiceIntegrationTest.java
│       │   ├── config/
│       │   │   ├── TestcontainersInitializer.java
│       │   │   └── TestMongoConfig.java
│       │   ├── game/
│       │   │   ├── application/
│       │   │   │   └── GameServiceTest.java
│       │   │   ├── domain/model/
│       │   │   │   └── GameTest.java
│       │   │   └── infrastructure/
│       │   │       ├── web/
│       │   │       │   └── GameControllerTest.java
│       │   │       └── persistence/
│       │   │           └── mongo/
│       │   │               └── GameMongoRepositoryTest.java
│       │   └── player/
│       │       ├── application/
│       │       │   └── PlayerServiceTest.java
│       │       ├── domain/model/
│       │       │   └── PlayerTest.java
│       │       └── infrastructure/
│       │           └── persistence/
│       │               └── r2dbc/
│       │                   └── PlayerRepositoryImplTest.java
│       └── resources/
│           ├── application.properties
│           └── schema.sql
├── Dockerfile
├── docker-compose.yml
├── pom.xml
└── README.md
```

### Layer Responsibilities

| Layer | Responsibility | Examples |
|-------|---------------|----------|
| **Domain** | Business rules and logic | `Game`, `Player`, `Card`, `Hand` |
| **Application** | Use cases and orchestration | `GameService`, `PlayerService` |
| **Infrastructure** | Technical concerns | `GameRepositoryImpl`, `CardMapper` |
| **Presentation** | External interfaces | `GameController`, `PlayerController` |

---

## 🚀 Quick Start

### Prerequisites

- **Java 21** or higher
- **Maven 3.8+**
- **Docker** and **Docker Compose** (for containers)
- **MongoDB** (optional, if not using Docker)
- **MySQL** (optional, if not using Docker)

### Local Development Setup

#### 1. Clone and Build

```bash
# Clone the repository
cd blackjack

# Build the project
./mvnw clean install -DskipTests
```

#### 2. Run with Docker Compose (Recommended)

```bash
# Start all services (App, MongoDB, MySQL)
docker-compose up -d

# View logs
docker-compose logs -f blackjack
```

#### 3. Run Locally (Without Docker)

**Start Databases:**

```bash
# Start MongoDB (example with Docker)
docker run -d -p 27017:27017 --name mongodb mongo:latest

# Start MySQL (example with Docker)
docker run -d -p 3306:3306 --name mysql \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=blackjack \
  mysql:latest
```

**Configure Application:**

Create `src/main/resources/application.properties`:
```properties
# MongoDB Configuration
spring.data.mongodb.uri=mongodb://localhost:27017/blackjack

# R2DBC/MySQL Configuration
spring.r2dbc.url=r2dbc:mysql://localhost:3306/blackjack
spring.r2dbc.username=root
spring.r2dbc.password=root

# Server Configuration
server.port=8080
```

**Run Application:**
```bash
./mvnw spring-boot:run
```

#### 4. Access the Application

| Service | URL |
|---------|-----|
| **Application** | http://localhost:8080 |
| **API Docs (Swagger)** | http://localhost:8080/swagger-ui.html |
| **H2 Console** | http://localhost:8080/h2-console (if enabled) |

---

## 🐳 Docker Setup

### Docker Compose Services

```yaml
# docker-compose.yml
services:
  # Application Service
  blackjack:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_DATA_MONGODB_URI=mongodb://mongodb:27017/blackjack
      - SPRING_R2DBC_URL=r2dbc:mysql://mysql:3306/blackjack
    depends_on:
      - mongodb
      - mysql
    networks:
      - blackjack-network

  # MongoDB for Game Storage
  mongodb:
    image: mongo:latest
    ports:
      - "27017:27017"
    volumes:
      - mongodb-data:/data/db
    networks:
      - blackjack-network

  # MySQL for Player Data
  mysql:
    image: mysql:latest
    environment:
      - MYSQL_ROOT_PASSWORD=root
      - MYSQL_DATABASE=blackjack
    ports:
      - "3306:3306"
    volumes:
      - mysql-data:/var/lib/mysql
      - ./src/main/resources/schema.sql:/docker-entrypoint-initdb.d/schema.sql
    networks:
      - blackjack-network

networks:
  blackjack-network:
    driver: bridge

volumes:
  mongodb-data:
  mysql-data:
```

### Docker Commands

```bash
# Build and start all services
docker-compose up --build

# Start in detached mode
docker-compose up -d

# Stop all services
docker-compose down

# Stop and remove volumes
docker-compose down -v

# View logs
docker-compose logs -f blackjack

# Rebuild after code changes
docker-compose build blackjack
docker-compose up -d blackjack
```

### Dockerfile

```dockerfile
# Build stage
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests -Pprod

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## 📚 API Documentation

The application uses **SpringDoc OpenAPI 2.3** for automatic API documentation.

### Access Swagger UI

Navigate to: **http://localhost:8080/swagger-ui.html**

### Key Endpoints

#### Player Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/players` | Create a new player |
| `GET` | `/api/v1/players/{id}` | Get player profile |
| `GET` | `/api/v1/players/{id}/stats` | Get player statistics |
| `GET` | `/api/v1/players/ranking` | Get player leaderboard |

#### Game Operations

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/games/player/{playerId}` | Start a new game |
| `POST` | `/api/v1/games/{gameId}/hit` | Player hits (draw card) |
| `POST` | `/api/v1/games/{gameId}/stand` | Player stands (end turn) |
| `GET` | `/api/v1/games/{gameId}` | Get game state |
| `GET` | `/api/v1/games/player/{playerId}/history` | Get player game history |

### Example API Request

**Create Player:**
```bash
curl -X POST http://localhost:8080/api/v1/players \
  -H "Content-Type: application/json" \
  -d '{"name": "John Doe"}'
```

**Response:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "John Doe",
  "createdAt": "2024-01-15T10:30:00Z"
}
```

**Start Game:**
```bash
curl -X POST http://localhost:8080/api/v1/games/player/550e8400-e29b-41d4-a716-446655440000
```

---

## 🧪 Testing

### Test Structure

```
src/test/java/
├── BlackjackApplicationTests.java       # Smoke test
├── GameServiceIntegrationTest.java      # Integration tests
├── PlayerGameIntegrationTest.java       # Full workflow tests
├── config/
│   ├── TestcontainersInitializer.java   # Container lifecycle
│   └── TestMongoConfig.java             # MongoDB test config
├── game/
│   ├── application/GameServiceTest.java      # Service unit tests
│   ├── domain/model/GameTest.java            # Domain unit tests
│   └── infrastructure/
│       ├── web/GameControllerTest.java       # Controller tests
│       └── persistence/mongo/                # Repository tests
└── player/
    ├── application/PlayerServiceTest.java    # Service unit tests
    ├── domain/model/PlayerTest.java          # Domain unit tests
    └── infrastructure/                        # Repository tests
```

### Running Tests

```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=GameServiceTest

# Run tests with coverage
./mvnw test jacoco:report

# Run integration tests only
./mvnw test -Dgroups=integration
```

### Test Technologies

| Type | Technology | Description |
|------|------------|-------------|
| **Unit Tests** | JUnit 5 + Mockito | Fast, isolated tests |
| **Integration Tests** | Testcontainers | Real database testing |
| **Web Tests** | WebFlux Test | Controller endpoint testing |

### Testcontainers

The project uses **Testcontainers** for integration testing with real databases:

```java
@TestConfiguration
@Testcontainers
public class TestcontainersInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    
    static final MySQLContainer MYSQL = new MySQLContainer("mysql:latest");
    static final MongoDBContainer MONGO = new MongoDBContainer("mongo:latest");
    
    static {
        MYSQL.start();
        MONGO.start();
    }
    
    @Override
    public void initialize(ConfigurableApplicationContext context) {
        // Set environment variables for test containers
    }
}
```

---

## 📖 Contributing Guidelines

We welcome contributions! This project is designed for educational purposes, so clear and well-documented code is essential.

### Getting Started

1. **Fork the repository**
2. **Create a feature branch**: `git checkout -b feature/amazing-feature`
3. **Make your changes**
4. **Run tests**: `./mvnw test`
5. **Commit**: `git commit -m 'Add amazing feature'`
6. **Push**: `git push origin feature/amazing-feature`
7. **Open a Pull Request**

### Code Style Guidelines

#### Java Conventions

```java
// ✅ GOOD: Clear naming, single responsibility
public class GameService {
    private final GameRepository gameRepository;
    private final PlayerRepository playerRepository;
    
    public Mono<GameResponse> startNewGame(UUID playerId) {
        return playerRepository.findById(playerId)
            .flatMap(player -> createAndSaveGame(player));
    }
}

// ❌ BAD: Unclear naming, too many responsibilities
public class G {
    private final R r;
    
    public O s(UUID i) {
        return r.f(i).flatMap(p -> cs(p));
    }
}
```

#### Architecture Rules

1. **Domain Layer Must Be Pure**
   - No imports from `infrastructure` packages
   - No Spring annotations (`@Component`, `@Service`)
   - Only pure Java business logic

2. **Dependencies Point Inward**
   - Domain → Nothing (innermost layer)
   - Application → Domain
   - Infrastructure → Domain + Application
   - Presentation → Application

3. **Ports Define Boundaries**
   - Repository interfaces in domain layer
   - Inbound/outbound adapters implement ports

#### DDD Naming Conventions

| Concept | Naming | Example |
|---------|--------|---------|
| Entity | Noun (PascalCase) | `Game`, `Player` |
| Value Object | Noun (PascalCase) | `Card`, `Hand` |
| Domain Service | Noun/Adjective (PascalCase) | `ScoringService` |
| Repository Interface | Noun (PascalCase) | `GameRepository` |
| Application Service | Noun (PascalCase) | `GameService` |
| DTO | Noun + Response/Request | `GameResponse`, `CreatePlayerRequest` |

### Pull Request Checklist

- [ ] Code follows architecture rules
- [ ] Tests pass (`./mvnw test`)
- [ ] New tests added for new features
- [ ] Documentation updated
- [ ] No TODO comments left
- [ ] Commit messages are clear and descriptive



## 🎓 Learning Resources

### DDD Concepts
- [Domain-Driven Design: Tackling Complexity in the Heart of Software](https://www.amazon.com/dp-0321125217) - Eric Evans (The Blue Book)
- [Implementing Domain-Driven Design](https://www.amazon.com/dp-0321834577) - Vaughn Vernon (The Red Book)
- [DDD Reference](https://domainlanguage.com/wp-content/uploads/2016/05/DDD_Reference_2015-03.pdf) - Quick reference card

### Hexagonal Architecture
- [Alistair Cockburn's Original Article](https://alistair.cockburn.us/hexagonal-architecture/)
- [Ports & Adapters Pattern](https://medium.com/@matias.pereira/ports-adapters-architecture-368a421e1738)

### SOLID Principles
- [SOLID Principles of Object-Oriented Design](https://www.amazon.com/dp/0134440790)
- [Uncle Bob's SOLID Principles](https://web.archive.org/web/20201026072341/https://www.objectmentor.com/resources/articles/Principles_and_Patterns.pdf)

### Reactive Programming
- [Project Reactor Documentation](https://projectreactor.io/docs/core/release/reference/)
- [WebFlux Documentation](https://docs.spring.io/spring-framework/reference/web-reactive.html)

---

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

---

**Happy Gaming! 🎰**

> "Luck is my middle name, said Rincewind, without conviction." — Moving Pictures (1990)
