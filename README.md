# AIRI Download Manager

**Modern Windows download manager by AIRI Technology**  
Founder: **Akhbar Arianda**

AIRI Download Manager is a native Windows download manager focused on fast direct downloads, browser integration, universal media detection, realtime telemetry, and a clean AIRI visual identity.

> Current commercial field-test build: **v2.6.2 Commercial Stable**
> Official offline license issuer: **AIRI License Manager — Admin Edition v1.1**

## AIRI Identity

- Warm AIRI Cream surfaces
- AIRI Blue primary controls
- Modern light dashboard
- Graphical progress bars
- Realtime speed, downloaded/total, ETA, Started and Elapsed

## Main Features

- Native Windows C++ desktop application
- Turbo segmented HTTP/HTTPS downloads
- Pause / Resume / Cancel for direct downloads
- Universal browser download handoff
- Universal video/audio detector on ordinary HTTP/HTTPS pages
- Direct MP4/WebM capture
- HLS/DASH page-extractor fallback
- yt-dlp + FFmpeg + Deno bundled by the installer
- Realtime media progress, speed, ETA and merge status
- Media process-tree Cancel
- Open final downloaded media file from the desktop app
- Search and status filters
- Pause All / Resume All for direct downloads
- Chrome/Edge Native Messaging bridge

## Commercial Trial & Licensing

AIRI Download Manager includes a **14-day full-feature trial**.

During the trial, normal download features remain available. When the trial expires, the application can still be opened to review information and activate a license, while new AIRI downloads are blocked until activation.

Commercial licenses use signed `AIRI1...` keys and a Machine ID displayed inside AIRI. The customer application contains only the production public verification key. The corresponding private signing key stays with AIRI Technology and is never bundled with the customer installer.

The production signing pair is CI-tested with a deliberately expired signed token, proving that the Admin Edition and customer build share the correct RSA key pair without publishing a usable license.

### AIRI License Manager — Admin Edition v1.1

The admin utility is for AIRI Technology only. It can generate signed licenses, keep a local SQLite customer/license database, search license history, resend keys, load a customer for renewal/reissue, and export records to CSV.

The Admin Edition validates the AIRI production public-key fingerprint before it can issue a license. The private signing key is loaded from an external file and is never embedded in the admin executable.

## Browser Media Capture

The AIRI browser extension detects media through both page elements and browser network activity. The AIRI download overlay can appear on compatible video players across ordinary websites—not only YouTube.

AIRI does **not** bypass DRM or protected playback systems such as Widevine.

## Installation

1. Download the current customer installer from **Releases**.
2. Install AIRI Download Manager.
3. Open Chrome or Edge extensions and load/reload the AIRI browser extension if required.
4. Run **Test AIRI connection** from the extension popup.
5. Browse normally. AIRI can intercept ordinary downloads and expose a download button on compatible media players.

## Public Release Checklist

Before a paid production launch, AIRI Technology should additionally complete:

- Windows Authenticode code signing
- Chrome Web Store / Microsoft Edge Add-ons publication
- Product website and checkout
- Online activation/payment backend for larger-scale sales
- EULA, Privacy Policy, Terms of Sale and support policy
- Wider Windows compatibility and field testing

## Development

Repository: `akhbararianda/AIRI`

Core desktop stack:

- C++20
- Win32 / Common Controls
- WinHTTP
- yt-dlp
- FFmpeg
- Deno
- Inno Setup
- GitHub Actions Windows builds

## Publisher

**Developed by AIRI Technology**  
**Founder: Akhbar Arianda**

© 2026 AIRI Technology.
