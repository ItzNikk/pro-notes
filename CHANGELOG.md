# Changelog

## Play Store Release Edition

### Laser Pointer Polish & Custom Colors
- Removed the end-cap circle/pointer dot from the laser stroke so only the clean glowing laser beam is drawn.
- Wired the toolbar's active selected color into both live and fading laser strokes (`LaserTrailStroke.color`).

### Complete Shapes Studio Rewrite
- Rebuilt the Shapes tool with a docked **Shape Studio Strip** and enhanced popover supporting both **Auto-Snap** (freehand sketch + 350ms hold) and **Direct Drag-to-Draw** for all 11 geometric shapes (Line, Arrow, Curve, Circle, Oval, Triangle, Rectangle, Diamond, Pentagon, Hexagon, Star).
- Added live 60fps geometric shape preview while dragging, optional translucent shape fill, draggable vertex handles, a center move handle, and a floating action bar (**Filled/Outline**, **Copy**, **Delete**, **Done**).

### Stylus Lag Elimination & Multi-Brand Pen Hardware Settings
- Eliminated `pointerInput` coroutine restarts on color/thickness/tool changes using `rememberUpdatedState` references and reduced stylus point coalescing distance to `0.85f` for ultra-low-latency ink.
- Added a dedicated **Stylus & Pens** category in Settings supporting **Samsung S Pen**, **Xiaomi Smart Pen (Gen 1 & 2)**, **OnePlus Stylo / OPPO Pencil**, **Lenovo Precision / Tab Pen Plus**, **Huawei M-Pencil**, **USI 2.0 Stylus**, and **Universal Active Pen**, with configurable barrel button actions, pressure sensitivity curve (`40%–180%`), tilt shading, hover cursor, and tail eraser toggles.

### Folder Color Visibility & Database Persistence Fix
- Fixed folder colors appearing transparent in Jetpack Compose by converting stored 32-bit ARGB `Long` values with `Color(((argb or 0xFF000000L)).toInt())`.
- Added `colorArgb` persistence to `FolderEntity` (Room schema v3 with `MIGRATION_2_3`), wired `colorArgb` through `createFolder` and `updateFolderColor`, and added inline folder color switching in both Grid and List views.

### Circle-to-Lasso & Selection Animations
- Added an animated hold-progress ring at the loop endpoint during Circle-to-Lasso hold detection, smooth quadratic Bezier loop morphing, and a soft selection glow transition.

### Hands-On Interactive Feature Tour & Support the Developer (UPI)
- Rewrote **Take a tour** to launch an interactive 5-step guided coach overlay directly inside the live Editor canvas so users can experience Stylus Ink, Live Shapes Studio, Smart Circle-to-Lasso, Color Laser, and Precision Eraser hands-on.
- Added a **Support the Developer** section in Settings where the developer UPI ID and payee name can be customized and saved, copied with one tap, or opened directly in any installed UPI payment app (`upi://pay`).


