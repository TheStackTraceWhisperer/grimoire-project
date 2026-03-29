#!/bin/bash
# Keycloak realm setup script for Grimoire
# This script imports the realm configuration into Keycloak

set -e

KEYCLOAK_URL="${KEYCLOAK_URL:-http://localhost:9000}"
ADMIN_USER="${KEYCLOAK_ADMIN:-admin}"
ADMIN_PASSWORD="${KEYCLOAK_ADMIN_PASSWORD:-admin}"
REALM_FILE="${1:-./grimoire-realm.json}"

echo "Waiting for Keycloak to start..."
until curl -sf "${KEYCLOAK_URL}/health/ready" > /dev/null; do
    echo "Keycloak is not ready yet. Waiting..."
    sleep 5
done

echo "Keycloak is ready!"

# Get admin access token
echo "Getting admin access token..."
ACCESS_TOKEN=$(curl -s -X POST "${KEYCLOAK_URL}/realms/master/protocol/openid-connect/token" \
    -H "Content-Type: application/x-www-form-urlencoded" \
    -d "username=${ADMIN_USER}" \
    -d "password=${ADMIN_PASSWORD}" \
    -d "grant_type=password" \
    -d "client_id=admin-cli" | jq -r '.access_token')

if [ -z "$ACCESS_TOKEN" ] || [ "$ACCESS_TOKEN" = "null" ]; then
    echo "Failed to get access token. Please check Keycloak credentials."
    exit 1
fi

echo "Access token obtained successfully!"

# Check if realm already exists
REALM_EXISTS=$(curl -s -o /dev/null -w "%{http_code}" \
    -H "Authorization: Bearer ${ACCESS_TOKEN}" \
    "${KEYCLOAK_URL}/admin/realms/grimoire")

if [ "$REALM_EXISTS" = "200" ]; then
    echo "Realm 'grimoire' already exists. Skipping import."
    echo "To re-import, delete the realm first via Keycloak admin console."
    exit 0
fi

# Import realm
echo "Importing realm configuration from ${REALM_FILE}..."
HTTP_STATUS=$(curl -s -o /dev/null -w "%{http_code}" -X POST \
    -H "Authorization: Bearer ${ACCESS_TOKEN}" \
    -H "Content-Type: application/json" \
    -d @"${REALM_FILE}" \
    "${KEYCLOAK_URL}/admin/realms")

if [ "$HTTP_STATUS" = "201" ]; then
    echo "Realm 'grimoire' imported successfully!"
    echo ""
    echo "Keycloak Configuration:"
    echo "  Realm: grimoire"
    echo "  URL: ${KEYCLOAK_URL}/realms/grimoire"
    echo "  Admin Console: ${KEYCLOAK_URL}/admin"
    echo ""
    echo "Clients configured:"
    echo "  1. grimoire-client (Game Client)"
    echo "     - Client ID: grimoire-client"
    echo "     - Client Secret: client-secret"
    echo "  2. grimoire-web-client (Web Interface)"
    echo "     - Client ID: grimoire-web-client"
    echo "     - Client Secret: web-secret"
    echo ""
    echo "Test User:"
    echo "  Username: grimoire"
    echo "  Password: grimoire"
else
    echo "Failed to import realm. HTTP Status: ${HTTP_STATUS}"
    exit 1
fi
