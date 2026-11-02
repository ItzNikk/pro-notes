package shapes_engine.gesture

import shapes_engine.model.RecognizedShape

/**
 * Consumer callback or target document interface for committing recognized shapes.
 */
fun interface ShapeCommitTarget {
    /**
     * Commits the finalized recognized shape into the target canvas or note document.
     * The committed shape MUST be unselected with no bounding box or active manipulation handles.
     *
     * @param shape The clean geometric [RecognizedShape] to persist/render.
     * @param isSelected Explicitly false per Milestone 2 specifications (direct unselected commit).
     */
    fun commitShape(shape: RecognizedShape, isSelected: Boolean)
}

/**
 * Direct Commit Handler (Milestone 2 Requirement 10).
 *
 * Commits recognized geometric shapes directly into the host canvas or document data model
 * immediately upon gesture completion (pointer release).
 *
 * Critical Design Contract:
 * - Direct unselected commit: shapes are committed immediately without remaining selected.
 * - No selection bounding boxes or transform handles are left visible.
 * - Handlers can register pre-commit validators or listeners.
 */
class DirectCommitHandler(
    private val commitTarget: ShapeCommitTarget
) {
    private var lastCommittedShape: RecognizedShape? = null
    private var commitCount: Long = 0L

    val lastCommitted: RecognizedShape? get() = lastCommittedShape
    val totalCommits: Long get() = commitCount

    /**
     * Commits [shape] directly to the target canvas/document without leaving it selected.
     *
     * @param shape The shape recognized and fitted by the engine.
     * @return True if commit succeeded, false if shape had insufficient geometry.
     */
    fun commitDirectly(shape: RecognizedShape): Boolean {
        if (shape.points.isEmpty()) {
            return false
        }

        // Direct commit semantics: isSelected = false
        commitTarget.commitShape(shape, isSelected = false)
        lastCommittedShape = shape
        commitCount++
        return true
    }

    /**
     * Resets the handler record.
     */
    fun reset() {
        lastCommittedShape = null
        commitCount = 0L
    }
}
