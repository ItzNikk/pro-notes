package com.example.util

import android.content.Context
import android.hardware.input.InputManager
import android.view.InputDevice
import android.view.MotionEvent
import android.view.View
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import com.example.model.StylusPenBrand
import kotlin.math.PI

/**
 * Tracks stylus hardware state directly from Activity-level [MotionEvent]s before Compose
 * processes them, including:
 * - Side/barrel button state (`BUTTON_STYLUS_PRIMARY`, `BUTTON_STYLUS_SECONDARY`)
 * - Hardware tail eraser (`TOOL_TYPE_ERASER`)
 * - Live barrel roll / nib orientation angle (`AXIS_ORIENTATION`, `AXIS_GENERIC_1`)
 * - Tilt angle (`AXIS_TILT`) & pressure (`AXIS_PRESSURE`)
 * - Hover position (`ACTION_HOVER_MOVE`) for live color & angled nib preview
 * - Automatic hardware stylus brand detection from `InputDevice` metadata
 */
object StylusInputTracker {
  @Volatile var buttonDown: Boolean = false
    private set

  @Volatile var isEraserTail: Boolean = false
    private set

  @Volatile var isStylusActive: Boolean = false
    private set

  var isHovering by mutableStateOf(false)
    private set

  var hoverScreenPosition by mutableStateOf<Offset?>(null)
    private set

  var barrelRollDegrees by mutableFloatStateOf(0f)
    private set

  var tiltRadians by mutableFloatStateOf(0f)
    private set

  var livePressure by mutableFloatStateOf(0.5f)
    private set

  var detectedHardwareBrand by mutableStateOf<StylusPenBrand?>(null)
    private set

  var detectedDeviceName by mutableStateOf("")
    private set

  fun isStylusNear(): Boolean = isStylusActive || isHovering

  private var unboostedView: View? = null

  private const val STYLUS_BUTTON_MASK =
    MotionEvent.BUTTON_STYLUS_PRIMARY or
      MotionEvent.BUTTON_STYLUS_SECONDARY or
      MotionEvent.BUTTON_SECONDARY or
      MotionEvent.BUTTON_TERTIARY

  fun onMotionEvent(ev: MotionEvent, rootView: View? = null) {
    if (rootView != null && unboostedView !== rootView) {
      rootView.isFocusableInTouchMode = true
      unboostedView = rootView
    }

    var anyStylus = false
    var anyEraser = false
    for (i in 0 until ev.pointerCount) {
      when (ev.getToolType(i)) {
        MotionEvent.TOOL_TYPE_STYLUS -> anyStylus = true
        MotionEvent.TOOL_TYPE_ERASER -> {
          anyStylus = true
          anyEraser = true
        }
      }
    }

    val bs = ev.buttonState
    val actionButton = ev.actionButton
    val isButtonPressAction =
      ev.actionMasked == MotionEvent.ACTION_BUTTON_PRESS &&
        (actionButton and STYLUS_BUTTON_MASK) != 0
    val isButtonReleaseAction =
      ev.actionMasked == MotionEvent.ACTION_BUTTON_RELEASE &&
        (actionButton and STYLUS_BUTTON_MASK) != 0

    isStylusActive = anyStylus
    isEraserTail = anyEraser

    // Track barrel roll / orientation angle & tilt for live nib + color preview
    val orientationRad = ev.getAxisValue(MotionEvent.AXIS_ORIENTATION)
    val genericRoll = ev.getAxisValue(MotionEvent.AXIS_GENERIC_1)
    val tiltRad = ev.getAxisValue(MotionEvent.AXIS_TILT)
    tiltRadians = tiltRad.coerceIn(0f, (PI / 2.0).toFloat())
    livePressure = ev.pressure.takeIf { it.isFinite() && it > 0f }?.coerceIn(0.05f, 1.0f) ?: 0.5f

    if (genericRoll != 0f && genericRoll.isFinite()) {
      barrelRollDegrees = ((genericRoll * 180f / PI.toFloat()) % 360f + 360f) % 360f
    } else if (orientationRad != 0f && orientationRad.isFinite()) {
      barrelRollDegrees = ((orientationRad * 180f / PI.toFloat()) % 360f + 360f) % 360f
    }

    if (anyStylus) {
      ev.device?.let { device ->
        val inferred = inferBrandFromDeviceName(device.name ?: "")
        if (inferred != null || detectedHardwareBrand == null) {
          detectedHardwareBrand = inferred ?: StylusPenBrand.SAMSUNG_S_PEN
          detectedDeviceName = device.name?.takeIf { it.isNotBlank() } ?: "Active Stylus Digitizer"
        }
      }
    }

    when (ev.actionMasked) {
      MotionEvent.ACTION_HOVER_ENTER,
      MotionEvent.ACTION_HOVER_MOVE -> {
        isHovering = true
        hoverScreenPosition = Offset(ev.x, ev.y)
        buttonDown = if (isButtonReleaseAction) false else ((bs and STYLUS_BUTTON_MASK) != 0 || isButtonPressAction)
      }
      MotionEvent.ACTION_HOVER_EXIT -> {
        isHovering = false
        hoverScreenPosition = null
        buttonDown = (bs and STYLUS_BUTTON_MASK) != 0
      }
      MotionEvent.ACTION_UP,
      MotionEvent.ACTION_CANCEL -> {
        isHovering = false
        hoverScreenPosition = null
        buttonDown = (bs and STYLUS_BUTTON_MASK) != 0 && !isButtonReleaseAction
        if (ev.actionMasked == MotionEvent.ACTION_CANCEL) {
          buttonDown = false
          isEraserTail = false
        }
      }
      else -> {
        if (anyStylus) {
          hoverScreenPosition = Offset(ev.x, ev.y)
        }
        buttonDown =
          when {
            isButtonReleaseAction -> false
            isButtonPressAction -> true
            else -> (bs and STYLUS_BUTTON_MASK) != 0
          }
      }
    }
  }

