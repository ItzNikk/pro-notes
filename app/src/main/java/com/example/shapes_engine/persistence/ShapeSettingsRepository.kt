package shapes_engine.persistence

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import shapes_engine.model.ShapeStyle

/**
 * Repository interface exposing reactive shape styling state and mutation methods.
 */
interface ShapeSettingsRepository {
    val styleFlow: StateFlow<ShapeStyle>
    fun setFillEnabled(enabled: Boolean)
    fun setFillOpacity(opacity: Float)
    fun setStrokeWidth(width: Float)
    fun setStrokeColor(colorArgb: Long)
    fun setFillColor(colorArgb: Long?)
    fun updateStyle(transform: (ShapeStyle) -> ShapeStyle)
    fun getStyle(): ShapeStyle
}

/**
 * Default implementation of [ShapeSettingsRepository] backed by a [ShapePreferencesStorage].
 */
class DefaultShapeSettingsRepository(
    private val storage: ShapePreferencesStorage = InMemoryShapePreferencesStorage()
) : ShapeSettingsRepository {

    private val _styleFlow = MutableStateFlow(storage.loadStyle())
    override val styleFlow: StateFlow<ShapeStyle> = _styleFlow.asStateFlow()

    override fun setFillEnabled(enabled: Boolean) {
        val updated = _styleFlow.value.copy(fillEnabled = enabled)
        _styleFlow.value = updated
        storage.saveStyle(updated)
    }

    override fun setFillOpacity(opacity: Float) {
        val clamped = opacity.coerceIn(0.0f, 1.0f)
        val updated = _styleFlow.value.copy(fillOpacity = clamped)
        _styleFlow.value = updated
        storage.saveStyle(updated)
    }

    override fun setStrokeWidth(width: Float) {
        val positive = width.coerceAtLeast(0.1f)
        val updated = _styleFlow.value.copy(strokeWidth = positive)
        _styleFlow.value = updated
        storage.saveStyle(updated)
    }

    override fun setStrokeColor(colorArgb: Long) {
        val updated = _styleFlow.value.copy(strokeColorArgb = colorArgb)
        _styleFlow.value = updated
        storage.saveStyle(updated)
    }

    override fun setFillColor(colorArgb: Long?) {
        val updated = _styleFlow.value.copy(fillColorArgb = colorArgb)
        _styleFlow.value = updated
        storage.saveStyle(updated)
    }

    override fun updateStyle(transform: (ShapeStyle) -> ShapeStyle) {
        val updated = transform(_styleFlow.value)
        _styleFlow.value = updated
        storage.saveStyle(updated)
    }

    override fun getStyle(): ShapeStyle = _styleFlow.value
}
