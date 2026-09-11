# NEXVARY Emergency Mesh

Offline-first Android emergency communications project for resilient local coordination during network outages.

## Stage 1050

The current development milestone is **Stage 1050** (`versionCode 1050`, `2.1.0-stage1050`). The branch is intentionally built around a stable Android shell before hardware transports are allowed to affect startup.

Implemented and continuously tested:

- Arabic RTL electric command dashboard with Arabic, English, Turkish, Spanish and German resources.
- Offline-first policy: no `INTERNET` permission.
- Emergency dashboard, field identity, transport health, rescue/medical/shelter/general channels and recent activity surfaces.
- NEM3 packet model with TTL, hop limit and emergency priority.
- AES-256-GCM encryption with PBKDF2-HMAC-SHA256 scoped key derivation.
- Replay/expiry/future-time protection.
- Store-and-forward routing decisions and bounded retry/backoff policy.
- NXC2 CRC-protected radio fragmentation/reassembly for constrained radio payloads.
- JVM unit tests covering Arabic payloads, encryption authentication, routing, replay protection, retries and out-of-order radio reassembly.
- GitHub release gate validating localization parity, RTL policy, offline permission policy, Gradle build, unit tests, Android Lint, APK install, process survival and emulator screenshot.

## Release rule

A change is **not considered ready for device testing** unless the GitHub Actions release gate passes. Static checks alone are not sufficient.

The hardware Bluetooth / Wi-Fi Direct / Meshtastic layers will be enabled incrementally behind this verified shell so transport faults cannot prevent the app from opening.
