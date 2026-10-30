# Pro Notes (Goodnotes 6 Parity) — Gap Analysis & Implementation Plan

## 1. Deep Goodnotes 6 Research: What Is Missing or Broken in Our App

Based on how **Goodnotes 6** works across iPadOS/Android and an audit of our current codebase, here is everything currently broken or missing in our implementation:

1. **Circle to Lasso Trigger Bug (Triggers Without Hold / Inconsistent Hold)**:
   - *How Goodnotes 6 works*: While using the Pen tool, you draw a closed loop/circle around existing ink or objects **and hold the stylus tip still at the end of the stroke (~380ms)** (or pause briefly to snap into a circle and hold/touch-and-hold) to convert that circle into an active dashed Lasso selection. Lifting immediately without holding leaves it as normal ink.
   - *What's broken in our app*: `addStroke()` in `ProNotesViewModel.kt` also triggered Circle-to-Lasso on normal pen lift (`snappedFromHold == false`), and the hold timer in `EditorScreen.kt` only checked a single timestamp rather than continuously tracking stationary hold duration while the stylus tip is pressed.

2. **Lasso Tool Is Visual-Only (Cannot Move, Resize, Recolor, or Delete Specific Selected Items)**:
   - *How Goodnotes 6 works*: Drawing a lasso performs a **point-in-polygon / bounding-box intersection test** against all strokes, shapes, text boxes, elements/stickers, sticky notes, and images (filtered by the active Lasso Filter toggles). Once selected:
     - **Dragging inside the lasso selection** moves all enclosed strokes & objects together across the page in real time.
     - Tapping the selection opens the floating pill bar (**Color**, **Resize/Rotate**, **AI**, **Convert**, **Cut**, **Duplicate**, **Delete**, **More / Arrange Layers**), and every action mutates **only the enclosed items** (and displays interactive Resize/Rotate handles when Resize is active).
   - *What's broken in our app*: Lasso currently just draws a dashed loop on screen. Dragging inside the loop starts drawing a brand-new lasso instead of moving the enclosed content, and **Delete / Duplicate / Scale** affect the entire page instead of only the items inside the lasso!

3. **Stylus & Multi-Page PDF / Notebook Swiping (Horizontal OR Vertical)**:
   - *How Goodnotes 6 works*: Under the `...` More menu -> **Scrolling Direction**, users can switch between **Horizontal** (page-by-page book turn) and **Vertical** (continuous vertical page scrolling). When Stylus/Palm Rejection is active (or with two-finger swipe / single-finger swipe outside drawing), users can swipe left/right or up/down to navigate across all imported PDF pages and notebook pages smoothly, or pull past the last page to add a new page.
   - *What's broken in our app*: `EditorScreen.kt` renders a single static `Box` for `currentPage` with no `HorizontalPager` or `VerticalPager`, so swiping horizontally or vertically never turns the page! Additionally, stylus pointer classification (`PointerType.Stylus` / `PointerType.Eraser`) vs finger page-turn/zoom gestures needs clean separation so fingers can swipe pages or pinch-zoom while the stylus draws effortlessly.

4. **Shapes Tool Forces Manual Shape Picking Instead of Auto-Deciding**:
   - *How Goodnotes 6 works*: Neither the **Shapes Tool** nor **Draw and Hold** asks you to choose "Rectangle" or "Circle" before drawing. You simply sketch any shape freehand, and Goodnotes **automatically recognizes and decides** whether you drew a **Straight Line**, **Arrow**, **Curved Arc**, **Circle / Ellipse**, **Triangle**, **Rectangle / Square**, **Pentagon / Polygon**, or **Star**, snapping your stroke into that exact geometry with live draggable vertex handles and optional semi-transparent interior fill.
   - *What's broken in our app*: `inferShapeKindOrDefault()` defaulted to a manually selected `ShapeKind` (e.g., always Rectangle) instead of analyzing the stroke geometry automatically.

