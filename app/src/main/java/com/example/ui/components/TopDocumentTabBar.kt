package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.EditOff
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.NotebookDocument
import com.example.model.ScreenMode
import com.example.ui.theme.ActiveTabBg
import com.example.ui.theme.DangerRed
import com.example.ui.theme.InactiveTabBg
import com.example.ui.theme.TextPrimaryWhite
import com.example.ui.theme.TextSecondaryGray
import com.example.ui.theme.TopTabBarBg

@Composable
fun TopDocumentTabBar(
  screenMode: ScreenMode,
  openTabs: List<NotebookDocument>,
  activeTabId: String,
  isReadOnlyMode: Boolean,
  onHomeClick: () -> Unit,
  onTabClick: (String) -> Unit,
  onTabClose: (String) -> Unit,
  onNewTabClick: () -> Unit,
  onToggleReadOnly: () -> Unit,
  onTripleTapShowcase: () -> Unit = {},
  modifier: Modifier = Modifier,
) {
  var tapCount by androidx.compose.runtime.remember { androidx.compose.runtime.mutableIntStateOf(0) }
  var lastTapMs by androidx.compose.runtime.remember { androidx.compose.runtime.mutableLongStateOf(0L) }

  fun registerPotentialTripleTap() {
    val now = System.currentTimeMillis()
    if (now - lastTapMs <= 520L) {
      tapCount += 1
    } else {
      tapCount = 1
    }
    lastTapMs = now
    if (tapCount >= 3) {
      tapCount = 0
      onTripleTapShowcase()
    }
  }

  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .height(38.dp)
        .background(TopTabBarBg)
        .clickable(
          interactionSource = androidx.compose.runtime.remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
          indication = null,
        ) {
          registerPotentialTripleTap()
        }
        .padding(horizontal = 6.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    // Home Icon Button (Top-Left of all 5 screenshots)
    val isHomeActive = screenMode == ScreenMode.DOCUMENTS
    Box(
      modifier =
        Modifier
          .size(34.dp)
          .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
          .background(if (isHomeActive) Color(0xFF1E1F22) else Color.Transparent)
          .clickable {
            registerPotentialTripleTap()
            onHomeClick()
          }
          .testTag("top_home_tab_button"),
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        imageVector = Icons.Outlined.Home,
        contentDescription = "Documents Library",
        tint = if (isHomeActive) TextPrimaryWhite else TextSecondaryGray,
        modifier = Modifier.size(18.dp),
      )
    }

    Spacer(modifier = Modifier.width(4.dp))

    // Scrollable Document Tabs
    Row(
      modifier =
        Modifier
          .weight(1f)
          .fillMaxHeight()
          .horizontalScroll(rememberScrollState()),
      verticalAlignment = Alignment.Bottom,
      horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
      openTabs.forEach { doc ->
        val isTabSelected = screenMode == ScreenMode.EDITOR && doc.id == activeTabId
        val tabBg =
          when {
            isTabSelected -> ActiveTabBg
            screenMode == ScreenMode.DOCUMENTS && doc.id == activeTabId -> Color(0xFF1A1B1E)
            else -> InactiveTabBg
          }

        Row(
          modifier =
            Modifier
              .widthIn(min = 140.dp, max = 240.dp)
              .height(34.dp)
              .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
              .background(tabBg)
              .clickable { onTabClick(doc.id) }
              .padding(horizontal = 12.dp)
              .testTag("doc_tab_${doc.id}"),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = doc.title,
            color = if (isTabSelected) TextPrimaryWhite else TextSecondaryGray,
            fontSize = 12.sp,
            fontWeight = if (isTabSelected) FontWeight.Medium else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          Spacer(modifier = Modifier.width(4.dp))
          Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = null,
            tint = if (isTabSelected) TextPrimaryWhite.copy(alpha = 0.8f) else TextSecondaryGray,
            modifier = Modifier.size(14.dp),
          )
          Spacer(modifier = Modifier.weight(1f))
          Spacer(modifier = Modifier.width(8.dp))
          Box(
            modifier =
              Modifier
                .size(20.dp)
                .clip(RoundedCornerShape(4.dp))
                .clickable { onTabClose(doc.id) }
                .testTag("close_tab_${doc.id}"),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close tab",
              tint = TextSecondaryGray,
              modifier = Modifier.size(13.dp),
            )
          }
        }
      }
    }

    // Right-side "+" New Tab & Read-only Stylus icon
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      Box(
        modifier =
          Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onNewTabClick)
            .testTag("new_tab_plus_button"),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = Icons.Default.Add,
          contentDescription = "Open new notebook tab",
          tint = TextSecondaryGray,
          modifier = Modifier.size(17.dp),
        )
      }

      Box(
        modifier =
          Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onToggleReadOnly)
            .testTag("toggle_readonly_button"),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = Icons.Outlined.EditOff,
          contentDescription = "Toggle read-only mode",
          tint = if (isReadOnlyMode) DangerRed else TextSecondaryGray,
          modifier = Modifier.size(16.dp),
        )
      }
    }
  }
}
