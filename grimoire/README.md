# Grimoire MMO

A 2D MMO-lite game server, client, and web portal built with Micronaut, JavaFX, Netty, and Keycloak.

## Documentation

All project documentation is located in the `/docs` directory.

- [**Project Overview**](./docs/1_OVERVIEW.md)
- [**Development Guide**](./docs/2_DEVELOPMENT_GUIDE.md)
- [**GCP Deployment Guide**](./docs/3_GCP_DEPLOYMENT.md)
- [**Keycloak Setup**](./docs/keycloak/README.md)

## Quick Start

```bash
# Start all services (PostgreSQL, Keycloak, Server, Web)
# Keycloak will automatically import the realm configuration on first startup
docker-compose up -d

# Wait for all services to be ready (30-60s)

# Run the game client
cd grimoire-client
mvn javafx:run
```
