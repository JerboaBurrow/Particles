package app.jerboa.spp.composable

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxColors
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import app.jerboa.spp.viewmodel.AboutViewModel
import app.jerboa.spp.viewmodel.MAX_LOG_AR
import app.jerboa.spp.viewmodel.MAX_LOG_FADE
import app.jerboa.spp.viewmodel.MAX_LOG_MASS
import app.jerboa.spp.viewmodel.MAX_LOG_ORBIT
import app.jerboa.spp.viewmodel.MAX_LOG_SPEED
import app.jerboa.spp.viewmodel.MAX_LOG_SPIN
import app.jerboa.spp.viewmodel.MAX_PARTICLES
import app.jerboa.spp.viewmodel.MIN_LOG_AR
import app.jerboa.spp.viewmodel.MIN_LOG_FADE
import app.jerboa.spp.viewmodel.MIN_LOG_MASS
import app.jerboa.spp.viewmodel.MIN_LOG_ORBIT
import app.jerboa.spp.viewmodel.MIN_LOG_SPIN
import app.jerboa.spp.viewmodel.ToyMenuViewModel
import app.jerboa.spp.viewmodel.PARAM
import app.jerboa.spp.viewmodel.PARTICLES_SLIDER_DEFAULT
import app.jerboa.spp.viewmodel.SPPViewModel
import app.jerboa.spp.viewmodel.TOY
import kotlin.math.ceil
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.round

@Composable
fun label(
    text: String
)
{
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(
                color = Color.Black,
                shape = RoundedCornerShape(30.dp)
            )
            .padding(4.dp)
    ) {
        Text(
            text = text,
            fontSize = MaterialTheme.typography.body1.fontSize,
            color = Color.White,
        )
    }
}

