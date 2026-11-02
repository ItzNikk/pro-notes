package shapes_engine.gesture

import shapes_engine.core.ShapeClassifier
import shapes_engine.core.ShapeFitter
import shapes_engine.model.Point2D
import shapes_engine.model.RecognizedShape
import shapes_engine.model.ShapeStyle
import kotlin.math.hypot

/**
 * Listener interface for GoodNotes-style draw-and-hold gestures.
 */
interface DrawAndHoldListener {
    /**
     * Called when the pointer has paused long enough within the stillness radius to trigger snap.
     *
     * @param rawPoints Complete stroke points drawn up to the hold point.
     * @param currentPosition Current pointer location at trigger time.
     * @param recognizedShape Initial snapped shape fitted from the stroke.
     */
    fun onHoldTriggered(
        rawPoints: List<Point2D>,
        currentPosition: Point2D,
        recognizedShape: RecognizedShape
    )

    /**
     * Called while continuing to hold and moving the pointer to adjust or transform the snapped shape.
     *
     * @param currentPosition New pointer position during hold-drag.
     * @param updatedShape Snapped shape transformed by the drag offset.
     */
    fun onHoldMoved(currentPosition: Point2D, updatedShape: RecognizedShape)

    /**
     * Called when pointer is lifted after hold was triggered, committing the shape.
     *
     * @param finalShape The final recognized and fitted shape to commit.
     */
    fun onHoldReleased(finalShape: RecognizedShape)

    /**
     * Called when the gesture is cancelled or aborted without committing.
     */
    fun onHoldCancelled()
}

/**
 * State of the [DrawAndHoldDetector].
 */
enum class HoldState {
    IDLE,
    DRAWING,
    HOLD_TRIGGERED,
    CANCELLED
}

/**
 * GoodNotes-style draw-and-hold detector.
 *
 * Tracks pointer input points, measures stillness (movement within [stillnessRadiusPx])
 * over a duration of [holdDurationMs] (~350-450ms). Once satisfied, classifies the stroke
 * via [ShapeClassifier], fits clean geometry via [ShapeFitter], and notifies the [listener].
 *
 * @param holdDurationMs Milliseconds of stillness required to trigger shape snap (default 380ms).
 * @param stillnessRadiusPx Maximum movement distance in pixels allowed while counting stillness (default 14px).
 * @param minStrokeLengthPx Minimum stroke path length in pixels required before hold detection activates (default 40px).
 * @param minPointsCount Minimum number of points required for valid shape recognition (default 5).
 * @param classifier [ShapeClassifier] instance used to recognize stroke geometry.
 * @param fitter [ShapeFitter] instance used to fit clean geometry.
 * @param listener Optional [DrawAndHoldListener] callback receiver.
 */
