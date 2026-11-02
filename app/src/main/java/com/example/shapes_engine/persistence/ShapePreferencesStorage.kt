package shapes_engine.persistence

import shapes_engine.model.ShapeStyle

/**
 * Interface for loading and persisting geometric shape style configurations.
 */
interface ShapePreferencesStorage {
    /**
     * Loads the saved [ShapeStyle], or returns [defaultStyle] if none is stored.
     */
    fun loadStyle(defaultStyle: ShapeStyle = ShapeStyle()): ShapeStyle

    /**
     * Persists the given [ShapeStyle].
     */
    fun saveStyle(style: ShapeStyle)

    /**
     * Updates and saves whether interior fill is enabled.
     */
    fun setFillEnabled(enabled: Boolean)

    /**
     * Updates and saves the fill opacity (clamped to 0.0f..1.0f).
     */
    fun setFillOpacity(opacity: Float)

    /**
     * Clears any saved style preferences.
     */
    fun clear()
}
