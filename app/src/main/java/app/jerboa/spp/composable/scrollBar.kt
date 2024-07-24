package app.jerboa.spp.composable

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun Modifier.verticalScrollBar(
    scroll: ScrollState,
    animated: Boolean,
    color: Color = Color.White,
    width: Dp = 8.dp,
    fadeIn: Int = 100,
    fadeOut: Int = 600
): Modifier {

    val alpha by animateFloatAsState(
        targetValue = if (!animated) 1f else {if (scroll.isScrollInProgress) 1f else 0f},
        animationSpec = tween(durationMillis = if (scroll.isScrollInProgress) fadeIn else fadeOut),
        label = ""
    )
    return drawWithContent {
        drawContent()
        val barHeight = (this.size.height*0.25f)
        val barRange = (this.size.height-barHeight)/scroll.maxValue
        val barWidth = width.toPx()
        if (scroll.isScrollInProgress || alpha > 0.0f) {
            val position = scroll.value * barRange
            drawRoundRect(
                cornerRadius = CornerRadius(barWidth, barWidth),
                size = Size(barWidth, barHeight),
                topLeft = Offset(this.size.width - barWidth, position),
                color = color.copy(alpha = alpha)
            )
        }
    }
}