5. **Highlighter Washes Out Ink & Lacks Straight-Line Snap / Popover**:
   - *How Goodnotes 6 works*: Highlighter ink renders **underneath pen ink** using a multiplicative/darken blend mode (`BlendMode.Multiply` on light paper) so black/dark text stays crisp, prevents self-overlapping opacity build-up within a single stroke, snaps into a **perfect straight line** when you **Draw and Hold** (or when **Draw in Straight Line** is enabled in the Highlighter popover), and opens a dedicated **Highlighter Settings Popover** when tapped while active.
   - *What's broken in our app*: Tapping the active Highlighter tool does not open a dedicated Highlighter popover, and highlighter strokes use standard alpha blending over text instead of `BlendMode.Multiply` + straight-line snapping.

6. **Color Palette Missing 2D Spectrum, Hex Input (`#RRGGBB`), and Full Slot Management**:
   - *How Goodnotes 6 works*: Tapping any color slot or the custom color wheel opens the **Pen / Highlighter Color Popover** with **Presets** and **Custom** views—featuring a 2D Saturation/Brightness spectrum square, a Hue slider, a direct **Hex Code input (`#RRGGBB`)**, a saved Custom Swatches grid, and the ability to **replace the current toolbar color slot**, **add (`+`) more color slots to the toolbar**, or remove slots.
   - *What's broken in our app*: The popover only has 3 basic sliders with no 2D color square, no `#HEX` input field, and limited slot management.

---

## 2. Proposed Architecture & Feature Upgrades

### A. Hold-Only Circle to Lasso (`EditorScreen.kt` & `ProNotesViewModel.kt`)
- Remove the on-lift Circle-to-Lasso conversion so normal fast circle strokes remain ink/shapes.
- In the active pointer loop, run a continuous stationary-hold detector while the pointer is down (`holdDurationMs >= 380ms` with movement `< 8px`).
- When the user draws a closed circular loop (`start-to-end distance < 28% of perimeter`, `circularityScore > 0.55`) around existing strokes/objects **and holds the stylus tip at the end of the circle**, immediately:
  1. Discard the temporary ink stroke,
  2. Convert the loop into `lassoLoopPoints`,
  3. Compute the exact enclosed items (`selectedStrokeIds`, `selectedTextBoxIds`, `selectedElementIds`, `selectedStickyIds`, `selectedImageIds`), and
  4. Show the animated dashed Lasso boundary, selection bounding box, and floating Goodnotes Lasso action bar.

### B. Fully Functional Interactive Lasso Engine
- **Point-in-Polygon Selection Engine**: Implement ray-casting point-in-polygon + bounding-box intersection in `ProNotesViewModel.kt` so only strokes, shapes, text boxes, elements, sticky notes, and images inside `lassoLoopPoints` (matching `LassoFilterSettings`) are added to the active selection set.
- **Drag Inside Lasso to Move**: When a Lasso selection is active and the user touches/drags inside the selection bounding box, translate all selected strokes (updating their `points` and `shapeControlPoints`), text boxes, elements, sticky notes, and images together in real time, and shifts the lasso loop + bounding box with them.
- **Resize / Rotate Bounding Box Mode**: Tapping the **Resize** icon in the floating Lasso pill activates a dashed bounding frame with a bottom-right **Scale & Rotate handle** to proportionally scale and rotate only the selected strokes and objects.
- **Scoped Lasso Actions**:
  - **Color**: Recolors *only* the selected strokes/shapes/text inside the lasso.
  - **Cut / Copy / Duplicate / Delete**: Operates *only* on the selected items inside the lasso and persists changes to Room.
  - **Take Screenshot / Convert to Text or Math / Bring to Front / Send to Back**: Operates directly on the enclosed selection.

