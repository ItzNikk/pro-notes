package shapes_engine.facade

import shapes_engine.core.ShapeClassifier
import shapes_engine.core.ShapeFitter
import shapes_engine.gesture.DirectCommitHandler
import shapes_engine.gesture.DrawAndHoldDetector
import shapes_engine.gesture.DrawAndHoldListener
import shapes_engine.gesture.HoldState
import shapes_engine.gesture.ShapeCommitTarget
import shapes_engine.model.Point2D
import shapes_engine.model.RecognizedShape
import shapes_engine.model.ShapeStyle
import shapes_engine.persistence.DefaultShapeSettingsRepository
import shapes_engine.persistence.InMemoryShapePreferencesStorage
import shapes_engine.persistence.ShapePreferencesStorage
import shapes_engine.persistence.ShapeSettingsRepository

/**
 * Central Facade coordinating shape recognition, draw-and-hold gesture tracking,
 * visual style persistence, and direct unselected shape committing.
 */
class ShapesEngine(
    val settingsRepository: ShapeSettingsRepository = DefaultShapeSettingsRepository(),
    val classifier: ShapeClassifier = ShapeClassifier(),
    val fitter: ShapeFitter = ShapeFitter(),
    val holdDetector: DrawAndHoldDetector = DrawAndHoldDetector(
        classifier = classifier,
        fitter = fitter
    ),
    commitTarget: ShapeCommitTarget = ShapeCommitTarget { _, _ -> }
) {
    val directCommitHandler = DirectCommitHandler(commitTarget)

    init {
        // Wire detector release callback directly to direct commit handler
        holdDetector.listener = object : DrawAndHoldListener {
            override fun onHoldTriggered(
                rawPoints: List<Point2D>,
                currentPosition: Point2D,
                recognizedShape: RecognizedShape
            ) {
                // Notified when shape snaps visually
            }

            override fun onHoldMoved(currentPosition: Point2D, updatedShape: RecognizedShape) {
                // Notified when shape is adjusted during hold
            }

            override fun onHoldReleased(finalShape: RecognizedShape) {
                directCommitHandler.commitDirectly(finalShape)
            }

            override fun onHoldCancelled() {
                // Cancelled
            }
        }
    }

    /**
     * Updates the commit target for recognized shapes.
     */
    var commitTarget: ShapeCommitTarget = commitTarget
        set(value) {
            field = value
            val newHandler = DirectCommitHandler(value)
            holdDetector.listener = object : DrawAndHoldListener {
                override fun onHoldTriggered(
                    rawPoints: List<Point2D>,
                    currentPosition: Point2D,
                    recognizedShape: RecognizedShape
                ) {}

                override fun onHoldMoved(currentPosition: Point2D, updatedShape: RecognizedShape) {}

                override fun onHoldReleased(finalShape: RecognizedShape) {
                    newHandler.commitDirectly(finalShape)
                }

                override fun onHoldCancelled() {}
            }
        }

    /**
     * Call when a pointer down occurs on the drawing canvas.
     */
    fun onPointerDown(position: Point2D, timestampMs: Long = System.currentTimeMillis()) {
        holdDetector.onPointerDown(position, timestampMs)
    }

    /**
     * Call when pointer moves across the drawing canvas.
     */
    fun onPointerMove(
        position: Point2D,
        timestampMs: Long = System.currentTimeMillis()
    ): HoldState {
        val currentStyle = settingsRepository.getStyle()
        return holdDetector.onPointerMove(position, timestampMs, currentStyle)
    }

    /**
     * Periodic stillness check (e.g. from timer or coroutine) when pointer stays still at one point.
     */
    fun checkHoldTrigger(timestampMs: Long = System.currentTimeMillis()): Boolean {
        val currentStyle = settingsRepository.getStyle()
        return holdDetector.checkHoldTrigger(timestampMs, currentStyle)
    }

    /**
     * Call when pointer is lifted from the drawing canvas.
     * Returns the snapped [RecognizedShape] if draw-and-hold snapped, or null if normal freehand ink.
     */
    fun onPointerUp(
        position: Point2D,
        timestampMs: Long = System.currentTimeMillis()
    ): RecognizedShape? {
        return holdDetector.onPointerUp(position, timestampMs)
    }

    /**
     * Abort / cancel gesture tracking.
     */
    fun onCancel() {
        holdDetector.onCancel()
    }

    /**
     * Direct one-shot recognition and fitting without hold gesture.
     */
    fun recognizeAndFit(
        points: List<Point2D>,
        style: ShapeStyle = settingsRepository.getStyle()
    ): RecognizedShape {
        val classification = classifier.classify(points)
        return fitter.fit(classification, points, style)
    }

    companion object {
        /**
         * Factory creating an engine with custom storage.
         */
        fun create(storage: ShapePreferencesStorage = InMemoryShapePreferencesStorage()): ShapesEngine {
            val repository = DefaultShapeSettingsRepository(storage)
            val classifier = ShapeClassifier()
            val fitter = ShapeFitter()
            val detector = DrawAndHoldDetector(classifier = classifier, fitter = fitter)
            return ShapesEngine(
                settingsRepository = repository,
                classifier = classifier,
                fitter = fitter,
                holdDetector = detector
            )
        }
    }
}
