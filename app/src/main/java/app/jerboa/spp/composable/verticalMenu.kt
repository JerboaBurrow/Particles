package app.jerboa.spp.composable

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.max

@Composable
fun verticalMenu(
    modifier: Modifier,
    contentPadding: Dp = 0.dp,
    offset: Pair<Dp, Dp> = Pair(0.dp, 0.dp),
    content: @Composable () -> Unit
) {
    val configuration = LocalConfiguration.current

    Layout(content = content, modifier = modifier) { children, constraints ->
        val screenWidth = configuration.screenWidthDp.dp.toPx()
        val screenHeight = configuration.screenHeightDp.dp.toPx()
        val pad = contentPadding.toPx()
        val offsetPx = Pair(offset.first.toPx().toInt(), offset.second.toPx().toInt())
        val items = children.map { it.measure(constraints) }

        var maxHeight = 0
        items.map { maxHeight = max(maxHeight, it.height) }

        layout(screenWidth.toInt(), screenWidth.toInt()) {
            var y = 0
            for (i in items.indices) {
                items[i].placeRelative(x=pad.toInt()+offsetPx.first, y=(screenHeight-y-maxHeight-offsetPx.second-pad.toInt()).toInt())
                y += items[i].height
            }
        }
    }
}