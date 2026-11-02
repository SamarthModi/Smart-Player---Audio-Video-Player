# 🎬 Smart Player Pro (Smart Video & Music Player)
### The Ultimate All-in-One Local Video & Audio Player for Android (MX Player & VLC Caliber)

**Smart Player Pro** is an advanced, production-grade media player created to surpass MX Player, VLC, and standalone music players. Built with Native Kotlin, Jetpack Compose, Material Design 3, and AndroidX Media3 (ExoPlayer + MediaSessionService).

---

## 🌟 Top-Tier Features (Play Store & Market Dominating)

### 🔒 1. Private Secret Vault (PIN Protected Safe)
- **4-Digit PIN Security**: Lock and hide personal/sensitive videos and music behind a secure PIN.
- **Hidden from Public Library**: Media moved to the Private Vault disappears from normal video lists, folders, and music tabs.
- **Built-in Vault Player**: Play private videos and audio directly inside the vault.
- **1-Click Unhide**: Restore files back to the public library at any time.

### 🎥 2. Pro Video Player (MX Player Pro Grade)
- **Press-and-Hold for 2x Fast Forward**: Touch and hold anywhere on the screen for instant 2x fast-forward playback with a neon HUD pill indicator (`[ 2X SPEED >> ]`). Releasing immediately restores normal speed!
- **Play as Audio / Background Video Playback**: Tap the Headphones button in the top bar or video menu to keep listening to video audio in the background service with notification & lock screen controls.
- **HW / SW Decoder Toggle**: Seamlessly switch between **Hardware Accelerated Decoding (HW)** (low battery, maximum GPU FPS) and **Software Decoding (SW)** (universal compatibility).
- **Audio Track Switching & Audio Delay Sync**:
  - Switch between audio languages (Hindi, Gujarati, English, etc.) and formats (Stereo, 5.1 Surround, Dolby).
  - **Audio Sync Calibration**: Fine-tune audio delay with `-50ms` / `+50ms` step buttons to eliminate lipsync lag.
- **Full Subtitle Customizer & External Subtitle Loading**:
  - Load external `.srt`, `.vtt`, or `.sub` files.
  - **Subtitle Customizer**: Choose text size (Small, Normal, Large, Extra Large), font color (White, Yellow, Cyan, Green), and toggle dark semi-transparent background box.
  - **Subtitle Sync Calibration**: Fine-tune subtitle delay with `-100ms` / `+100ms` micro-step buttons.
- **Continue Watching Shelf**: Quick-resume carousel at the top of the video library with progress bar and timestamp.
- **Screenshot / Frame Capture**: Capture full-resolution video frames at current timestamp auto-saved to `Pictures/SmartPlayer`.
- **A-B Repeat Loop**: Set Point A and Point B to loop any scene, choreography, or tutorial continuously.
- **Night Mode Filter**: Soft warm eye-comfort amber overlay for comfortable night viewing.
- **Screen Lock (Touch Lock)**: Prevents accidental touches while watching.
- **Aspect Ratio Modes**: Fit, Stretch/Fill, Zoom/Crop, 16:9 Widescreen, 4:3 Standard.
- **Picture-in-Picture (PiP)**: Floating mini player while using other apps.
- **Direct Sound & Audio Boost Popup**: Adjust 5-Band EQ, Bass Boost, and 200% volume boost directly inside video player.
- **Configurable Gestures**: Swipe left for brightness, swipe right for volume, horizontal swipe for seek, double-tap to seek (5s, 10s, 15s, 30s).

---

### 🎵 3. Advanced Music Player
- **Continue Listening / Recently Played Shelf**: Jump back into recent songs with 1 tap.
- **MediaSessionService Background Playback**: Persistent background audio with notification controls and lockscreen art.
- **Set as Phone Ringtone**: Set any song as your phone ringtone directly from the 3-dot menu.
- **Headphone Unplug Pause**: Auto-pause when wired or bluetooth headset disconnects.
- **Dynamic Queue Reordering**: Reorder songs, remove tracks, or clear queue.
- **Smart Sleep Timer**: Countdown auto-stop or **"Stop at End of Current Song"**.
- **Playback Speed Controller**: 0.5x, 0.75x, 1.0x, 1.25x, 1.5x, 2.0x for audiobooks and podcasts.
- **Lyrics Viewer**: Read song lyrics with a dedicated viewer.
- **Audio Metadata Inspector**: View Title, Artist, Album, Duration, File Size, Path.
- **Safe File Operations**: Rename, Share, and Delete tracks.
- **Floating MiniPlayer**: Access playback controls from any screen.

---

### 🎚️ 4. 5-Band Equalizer & 200% Audio Boost
- Graphic Equalizer (60Hz, 230Hz, 910Hz, 3.6kHz, 14kHz).
- Presets: Rock, Pop, Jazz, Bass, Classical, Flat, Metal, Hip-Hop, Custom.
- Bass Boost (0%–100%) & 3D Surround Virtualizer.
- VLC-style Software Loudness Enhancer (up to +200% volume boost).

---

### 🌐 5. Multi-Language System
- Change application language anytime in Settings without reinstalling:
  - **English**
  - **ગુજરાતી (Gujarati)**
  - **हिन्दी (Hindi)**
  - **System Default**

---

## 📦 Build & Installation

The debug APK is ready:
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
