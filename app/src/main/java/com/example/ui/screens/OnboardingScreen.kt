package com.example.ui.screens

import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.net.Uri
import android.view.Surface
import android.view.TextureView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.R
import com.example.model.StylusPenBrand
import com.example.ui.theme.NewButtonBlue
import com.example.util.StylusInputTracker
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
  onStartCreating: () -> Unit,
  onStylusAutoDetected: (StylusPenBrand) -> Unit = {},
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  var videoFailed by remember { mutableStateOf(false) }

  val detectedStylusInfo = remember(context) {
    StylusInputTracker.detectConnectedStylusHardware(context)
  }

  LaunchedEffect(detectedStylusInfo) {
    onStylusAutoDetected(detectedStylusInfo.first)
  }

  val titleAlpha = remember { Animatable(0f) }
  val titleOffsetY = remember { Animatable(24f) }
  val subtitleAlpha = remember { Animatable(0f) }
  val subtitleOffsetY = remember { Animatable(24f) }
  val buttonsAlpha = remember { Animatable(0f) }
  val buttonsOffsetY = remember { Animatable(24f) }

  LaunchedEffect(Unit) {
    launch {
      titleAlpha.animateTo(1f, tween(durationMillis = 520, easing = FastOutSlowInEasing))
    }
    launch {
      titleOffsetY.animateTo(0f, tween(durationMillis = 520, easing = FastOutSlowInEasing))
    }
    delay(110L)
    launch {
      subtitleAlpha.animateTo(1f, tween(durationMillis = 520, easing = FastOutSlowInEasing))
    }
    launch {
      subtitleOffsetY.animateTo(0f, tween(durationMillis = 520, easing = FastOutSlowInEasing))
    }
    delay(130L)
    launch {
      buttonsAlpha.animateTo(1f, tween(durationMillis = 520, easing = FastOutSlowInEasing))
    }
    launch {
      buttonsOffsetY.animateTo(0f, tween(durationMillis = 520, easing = FastOutSlowInEasing))
    }
  }

  val mediaPlayerHolder = remember { arrayOfNulls<MediaPlayer>(1) }
  val surfaceHolder = remember { arrayOfNulls<Surface>(1) }

  DisposableEffect(Unit) {
    onDispose {
      runCatching {
        mediaPlayerHolder[0]?.stop()
      }
      runCatching {
        mediaPlayerHolder[0]?.release()
      }
      mediaPlayerHolder[0] = null
      runCatching {
        surfaceHolder[0]?.release()
      }
      surfaceHolder[0] = null
    }
  }

  Box(
    modifier =
      modifier
        .fillMaxSize()
        .background(
          Brush.verticalGradient(
            colors =
              listOf(
                Color(0xFF0B1220),
                Color(0xFF101B33),
                Color(0xFF090D16),
              ),
          ),
        )
        .testTag("onboarding_screen"),
  ) {
    if (!videoFailed) {
      AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
          TextureView(ctx).apply {
            isOpaque = false
            surfaceTextureListener =
              object : TextureView.SurfaceTextureListener {
                override fun onSurfaceTextureAvailable(
                  surfaceTexture: SurfaceTexture,
                  width: Int,
                  height: Int,
                ) {
                  runCatching {
                    val surface = Surface(surfaceTexture)
                    surfaceHolder[0] = surface
                    val player = MediaPlayer()
                    mediaPlayerHolder[0] = player
                    val uri =
                      Uri.parse("android.resource://${ctx.packageName}/${R.raw.onboarding_video}")
                    player.setDataSource(ctx, uri)
                    player.setSurface(surface)
                    player.isLooping = true
                    player.setVolume(0f, 0f)
                    player.setOnVideoSizeChangedListener { _, videoW, videoH ->
                      updateCenterCropTransform(this@apply, width, height, videoW, videoH)
                    }
                    player.setOnPreparedListener { mp ->
                      mp.isLooping = true
                      mp.setVolume(0f, 0f)
                      updateCenterCropTransform(
                        this@apply,
                        width,
                        height,
                        mp.videoWidth,
                        mp.videoHeight,
                      )
                      mp.start()
                    }
                    player.setOnErrorListener { _, _, _ ->
                      videoFailed = true
                      true
                    }
                    player.prepareAsync()
                  }.onFailure {
                    videoFailed = true
                  }
                }

                override fun onSurfaceTextureSizeChanged(
                  surfaceTexture: SurfaceTexture,
                  width: Int,
                  height: Int,
                ) {
                  val mp = mediaPlayerHolder[0] ?: return
                  updateCenterCropTransform(
                    this@apply,
                    width,
                    height,
                    mp.videoWidth,
                    mp.videoHeight,
                  )
                }

                override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean {
                  runCatching { mediaPlayerHolder[0]?.stop() }
                  runCatching { mediaPlayerHolder[0]?.release() }
                  mediaPlayerHolder[0] = null
                  runCatching { surfaceHolder[0]?.release() }
                  surfaceHolder[0] = null
                  return true
                }

                override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) = Unit
              }
          }
        },
      )
    }

    // Dark gradient overlay so all text and buttons remain crisp and readable
    Box(
      modifier =
        Modifier
          .fillMaxSize()
          .background(
            Brush.verticalGradient(
              colorStops =
                arrayOf(
                  0.0f to Color(0x66060A12),
                  0.45f to Color(0x8C070C17),
                  0.78f to Color(0xD9070C17),
                  1.0f to Color(0xF2060A12),
                ),
            ),
          ),
    )

    Column(
      modifier =
        Modifier
          .fillMaxSize()
          .padding(horizontal = 28.dp, vertical = 44.dp),
      verticalArrangement = Arrangement.Bottom,
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Column(
        modifier = Modifier.widthIn(max = 460.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        var selectedBrand by remember(detectedStylusInfo) { mutableStateOf(detectedStylusInfo.first) }
        var showStylusPicker by remember { mutableStateOf(true) }

        // Hardware Stylus Auto-Detection Badge during setup (tap to expand/collapse brand selector)
        Row(
          modifier =
            Modifier
              .offset { IntOffset(0, titleOffsetY.value.roundToInt()) }
              .alpha(titleAlpha.value)
              .clip(RoundedCornerShape(20.dp))
              .background(Color(0xCC111C2D))
              .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.45f), RoundedCornerShape(20.dp))
              .clickable { showStylusPicker = !showStylusPicker }
              .padding(horizontal = 14.dp, vertical = 6.dp)
              .testTag("onboarding_detected_stylus_badge"),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Box(
            modifier =
              Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(Color(0xFF34D399)),
          )
          Text(
            text = "Stylus Setup Ready: ${selectedBrand.label}",
            color = Color(0xFF7DD3FC),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
          text = "Welcome to Pro Notes",
          color = Color.White,
          fontSize = 32.sp,
          fontWeight = FontWeight.Bold,
          textAlign = TextAlign.Center,
          lineHeight = 38.sp,
          modifier =
            Modifier
              .offset { IntOffset(0, titleOffsetY.value.roundToInt()) }
              .alpha(titleAlpha.value)
              .testTag("onboarding_title"),
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
          text = "A calm place for your ideas.",
          color = Color.White.copy(alpha = 0.84f),
          fontSize = 17.sp,
          fontWeight = FontWeight.Medium,
          textAlign = TextAlign.Center,
          modifier =
            Modifier
              .offset { IntOffset(0, subtitleOffsetY.value.roundToInt()) }
              .alpha(subtitleAlpha.value)
              .testTag("onboarding_subtitle"),
        )

        Spacer(modifier = Modifier.height(18.dp))

        AnimatedVisibility(
          visible = showStylusPicker,
          enter = fadeIn(tween(220)),
          exit = fadeOut(tween(180)),
        ) {
          Column(
            modifier =
              Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xCC111A2B))
                .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(16.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp)
                .testTag("onboarding_stylus_setup_card"),
            horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            Text(
              text = "Choose Your Stylus Pen Profile",
              color = Color(0xFF93C5FD),
              fontSize = 12.5.sp,
              fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
            ) {
              listOf(
                StylusPenBrand.SAMSUNG_S_PEN,
                StylusPenBrand.LENOVO_PRECISION_PEN,
                StylusPenBrand.XIAOMI_SMART_PEN,
                StylusPenBrand.GENERIC_STYLUS,
              ).forEach { brand ->
                val isSelected = selectedBrand == brand
                Box(
                  modifier =
                    Modifier
                      .clip(RoundedCornerShape(10.dp))
                      .background(
                        if (isSelected) NewButtonBlue.copy(alpha = 0.32f)
                        else Color.White.copy(alpha = 0.07f)
                      )
                      .border(
                        1.dp,
                        if (isSelected) Color(0xFF38BDF8) else Color.White.copy(alpha = 0.14f),
                        RoundedCornerShape(10.dp),
                      )
                      .clickable {
                        selectedBrand = brand
                        onStylusAutoDetected(brand)
                      }
                      .padding(horizontal = 9.dp, vertical = 6.dp)
                      .testTag("onboarding_stylus_${brand.name.lowercase()}"),
                ) {
                  Text(
                    text =
                      when (brand) {
                        StylusPenBrand.SAMSUNG_S_PEN -> "S Pen"
                        StylusPenBrand.LENOVO_PRECISION_PEN -> "Lenovo"
                        StylusPenBrand.XIAOMI_SMART_PEN -> "Xiaomi"
                        else -> "Universal"
                      },
                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.72f),
                    fontSize = 11.5.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                  )
                }
              }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = selectedBrand.subtitle,
              color = Color.White.copy(alpha = 0.68f),
              fontSize = 11.sp,
              textAlign = TextAlign.Center,
            )
          }
        }

        Column(
          modifier =
            Modifier
              .fillMaxWidth()
              .offset { IntOffset(0, buttonsOffsetY.value.roundToInt()) }
              .alpha(buttonsAlpha.value),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          // Primary button: "Start creating"
          Box(
            modifier =
              Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(NewButtonBlue)
                .clickable(onClick = onStartCreating)
                .testTag("onboarding_start_creating_button"),
            contentAlignment = Alignment.Center,
          ) {
            Text(
              text = "Start creating",
              color = Color.White,
              fontSize = 16.sp,
              fontWeight = FontWeight.SemiBold,
            )
          }
        }
      }
    }
  }
}

