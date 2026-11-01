package com.example.util

import android.os.SystemClock
import android.view.MotionEvent
import android.view.View

/**
 * Process-wide view of what the stylus is doing, fed straight from the Activity's raw
 * MotionEvent stream (touch + hover) BEFORE Compose processes the same event, so the editor
 * always reads fresh values on pen-down.
 *
 * Works with every Android stylus that reports standard MotionEvent data:
 *  - Samsung S Pen            -> BUTTON_STYLUS_PRIMARY
 *  - Wacom / Surface / Lenovo / HP / Huawei / Xiaomi / OPPO / USI pens
 *                              -> BUTTON_STYLUS_PRIMARY, BUTTON_STYLUS_SECONDARY or BUTTON_SECONDARY
 *  - Pens with an eraser tail -> TOOL_TYPE_ERASER
 *  - Pens that map the barrel to the tertiary / back / forward button
 * Buttons are only honoured when the pointer is a stylus, so a mouse right-click never erases.
 */
object StylusInputTracker {
  private const val BARREL_BUTTON_MASK =
    MotionEvent.BUTTON_STYLUS_PRIMARY or
      MotionEvent.BUTTON_STYLUS_SECONDARY or
      MotionEvent.BUTTON_SECONDARY or
      MotionEvent.BUTTON_TERTIARY or
      MotionEvent.BUTTON_BACK or
      MotionEvent.BUTTON_FORWARD

  /** A stylus barrel/side button is currently held. */
  var buttonDown: Boolean = false
    private set

  /** The latest event contained a stylus or eraser-tail pointer. */
  var isStylusOrEraser: Boolean = false
    private set

  /** The latest stylus pointer is the eraser end of the pen. */
  var isEraserTail: Boolean = false
    private set

  private var stylusNear: Boolean = false
  private var lastStylusEventMs: Long = 0L

  /** True while a stylus is hovering over (or touching) the screen. Used for palm rejection. */
  fun isStylusNear(windowMs: Long = 600L): Boolean =
    stylusNear && SystemClock.uptimeMillis() - lastStylusEventMs <= windowMs

  fun onMotionEvent(ev: MotionEvent, decorView: View?) {
    val action = ev.actionMasked
    val liftedIndex = if (action == MotionEvent.ACTION_POINTER_UP) ev.actionIndex else -1

    var stylus = false
    var eraser = false
    for (i in 0 until ev.pointerCount) {
      if (i == liftedIndex) continue
      when (ev.getToolType(i)) {
        MotionEvent.TOOL_TYPE_STYLUS -> stylus = true
        MotionEvent.TOOL_TYPE_ERASER -> {
          stylus = true
          eraser = true
        }
      }
    }

    if (stylus) {
      lastStylusEventMs = SystemClock.uptimeMillis()
      stylusNear = action != MotionEvent.ACTION_HOVER_EXIT
      // Ask the system to deliver pen samples immediately instead of batching them to the
      // next frame: lower ink latency.
      if (action == MotionEvent.ACTION_DOWN) decorView?.requestUnbufferedDispatch(ev)
    } else if (action == MotionEvent.ACTION_HOVER_EXIT) {
      stylusNear = false
    }

    isStylusOrEraser = stylus
    isEraserTail = eraser
    buttonDown = stylus && (ev.buttonState and BARREL_BUTTON_MASK) != 0

    if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
      isStylusOrEraser = false
      isEraserTail = false
      buttonDown = false
    }
  }
}
