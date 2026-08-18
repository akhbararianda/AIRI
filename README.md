# AIRI Download Manager

**Modern Windows download manager by AIRI Technology**  
Founder: **Akhbar Arianda**

AIRI Download Manager is a native Windows download manager focused on fast direct downloads, browser integration, universal media detection, realtime telemetry, and a clean AIRI visual identity.

> Current public-testing branch: **v2.6.1 AIRI Stable**

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

During the trial, all normal download features remain available. When the trial expires, the application can still be opened to view existing information and activate a license, while new AIRI downloads are blocked until activation.

Commercial licenses use signed license keys and a Machine ID displayed inside AIRI. The client application contains only the public verification key; the private signing key must remain offline with AIRI Technology and must never be committed to this repository.

## Browser Media Capture

The AIRI browser extension detects media through both page elements and browser network activity. The AIRI download overlay can appear on compatible video players across ordinary websites—not only YouTube.

AIRI does **not** bypass DRM or protected playback systems such as Widevine.

## Installation

1. Download the latest Windows installer from **Releases**.
2. Install AIRI Download Manager.
3. Open Chrome or Edge extensions and load/reload the AIRI browser extension if required.
4. Run **Test AIRI connection** from the extension popup.
5. Browse normally. AIRI can intercept ordinary downloads and expose a download button on compatible media players.

## Public Release Checklist

Before a paid production launch, AIRI Technology should additionally complete:

- Windows Authenticode code signing
- Chrome Web Store / Microsoft Edge Add-ons publication
- Product website and checkout
- License issuance/activation backend or controlled offline sales workflow
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
