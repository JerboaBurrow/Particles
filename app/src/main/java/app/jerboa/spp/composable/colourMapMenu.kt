package app.jerboa.spp.composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import app.jerboa.spp.viewmodel.COLOUR_MAP

@Composable
fun colourMapMenu(
    images: Map<String,Int>,
    menuItemHeight: Double,
    onSelectColourMap: (COLOUR_MAP) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height((menuItemHeight*2.0).dp)
            .padding((menuItemHeight * 0.05).dp)
    ) {
        LazyRow(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            item {
                Image(
                    painter = painterResource(id = images["rainbow1"]!!),
                    contentDescription = "rainbow 1",
                    modifier = Modifier.fillMaxHeight()
                        .size(menuItemHeight.dp)
                        .clickable { onSelectColourMap(COLOUR_MAP.R1) }
                )
            }
            item {
                Image(
                    painter = painterResource(id = images["rainbow2"]!!),
                    contentDescription = "rainbow 2",
                    modifier = Modifier.fillMaxHeight()
                        .size(menuItemHeight.dp)
                        .clickable { onSelectColourMap(COLOUR_MAP.R2) }
                )
            }
            item {
                Image(
                    painter = painterResource(id = images["ace"]!!),
                    contentDescription = "ace",
                    modifier = Modifier.fillMaxHeight()
                        .size(menuItemHeight.dp)
                        .clickable { onSelectColourMap(COLOUR_MAP.ACE) }
                )
            }
            item{
                Image(
                    painter = painterResource(id = images["c3"]!!),
                    contentDescription = "c3",
                    modifier = Modifier.fillMaxHeight()
                        .size(menuItemHeight.dp)
                        .clickable { onSelectColourMap(COLOUR_MAP.C3) }
                )
            }
            item {
                Image(
                    painter = painterResource(id = images["trans"]!!),
                    contentDescription = "trans",
                    modifier = Modifier.fillMaxHeight()
                        .size(menuItemHeight.dp)
                        .clickable { onSelectColourMap(COLOUR_MAP.TRANS) }
                )
            }
            item {
                Image(
                    painter = painterResource(id = images["pride"]!!),
                    contentDescription = "pride",
                    modifier = Modifier.fillMaxHeight()
                        .size(menuItemHeight.dp)
                        .clickable { onSelectColourMap(COLOUR_MAP.PRIDE) }
                )
            }
            item{
                Image(
                    painter = painterResource(id = images["cb1"]!!),
                    contentDescription = "colour blind 1",
                    modifier = Modifier.fillMaxHeight()
                        .size(menuItemHeight.dp)
                        .clickable { onSelectColourMap(COLOUR_MAP.CB1) }
                )
            }
            item {
                Image(
                    painter = painterResource(id = images["cb2"]!!),
                    contentDescription = "colour blind 2",
                    modifier = Modifier.fillMaxHeight()
                        .size(menuItemHeight.dp)
                        .clickable { onSelectColourMap(COLOUR_MAP.CB2) }
                )
            }
        }
    }
}