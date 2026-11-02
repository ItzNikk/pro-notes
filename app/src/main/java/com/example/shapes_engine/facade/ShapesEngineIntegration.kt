package shapes_engine.facade

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import shapes_engine.gesture.ShapeCommitTarget
import shapes_engine.model.Point2D
import shapes_engine.model.RecognizedShape
import shapes_engine.persistence.AndroidShapePreferencesStorage
import shapes_engine.persistence.DefaultShapeSettingsRepository
import shapes_engine.persistence.InMemoryShapePreferencesStorage
import shapes_engine.persistence.ShapePreferencesStorage
import shapes_engine.persistence.ShapeSettingsRepository

/**
 * Android integration helpers and dependency wiring for [ShapesEngine].
 *
 * Provides factory functions, Compose remember utilities, and adapters to integrate
 * the engine seamlessly into existing Android UI and ViewModels.
 */
object ShapesEngineIntegration {

    /**
     * Creates an Android application-backed [ShapesEngine] instance using SharedPreferences for persistence.
     *
     * @param context Android context for preferences storage.
     * @param commitTarget Optional shape commit consumer.
     */
    fun createAndroidEngine(
        context: Context,
        commitTarget: ShapeCommitTarget = ShapeCommitTarget { _, _ -> }
    ): ShapesEngine {
        val storage = AndroidShapePreferencesStorage(context.applicationContext)
        val repository = DefaultShapeSettingsRepository(storage)
        return ShapesEngine(
            settingsRepository = repository,
            commitTarget = commitTarget
        )
    }

    /**
     * Creates a lightweight in-memory [ShapesEngine] instance.
     */
    fun createInMemoryEngine(
        commitTarget: ShapeCommitTarget = ShapeCommitTarget { _, _ -> }
    ): ShapesEngine {
        val storage = InMemoryShapePreferencesStorage()
        val repository = DefaultShapeSettingsRepository(storage)
        return ShapesEngine(
            settingsRepository = repository,
            commitTarget = commitTarget
        )
    }

    /**
     * Converts a list of float pair coordinates into [Point2D] representations.
     */
    fun toPoints2D(coords: List<Pair<Float, Float>>): List<Point2D> {
        return coords.map { Point2D(it.first, it.second) }
    }
}

/**
 * Remember an instance of [ShapesEngine] inside a Jetpack Compose hierarchy.
 */
@Composable
fun rememberShapesEngine(
    storage: ShapePreferencesStorage? = null,
    commitTarget: ShapeCommitTarget = ShapeCommitTarget { _, _ -> }
): ShapesEngine {
    val context = LocalContext.current
    return remember {
        val effectiveStorage = storage ?: AndroidShapePreferencesStorage(context.applicationContext)
        val repository = DefaultShapeSettingsRepository(effectiveStorage)
        ShapesEngine(
            settingsRepository = repository,
            commitTarget = commitTarget
        )
    }
}
