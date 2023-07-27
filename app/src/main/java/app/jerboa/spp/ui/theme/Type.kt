package app.jerboa.spp.ui.theme

import androidx.compose.material.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import app.jerboa.spp.R
import kotlin.math.floor

val odFont = FontFamily(
    Font(R.font.open_dyslexic_regular)
)

// Set of Material typography styles to start with
val Typography = Typography(
    body1 = TextStyle(
        fontFamily = odFont,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp
    ),
    caption = TextStyle(
        fontFamily = odFont,
        fontWeight = FontWeight.Normal,
        fontSize = 8.sp
    ),
    overline = TextStyle(
            fontFamily = odFont,
        fontWeight = FontWeight.Normal,
        fontSize = 6.sp
)
    /* Other default text styles to override
    button = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W500,
        fontSize = 14.sp
    ),
    caption = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp
    )
    */
)