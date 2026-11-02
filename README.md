# Pro Notes — Studio Handwriting, Vector Ink & PDF Workspace for Android

<p align="center">
  <img src="app/src/main/res/drawable/app_logo.png" width="110" height="110" alt="Pro Notes Logo" />
</p>

<p align="center">
  <strong>Low-Latency Stylus Ink • Continuous Hold-to-Snap Geometry • Smart Lasso • PDF Markup • Encrypted Vault</strong>
</p>

<p align="center">
  <a href="https://github.com/ItzNikk/pro-notes/releases"><img src="https://img.shields.io/badge/Release-v2.5.0%20Studio%20Edition-2563EB?style=for-the-badge&logo=android" alt="Release v2.5.0" /></a>
  <img src="https://img.shields.io/badge/Kotlin-2.0%2B-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin" />
  <img src="https://img.shields.io/badge/Jetpack%20Compose-Material%203-10B981?style=for-the-badge&logo=jetpackcompose&logoColor=white" alt="Jetpack Compose" />
  <img src="https://img.shields.io/badge/Room%20SQLite-Schema%20v3-F59E0B?style=for-the-badge&logo=sqlite&logoColor=white" alt="Room Database" />
</p>

---

## Visual Overview & Screenshots

### 1. Studio Canvas, 7 Precision Pens, Continuous Shape Snap & Interactive Tour HUD
<p align="center">
  <img src="docs/screenshots/editor_studio_showcase.svg" width="100%" alt="Pro Notes Editor Studio Showcase" />
</p>

### 2. Drag-and-Drop Document Library & Stylus-Synced Folder Color Studio
<p align="center">
  <img src="docs/screenshots/library_drag_drop_showcase.svg" width="100%" alt="Pro Notes Library & Folder Color Studio Showcase" />
</p>

---

## Key Features in v2.5.0 Studio Release

### 7 Studio Pens & Live Color Toolbar
- **7 Expressive Pen Engines**:
  1. **Fountain Pen**: Velocity & pressure-sensitive dynamic calligraphy taper.
  2. **Ballpoint Pen**: Uniform, crisp everyday note-taking ink.
  3. **Brush Pen**: Wide dynamic pressure modulation for hand-lettering and sketching.
  4. **Fineliner**: Ultra-crisp technical drafting nib for diagrams and equations.
  5. **Calligraphy (45° Italic Nib)**: Direction-modulated chisel ribbon strokes that widen on downstrokes and thin on cross-strokes.
  6. **Marker (Chisel Felt-Tip)**: Bold square-capped studio marker.
  7. **Neon Glow Pen**: Triple-layer luminous outer/mid halo with a bright white-hot core.
- **Live Toolbar Color Previews**: Every active tool icon in the sub-toolbar (Pen, Highlighter, Shapes, Lasso, Tape, Ruler, Laser) dynamically renders a live color badge and accent preview matching its active ink swatch.

### Continuous Draw-and-Hold Shape Engine (14 Geometric Shapes)
- **Zero-Interruption Continuous Drawing**: Draw naturally in either the Pen tool (with Draw & Hold enabled) or the Shapes tool—holding the stylus steady for `320ms` snaps your freehand stroke directly onto the canvas without spawning a blue selection box, allowing immediate continuous drawing.
- **Improved Circle & Loop Recognition**:
  - Pre-trims stylus closure tails before evaluating circularity so hand-drawn circles and ovals reliably snap to crisp geometric circles without collapsing on hold or misclassifying as polygons.
  - Supports **14 Geometric Primitives**: `Line`, `Arrow`, `Curve`, `Circle`, `Oval`, `Triangle`, `Right Triangle`, `Rectangle`, `Diamond`, `Parallelogram`, `Pentagon`, `Hexagon`, `Star`, and `Angle Brackets / Chevrons (> , < , L)`.

### Smart Dual-Mode Lasso & Circle-to-Lasso
- **Move vs. Lift Memory**:
  - **Drag & Move Selection**: When you circle strokes with the Lasso and **drag/move** them, lifting the stylus automatically switches back to your previous writing tool (`lastWritingToolBeforeLasso`) for seamless note-taking.
  - **Lift Without Moving**: If you draw a Lasso selection and **lift immediately** without dragging, the Lasso selection stays active with its floating action bar (`Color`, `Resize`, `Copy`, `To Text`, `Delete`).

### Study Tape, Interactive Ruler & Color Laser
- **Tap-to-Reveal Study Tape**: Draw straight highlighter-style occlusion strips over key terms or formulas, then tap any tape strip with the Pen, Hand, or Tape tool to toggle translucent reveal mode for active-recall studying.
- **Interactive Rotatable Ruler**: Toggle the **Ruler** directly from the sub-toolbar, drag to position, rotate via the degree badge, and draw perfectly straight snapped ink lines along either edge.
- **Multi-Color Fading Laser Pointer**: Present notes with a glowing, fading laser beam tinted with your active toolbar swatch.