  /**
   * Scans Android's [InputManager] for any built-in or paired stylus digitizer hardware so setup
   * and the interactive tour can automatically detect the user's stylus model.
   */
  fun detectConnectedStylusHardware(context: Context): Pair<StylusPenBrand, String> {
    detectedHardwareBrand?.let { brand ->
      return brand to detectedDeviceName.ifBlank { brand.label }
    }
    val inputManager = context.getSystemService(Context.INPUT_SERVICE) as? InputManager
    val deviceIds = inputManager?.inputDeviceIds ?: InputDevice.getDeviceIds()
    var fallbackStylusDeviceName: String? = null

    for (id in deviceIds) {
      val dev = InputDevice.getDevice(id) ?: continue
      val name = dev.name ?: continue
      val supportsStylusSource =
        (dev.sources and InputDevice.SOURCE_STYLUS) == InputDevice.SOURCE_STYLUS ||
          (dev.sources and InputDevice.SOURCE_BLUETOOTH_STYLUS) == InputDevice.SOURCE_BLUETOOTH_STYLUS

      val brand = inferBrandFromDeviceName(name)
      if (brand != null) {
        detectedHardwareBrand = brand
        detectedDeviceName = name
        return brand to name
      }
      if (supportsStylusSource && fallbackStylusDeviceName == null) {
        fallbackStylusDeviceName = name
      }
    }

    val manufacturer = android.os.Build.MANUFACTURER.lowercase()
    val brandFromManufacturer =
      when {
        manufacturer.contains("samsung") -> StylusPenBrand.SAMSUNG_S_PEN
        manufacturer.contains("xiaomi") || manufacturer.contains("redmi") -> StylusPenBrand.XIAOMI_SMART_PEN
        manufacturer.contains("oneplus") || manufacturer.contains("oppo") || manufacturer.contains("realme") ->
          StylusPenBrand.ONEPLUS_STYLO
        manufacturer.contains("lenovo") || manufacturer.contains("motorola") -> StylusPenBrand.LENOVO_PRECISION_PEN
        manufacturer.contains("huawei") || manufacturer.contains("honor") -> StylusPenBrand.HUAWEI_M_PENCIL
        else -> null
      }

    val nonNullFallbackName = fallbackStylusDeviceName
    if (nonNullFallbackName != null) {
      val resolved = brandFromManufacturer ?: StylusPenBrand.USI_STYLUS
      detectedHardwareBrand = resolved
      detectedDeviceName = nonNullFallbackName
      return resolved to nonNullFallbackName
    }

    if (brandFromManufacturer != null) {
      return brandFromManufacturer to "${brandFromManufacturer.label} (Ready)"
    }

    return StylusPenBrand.GENERIC_STYLUS to "Universal Stylus / Touch Ready"
  }

  private fun inferBrandFromDeviceName(rawName: String): StylusPenBrand? {
    val lower = rawName.lowercase()
    return when {
      lower.contains("s pen") ||
        lower.contains("sec_e-pen") ||
        lower.contains("epen") ||
        lower.contains("wacom") -> StylusPenBrand.SAMSUNG_S_PEN
      lower.contains("xiaomi") ||
        lower.contains("mi pen") ||
        lower.contains("smart pen") -> StylusPenBrand.XIAOMI_SMART_PEN
      lower.contains("oneplus") ||
        lower.contains("stylo") ||
        lower.contains("oppo") -> StylusPenBrand.ONEPLUS_STYLO
      lower.contains("lenovo") ||
        lower.contains("precision pen") ||
        lower.contains("tab pen") -> StylusPenBrand.LENOVO_PRECISION_PEN
      lower.contains("huawei") ||
        lower.contains("m-pencil") ||
        lower.contains("mpencil") -> StylusPenBrand.HUAWEI_M_PENCIL
      lower.contains("usi") ||
        lower.contains("chromebook") -> StylusPenBrand.USI_STYLUS
      else -> null
    }
  }
}
