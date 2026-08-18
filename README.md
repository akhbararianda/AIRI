# AIRI Download Manager

**Modern Windows download manager by AIRI Technology**  
Founder: **Akhbar Arianda**

AIRI Download Manager is a native Windows download manager focused on accelerated direct downloads, browser integration, universal media detection, licensing, realtime telemetry, and the clean AIRI Cream + AIRI Blue visual identity.

> Current commercial field-test target: **v2.7 — License Cloud & Intelligence**  
> Offline license issuer: **AIRI License Manager — Admin Edition v1.1**

## AIRI Identity

- AIRI Cream surfaces
- AIRI Blue primary controls
- Modern light dashboard
- Graphical progress bars
- Realtime speed, downloaded/total, ETA, Started and Elapsed
- Developed by AIRI Technology · Founder Akhbar Arianda

## Download Features

- Native Windows C++ application
- Turbo segmented HTTP/HTTPS downloads
- Pause / Resume / Cancel for direct downloads
- Universal browser download handoff
- Universal video/audio detector across ordinary websites
- Direct MP4/WebM capture
- HLS/DASH extractor fallback
- yt-dlp + FFmpeg + Deno bundled by the installer
- Realtime media progress, speed, ETA and merge status
- Media process-tree Cancel
- Open final downloaded media file
- Search and status filters
- Pause All / Resume All
- Chrome/Edge Native Messaging bridge

## Commercial Trial & Offline Licensing

AIRI includes a **14-day full-feature trial**. After expiry the application can still be opened for license activation, while new downloads are blocked until activation.

Commercial licenses use signed `AIRI1...` keys and the AIRI Machine ID. The customer application contains only the production RSA public key. The private signing key remains offline with AIRI Technology and is never bundled with customer software.

### AIRI License Manager — Admin Edition v1.1

The private admin utility can:

- issue Pro / Business / Enterprise licenses
- create Lifetime, yearly, monthly, custom-duration or explicit-expiry licenses
- bind a license to a Machine ID
- keep a local SQLite customer/license database
- search and reload customers
- resend existing license keys
- export license/customer records to CSV

## AIRI v2.7 License Cloud & Intelligence

`license-cloud/` contains the self-hosted PHP + MySQL licensing backend and admin dashboard.

Cloud capabilities:

- total installation count
- active users in 24 hours
- monthly active users
- licensed/trial device counts
- license activation and heartbeat
- device limits per license
- suspend/revoke license
- revoke individual devices
- 7-day offline grace
- activation-burst detection
- cloned Installation ID detection
- clock rollback security event
- risk scoring
- security event log
- binary SHA-256 integrity fingerprint
- official build hash registry and optional integrity enforcement
- admin audit log

License Cloud is privacy-minimal. It does **not** collect browsing history, download URLs, filenames, cookies, media titles, or a user's downloaded-file history. Device and license identifiers stored by the server are hashed/pseudonymous where possible.

See [`license-cloud/README.md`](license-cloud/README.md) for deployment instructions.

## Anti-piracy model

AIRI combines multiple controls rather than relying on one local check:

1. RSA-signed offline license keys.
2. Machine-bound activation.
3. License Cloud check-in.
4. Device-count limits.
5. Suspend/revoke controls.
6. Trial clock rollback reporting.
7. Executable SHA-256 integrity comparison against official builds.
8. Risk scoring for suspicious activation/device patterns.
9. Seven-day offline grace so legitimate customers are not immediately blocked by internet/server outages.

No system can guarantee detection of every heavily patched/cracked executable, so Windows Authenticode signing remains recommended before a large paid launch.

## Browser Media Capture

The browser extension detects media using both page elements and browser network activity. AIRI does **not** bypass DRM/protected playback systems such as Widevine.

## Installation

1. Download the current Windows installer from **Releases**.
2. Install AIRI Download Manager.
3. Load/reload the AIRI extension in Chrome or Edge when required.
4. Run **Test AIRI connection** from the extension popup.
5. Activate a license after the trial when required.

## License Cloud deployment

The cloud service requires PHP 8.1+, MySQL/MariaDB, and HTTPS. Import `license-cloud/schema.sql`, create `license-cloud/config.php` from the example, and set the web document root to `license-cloud/public/`.

The v2.7 GitHub build can bundle the production endpoint automatically using repository variables:

- `AIRI_LICENSE_CLOUD_URL`
- `AIRI_LICENSE_CLOUD_ENFORCE=1`

When those variables are absent, the v2.7 cloud client remains dormant/fail-safe and local licensing continues to work.

## Before broad paid distribution

- Windows Authenticode code signing
- HTTPS production License Cloud deployment
- register the official EXE SHA-256 in License Cloud
- Chrome Web Store / Microsoft Edge Add-ons publication
- product website and checkout
- EULA, Privacy Policy, Terms of Sale and support policy
- wider Windows compatibility/field testing

## Development Stack

- C++20
- Win32 / Common Controls
- WinHTTP
- BCrypt
- yt-dlp
- FFmpeg
- Deno
- Inno Setup
- PHP 8.1+
- MySQL / MariaDB
- GitHub Actions

## Publisher

**Developed by AIRI Technology**  
**Founder: Akhbar Arianda**

© 2026 AIRI Technology.
