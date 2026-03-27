# GCP Deployment Guide

Quick guide for deploying Grimoire MMO on Google Cloud Platform using a cost-effective e2-micro VM.

## Overview

- **Compute Engine e2-micro**: ~$8-10/month (or FREE with GCP free tier)
- **Docker Compose**: All services (PostgreSQL, Keycloak, Server, Web) in containers
- **Caddy**: Automatic HTTPS with Let's Encrypt

## Prerequisites

- GCP Project with billing enabled
- `gcloud` CLI installed and authenticated
- Domain name (optional for HTTPS)

## Quick Setup

### 1. Create VM Instance

```bash
export PROJECT_ID="your-project-id"
export ZONE="us-central1-a"
export VM_NAME="grimoire-server"

gcloud compute instances create $VM_NAME \
  --zone=$ZONE \
  --machine-type=e2-micro \
  --image-family=cos-stable \
  --image-project=cos-cloud \
  --boot-disk-size=30GB

# Create firewall rules
gcloud compute firewall-rules create grimoire-http \
  --allow tcp:80,tcp:443,tcp:8888 \
  --source-ranges 0.0.0.0/0
```

### 2. Deploy Application

SSH into the VM and create docker-compose.yml:

```bash
gcloud compute ssh $VM_NAME --zone=$ZONE

mkdir -p ~/grimoire && cd ~/grimoire

# Create docker-compose.yml with all services
# (Copy from project repository)

# Create .env file with passwords
cat > .env << EOF
DB_PASSWORD=$(openssl rand -base64 32)
KEYCLOAK_ADMIN_PASSWORD=$(openssl rand -base64 32)
WEB_CLIENT_SECRET=$(openssl rand -base64 32)
DOMAIN=your-domain.com
IMAGE_REGISTRY=ghcr.io/thestacktracewhisperer/grimoire
VERSION=latest
EOF

# Start services
docker-compose up -d
```

## Management

### View Logs
```bash
gcloud compute ssh $VM_NAME --zone=$ZONE
cd ~/grimoire
docker-compose logs -f
```

### Update to New Version
```bash
# Update .env with new version
vi .env  # Change VERSION=v1.2.0
docker-compose pull
docker-compose up -d
```

### Backup Database
```bash
docker exec grimoire-postgres pg_dump -U grimoire grimoire > backup.sql
gsutil cp backup.sql gs://your-backup-bucket/
```

## Troubleshooting

- **Services not starting**: Check logs with `docker-compose logs`
- **Out of memory**: Upgrade to e2-small if needed
- **Certificate issues**: Check Caddy logs and verify DNS configuration

For detailed deployment information, see the [Development Guide](./2_DEVELOPMENT_GUIDE.md).

