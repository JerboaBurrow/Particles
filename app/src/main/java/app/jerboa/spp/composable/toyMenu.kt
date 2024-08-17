package app.jerboa.spp.composable

import android.util.Log
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
import app.jerboa.spp.viewmodel.MAX_SCALE
import app.jerboa.spp.viewmodel.MIN_LOG_AR
import app.jerboa.spp.viewmodel.MIN_LOG_FADE
import app.jerboa.spp.viewmodel.MIN_LOG_MASS
import app.jerboa.spp.viewmodel.MIN_LOG_ORBIT
import app.jerboa.spp.viewmodel.MIN_LOG_SPIN
import app.jerboa.spp.viewmodel.MIN_SCALE
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
fun slider(
    v: Float,
    min: Float,
    max: Float,
    name: String,
    onChange: (Float) -> Unit,
    width75Percent: Double,
    label: (Float) -> String = {v: Float -> "${round(10.0f.pow(v) * 100.0f) / 100.0f}"},
    postValue: (Float) -> Float = {v: Float -> 10.0f.pow(v)},
    steps: Int = 100
) {

    var sliderValue by remember {
        mutableFloatStateOf(v)
    }

    label(text = "$name " + label(sliderValue))
    Slider(
        value = sliderValue,
        onValueChange = { sliderValue = it },
        onValueChangeFinished = {
            onChange(postValue(sliderValue))
        },
        valueRange = min..max,
        steps = steps,
        modifier = Modifier
            .width(width75Percent.dp * 0.75f)
            .background(color = Color(1, 1, 1, 1))
    )
}
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun toyMenu(
    toyMenuViewModel: ToyMenuViewModel,
    displayingMenu: Boolean,
    width75Percent: Double,
    height10Percent: Double,
    menuItemHeight: Double,
    images: Map<String,Int>
) {

    val particleNumber: Float by toyMenuViewModel.particleNumber.observeAsState(initial = PARTICLES_SLIDER_DEFAULT)
    val speed: Float by toyMenuViewModel.speed.observeAsState(initial = 1.0f)
    val attraction: Float by toyMenuViewModel.attractorStrength.observeAsState(50000f)
    val repulsion: Float by toyMenuViewModel.repellorStrength.observeAsState(50000f)
    val orbit: Float by toyMenuViewModel.orbitStrength.observeAsState(0.5f)
    val spin: Float by toyMenuViewModel.spinStrength.observeAsState(1500f)
    val mass: Float by toyMenuViewModel.mass.observeAsState(0.1f)
    val fade: Float by toyMenuViewModel.fade.observeAsState(initial = 1.0f)
    val scale: Float by toyMenuViewModel.scale.observeAsState(initial = 3.0f)
    val showToys: Boolean by toyMenuViewModel.showToys.observeAsState(initial = false)

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
                slider(
                    log10(particleNumber),
                    -6.0f,
                    0.0f,
                    "Particles",
                    {toyMenuViewModel.onParameterChanged(Pair(it, PARAM.PARTICLES))},
                    width75Percent,
                    label = {v: Float -> "${ceil(10.0f.pow(v) * MAX_PARTICLES).toInt()}"}
                )
                Spacer(modifier = Modifier.size(8.dp))
                slider(
                    scale,
                    MIN_SCALE,
                    MAX_SCALE,
                    "Size",
                    {toyMenuViewModel.onParameterChanged(Pair(it, PARAM.SCALE))},
                    width75Percent,
                    label = {v: Float -> "$v"},
                    postValue = {v: Float -> v}
                )
                Spacer(modifier = Modifier.size(8.dp))
                slider(log10(speed), -3.0f, MAX_LOG_SPEED, "Speed", {toyMenuViewModel.onParameterChanged(Pair(it, PARAM.SPEED))}, width75Percent)
                Spacer(modifier = Modifier.size(8.dp))
                slider(
                    log10(attraction),
                    MIN_LOG_AR,
                    MAX_LOG_AR,
                    "Attraction",
                    {toyMenuViewModel.onParameterChanged(Pair(it, PARAM.ATTRACTION))},
                    width75Percent
                )
                Spacer(modifier = Modifier.size(8.dp))
                slider(
                    log10(repulsion),
                    MIN_LOG_AR,
                    MAX_LOG_AR,
                    "Repulsion",
                    {toyMenuViewModel.onParameterChanged(Pair(it, PARAM.REPULSION))},
                    width75Percent
                )
                Spacer(modifier = Modifier.size(8.dp))
                slider(log10(orbit), MIN_LOG_ORBIT, MAX_LOG_ORBIT, "Orbit", {toyMenuViewModel.onParameterChanged(Pair(it, PARAM.ORBIT))}, width75Percent)
                Spacer(modifier = Modifier.size(8.dp))
                slider(log10(spin), MIN_LOG_SPIN, MAX_LOG_SPIN, "Spin", {toyMenuViewModel.onParameterChanged(Pair(it, PARAM.SPIN))}, width75Percent)
                Spacer(modifier = Modifier.size(8.dp))
                slider(log10(mass), MIN_LOG_MASS, MAX_LOG_MASS, "Mass", {toyMenuViewModel.onParameterChanged(Pair(it, PARAM.MASS))}, width75Percent)
                Spacer(modifier = Modifier.size(8.dp))
                slider(
                    fade,
                    10.0f.pow(MIN_LOG_FADE),
                    10.0f.pow(MAX_LOG_FADE),
                    "Tracing",
                    {toyMenuViewModel.onParameterChanged(Pair(it, PARAM.FADE))},
                    width75Percent,
                    postValue = {v: Float -> v},
                    label = {v: Float -> "${(v-10.0f.pow(MIN_LOG_FADE))/(10.0f.pow(MAX_LOG_FADE)-10.0f.pow(MIN_LOG_FADE))}"}
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