class DrawAndHoldDetector(
    val holdDurationMs: Long = 380L,
    val stillnessRadiusPx: Float = 14f,
    val minStrokeLengthPx: Float = 40f,
    val minPointsCount: Int = 5,
    private val classifier: ShapeClassifier = ShapeClassifier(),
    private val fitter: ShapeFitter = ShapeFitter(),
    var listener: DrawAndHoldListener? = null
) {
    private val points = mutableListOf<Point2D>()
    private var state: HoldState = HoldState.IDLE

    private var anchorPosition: Point2D = Point2D.Zero
    private var anchorTimestampMs: Long = 0L

    private var currentSnappedShape: RecognizedShape? = null
    private var totalPathLength: Float = 0f

    val currentState: HoldState get() = state
    val rawPoints: List<Point2D> get() = points.toList()
    val activeShape: RecognizedShape? get() = currentSnappedShape

    /**
     * Pointer down event. Starts tracking a new stroke.
     */
    fun onPointerDown(position: Point2D, timestampMs: Long = System.currentTimeMillis()) {
        points.clear()
        points.add(position)
        anchorPosition = position
        anchorTimestampMs = timestampMs
        state = HoldState.DRAWING
        currentSnappedShape = null
        totalPathLength = 0f
    }

    /**
     * Pointer move event.
     *
     * @param position Current pointer coordinates.
     * @param timestampMs Event timestamp in milliseconds.
     * @param activeStyle Visual style to apply if hold snaps.
     * @return Current [HoldState] after processing this move event.
     */
    fun onPointerMove(
        position: Point2D,
        timestampMs: Long = System.currentTimeMillis(),
        activeStyle: ShapeStyle = ShapeStyle()
    ): HoldState {
        if (state == HoldState.IDLE || state == HoldState.CANCELLED) {
            return state
        }

        val lastPoint = points.lastOrNull() ?: position
        val segmentDist = position.distanceTo(lastPoint)
        if (segmentDist > 0.5f) {
            points.add(position)
            totalPathLength += segmentDist
        }

        when (state) {
            HoldState.DRAWING -> {
                val distFromAnchor = position.distanceTo(anchorPosition)
                if (distFromAnchor > stillnessRadiusPx) {
                    // Pointer moved outside stillness threshold, reset anchor
                    anchorPosition = position
                    anchorTimestampMs = timestampMs
                } else {
                    // Within stillness threshold, check elapsed duration
                    val elapsed = timestampMs - anchorTimestampMs
                    if (elapsed >= holdDurationMs && canTriggerHold()) {
                        triggerHold(position, activeStyle)
                    }
                }
            }
            HoldState.HOLD_TRIGGERED -> {
                // Already snapped: dragging pointer adjusts/scales/transforms the shape
                val snapped = currentSnappedShape
                if (snapped != null) {
                    val updated = adjustShapeOnDrag(snapped, position)
                    currentSnappedShape = updated
                    listener?.onHoldMoved(position, updated)
                }
            }
            else -> {}
        }

        return state
    }

    /**
     * Periodic tick or timer evaluation for cases where pointer moves 0px and sits completely still.
     */
    fun checkHoldTrigger(
        timestampMs: Long = System.currentTimeMillis(),
        activeStyle: ShapeStyle = ShapeStyle()
    ): Boolean {
        if (state == HoldState.DRAWING && canTriggerHold()) {
            val elapsed = timestampMs - anchorTimestampMs
            if (elapsed >= holdDurationMs) {
                triggerHold(anchorPosition, activeStyle)
                return true
            }
        }
        return state == HoldState.HOLD_TRIGGERED
    }

    /**
     * Pointer up event.
     *
     * @param position Lift coordinates.
     * @param timestampMs Event timestamp.
     * @return Snapped [RecognizedShape] if hold was active, or null if normal freehand lift.
     */
    fun onPointerUp(
        position: Point2D,
        timestampMs: Long = System.currentTimeMillis()
    ): RecognizedShape? {
        val finalShape = currentSnappedShape

        when (state) {
            HoldState.HOLD_TRIGGERED -> {
                if (finalShape != null) {
                    listener?.onHoldReleased(finalShape)
                }
            }
            HoldState.DRAWING -> {
                // Lifted without holding - stroke ends as normal ink
            }
            else -> {}
        }

        state = HoldState.IDLE
        currentSnappedShape = null
        return finalShape
    }

    /**
     * Cancels the current gesture detector operation.
     */
    fun onCancel() {
        if (state != HoldState.IDLE) {
            state = HoldState.CANCELLED
            currentSnappedShape = null
            points.clear()
            listener?.onHoldCancelled()
            state = HoldState.IDLE
        }
    }

    private fun canTriggerHold(): Boolean {
        return points.size >= minPointsCount && totalPathLength >= minStrokeLengthPx
    }

    private fun triggerHold(position: Point2D, activeStyle: ShapeStyle) {
        val classification = classifier.classify(points)
        val fitted = fitter.fit(classification, points, activeStyle)
        currentSnappedShape = fitted
        state = HoldState.HOLD_TRIGGERED
        listener?.onHoldTriggered(points.toList(), position, fitted)
    }

    private fun adjustShapeOnDrag(originalShape: RecognizedShape, newPointerPos: Point2D): RecognizedShape {
        val center = originalShape.center
        val initialDist = anchorPosition.distanceTo(center).coerceAtLeast(10f)
        val currentDist = newPointerPos.distanceTo(center).coerceAtLeast(10f)
        val scale = (currentDist / initialDist).coerceIn(0.2f, 5.0f)

        if (scale == 1.0f) return originalShape

        val scaledPoints = originalShape.points.map { pt ->
            Point2D(
                center.x + (pt.x - center.x) * scale,
                center.y + (pt.y - center.y) * scale
            )
        }

        val scaledControlPoints = originalShape.controlPoints.map { pt ->
            Point2D(
                center.x + (pt.x - center.x) * scale,
                center.y + (pt.y - center.y) * scale
            )
        }

        return originalShape.copy(
            points = scaledPoints,
            controlPoints = scaledControlPoints,
            boundingBox = shapes_engine.model.BoundingBox.fromPoints(scaledPoints),
            radius = originalShape.radius * scale,
            semiMajorAxis = originalShape.semiMajorAxis * scale,
            semiMinorAxis = originalShape.semiMinorAxis * scale
        )
    }
}
