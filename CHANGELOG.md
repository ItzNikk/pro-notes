# Changelog

All notable architectural and functional changes to **Pro Notes** are documented in this file.

## [2.5.0] — Production Release

### Vector Ink & Rendering Pipeline
- **Smoothed Variable-Width Calligraphy & Pressure Engine**: Implemented continuous segment interpolation with rounded joins for directional (`CALLIGRAPHY`) and pressure-modulated (`FOUNTAIN`, `BRUSH`) strokes, eliminating vertex separation on high-velocity curves.
- **Asynchronous Off-Thread Persistence**: Debounced Room SQLite serialization onto `Dispatchers.Default`, preventing main-thread contention during rapid stylus-lift and consecutive stroke initiation.
- **Proximity-Gated Precision Ruler**: Restricted ruler edge snapping (`snapPointAlongRuler`) to strokes originating within the active ruler's local bounding corridor while preserving independent pen/highlighter sub-tool states.

### Geometric Primitive Recognition
- **Closure-Normalized Shape Classification**: Pre-trimmed stylus closure overshoot (`StrokeResampler.trimClosureOvershoot`) prior to circularity and aspect evaluation, improving classification accuracy for hand-drawn circles and ellipses.
- **Centroid-Anchored Hold Stabilization**: Anchored closed geometric primitives around their fitted centroid (`snappedHoldCenter`) during stationary hold gestures to prevent unintended radial collapse.
- **14-Primitive Recognition Engine**: Full support for lines, arrows, quadratic curves, circles, ellipses, triangles, right triangles, rectangles, diamonds, parallelograms, pentagons, hexagons, stars, and orthogonal/chevron vertices.

### Spatial Selection & Multi-Layer Manipulation
- **Persistent Spatial Lasso State**: Preserved active bounding-box selection state across tool inspections and post-translation releases, with configurable automatic pen restoration (`autoReturnToPenAfterMove`).
- **Unified Layer Filtering & Z-Order Control**: Added granular inclusion filters across six canvas layers (Vector Ink, Geometric Shapes, Typography, Media Assets, Annotations, and Occlusion Masks) alongside explicit Z-index promotion/demotion.

### Document Architecture, Full-Text Indexing & Workspace Hierarchy
- **PDF & Multi-Layer Full-Text Indexing**: Integrated automated text extraction during PDF ingestion (`PdfImportHelper.extractTextFromPdfFile`) and added an in-document search bar indexing PDF text layers, typography boxes, annotations, and canvas metadata with instant page navigation.
- **Custom Cover Ingestion & First-Page Canvas Integration**: Enabled high-resolution custom cover image import and direct vector ink rendering on Page 1 cover canvases, with live stroke composite previews in the document library.
- **Hierarchical Folder-First Library**: Prioritized directory structures ahead of document cards and added multi-target drag-and-drop organization with HSV color synchronization.
