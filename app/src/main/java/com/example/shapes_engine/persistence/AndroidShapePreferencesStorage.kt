package shapes_engine.persistence

import android.content.Context
import android.content.SharedPreferences
import shapes_engine.model.ShapeStyle

/**
 * Android [SharedPreferences] implementation of [ShapePreferencesStorage].
 * Persists fill state, fill opacity, stroke width, and colors across application restarts.
 */
class AndroidShapePreferencesStorage(
    private val sharedPreferences: SharedPreferences
) : ShapePreferencesStorage {

    constructor(context: Context, prefName: String = PREFS_NAME) : this(
        context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
    )

    companion object {
        const val PREFS_NAME = "shapes_engine_preferences"
        private const val KEY_FILL_ENABLED = "shapes_fill_enabled"
        private const val KEY_FILL_OPACITY = "shapes_fill_opacity"
        private const val KEY_STROKE_WIDTH = "shapes_stroke_width"
        private const val KEY_STROKE_COLOR = "shapes_stroke_color"
        private const val KEY_FILL_COLOR = "shapes_fill_color"
        private const val KEY_HAS_FILL_COLOR = "shapes_has_fill_color"
    }

    override fun loadStyle(defaultStyle: ShapeStyle): ShapeStyle {
        val fillEnabled = sharedPreferences.getBoolean(KEY_FILL_ENABLED, defaultStyle.fillEnabled)
        val fillOpacity = sharedPreferences.getFloat(KEY_FILL_OPACITY, defaultStyle.fillOpacity).coerceIn(0.0f, 1.0f)
        val strokeWidth = sharedPreferences.getFloat(KEY_STROKE_WIDTH, defaultStyle.strokeWidth).coerceAtLeast(0.1f)
        val strokeColor = sharedPreferences.getLong(KEY_STROKE_COLOR, defaultStyle.strokeColorArgb)
        val hasFillColor = sharedPreferences.getBoolean(KEY_HAS_FILL_COLOR, defaultStyle.fillColorArgb != null)
        val fillColor = if (hasFillColor) {
            sharedPreferences.getLong(KEY_FILL_COLOR, defaultStyle.fillColorArgb ?: strokeColor)
        } else {
            null
        }

        return ShapeStyle(
            fillEnabled = fillEnabled,
            fillOpacity = fillOpacity,
            strokeWidth = strokeWidth,
            strokeColorArgb = strokeColor,
            fillColorArgb = fillColor
        )
    }

    override fun saveStyle(style: ShapeStyle) {
        val editor = sharedPreferences.edit()
            .putBoolean(KEY_FILL_ENABLED, style.fillEnabled)
            .putFloat(KEY_FILL_OPACITY, style.fillOpacity.coerceIn(0.0f, 1.0f))
            .putFloat(KEY_STROKE_WIDTH, style.strokeWidth)
            .putLong(KEY_STROKE_COLOR, style.strokeColorArgb)
            .putBoolean(KEY_HAS_FILL_COLOR, style.fillColorArgb != null)

        if (style.fillColorArgb != null) {
            editor.putLong(KEY_FILL_COLOR, style.fillColorArgb)
        } else {
            editor.remove(KEY_FILL_COLOR)
        }
        editor.apply()
    }

    override fun setFillEnabled(enabled: Boolean) {
        sharedPreferences.edit()
            .putBoolean(KEY_FILL_ENABLED, enabled)
            .apply()
    }

    override fun setFillOpacity(opacity: Float) {
        sharedPreferences.edit()
            .putFloat(KEY_FILL_OPACITY, opacity.coerceIn(0.0f, 1.0f))
            .apply()
    }

    override fun clear() {
        sharedPreferences.edit()
            .remove(KEY_FILL_ENABLED)
            .remove(KEY_FILL_OPACITY)
            .remove(KEY_STROKE_WIDTH)
            .remove(KEY_STROKE_COLOR)
            .remove(KEY_FILL_COLOR)
            .remove(KEY_HAS_FILL_COLOR)
            .apply()
    }
}
