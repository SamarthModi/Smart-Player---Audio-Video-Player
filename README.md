# 🎬 Smart Player Pro (Smart Video & Music Player)
### The Ultimate All-in-One Local Video & Audio Player for Android (MX Player & VLC Caliber)

**Smart Player Pro** is an advanced, production-grade media player created to surpass MX Player, VLC, and standalone music players. Built with Native Kotlin, Jetpack Compose, Material Design 3, and AndroidX Media3 (ExoPlayer + MediaSessionService).

---

## 🌟 Advanced Features (MX Player & VLC Style)

### 🎥 1. Pro Video Player (MX Player Pro Grade)
- **HW / SW Decoder Toggle**: Dedicated top-bar badge to seamlessly switch between **Hardware Accelerated Decoding (HW)** (low battery, maximum GPU FPS) and **Software Decoding (SW)** (universal compatibility).
- **Audio Track Switching & Audio Delay Sync**:
  - Switch between audio languages (Hindi, Gujarati, English, etc.) and formats (Stereo, 5.1 Surround, Dolby).
  - **Audio Sync Calibration**: Fine-tune audio delay with `-50ms` / `+50ms` step buttons and reset to eliminate lipsync lag!
- **Subtitles & External Subtitle File Loading**:
  - Pick and load any external `.srt`, `.vtt`, or `.sub` file directly from device storage.
  - **Subtitle Sync Calibration**: Fine-tune subtitle delay with `-100ms` / `+100ms` micro-step buttons.
- **Screenshot / Frame Capture**: Capture full-resolution video frames at the current timestamp with one tap, auto-saved to `Pictures/SmartPlayer`.
- **A-B Repeat Mode**: Set Point A and Point B to loop any scene, choreography, or tutorial continuously.
- **Night Mode Filter**: Soft warm eye-comfort amber overlay for night viewing.
- **Screen Lock (Child / Accidental Touch Lock)**: Locks all controls except a floating unlock button.
- **Aspect Ratio Resizing**: Fit, Stretch/Fill, Zoom/Crop, 16:9 Widescreen, 4:3 Standard.
- **Picture-in-Picture (PiP)**: Keep watching in a floating mini window while using WhatsApp, Browser, etc.
- **Direct Sound & Audio Boost Popup**: Adjust 5-Band EQ, Bass Boost, and 200% volume boost directly inside the video player.
- **Configurable Double-Tap Seek**: Set seek interval to 5s, 10s, 15s, or 30s.
- **Configurable Auto-Hide Timeout**: Set controls disappearance timer to 2s, 4s, 6s, or 8s.

---

### 🎵 2. Advanced Music Player
- **MediaSessionService Background Playback**: Persistent background audio with notification controls and lockscreen art.
- **Headphone Unplug Pause**: Auto-pause when wired or bluetooth headset disconnects.
- **Dynamic Queue Reordering**: Reorder songs with Move Up / Move Down buttons, remove tracks, or Clear Queue.
- **Smart Sleep Timer**: Set auto-stop countdown (10m, 15m, 30m, 45m, 60m, 90m) or enable **"Stop at End of Current Song"**!
- **Playback Speed Controller**: 0.5x, 0.75x, 1.0x, 1.25x, 1.5x, 1.75x, 2.0x for audiobooks and podcasts.
- **Lyrics Viewer**: Read song lyrics with a dedicated viewer.
- **Audio Metadata / Specs Dialog**: View Title, Artist, Album, Duration, File Size, Path.
- **One-Tap Share**: Share any track directly from the library.
- **Floating MiniPlayer**: Access playback controls from any screen.

---

### 🌐 3. Multi-Language System
- Change application language anytime in Settings without reinstalling:
  - **English**
  - **ગુજરાતી (Gujarati)**
  - **हिन्दी (Hindi)**
  - **System Default**

---

### 🎚️ 4. 5-Band Equalizer & 200% Audio Boost
- Graphic Equalizer (60Hz, 230Hz, 910Hz, 3.6kHz, 14kHz).
- Presets: Rock, Pop, Jazz, Bass, Classical, Flat, Metal, Hip-Hop, Custom.
- Bass Boost (0%–100%) & 3D Surround Virtualizer.
- VLC-style Software Loudness Enhancer (up to +200% volume boost).

---

## 📦 Build & Installation

The debug APK has been assembled:
```bash
./gradlew assembleDebug
```
APK Location:
```
app/build/outputs/apk/debug/app-debug.apk
```
Install on device:
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```
