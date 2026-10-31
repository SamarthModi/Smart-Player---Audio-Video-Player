# 🎬 Smart Player Pro (Smart Video & Music Player)
### The Ultimate All-in-One Local Video & Audio Player for Android

**Smart Player Pro** is a high-performance, professional media player designed to replace MX Player, VLC, and standalone Music Players. Built with native Kotlin, modern Jetpack Compose, Material 3, and AndroidX Media3 (ExoPlayer + MediaSessionService).

---

## 🌟 Key Features

### 🎥 1. Pro Video Player (MX Player & VLC Caliber)
- **Intuitive Touch Gesture Engine**:
  - **Left Vertical Swipe**: Smooth screen brightness control (0%–100%) with animated Sun HUD overlay.
  - **Right Vertical Swipe**: System media volume control with volume HUD overlay.
  - **Horizontal Swipe**: Precise forward/backward seeking with duration jump and timestamp preview (`+00:15`, `02:14 / 15:30`).
  - **Double-Tap Controls**: Double-tap left to rewind 10s, double-tap right to fast forward 10s, double-tap center to Play/Pause.
  - **Single Tap**: Shows / hides video HUD controls with auto-hide timer.
- **Screen Lock Mode**: One-tap lock to prevent accidental touches during video watching.
- **Aspect Ratio Switching**: Easily toggle between **Fit to Screen**, **Stretch / Fill**, **Zoom / Crop**, **16:9 Widescreen**, and **4:3 Standard**.
- **Picture-in-Picture (PiP)**: Native floating PiP window allowing seamless multitasking while watching movies.
- **Multi-Audio Track Selector**: Switch between dual/multiple audio language tracks (e.g., English, Hindi, Gujarati) embedded in MKV/MP4 files.
- **Subtitle Track Selector**: Switch or toggle embedded text subtitles.
- **Playback Speed**: Precision speed controls (0.25x, 0.5x, 0.75x, 1.0x, 1.25x, 1.5x, 1.75x, 2.0x, 2.5x, 3.0x).
- **Auto-Resume**: Remembers last played video position so you never lose your spot.
- **Orientation Control**: Toggle between Landscape, Portrait, and Sensor orientation.
- **Video Info Dialog**: Detailed metadata view (Resolution, Bitrate, Framerate, Duration, File Size, File Path).

---

### 🎵 2. Smart Music & Audio Player
- **AndroidX MediaSessionService**: Complete background playback service with rich system media notification, lockscreen art, and playback controls.
- **Audio Interruption & Headset Handling**: Automatically pauses when headphones are unplugged (`ACTION_AUDIO_BECOMING_NOISY`) and handles audio focus transitions cleanly during phone calls or notifications.
- **Floating Interactive MiniPlayer**: Stays docked at the bottom across all tabs; tap to open the full Now Playing screen.
- **Flagship Now Playing Experience**:
  - Vinyl-style album artwork with ambient glow.
  - Interactive scrubbing seekbar with duration formatting.
  - Shuffle, Repeat (Off / All / One), Skip Next / Previous.
  - Live Queue Sheet: View, reorder, or jump to any upcoming track in the queue.
  - Sleep Timer: Set auto-pause countdown (15m, 30m, 45m, 60m, custom).
- **Complete Library Categorization**:
  - **Tracks**: All songs with quick search and sorting.
  - **Albums**: Grid view with album artwork and album tracks list.
  - **Artists**: Artist directory with track counts.
  - **Folders**: Browse music by filesystem folders.

---

### 🎚️ 3. Built-in Studio Equalizer & Audio Boost
- **5-Band Graphic Equalizer**: Precision sliders with center frequency labels (60Hz, 230Hz, 910Hz, 3.6kHz, 14kHz).
- **Audio Presets**: Normal, Classical, Dance, Flat, Heavy Metal, Hip Hop, Jazz, Pop, Rock, Techno, and Custom.
- **Bass Boost**: High-definition bass amplification slider (0%–100%).
- **3D Surround Virtualizer**: Spatial audio enhancement slider (0%–100%).
- **Loudness / Audio Boost (VLC Style)**: Up to +200% volume gain using Android `LoudnessEnhancer`.

---

### 📁 4. Smart Local Library & Playlist Manager
- **Fast MediaStore Scanner**: Real-time querying of device video and audio files with Android 13+ permission compliance (`READ_MEDIA_VIDEO`, `READ_MEDIA_AUDIO`).
- **Folder Navigation**: Browse videos and music directory-by-directory.
- **Favorites & Playlists**:
  - Unified Favorites tab for one-tap access to your favorite songs and videos.
  - Playback History: Recently played tracks with instant resume.
  - Custom Playlists: Create, rename, delete, and add/remove songs on the fly.
- **Search & Sort**: Filter by name, artist, album, or folder. Sort by Name (A-Z), Date Added, Size, or Duration (Ascending / Descending).

---

## 🏗️ Architecture & Tech Stack

- **Language**: Kotlin 2.3.0
- **UI Toolkit**: Jetpack Compose with Material Design 3
- **Media Engine**: AndroidX Media3 ExoPlayer 1.4.1 & MediaSessionService
- **Image / Thumbnail Loading**: Coil 3
- **State Management**: Kotlin Coroutines & StateFlow (MVI/MVVM clean architecture)
- **Audio Effects**: Android `AudioEffect`, `Equalizer`, `BassBoost`, `Virtualizer`, `LoudnessEnhancer`
- **Build System**: Gradle 9.8.2, Android Gradle Plugin 9.0.0
- **Compatibility**: Android 8.0 (API 26) through Android 15 / 16 (API 35/36)

---

## 🚀 Building & Running

1. Open this project in **Android Studio** (or use the Gradle command line):
   ```bash
   ./gradlew assembleDebug
   ```
2. The generated APK will be at:
   ```
   app/build/outputs/apk/debug/app-debug.apk
   ```
3. Install on your Android device or emulator:
   ```bash
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```