### Drag-and-Drop Folder System & Stylus Color Studio
- **Instant & Long-Press Drag-and-Drop**: Drag notebooks or folders using either the instant **`⋮⋮ Drag`** handle badge on any card or a long-press gesture, and drop them directly onto any folder card, the top **Quick-Drop Folder Strip**, or the breadcrumb trail.
- **Stylus-Synced Folder Color Studio**: Customize new or existing folders using your active **Stylus Quick Swatches**, **Custom Stylus Palette**, **Studio Grid**, or the **3-Slider HSV Studio (Hue / Saturation / Brightness)** with one-tap syncing back to your stylus palette.

### Triple-Tap (3x Tap) Cinema Video Showcase & Redesigned Interactive Tour
- **3x Tap Cinema Video Showcase**: Triple-tap rapidly on the Editor canvas, top tab bar, Documents header, or Settings footer to launch the full-screen `onboarding_video.mp4` showcase (`ThankYouScreen`). Triple-tap inside the showcase to toggle **Pure Cinema Video Mode** (hiding all UI cards for edge-to-edge video playback).
- **Interactive Studio Tour HUD**: Minimizable floating HUD with clickable **Step 1–6 pills**, an embedded **Picture-in-Picture Video Guide** toggle, and an **"✨ Auto-Demo Step"** button that draws live demonstrations directly on your canvas.

---

## Multi-Brand Stylus Hardware Compatibility

| Stylus Hardware Profile | Pressure Curve | Tilt Shading | Barrel Button Mapping | Hover Cursor |
| :--- | :---: | :---: | :---: | :---: |
| **Samsung S Pen** (Galaxy Tab S / S Ultra) | 4,096 Levels | Yes | Eraser / Lasso / Color | Yes |
| **Xiaomi Smart Pen** (Gen 1 & Gen 2) | 4,096 Levels | Yes | Primary & Secondary Buttons | Yes |
| **OnePlus Stylo / OPPO Pencil** | 4,096 Levels | Yes | Double-Tap / Barrel Switch | Yes |
| **Lenovo Precision Pen / Tab Pen Plus** | 4,096 Levels | Yes | Barrel Click Action | Yes |
| **Huawei M-Pencil** (Gen 2 / Gen 3) | 4,096 Levels | Yes | Double-Tap Switch | Yes |
| **USI 2.0 Active Stylus** | 4,096 Levels | Yes | Barrel & Tail Eraser | Yes |

---

## Building & Publishing a Release APK to GitHub

### 1. Build Debug & Release APKs Locally
```bash
# Clone the repository
git clone https://github.com/ItzNikk/pro-notes.git
cd pro-notes

# Compile and package the Release APK & Debug APK
gradle :app:assembleRelease :app:assembleDebug
```

Once built, the signed APKs are generated at:
- **Release APK**: `app/build/outputs/apk/release/app-release.apk`
- **Debug APK**: `app/build/outputs/apk/debug/app-debug.apk`

### 2. Push Code & Publish Release APK to GitHub Releases
You can push the repository and upload the generated Release APK directly to GitHub using the GitHub CLI (`gh`) or AI Studio's **Export to GitHub / Build APK** menu:

```bash
# Stage and push your latest changes
git add .
git commit -m "Release v2.5.0: 7 Studio Pens, Circle Snap Engine, Stylus Folder Studio & Video Showcase"
git push origin main

# Create a GitHub Release with the built APK attached
gh release create v2.5.0 \
  app/build/outputs/apk/release/app-release.apk \
  --title "Pro Notes v2.5.0 — Studio Edition" \
  --notes-file CHANGELOG.md
```

---

## Project Architecture

```text
app/src/main/java/com/example/
├── MainActivity.kt                  # App entry point, navigation host & PDF import/export
├── data/                            # Room SQLite database (v3), entities, DAOs & AES-GCM Vault encryption
├── model/ProNotesModels.kt          # Immutable domain models (7 PenStyles, 14 ShapeKinds, StudyTape, Ruler)
├── shapes_engine/                   # 60fps geometric recognition engine (Circle, Polygon, Bracket & Bezier fitters)
├── ui/
│   ├── components/                  # CustomToolbarIcons (live color badges), EditorPopovers, TopDocumentTabBar
│   ├── screens/                     # EditorScreen, DocumentsScreen, OnboardingScreen, SettingsScreen
│   └── theme/                       # Material 3 Light, Dark & True Pitch-Black AMOLED themes
└── viewmodel/ProNotesViewModel.kt   # Reactive StateFlow state container, undo/redo history & Auto-Demo Tour engine
```

## License

Copyright © ItzNikk. All rights reserved.
