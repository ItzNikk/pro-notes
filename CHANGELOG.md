# Changelog

## v2.5.0 — Studio Release Edition

### Triple-Tap (3x Tap) Cinema Video Showcase (`onboarding_video.mp4`)
- Added a **3-times rapid tap gesture** across the **Editor Canvas**, **Top Document Tab Bar**, **Documents Header**, and **Settings Footer** to launch the full-screen `onboarding_video.mp4` Video Showcase (`ThankYouScreen`).
- Added **Pure Cinema Video Mode** inside `ThankYouScreen`: triple-tapping anywhere on the video screen toggles the rainbow card overlay on/off so the video can be watched edge-to-edge without any UI obstruction.

### Redesigned Interactive Studio Tour (6 Steps + Live Auto-Demo + Video PiP)
- Upgraded the in-editor **Interactive Feature Tour** with a minimizable studio HUD, clickable **Step 1–6 progress pills**, an embedded **Picture-in-Picture (`R.raw.onboarding_video`) video guide toggle**, and an **"✨ Auto-Demo Step"** action (`runTourStepAutoDemo()`) that draws live ink, snapped shapes, and study tape demonstrations directly on the canvas.

### Snap-to-Shape Engine & Circle Recognition Fix
- **Pre-Trimmed Closure Detection**: Updated `ShapeClassifier.classify` to trim stylus closure tail overshoot (`StrokeResampler.trimClosureOvershoot`) *before* evaluating closure ratio and aspect bounds, ensuring hand-drawn circles and ovals reliably snap to crisp geometric circles.
- **Hold-Snap Circle Stability**: Fixed the hold-to-snap drag handler in `EditorScreen.kt` so closed shapes (Circles, Ovals, Polygons) anchor around their snapped center (`snappedHoldCenter`) and require deliberate movement (`> 16px`) before resizing, eliminating the bug where holding a snapped circle collapsed it toward the start point.
- **Empty Circle-to-Lasso Guard**: Updated `triggerInstantCircleToLasso` so drawing an empty circle on the canvas snaps to a geometric circle instead of triggering an empty Lasso selection.
- **Expanded Shape Vocabulary (14 Shapes)**: Added recognition and direct drawing for **Angle Brackets / Chevrons (`>`, `<`, `L`)**, **Right Triangles**, and **Parallelograms** alongside Circles, Ovals, Triangles, Rectangles, Diamonds, Pentagons, Hexagons, Stars, Lines, Arrows, and Curves.

### 7 Studio Pens, Rotatable Ruler & Clean Bottom Canvas
- Expanded `PenStyle` with 4 new studio pens: **Fineliner**, **Calligraphy (45° Italic Nib)**, **Marker (Chisel Felt-Tip)**, and **Neon Glow (3-Layer Luminous Halo)**.
- Added **Ruler** directly to `WritingSubTool` in the sub-toolbar so the rotatable `InteractiveRulerOverlay` can be toggled and used to snap straight lines along any angle.
- Removed the bottom `"Pull or Tap + Page"` / `"Templates & Cover"` overlay bar from `EditorScreen.kt` for an unobstructed edge-to-edge writing surface.

### Folder Drag-and-Drop Fix & Stylus-Synced Folder Color Studio
- Fixed the stale `pointerInput` closure bug in `DocumentsScreen.kt` using `rememberUpdatedState` so releasing a dragged notebook or folder over a target folder reliably moves the item.
- Added an instant **`⋮⋮ Drag`** handle badge on notebook cards and a **Quick-Drop Folder Target Strip** at the top of the Documents view.
- Upgraded Folder creation and Folder color customization with the **Stylus Color System**, syncing your active Stylus Quick Swatches, Custom Stylus Palette, Studio Grid, and a 3-slider **HSV Studio (Hue, Saturation, Brightness)**.

---

## v2.0.0 — Play Store Release Edition

### Laser Pointer Polish & Custom Colors
- Removed the end-cap circle/pointer dot from the laser stroke so only the clean glowing laser beam is drawn.
- Wired the toolbar's active selected color into both live and fading laser strokes (`LaserTrailStroke.color`).

### Complete Shapes Studio Rewrite
- Rebuilt the Shapes tool with a docked **Shape Studio Strip** and enhanced popover supporting both **Auto-Snap** (freehand sketch + 320ms hold) and **Direct Drag-to-Draw**.

### Stylus Lag Elimination & Multi-Brand Pen Hardware Settings
- Eliminated `pointerInput` coroutine restarts on color/thickness/tool changes using `rememberUpdatedState` references and reduced stylus point coalescing distance to `0.85f` for ultra-low-latency ink.
- Added a dedicated **Stylus & Pens** category in Settings supporting **Samsung S Pen**, **Xiaomi Smart Pen (Gen 1 & 2)**, **OnePlus Stylo / OPPO Pencil**, **Lenovo Precision / Tab Pen Plus**, **Huawei M-Pencil**, **USI 2.0 Stylus**, and **Universal Active Pen**.
