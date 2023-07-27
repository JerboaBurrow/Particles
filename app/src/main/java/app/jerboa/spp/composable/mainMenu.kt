package app.jerboa.spp.composable

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import app.jerboa.spp.AppInfo
import app.jerboa.spp.ViewModel.COLOUR_MAP
import app.jerboa.spp.ViewModel.MAX_PARTICLES
import app.jerboa.spp.ViewModel.TOY
import kotlin.math.ceil
import kotlin.math.log10
import kotlin.math.pow

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun menu(
    displayingMenu: Boolean,
    particleNumber: Float,
    width75Percent: Double,
    height10Percent: Double,
    menuItemHeight: Double,
    images: Map<String,Int>,
    info: AppInfo,
    onDisplayingAboutChanged: (Boolean) -> Unit,
    onAttractorChanged: (TOY) -> Unit,
    onRequestPlayServices: () -> Unit,
    onParticleNumberChanged: (Float) -> Unit,
    onSelectColourMap: (COLOUR_MAP) -> Unit
) {
    var particleSliderValue by remember {
        mutableStateOf(log10(particleNumber))
    }

    AnimatedVisibility(
        visible = displayingMenu,
        enter = slideInVertically(
            // Enters by sliding down from offset -fullHeight to 0.
            initialOffsetY = { fullHeight -> -fullHeight }
        ),
        exit = slideOutVertically(
            // Exits by sliding up from offset 0 to -fullHeight.
            targetOffsetY = { fullHeight -> -fullHeight }
        )
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            colourMapMenu(images,menuItemHeight,onSelectColourMap)
            Box(
                androidx.compose.ui.Modifier
                    .width(width75Percent.dp)
                    .height(height10Percent.dp)
                    .background(
                        color = MaterialTheme.colors.secondary,
                        shape = RoundedCornerShape(75)
                    )
                    .animateContentSize()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                {
                    Row(
                        modifier = androidx.compose.ui.Modifier.width(width75Percent.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = { onAttractorChanged(TOY.ATTRACTOR) }) {
                            Image(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .size(menuItemHeight.dp)
                                    .padding(start = 10.dp),
                                painter = painterResource(id = images["attractor"]!!),
                                contentDescription = "Image"
                            )
                        }
                        IconButton(onClick = { onAttractorChanged(TOY.REPELLOR) }) {
                            Image(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .size(menuItemHeight.dp)
                                    .padding(end = 10.dp),
                                painter = painterResource(id = images["repeller"]!!),
                                contentDescription = "Image"
                            )
                        }
                        IconButton(onClick = { onAttractorChanged(TOY.SPINNER) }) {
                            Image(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .size(menuItemHeight.dp)
                                    .padding(end = 10.dp),
                                painter = painterResource(id = images["spinner"]!!),
                                contentDescription = "Image"
                            )
                        }
                        IconButton(onClick = { onDisplayingAboutChanged(true) }) {
                            Image(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .size(menuItemHeight.dp),
                                painter = painterResource(id = images["about"]!!),
                                contentDescription = "Image"
                            )
                        }
                        IconButton(onClick = { onRequestPlayServices() }) {
                            Image(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .size(menuItemHeight.dp)
                                    .alpha(
                                        if (!info.playGamesServices) {
                                            0.33f
                                        } else {
                                            1f
                                        }
                                    ),
                                painter = painterResource(id = images["play-controller"]!!),
                                contentDescription = "play"
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = "Particles " + ceil(particleNumber * MAX_PARTICLES).toInt()
                    .toString(),
                fontSize = MaterialTheme.typography.body1.fontSize,
                color = Color.White
            )
            Slider(
                value = particleSliderValue,
                onValueChange = { particleSliderValue = it },
                onValueChangeFinished = {
                    onParticleNumberChanged(
                        10.0f.pow(
                            particleSliderValue
                        )
                    )
                },
                valueRange = -3.0f..0.0f,
                steps = 100,
                modifier = androidx.compose.ui.Modifier
                    .width(width75Percent.dp * 0.75f)
                    .background(color = Color(1, 1, 1, 1))
            )
        }
    }
}