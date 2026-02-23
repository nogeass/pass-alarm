package com.nogeass.passalarm.presentation.designsystem

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.shadow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

/**
 * Tab enum for the two-panel content layout.
 */
enum class ContentTab(val label: String) {
    LIST("一覧"),
    SKIP("スキップ"),
}

/**
 * Floating action button that toggles between LIST and SKIP modes.
 *
 * The icon shows what the user will switch TO:
 * - In LIST mode, shows a card-stack icon (ViewAgenda) to indicate switching to SKIP.
 * - In SKIP mode, shows a list icon (ViewList) to indicate switching to LIST.
 *
 * Uses PassColors.brand as the FAB background with a white icon, circular shape,
 * and a rotation animation on click.
 *
 * @param selectedTab  The currently active tab.
 * @param onTabSelected Called when the user taps the FAB, with the new tab.
 * @param modifier     Outer modifier.
 */
@Composable
fun ModeToggleFab(
    selectedTab: ContentTab,
    onTabSelected: (ContentTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val view = rememberHapticView()

    var targetRotation by remember { mutableFloatStateOf(0f) }

    val rotation by animateFloatAsState(
        targetValue = targetRotation,
        animationSpec = spring(
            dampingRatio = 0.6f,
            stiffness = 300f,
        ),
        label = "fab_rotation",
    )

    val fabColor = if (selectedTab == ContentTab.LIST) PassColors.fabList else PassColors.fabSkip

    FloatingActionButton(
        onClick = {
            PassHaptics.medium(view)
            targetRotation += 360f
            val next = if (selectedTab == ContentTab.LIST) ContentTab.SKIP else ContentTab.LIST
            onTabSelected(next)
        },
        modifier = modifier
            .size(84.dp)
            .shadow(elevation = 8.dp, shape = CircleShape)
            .graphicsLayer { rotationZ = rotation },
        shape = CircleShape,
        containerColor = fabColor,
        contentColor = Color.White,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            hoveredElevation = 0.dp,
            focusedElevation = 0.dp,
        ),
    ) {
        Icon(
            imageVector = if (selectedTab == ContentTab.LIST) {
                Icons.Filled.Alarm
            } else {
                Icons.Filled.DateRange
            },
            contentDescription = if (selectedTab == ContentTab.LIST) {
                "スキップモードへ切替"
            } else {
                "一覧モードへ切替"
            },
            modifier = Modifier.size(32.dp),
        )
    }
}
