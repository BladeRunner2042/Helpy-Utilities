# Helpy Utilities

A fast, privacy-first, all-in-one native Android utility suite built with **Kotlin** and **Jetpack Compose**. Designed with a clean Material 3 interface, adaptive scaling for phones and tablets, and zero unnecessary bloat.

---

## Feature Highlights

### Smart Productivity & Calculation
* **Smart Clipboard Sense:** Automatically detects copied math expressions, foreign phrases, currency amounts, or tracking links on your clipboard for instant one-tap actions.
* **Scientific & Everyday Calculator:** Clean, responsive calculation engine with full expression history and tabular figures (`tnum`).
* **Unit & Live Currency Converter:** Convert everyday units offline or check live exchange rates across 160+ world currencies.
* **Offline World Time Scrubber:** Scrub a 24-hour time slider in 15-minute increments to compare times across global cities completely offline.

### Language & Camera OCR
* **Online Universal Translator:** Fast cloud translation across 100+ languages with zero bulky offline language pack downloads.
* **Google Lens-Style Camera & Image OCR:** Snap a photo or pick an image to highlight words with your finger, copy text, or send phrases directly into the main translator.

### Hardware, Sensors & Network Diagnostics
* **Multi-Stream Network Speed Test & Wi-Fi Inspector:** Measure download speed, upload speed, ping, and jitter with automatic mobile data protection, paired with a local network inspector (IPv4, Gateway, DNS, Wi-Fi frequency band, and link speed).
* **Live Hardware & Battery Telemetry:** Inspect real-time battery health, voltage (mV), temperature (°C), live current draw (mA) sparklines, dual RAM/storage progress rings, and live per-core CPU clock frequencies.
* **Precision Sensors & Quick Tools:** Includes a bubble spirit level, screen ruler, acoustic sound meter & frequency generator, Morse code/SOS flashlight transmitter, URL tracking stripper, and SHA-256/MD5 text hasher.
* **Native Android Integrations:** Supports Android Quick Settings tiles, long-press launcher shortcuts, and the system text-selection menu (`Translate / Solve with Helpy`).

---

## Material 3 Theme Suite

Helpy Utilities adapts seamlessly across **6 built-in themes** with consistent contrast and clean typography:

1. **Follow System** – Automatically matches your device's light or dark theme and dynamic wallpaper palette.
2. **Light** – Crisp, high-legibility daylight theme.
3. **Dark** – Balanced low-glare dark mode.
4. **OLED** – True `#000000` pitch-black surfaces optimized for AMOLED battery savings.
5. **Monochrome** – Clean, distraction-free grayscale light theme.
6. **Mono OLED** – High-contrast pure black and white theme.

---

## Download & Installation

1. Visit the [**Releases**](https://github.com) page on GitHub.
2. Under the latest release (e.g., `v1.0.0`), expand **Assets** and download **`app-release.apk`**.
3. Open the downloaded `.apk` file on your Android device (enable *Install from unknown sources* for your browser or file manager if prompted).
4. **In-App Updates:** Open **Settings -> Check for Updates** inside Helpy Utilities anytime to check for new GitHub releases automatically.

---

## Tech Stack

* **Language:** Kotlin
* **UI Framework:** Jetpack Compose (Material 3)
* **Architecture & Persistence:** AndroidX Lifecycle, Preferences DataStore, Native `BatteryManager` / `ConnectivityManager` / `CameraManager` APIs
* **Minimum SDK:** Android 12 (API 31+)

---

## Building from Source

1. Clone the repository:
   ```bash
   git clone https://github.com
   ```
2. Open the project in **Android Studio**.
3. Sync Gradle and run `./gradlew assembleDebug` (or build a signed release APK via **Build -> Generate Signed App Bundle / APK**).