@Composable
fun logSlider(
    v: Float,
    min: Float,
    max: Float,
    name: String,
    onChange: (Float) -> Unit,
    width75Percent: Double
) {

    var sliderValue by remember {
        mutableFloatStateOf(log10(v))
    }

    label(text = "$name " + "${round(10.0f.pow(sliderValue) * 100.0f) / 100.0f}")
    Slider(
        value = sliderValue,
        onValueChange = { sliderValue = it },
        onValueChangeFinished = {
            onChange(
                10.0f.pow(
                    sliderValue
                )
            )
        },
        valueRange = min..max,
        steps = 100,
        modifier = Modifier
            .width(width75Percent.dp * 0.75f)
            .background(color = Color(1, 1, 1, 1))
    )
}
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun toyMenu(
    toyMenuViewModel: ToyMenuViewModel,
    sppViewModel: SPPViewModel,
    aboutViewModel: AboutViewModel,
    displayingMenu: Boolean,
    width75Percent: Double,
    height10Percent: Double,
    menuItemHeight: Double,
    images: Map<String,Int>
) {

    val playSuccess: Boolean by sppViewModel.playSuccess.observeAsState(initial = false)
    val particleNumber: Float by toyMenuViewModel.particleNumber.observeAsState(initial = PARTICLES_SLIDER_DEFAULT)
    val speed: Float by toyMenuViewModel.speed.observeAsState(initial = 1.0f)
    val attraction: Float by toyMenuViewModel.attractorStrength.observeAsState(50000f)
    val repulsion: Float by toyMenuViewModel.repellorStrength.observeAsState(50000f)
    val orbit: Float by toyMenuViewModel.orbitStrength.observeAsState(0.5f)
    val spin: Float by toyMenuViewModel.spinStrength.observeAsState(1500f)
    val mass: Float by toyMenuViewModel.mass.observeAsState(0.1f)
    val fade: Float by toyMenuViewModel.fade.observeAsState(initial = 1.0f)
    val showToys: Boolean by toyMenuViewModel.showToys.observeAsState(initial = false)

    var particleSliderValue by remember {
        mutableFloatStateOf(log10(particleNumber))
    }

    var fadeSliderValue by remember {
        mutableFloatStateOf(log10(fade))
    }

    var showToysValue by remember {
        mutableStateOf(showToys)
    }

    var scroll = rememberScrollState()

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
            modifier = Modifier.fillMaxWidth(),
        ) {
            colourMapMenu(images, menuItemHeight) { toyMenuViewModel.onSelectColourMap(it) }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height((menuItemHeight).dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    IconButton(onClick = { sppViewModel.onRequestPlayServices() }) {
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
                    IconButton(onClick = { aboutViewModel.onDisplayingAboutChanged(true) }) {
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
                        IconButton(onClick = { toyMenuViewModel.onToyChanged(TOY.ATTRACTOR) }) {
                            Image(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .size(menuItemHeight.dp)
                                    .padding(2.dp),
                                painter = painterResource(id = images["attractor"]!!),
                                contentDescription = "Image"
                            )
                        }
                        IconButton(onClick = { toyMenuViewModel.onToyChanged(TOY.REPELLOR) }) {
                            Image(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .size(menuItemHeight.dp)
                                    .padding(2.dp),
                                painter = painterResource(id = images["repeller"]!!),
                                contentDescription = "Image"
                            )
                        }
                        IconButton(onClick = { toyMenuViewModel.onToyChanged(TOY.SPINNER) }) {
                            Image(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .size(menuItemHeight.dp)
                                    .padding(2.dp),
                                painter = painterResource(id = images["spinner"]!!),
                                contentDescription = "Image"
                            )
                        }
                        IconButton(onClick = { toyMenuViewModel.onToyChanged(TOY.FREEZER) }) {
                            Image(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .size(menuItemHeight.dp)
                                    .padding(2.dp),
                                painter = painterResource(id = images["freezer"]!!),
                                contentDescription = "Image"
                            )
                        }
                        IconButton(onClick = { toyMenuViewModel.onToyChanged(TOY.ORBITER) }) {
                            Image(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .size(menuItemHeight.dp)
                                    .padding(2.dp),
                                painter = painterResource(id = images["orbiter"]!!),
                                contentDescription = "Image"
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.size(8.dp))
            Column (
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .width(width75Percent.dp * 0.75f)
                    .verticalScroll(scroll)
                    .verticalScrollBar(scroll, false)
            ) {
                label(text = "Particles " + ceil(particleNumber * MAX_PARTICLES).toInt())
                Slider(
                    value = particleSliderValue,
                    onValueChange = { particleSliderValue = it },
                    onValueChangeFinished = {
                        toyMenuViewModel.onParameterChanged(
                            Pair(
                                10.0f.pow(
                                    particleSliderValue
                                ),
                                PARAM.PARTICLES
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
                logSlider(speed, -3.0f, MAX_LOG_SPEED, "Speed", {toyMenuViewModel.onParameterChanged(Pair(it, PARAM.SPEED))}, width75Percent)
                Spacer(modifier = Modifier.size(8.dp))
                logSlider(
                    attraction,
                    MIN_LOG_AR,
                    MAX_LOG_AR,
                    "Attraction",
                    {toyMenuViewModel.onParameterChanged(Pair(it, PARAM.ATTRACTION))},
                    width75Percent
                )
                Spacer(modifier = Modifier.size(8.dp))
                logSlider(
                    repulsion,
                    MIN_LOG_AR,
                    MAX_LOG_AR,
                    "Repulsion",
                    {toyMenuViewModel.onParameterChanged(Pair(it, PARAM.REPULSION))},
                    width75Percent
                )
                Spacer(modifier = Modifier.size(8.dp))
                logSlider(orbit, MIN_LOG_ORBIT, MAX_LOG_ORBIT, "Orbit", {toyMenuViewModel.onParameterChanged(Pair(it, PARAM.ORBIT))}, width75Percent)
                Spacer(modifier = Modifier.size(8.dp))
                logSlider(spin, MIN_LOG_SPIN, MAX_LOG_SPIN, "Spin", {toyMenuViewModel.onParameterChanged(Pair(it, PARAM.SPIN))}, width75Percent)
                Spacer(modifier = Modifier.size(8.dp))
                logSlider(mass, MIN_LOG_MASS, MAX_LOG_MASS, "Mass", {toyMenuViewModel.onParameterChanged(Pair(it, PARAM.MASS))}, width75Percent)
                Spacer(modifier = Modifier.size(8.dp))
                label(text = "Tracing " + "${round(fadeSliderValue * 100.0f) / 100.0f}")
                Slider(
                    value = fadeSliderValue,
                    onValueChange = { fadeSliderValue = it },
                    onValueChangeFinished = {
                        toyMenuViewModel.onParameterChanged(
                            Pair(
                                10.0f.pow((1.0f - fadeSliderValue) * (MAX_LOG_FADE - MIN_LOG_FADE) + MIN_LOG_FADE),
                                PARAM.FADE
                            )
                        )
                    },
                    valueRange = 0.0f..1.0f,
                    steps = 100,
                    modifier = Modifier
                        .width(width75Percent.dp * 0.75f)
                        .background(color = Color(1, 1, 1, 1))
                )
                Spacer(modifier = Modifier.size(8.dp))
                label(text = "Show Toys")
                Checkbox(
                    checked = showToysValue,
                    onCheckedChange = { showToysValue = it; toyMenuViewModel.onShowToysChanged(it) },
                    colors = checkBoxColors()
                )
            }
        }
    }
}

@Composable
fun checkBoxColors(): CheckboxColors {
    return CheckboxDefaults.colors(
        checkedColor = Color.White,
        uncheckedColor = Color(1.0f,1.0f,1.0f,0.5f)
    )
}