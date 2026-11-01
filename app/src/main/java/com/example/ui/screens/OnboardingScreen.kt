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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.R
import com.example.ui.theme.NewButtonBlue
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private data class TourSlide(
  val title: String,
  val body: String,
)

private val OnboardingTourSlides =
  listOf(
    TourSlide(
      title = "Fluid Stylus & Ink",
      body = "Write naturally with pressure-sensitive pen, pencil, and highlighter tools. Hold your stylus side button anytime to erase Effortlessly.",
    ),
    TourSlide(
      title = "Smart Circle to Select",
      body = "Draw a closed loop with your pen or pencil, lift, and briefly hold on the loop endpoint to transform it into a live Lasso selection.",
    ),
    TourSlide(
      title = "Color-Coded Folders & Search",
      body = "Organize notebooks and PDFs inside custom-colored folders and find any note or folder as you type.",
    ),
  )

@Composable
fun OnboardingScreen(
  onStartCreating: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  var videoFailed by remember { mutableStateOf(false) }
  var showTourOverlay by remember { mutableStateOf(false) }
  var tourStepIndex by remember { mutableIntStateOf(0) }

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

        Spacer(modifier = Modifier.height(28.dp))

        AnimatedVisibility(
          visible = showTourOverlay,
          enter = fadeIn(tween(220)),
          exit = fadeOut(tween(180)),
        ) {
          val slide = OnboardingTourSlides[tourStepIndex.coerceIn(0, OnboardingTourSlides.lastIndex)]
          Column(
            modifier =
              Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xD9131C2E))
                .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(18.dp))
                .padding(horizontal = 20.dp, vertical = 18.dp)
                .testTag("onboarding_tour_card"),
            horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            Text(
              text = slide.title,
              color = Color.White,
              fontSize = 16.sp,
              fontWeight = FontWeight.SemiBold,
              textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = slide.body,
              color = Color.White.copy(alpha = 0.80f),
              fontSize = 13.5.sp,
              lineHeight = 19.sp,
              textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(14.dp))
            Row(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              OnboardingTourSlides.indices.forEach { idx ->
                Box(
                  modifier =
                    Modifier
                      .size(if (idx == tourStepIndex) 8.dp else 6.dp)
                      .clip(CircleShape)
                      .background(
                        if (idx == tourStepIndex) NewButtonBlue else Color.White.copy(alpha = 0.32f)
                      ),
                )
              }
            }
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

          // Secondary button: "Take a tour"
          Box(
            modifier =
              Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White.copy(alpha = 0.12f))
                .border(1.dp, Color.White.copy(alpha = 0.24f), RoundedCornerShape(14.dp))
                .clickable {
                  if (!showTourOverlay) {
                    showTourOverlay = true
                    tourStepIndex = 0
                  } else if (tourStepIndex < OnboardingTourSlides.lastIndex) {
                    tourStepIndex += 1
                  } else {
                    onStartCreating()
                  }
                }
                .testTag("onboarding_take_a_tour_button"),
            contentAlignment = Alignment.Center,
          ) {
            Text(
              text =
                when {
                  !showTourOverlay -> "Take a tour"
                  tourStepIndex < OnboardingTourSlides.lastIndex -> "Next tip"
                  else -> "Done tour"
                },
              color = Color.White,
              fontSize = 15.sp,
              fontWeight = FontWeight.Medium,
            )
          }
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
