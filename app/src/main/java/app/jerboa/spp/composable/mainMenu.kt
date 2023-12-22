package app.jerboa.spp.composable

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import app.jerboa.spp.ViewModel.MAX_LOG_SPEED
import app.jerboa.spp.ViewModel.MAX_PARTICLES
import app.jerboa.spp.ViewModel.TOY
import kotlin.math.ceil
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.roundToLong

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun menu(
    displayingMenu: Boolean,
    playSuccess: Boolean,
    particleNumber: Float,
    speed: Float,
    width75Percent: Double,
    height10Percent: Double,
    menuItemHeight: Double,
    images: Map<String,Int>,
    info: AppInfo,
    onDisplayingAboutChanged: (Boolean) -> Unit,
    onAttractorChanged: (TOY) -> Unit,
    onRequestPlayServices: () -> Unit,
    onParticleNumberChanged: (Float) -> Unit,
    onSelectColourMap: (COLOUR_MAP) -> Unit,
    onSpeedChanged: (Float) -> Unit
) {
    var particleSliderValue by remember {
        mutableFloatStateOf(log10(particleNumber))
    }

    var speedSliderValue by remember {
        mutableFloatStateOf(log10(speed))
    }

    AnimatedVisibility(
        visible = displayingMenu,
        enter = slideInVertically(
            // Enters by sliding down from offset -fullHeight to 0.
            initialOffsetY = { fullHeight -> -fullHeight }
        ),
        exit = slideOutVertically(
            // Exits by sliding up from offset 0 to -fullHeight.
            targetOffsetY = { fullHeight -> fullHeight }
        )
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            colourMapMenu(images,menuItemHeight,onSelectColourMap)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height((menuItemHeight).dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().fillMaxHeight(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    IconButton(onClick = { onRequestPlayServices() }) {
                        Image(
                            modifier = Modifier
                                .fillMaxHeight()
                                .size(menuItemHeight.dp)
                                .alpha(
                                    if (playSuccess) {
                                        0.66f
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
            Box(
                Modifier
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
                        modifier = Modifier.width(width75Percent.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = { onAttractorChanged(TOY.ATTRACTOR) }) {
                            Image(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .size(menuItemHeight.dp)
                                    .padding(2.dp),
                                painter = painterResource(id = images["attractor"]!!),
                                contentDescription = "Image"
                            )
                        }
                        IconButton(onClick = { onAttractorChanged(TOY.REPELLOR) }) {
                            Image(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .size(menuItemHeight.dp)
                                    .padding(2.dp),
                                painter = painterResource(id = images["repeller"]!!),
                                contentDescription = "Image"
                            )
                        }
                        IconButton(onClick = { onAttractorChanged(TOY.SPINNER) }) {
                            Image(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .size(menuItemHeight.dp)
                                    .padding(2.dp),
                                painter = painterResource(id = images["spinner"]!!),
                                contentDescription = "Image"
                            )
                        }
                        IconButton(onClick = { onAttractorChanged(TOY.FREEZER) }) {
                            Image(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .size(menuItemHeight.dp)
                                    .padding(2.dp),
                                painter = painterResource(id = images["freezer"]!!),
                                contentDescription = "Image"
                            )
                        }
                        IconButton(onClick = { onDisplayingAboutChanged(true) }) {
                            Image(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .size(menuItemHeight.dp)
                                    .padding(2.dp),
                                painter = painterResource(id = images["about"]!!),
                                contentDescription = "Image"
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
                modifier = Modifier
                    .width(width75Percent.dp * 0.75f)
                    .background(color = Color(1, 1, 1, 1))
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = "Speed " + "${round(10.0f.pow(speedSliderValue)*100.0f)/100.0f}"
                    .toString(),
                fontSize = MaterialTheme.typography.body1.fontSize,
                color = Color.White
            )
            Slider(
                value = speedSliderValue,
                onValueChange = { speedSliderValue = it },
                onValueChangeFinished = {
                    onSpeedChanged(
                        10.0f.pow(
                            speedSliderValue
                        )
                    )
                },
                valueRange = -MAX_LOG_SPEED..MAX_LOG_SPEED,
                steps = 100,
                modifier = Modifier
                    .width(width75Percent.dp * 0.75f)
                    .background(color = Color(1, 1, 1, 1))
            )
        }
    }
}