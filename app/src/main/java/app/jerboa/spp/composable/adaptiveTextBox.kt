package app.jerboa.spp.composable

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp

fun Modifier.conditional(condition : Boolean, modifier : Modifier.() -> Modifier) : Modifier {
    return if (condition) {
        then(modifier(Modifier))
    } else {
        this
    }
}

@Composable
fun Modifier.simpleVerticalScrollbar(
    state: ScrollState,
    width: Dp = 8.dp
): Modifier {

    return drawWithContent {
        drawContent()

        val elementHeight = this.size.height / state.maxValue
        val scrollbarHeight = width.toPx()*3f
        val scrollbarOffsetY = state.value * elementHeight - scrollbarHeight/2f
        val outline = width.toPx()/8f

        drawRect(
            color = Color(0,0,0,255),
            topLeft = Offset(this.size.width - width.toPx()-outline/2f, scrollbarOffsetY-outline/2f),
            size = Size(width.toPx()+outline, scrollbarHeight+outline),
            alpha = 1f,
        )
        drawRect(
            color = Color(150,150,255,255),
            topLeft = Offset(this.size.width - width.toPx(), scrollbarOffsetY),
            size = Size(width.toPx(), scrollbarHeight),
            alpha = 1f,
        )
    }
}

@Composable
fun adaptiveTextBox(
    text: String,
    fontSize: TextUnit,
    maxLines: Int,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Center,
    colour: Color = Color.Black){
    val scroll = rememberScrollState(0)
    var isOverflowing by remember { mutableStateOf<Boolean?>(null) }
    val maybeScrollText = @Composable {
        Text(
            text = text,
            onTextLayout = { textLayoutResult: TextLayoutResult ->
                isOverflowing = textLayoutResult.lineCount > maxLines
            },
            modifier = modifier
                .drawWithContent {
                    if (isOverflowing != null) {
                        drawContent()
                    }
                }
                .conditional(isOverflowing == true) { verticalScroll(scroll) }
                .simpleVerticalScrollbar(
                    scroll, if (isOverflowing == true) {
                        (fontSize.value / 2f).dp
                    } else {
                        0f.dp
                    }
                ),
            fontSize = fontSize,
            color = colour,
            textAlign = textAlign
        )
    }
    maybeScrollText()
}