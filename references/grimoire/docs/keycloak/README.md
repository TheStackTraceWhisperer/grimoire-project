# Keycloak Configuration for Grimoire

This directory contains the Keycloak realm configuration for the Grimoire MMO project.

## Overview

Keycloak replaces the Spring Authorization Server and provides:
- OAuth2/OIDC authentication for both the game client and web interface
- User management and registration
- JWT token issuance and validation
- Session management

## Quick Setup

### Using Docker Compose (Recommended)

1. Start all services including Keycloak:
```bash
docker-compose up -d
```

2. Wait for all services to be ready (about 30-60 seconds)

The realm configuration is automatically imported from `grimoire-realm.json` on first startup using Keycloak's `--import-realm` feature. No manual import step is required.

### Manual Setup (Alternative)

If you prefer to configure Keycloak manually or need to re-import the realm:

1. Access the Keycloak admin console at http://localhost:9000/admin
2. Login with grimoire/grimoire
3. Import the `grimoire-realm.json` file:
   - Click on the realm dropdown (top left)
   - Select "Create Realm"
   - Click "Browse" and select `grimoire-realm.json`
   - Click "Create"

### Legacy Setup Script

The `setup-realm.sh` script is deprecated but still available for advanced use cases or troubleshooting. The automatic import via Docker Compose is now the recommended approach.

## Realm Configuration

The `grimoire` realm includes:

### Clients

1. **grimoire-client** (Game Client)
   - Client ID: `grimoire-client`
   - Client Secret: `client-secret`
   - Redirect URIs: `http://localhost:8889/authorized`
   - Used by the JavaFX game client for OAuth2 authentication

2. **grimoire-web-client** (Web Interface)
   - Client ID: `grimoire-web-client`
   - Client Secret: `web-secret`
   - Redirect URIs: `http://localhost:8081/*`
   - Used by the Micronaut web application for OAuth2 authentication

### Test User

A test user is pre-configured:
- Username: `grimoire`
- Password: `grimoire`
- Email: `grimoire@grimoire.local`

## Integration Points

### Game Server (grimoire-server)

The game server validates JWT tokens using Keycloak's JWKS endpoint:
```yaml
micronaut:
  security:
    token:
      jwt:
        signatures:
          jwks:
            keycloak:
              url: http://localhost:9000/realms/grimoire/protocol/openid-connect/certs
```

### Web Application (grimoire-web)

The web application uses Micronaut Security OAuth2 to integrate with Keycloak:
```yaml
micronaut:
  security:
    oauth2:
      clients:
        keycloak:
          client-id: grimoire-web-client
          client-secret: web-secret
          openid:
            issuer: http://localhost:9000/realms/grimoire
```

### Game Client (grimoire-client)

The JavaFX client uses the OAuth2 authorization code flow:
- Authorization endpoint: `http://localhost:9000/realms/grimoire/protocol/openid-connect/auth`
- Token endpoint: `http://localhost:9000/realms/grimoire/protocol/openid-connect/token`

## Accessing Keycloak

- **Admin Console**: http://localhost:9000/admin
  - Username: grimoire
  - Password: grimoire

- **Account Console**: http://localhost:9000/realms/grimoire/account
  - Users can manage their accounts here

- **Realm Info**: http://localhost:9000/realms/grimoire

## Customization

To modify the realm configuration:

1. Make changes via the Keycloak admin console
2. Export the realm:
   ```bash
   docker exec -it grimoire-keycloak /opt/keycloak/bin/kc.sh export \
     --realm grimoire --file /tmp/grimoire-realm.json
   docker cp grimoire-keycloak:/tmp/grimoire-realm.json ./grimoire-realm.json
   ```
3. Commit the updated `grimoire-realm.json`

## Production Considerations

For production deployments:

1. **Change default credentials**: Update admin password and client secrets
2. **Enable HTTPS**: Configure SSL/TLS certificates
3. **Use external database**: Configure PostgreSQL instead of embedded H2
4. **Set proper redirect URIs**: Update to match your domain
5. **Enable email verification**: Configure SMTP settings
6. **Review security settings**: Adjust token lifespans, session timeouts, etc.

## Troubleshooting

### Keycloak not starting
- Check Docker logs: `docker logs grimoire-keycloak`
- Ensure PostgreSQL is healthy: `docker ps`

### Cannot import realm
- Ensure Keycloak is fully started (check /health/ready endpoint)
- Verify admin credentials are correct
- Check that realm doesn't already exist

### Authentication failing
- Verify client secrets match in application configurations
- Check redirect URIs are correctly configured
- Review Keycloak logs for errors

## References

- [Keycloak Documentation](https://www.keycloak.org/documentation)
- [OAuth2/OIDC Protocols](https://oauth.net/2/)
- [Micronaut Security](https://micronaut-projects.github.io/micronaut-security/latest/guide/)
