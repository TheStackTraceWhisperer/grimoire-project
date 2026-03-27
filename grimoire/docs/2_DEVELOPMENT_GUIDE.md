# Development Guide

## Prerequisites

- Java 21 or higher
- Maven 3.8+
- Docker and Docker Compose

## Building

```bash
# Build all modules
mvn clean install

# Run tests
mvn test
```

## Running Locally

### With Docker Compose (Recommended)

```bash
docker-compose up -d
```

### Manual Setup

1. **Start PostgreSQL**:
```bash
docker run --name grimoire-postgres \
  -e POSTGRES_DB=grimoire \
  -e POSTGRES_USER=grimoire \
  -e POSTGRES_PASSWORD=grimoire \
  -p 5432:5432 \
  -d postgres:16-alpine
```

2. **Run the server**:
```bash
cd grimoire-server
mvn clean package
java -jar target/grimoire-server-1.0-SNAPSHOT.jar
```

The server starts on:
- HTTP API: `http://localhost:8080`
- Game Server: `tcp://localhost:8888`

## Project Structure

```
grimoire/
├── grimoire-shared/          # Shared contracts and DTOs
│   ├── protocol/             # Network packet definitions
│   └── dto/                  # Data transfer objects
├── grimoire-server/          # Game server
│   ├── ecs/                  # Entity Component System
│   ├── system/               # Game systems
│   └── network/              # Netty networking
└── grimoire-client/          # JavaFX client
```

## Game Systems

Systems execute in this order:

1. **PlayerInputSystem**: Processes player movement
2. **PortalCooldownSystem**: Manages portal cooldowns
3. **NpcAiSystem**: Controls NPC behavior
4. **MovementSystem**: Updates entity positions
5. **ZoneChangeSystem**: Handles zone transitions
6. **NetworkSyncSystem**: Sends updates to clients
7. **NetworkVisibilitySystem**: Manages visibility

## Network Protocol

Uses Java serialization over TCP with SSL/TLS.

**Client-to-Server**:
- `C2S_LOGIN_REQUEST`: Authentication
- `C2S_MOVEMENT_INTENT`: Movement target
- `C2S_CHAT_MESSAGE`: Chat messages

**Server-to-Client**:
- `S2C_LOGIN_SUCCESS`/`S2C_LOGIN_FAILURE`: Auth response
- `S2C_GAME_STATE_UPDATE`: Delta updates
- `S2C_ENTITY_SPAWN`: New entities
- `S2C_ENTITY_DESPAWN`: Removed entities
- `S2C_ZONE_CHANGE`: Zone transition
- `S2C_CHAT_BROADCAST`: Chat messages

## Configuration

Server configuration in `grimoire-server/src/main/resources/application.yml`:

```yaml
micronaut:
  application:
    name: grimoire-server
  server:
    port: 8080
    
datasources:
  default:
    url: jdbc:postgresql://localhost:5432/grimoire
    username: grimoire
    password: grimoire
```
