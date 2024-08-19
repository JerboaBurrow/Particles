package app.jerboa.spp.composable

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.max

@Composable
fun horizontalMenu(
    modifier: Modifier,
    sideSpacePx: Float? = null,
    contentPadding: Dp = 0.dp,
    offset: Pair<Dp, Dp> = Pair(0.dp, 0.dp),
    content: @Composable () -> Unit,
) {
    val configuration = LocalConfiguration.current

    Layout(content = content, modifier = modifier) { children, constraints ->
        val screenWidth = configuration.screenWidthDp.dp.toPx()
        val pad = contentPadding.toPx()
        val offsetPx = Pair(offset.first.toPx().toInt(), offset.second.toPx().toInt())
        val items = children.map { it.measure(constraints) }

        var maxWidth = 0
        var width = 0
        items.map { maxWidth = max(maxWidth, it.width); width += it.width }
        val sign = if (sideSpacePx != null && sideSpacePx < width+(maxWidth+offsetPx.first+pad.toInt())) {
            1
        }
        else {
            -1
        }
        layout(screenWidth.toInt(), screenWidth.toInt()) {
            var x = 0
            for (i in items.indices) {
                items[i].placeRelative(x=x+sign*(maxWidth+offsetPx.first+pad.toInt()), y=pad.toInt()+offsetPx.second)
                x += sign*items[i].width
            }
        }
    }
}