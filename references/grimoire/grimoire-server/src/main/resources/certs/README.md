# SSL/TLS Certificates for Development

This directory contains self-signed SSL certificates for development use only.

## Files

- `server-keystore.p12`: PKCS12 keystore containing the server's private key and certificate

## Certificate Details

- **Alias**: grimoire-server
- **Algorithm**: RSA 2048-bit
- **Validity**: 10 years
- **Password**: grimoire (both keystore and key password)
- **CN**: localhost
- **Organization**: Grimoire Development

## Regenerating Certificates

To regenerate the certificate (e.g., if expired):

```bash
keytool -genkeypair \
  -alias grimoire-server \
  -keyalg RSA \
  -keysize 2048 \
  -storetype PKCS12 \
  -keystore server-keystore.p12 \
  -validity 3650 \
  -storepass grimoire \
  -keypass grimoire \
  -dname "CN=localhost, OU=Development, O=Grimoire, L=Unknown, ST=Unknown, C=US"
```

## Production Use

**WARNING**: These certificates are for development only. For production:
1. Obtain certificates from a trusted Certificate Authority (CA)
2. Use proper certificate management
3. Store passwords securely (environment variables, secrets management)
4. Implement certificate rotation
