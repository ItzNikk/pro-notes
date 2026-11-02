package shapes_engine.model

/**
 * Visual styling attributes for geometric shapes.
 *
 * @param fillEnabled Whether closed shapes should render an interior fill.
 * @param fillOpacity Opacity of the shape fill, normalized from 0.0f (transparent) to 1.0f (fully opaque).
 * @param strokeWidth Stroke outline thickness in density-independent pixels.
 * @param strokeColorArgb 32-bit ARGB color for the shape outline.
 * @param fillColorArgb Optional dedicated fill color. If null, [strokeColorArgb] is used with [fillOpacity].
 */
data class ShapeStyle(
    val fillEnabled: Boolean = false,
    val fillOpacity: Float = 0.35f,
    val strokeWidth: Float = 3.0f,
    val strokeColorArgb: Long = 0xFF000000L,
    val fillColorArgb: Long? = null,
) {
    init {
        require(fillOpacity in 0.0f..1.0f) {
            "fillOpacity must be between 0.0f and 1.0f, was $fillOpacity"
        }
        require(strokeWidth > 0f) {
            "strokeWidth must be positive, was $strokeWidth"
        }
    }

    /** Returns fill opacity formatted as an integer percentage string (e.g., "35%"). */
    val opacityPercentageText: String
        get() = "${(fillOpacity * 100f).toInt()}%"
}