### C. Stylus & Palm Rejection + Horizontal / Vertical PDF & Page Swiping
- Add `pageScrollDirection: PageScrollDirection` (`HORIZONTAL` or `VERTICAL`) to `ProNotesUiState`, controllable from the `...` Document More menu (**Scrolling Direction: Horizontal / Vertical**) and Settings.
- Wrap the document page canvas in Jetpack Compose's `HorizontalPager` (when `HORIZONTAL`) or `VerticalPager` (when `VERTICAL`), synchronized bidirectionally with `currentPageIndex`.
- **Smart Stylus vs Finger Routing**:
  - `PointerType.Stylus` and `PointerType.Eraser` are always routed directly to the active writing/eraser canvas with zero pager interception (`userScrollEnabled` dynamically controlled or multi-touch / stylus separation).
  - When **Stylus Only (Palm Rejection)** is enabled (or in **Read-Only Mode**, or using a **2-finger swipe/pinch**), finger swipes smoothly turn pages horizontally or vertically across PDFs and notebooks, and pinch gestures zoom/pan the page.
  - Swiping past the final page offers a quick **"+ Pull / Tap to Add Page"** action matching Goodnotes 6.

### D. Auto-Deciding Geometric Shape Recognition Engine
- Replace manual shape selection with `autoDetectShapeKind(points: List<Offset>): ShapeKind` used by both the **Shapes Tool** (`PrimaryTool.SHAPE` / `WritingSubTool.DRAW_SHAPE`) and **Draw and Hold**:
  1. **Straight Line / Arrow**: High linearity (`endToStartDistance / pathLength > 0.88`), checking for arrowhead hooks at the end of the stroke.
  2. **Curved Arc / Parabola**: Open path (`endToStartDistance / pathLength` between `0.35` and `0.88`) with smooth single-sided curvature.
  3. **Closed Shapes** (`endToStartDistance < 0.28 * pathLength` or endpoints within `65px`):
     - Compute Ramer-Douglas-Peucker (RDP) simplified polygon corners (`corners`) and radial variance from the centroid:
     - **Circle / Ellipse**: Low radial distance variance from normalized ellipse radii and smooth corner angles (`corners >= 6` or circularity `> 0.78`).
     - **Triangle**: 3 dominant corner vertices (`corners == 3`).
     - **Rectangle / Square**: 4 dominant corner vertices (`corners == 4`) with near-orthogonal angles; snaps to axis-aligned or rotated rectangle.
     - **Pentagon / Polygon**: 5–6 dominant vertices (`corners in 5..6`).
     - **Star**: Alternating inner/outer radial peaks (`corners >= 8` with alternating radii).
- Remove manual shape-type buttons from the Shapes popover and instead display **Fill Color (toggle)**, **Snap to Other Shapes**, and a live preview of the auto-recognized shape with draggable control handles.

### E. True Goodnotes 6 Highlighter
- Render Highlighter strokes on the background ink layer (`zIndex = -1`) using `BlendMode.Darken` / `BlendMode.Multiply` so dark PDF text and pen strokes remain 100% legible underneath.
- Render each highlighter stroke as a single unified `Path` so overlapping segments within the same stroke never darken/compound opacity.
- Support both **Draw and Hold** (automatically snaps any held highlighter stroke into a crisp straight line) and the **Draw in Straight Line** toggle.
- Add `ActivePopover.HIGHLIGHTER_SETTINGS` when tapping the selected Highlighter icon, with **Draw in Straight Line** toggle, **Auto-Deselect**, and thickness sliders.

### F. 1:1 Goodnotes Color Palette & Toolbar Slot Customization
- Upgrade `PenColorPopover` into a complete Goodnotes 6 color studio:
  - **Presets Tab** (curated Goodnotes paper-friendly ink & pastel highlighter palettes) and **Custom Tab**.
  - **Interactive 2D Saturation-Value Color Square** + **Hue Rainbow Bar**.
  - **Editable `#RRGGBB` Hex Code Input Field** with live preview swatch.
  - **Toolbar Slot Actions**:
    - **Replace Current Toolbar Slot** with the chosen color,
    - **`+ Add to Toolbar`** (adds a new quick-access color slot directly into the floating sub-toolbar),
    - **Remove Slot** (when more than 3 slots exist), and
    - **Save to Custom Palette Grid**.
