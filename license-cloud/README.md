# AIRI License Cloud

Self-hosted licensing, usage analytics, device control, abuse detection, and integrity intelligence for **AIRI Download Manager**.

**Developed by AIRI Technology**  
**Founder: Akhbar Arianda**

## What the cloud records

AIRI License Cloud is intentionally privacy-minimal. It records only data needed for licensing and product reliability:

- random Installation ID
- SHA-256 hash of the AIRI Machine ID
- application version and release channel
- Windows version and CPU architecture
- local license state (trial/licensed/expired)
- SHA-256 hash of the installed AIRI executable
- license hash, edition, activation/device state
- check-in timestamps
- HMAC-hashed network address for abuse correlation
- security events such as invalid license, clock rollback, device-limit abuse, or binary-integrity mismatch

It does **not** collect browsing history, visited pages, download URLs, filenames, media titles, cookies, or downloaded-file history.

## Requirements

- PHP 8.1 or newer
- PHP extensions: PDO MySQL, OpenSSL, mbstring, JSON, session
- MySQL 8 / MariaDB 10.5+
- HTTPS certificate
- Apache with `mod_rewrite` (or equivalent Nginx routing)

## cPanel deployment

1. Create a database and database user, for example `airi_license`.
2. Import `schema.sql` in phpMyAdmin.
3. Copy `config.example.php` to `config.php`.
4. Edit `config.php` with your database credentials, a long random `app_secret`, admin username, password hash, and HTTPS base URL.
5. Set the domain/subdomain document root to the `public/` directory.
6. Keep `app/`, `keys/`, `schema.sql`, and `config.php` outside the public document root when your hosting layout allows it.
7. Open `/health`. A healthy server returns JSON with `"ok":true`.
8. Open `/?admin=login` and sign in.

Generate an admin password hash locally/server-side:

```bash
php -r "echo password_hash('CHANGE-THIS-PASSWORD', PASSWORD_DEFAULT), PHP_EOL;"
```

Generate a strong app secret:

```bash
php -r "echo bin2hex(random_bytes(32)), PHP_EOL;"
```

Never reuse the AIRI RSA private license-signing key as the web app secret. The RSA private key remains offline with AIRI Technology and is **not needed by License Cloud**.

## API

The Windows client uses:

- `POST /api/v1/install/register`
- `POST /api/v1/license/activate`
- `POST /api/v1/license/heartbeat`
- `POST /api/v1/license/deactivate`
- `POST /api/v1/security/event`

License keys are verified on the server with `keys/airi-license-public.pem`. The raw license key is never stored; only its SHA-256 hash is retained.

## Dashboard

Main dashboard:

`/?admin=dashboard`

Shows total installations, active users, monthly active users, licensed PCs, active licenses, suspicious devices/licenses, devices, security events, and recent installations.

Official build hashes:

`/releases.php`

Register the SHA-256 of each official `AIRI Download Manager.exe`. A binary mismatch can be configured as alert-only or enforced blocking. Mark a compromised/vulnerable build as `revoked` to deny it.

## Cloud configuration in AIRI Download Manager

The AIRI v2.7 client looks for its cloud URL in this order:

1. environment variable `AIRI_LICENSE_CLOUD_URL`
2. registry `HKCU\Software\AIRI Technology\AIRI Download Manager\Cloud\BaseUrl`
3. `airi-cloud.url` beside `AIRI Download Manager.exe`

Enforcement is enabled by one of:

- environment variable `AIRI_LICENSE_CLOUD_ENFORCE=1`
- registry value `Enforcement=1`
- an `airi-cloud.enforce` marker file beside the EXE

The GitHub v2.7 build workflow can automatically bundle these files when repository variables `AIRI_LICENSE_CLOUD_URL` and `AIRI_LICENSE_CLOUD_ENFORCE` are configured.

## Offline behavior

When enforcement is enabled, a legitimate client receives a **7-day offline grace period** after a successful cloud validation. Temporary loss of internet or a server outage does not immediately disable the application. A known server-side suspend/revoke or verified integrity block is applied immediately after a successful check-in.

## Anti-piracy limitations

No client-side licensing system can guarantee detection of every cracked binary. A determined attacker can patch networking or validation code. AIRI therefore combines several signals:

- RSA-signed offline license keys
- cloud activation and device limits
- periodic heartbeat
- installation/device correlation
- activation-burst detection
- trial clock rollback reporting
- official executable SHA-256 comparison
- license/device revoke controls
- risk scoring and security-event audit

For a public paid launch, also use Windows Authenticode code signing and HTTPS-only cloud endpoints.