/**
 * Full-screen Thank You experience using the exact same looping welcome video and dark gradient
 * overlay, featuring a smoothly animated rainbow color-changing "Thank You" headline.
 */
@Composable
fun ThankYouScreen(
  onClose: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var videoFailed by remember { mutableStateOf(false) }
  var pureVideoCinemaMode by remember { mutableStateOf(false) }

  val infiniteTransition = rememberInfiniteTransition(label = "rainbowThankYouTransition")
  val rainbowPhase by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec =
      infiniteRepeatable(
        animation = tween(durationMillis = 3200, easing = LinearEasing),
        repeatMode = RepeatMode.Restart,
      ),
    label = "rainbowPhase",
  )

  val rainbowPalette =
    remember {
      listOf(
        Color(0xFFFF3B30), // Red
        Color(0xFFFF9500), // Orange
        Color(0xFFFFCC00), // Yellow
        Color(0xFF34C759), // Green
        Color(0xFF00C7BE), // Cyan
        Color(0xFF007AFF), // Blue
        Color(0xFFAF52DE), // Violet
        Color(0xFFFF2D55), // Magenta
        Color(0xFFFF3B30), // Red loop
      )
    }

  val activeRainbowColor =
    remember(rainbowPhase) {
      val scaled = rainbowPhase * (rainbowPalette.size - 1)
      val idx = scaled.toInt().coerceIn(0, rainbowPalette.size - 2)
      val frac = scaled - idx
      val c1 = rainbowPalette[idx]
      val c2 = rainbowPalette[idx + 1]
      Color(
        red = c1.red + (c2.red - c1.red) * frac,
        green = c1.green + (c2.green - c1.green) * frac,
        blue = c1.blue + (c2.blue - c1.blue) * frac,
        alpha = 1f,
      )
    }

  val shiftX = rainbowPhase * 900f
  val rainbowBrush =
    Brush.linearGradient(
      colors = rainbowPalette,
      start = Offset(shiftX - 450f, 0f),
      end = Offset(shiftX + 450f, 180f),
    )

  val mediaPlayerHolder = remember { arrayOfNulls<MediaPlayer>(1) }
  val surfaceHolder = remember { arrayOfNulls<Surface>(1) }

  DisposableEffect(Unit) {
    onDispose {
      runCatching { mediaPlayerHolder[0]?.stop() }
      runCatching { mediaPlayerHolder[0]?.release() }
      mediaPlayerHolder[0] = null
      runCatching { surfaceHolder[0]?.release() }
      surfaceHolder[0] = null
    }
  }

  Box(
    modifier =
      modifier
        .fillMaxSize()
        .background(
          Brush.verticalGradient(
            colors =
              listOf(
                Color(0xFF0B1220),
                Color(0xFF101B33),
                Color(0xFF090D16),
              ),
          ),
        )
        .testTag("thank_you_screen"),
  ) {
    if (!videoFailed) {
      AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
          TextureView(ctx).apply {
            isOpaque = false
            surfaceTextureListener =
              object : TextureView.SurfaceTextureListener {
                override fun onSurfaceTextureAvailable(
                  surfaceTexture: SurfaceTexture,
                  width: Int,
                  height: Int,
                ) {
                  runCatching {
                    val surface = Surface(surfaceTexture)
                    surfaceHolder[0] = surface
                    val player = MediaPlayer()
                    mediaPlayerHolder[0] = player
                    val uri =
                      Uri.parse("android.resource://${ctx.packageName}/${R.raw.onboarding_video}")
                    player.setDataSource(ctx, uri)
                    player.setSurface(surface)
                    player.isLooping = true
                    player.setVolume(0f, 0f)
                    player.setOnVideoSizeChangedListener { _, videoW, videoH ->
                      updateCenterCropTransform(this@apply, width, height, videoW, videoH)
                    }
                    player.setOnPreparedListener { mp ->
                      mp.isLooping = true
                      mp.setVolume(0f, 0f)
                      updateCenterCropTransform(
                        this@apply,
                        width,
                        height,
                        mp.videoWidth,
                        mp.videoHeight,
                      )
                      mp.start()
                    }
                    player.setOnErrorListener { _, _, _ ->
                      videoFailed = true
                      true
                    }
                    player.prepareAsync()
                  }.onFailure {
                    videoFailed = true
                  }
                }

                override fun onSurfaceTextureSizeChanged(
                  surfaceTexture: SurfaceTexture,
                  width: Int,
                  height: Int,
                ) {
                  val mp = mediaPlayerHolder[0] ?: return
                  updateCenterCropTransform(
                    this@apply,
                    width,
                    height,
                    mp.videoWidth,
                    mp.videoHeight,
                  )
                }

                override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean {
                  runCatching { mediaPlayerHolder[0]?.stop() }
                  runCatching { mediaPlayerHolder[0]?.release() }
                  mediaPlayerHolder[0] = null
                  runCatching { surfaceHolder[0]?.release() }
                  surfaceHolder[0] = null
                  return true
                }

                override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) = Unit
              }
          }
        },
      )
    }

    if (!pureVideoCinemaMode) {
      // Dark gradient overlay so the rainbow Thank You card shines vividly
      Box(
        modifier =
          Modifier
            .fillMaxSize()
            .background(
              Brush.verticalGradient(
                colorStops =
                  arrayOf(
                    0.0f to Color(0x66060A12),
                    0.45f to Color(0x8C070C17),
                    0.78f to Color(0xD9070C17),
                    1.0f to Color(0xF2060A12),
                  ),
              ),
            ),
      )

      Column(
        modifier =
          Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp, vertical = 48.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        Column(
          modifier =
            Modifier
              .widthIn(max = 460.dp)
              .clip(RoundedCornerShape(28.dp))
              .background(Color(0xBF0B1222))
              .border(1.5.dp, activeRainbowColor.copy(alpha = 0.65f), RoundedCornerShape(28.dp))
              .padding(horizontal = 28.dp, vertical = 34.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          Box(
            modifier =
              Modifier
                .clip(RoundedCornerShape(50))
                .background(Color.White.copy(alpha = 0.10f))
                .clickable { pureVideoCinemaMode = true }
                .padding(horizontal = 12.dp, vertical = 5.dp),
          ) {
            Text(
              text = "🎬 Tap here for Pure Video View",
              color = Color(0xFF67C8EB),
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold,
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          Text(
            text = "Thank You",
            style =
              TextStyle(
                brush = rainbowBrush,
                fontSize = 46.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                lineHeight = 52.sp,
              ),
            color = activeRainbowColor,
            modifier = Modifier.testTag("thank_you_rainbow_title"),
          )

          Spacer(modifier = Modifier.height(14.dp))

          Text(
            text = "Your support keeps Pro Notes fast, independent, and built with care for every note-taker.",
            color = Color.White.copy(alpha = 0.88f),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            lineHeight = 23.sp,
            modifier = Modifier.testTag("thank_you_subtitle"),
          )

          Spacer(modifier = Modifier.height(28.dp))

          Box(
            modifier =
              Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(rainbowBrush)
                .clickable(onClick = onClose)
                .testTag("thank_you_close_button"),
            contentAlignment = Alignment.Center,
          ) {
            Text(
              text = "Back to Pro Notes",
              color = Color.White,
              fontSize = 15.5.sp,
              fontWeight = FontWeight.Bold,
            )
          }
        }
      }
    } else {
      // Pure Cinema Video Mode floating control pill at the bottom
      Row(
        modifier =
          Modifier
            .align(Alignment.BottomCenter)
            .padding(bottom = 32.dp)
            .clip(RoundedCornerShape(50))
            .background(Color(0xCC0B1222))
            .border(1.dp, activeRainbowColor.copy(alpha = 0.7f), RoundedCornerShape(50))
            .padding(horizontal = 18.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        Text(
          text = "🎬 Pure Cinema Reel (Tap to show card)",
          color = Color.White,
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold,
          modifier = Modifier.clickable { pureVideoCinemaMode = false },
        )
        Box(
          modifier =
            Modifier
              .clip(RoundedCornerShape(50))
              .background(rainbowBrush)
              .clickable(onClick = onClose)
              .padding(horizontal = 12.dp, vertical = 5.dp),
        ) {
          Text(
            text = "Exit",
            color = Color.White,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
          )
        }
      }
    }
  }
}

private fun updateCenterCropTransform(
  textureView: TextureView,
  viewWidth: Int,
  viewHeight: Int,
  videoWidth: Int,
  videoHeight: Int,
) {
  if (viewWidth <= 0 || viewHeight <= 0 || videoWidth <= 0 || videoHeight <= 0) return
  val viewRatio = viewWidth.toFloat() / viewHeight.toFloat()
  val videoRatio = videoWidth.toFloat() / videoHeight.toFloat()
  val scaleX: Float
  val scaleY: Float
  if (videoRatio > viewRatio) {
    scaleX = videoRatio / viewRatio
    scaleY = 1f
  } else {
    scaleX = 1f
    scaleY = viewRatio / videoRatio
  }
  val matrix = Matrix()
  matrix.setScale(scaleX, scaleY, viewWidth / 2f, viewHeight / 2f)
  textureView.setTransform(matrix)
}
