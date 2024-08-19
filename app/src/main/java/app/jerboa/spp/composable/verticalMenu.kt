package app.jerboa.spp.composable

import android.util.Log
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
    headSpacePx: Float? = null,
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
        var maxHeight = 0
        var height = 0
        items.map { maxHeight = max(maxHeight, it.height); height += it.height }
        val sign = if (headSpacePx != null && headSpacePx < height+(maxHeight+offsetPx.second+pad.toInt())) {
            1
        }
        else {
            -1
        }
        layout(screenWidth.toInt(), screenWidth.toInt()) {
            var y = 0
            for (i in items.indices) {
                items[i].placeRelative(x=pad.toInt()+offsetPx.first, y=(y+sign*(maxHeight+offsetPx.second+pad.toInt())).toInt())
                y += sign*items[i].height
            }
        }
    }
}