# Grimoire

A minimal, 2D, real-time MMO-lite game with OAuth2 authentication and persistent accounts.

## Features

- **OAuth2 Authentication**: Secure authentication using Keycloak
- **Real-time multiplayer**: 20 TPS server with zone-based gameplay
- **Multi-zone world**: Interconnected zones with portal-based transitions
- **NPC AI**: Friendly and hostile NPC behaviors
- **Persistent accounts**: Player progress saved to PostgreSQL
- **Web Interface**: Account management portal

## Technology Stack

- Java 21 (Client/Server) / Java 17 (Web)
- Keycloak 23 (OAuth2/OIDC)
- Micronaut 4.x (server and web frameworks)
- Netty (networking)
- JavaFX 21 (client UI)
- PostgreSQL (persistence)

## Quick Start

```bash
# Start all services
docker-compose up -d

# Run game client
cd grimoire-client
mvn javafx:run
```

## Project Structure

- `grimoire-shared`: Network protocol and shared DTOs
- `grimoire-server`: Game server with ECS and systems
- `grimoire-client`: JavaFX client with OAuth2 support
- `grimoire-web`: Micronaut web interface
- `grimoire-launcher`: Desktop launcher application

## Documentation

- [Development Guide](2_DEVELOPMENT_GUIDE.md)
- [GCP Deployment](3_GCP_DEPLOYMENT.md)
- [Keycloak Setup](keycloak/README.md)
