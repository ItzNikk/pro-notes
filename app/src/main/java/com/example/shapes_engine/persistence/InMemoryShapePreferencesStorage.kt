package shapes_engine.persistence

import shapes_engine.model.ShapeStyle

/**
 * In-memory fallback / headless implementation of [ShapePreferencesStorage].
 * Useful for testing, previews, and environments without Android Context.
 */
class InMemoryShapePreferencesStorage(
    initialStyle: ShapeStyle = ShapeStyle()
) : ShapePreferencesStorage {

    private var currentStyle: ShapeStyle = initialStyle

    @Synchronized
    override fun loadStyle(defaultStyle: ShapeStyle): ShapeStyle {
        return currentStyle
    }

    @Synchronized
    override fun saveStyle(style: ShapeStyle) {
        currentStyle = style.copy(fillOpacity = style.fillOpacity.coerceIn(0.0f, 1.0f))
    }

    @Synchronized
    override fun setFillEnabled(enabled: Boolean) {
        currentStyle = currentStyle.copy(fillEnabled = enabled)
    }

    @Synchronized
    override fun setFillOpacity(opacity: Float) {
        currentStyle = currentStyle.copy(fillOpacity = opacity.coerceIn(0.0f, 1.0f))
    }

    @Synchronized
    override fun clear() {
        currentStyle = ShapeStyle()
    }
}
