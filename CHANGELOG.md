# Changelog

## Unreleased

### Toolbar Cleanup
- Simplified the editor toolbar to core note-taking actions: writing tools (Pen, Pencil, Highlighter), Eraser, Lasso, Shapes, Laser, Undo/Redo, document navigation, and Settings.
- Removed Stickers, Text insertion, Sticky Notes, Image insertion, and Audio recording toolbar actions along with their associated popovers, dead toolbar icons, and temporary status banners.

### Stylus Erasing & Side-Button Responsiveness
- Detected `MotionEvent.BUTTON_STYLUS_PRIMARY` and `MotionEvent.BUTTON_STYLUS_SECONDARY` to temporarily activate erasing without changing the permanent toolbar selection, immediately restoring the previous writing tool on button release.
- Fixed erasing lag by adding a fast bounding-box pre-check in `isStrokeHitByPoint`, restricting stroke densification in `PRECISION` mode to hit strokes only, and batching Room database persistence into `finishEraserGesture()` on pointer-up or button release instead of persisting on every move event.

### First-Launch Video Onboarding
- Added a first-launch `OnboardingScreen` with muted, seamlessly looping video playback (`onboarding_video.mp4`), center-crop scaling, a static fallback background, a dark readability gradient overlay, and subtle entrance animations for `"Welcome to Pro Notes"`, `"A calm place for your ideas."`, `"Start creating"`, and `"Take a tour"`.
- Released `MediaPlayer` resources cleanly when leaving onboarding and added a `BuildConfig.DEBUG`-only developer action in Settings to replay onboarding for testing.

### Laser Pointer Rework
- Rebuilt the laser trail using per-point canvas coordinates and timestamps (`LaserTrailPoint`) so trail segments remain stationary where drawn, fade out smoothly by age (`520ms`), continue fading via a 16ms frame ticker after pointer-up, and clear immediately on tool, page, or document changes.

### Circle-to-Lasso Animation
- Ordered the closed loop starting from the held endpoint (`orderLoopPointsFromEndpoint`) and animated `circleLassoConversionProgress` from `0f` to `1f` over `340ms`, progressively replacing the temporary hand-drawn ink loop with a dashed lasso outline before revealing the active selection boundary.

### Library Search & Folder System
- Implemented live case-insensitive search in the Library across both notebook titles and folder names, surfacing matching folders alongside matching notebooks with their folder context and a clean empty state with a one-tap clear action.
- Updated folder creation to support a folder name and custom color (curated palette plus custom HSV Hue/Saturation picker) and redesigned folder cards to display a single large tinted folder icon (`128.dp`) with the folder title below.

### Launcher Icon & Input Pipeline
- Enlarged and centered the Pro Notes emblem within the Android adaptive-icon safe zone (`ic_launcher_foreground.xml` and `app_logo.png`) across all `mipmap-*` launcher and round launcher assets.
- Unified canvas pointer handling into a single gesture pipeline with safe pressure fallback (`0.5f`), corner-preserving smoothing, zoom-aware point coalescing, and strict Stylus-Only touch filtering.

