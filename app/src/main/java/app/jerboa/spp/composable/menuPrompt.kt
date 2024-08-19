package app.jerboa.spp.composable

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import app.jerboa.spp.AppInfo
import app.jerboa.spp.viewmodel.AboutViewModel
import app.jerboa.spp.viewmodel.MenuPromptViewModel
import app.jerboa.spp.viewmodel.SPPViewModel
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@Composable
fun menuPrompt(
    menuPromptViewModel: MenuPromptViewModel,
    aboutViewModel: AboutViewModel,
    sppViewModel: SPPViewModel,
    images: Map<String,Int>,
    menuItemHeight: Double,
    info: AppInfo
){

    val displayingMenu: Boolean by menuPromptViewModel.displayingMenu.observeAsState(initial = false)
    val displayingAbout: Boolean by aboutViewModel.displayingAbout.observeAsState(initial = false)
    val displayingSliders: Boolean by menuPromptViewModel.displayingToyMenu.observeAsState(initial = false)

    val displayingSound: Boolean by menuPromptViewModel.displayingSound.observeAsState(initial = false)
    val paused: Boolean by menuPromptViewModel.paused.observeAsState(initial = false)
    val playSuccess: Boolean by sppViewModel.playSuccess.observeAsState(initial = false)

    val fadePromptAlpha: Float by animateFloatAsState(
        targetValue = if (!displayingMenu) 0.33f else 1.0f,
        animationSpec = tween(
            durationMillis = 500,
            easing = LinearEasing,
        ), label = "alpha for fading the prompt"
    )

    val xmax = info.widthDp*info.density-1.7f*menuItemHeight.toFloat()*info.density
    val ymax = info.heightDp*info.density-1.75f*menuItemHeight.toFloat()*info.density
    var position by remember { mutableStateOf(Offset(0.0f,ymax)) }

    Box(modifier = Modifier
        .fillMaxHeight()
        .fillMaxWidth()
        .padding(16.dp)) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .graphicsLayer {
                    if (displayingAbout || displayingSliders) {
                        if (abs(position.x-xmax) < abs(position.x)) {
                            translationX = xmax
                        }
                        else {
                            translationX = 0.0f
                        }
                        translationY = ymax
                    } else {
                        translationX = position.x
                        translationY = position.y
                    }
                }
        ) {
            AnimatedVisibility(
                visible = displayingMenu,
                enter = fadeIn(tween(0)),
                exit = fadeOut(tween(0))
            ) {
                verticalMenu(
                    modifier = Modifier,
                    offset = Pair(0.dp, 0.dp),
                    contentPadding = 16.dp,
                    headSpacePx = if (displayingAbout || displayingSliders) { ymax } else { position.y }
                ) {
                    Image(
                        painter = painterResource(id = images["toyMenu"]!!),
                        contentDescription = "Toy menu and sliders",
                        modifier = Modifier
                            .size(menuItemHeight.dp)
                            .clickable(
                                onClick = {
                                    menuPromptViewModel.onDisplayingToyMenuChanged(true)
                                    menuPromptViewModel.onDisplayingMusicChanged(false)
                                    aboutViewModel.onDisplayingAboutChanged(false)
                                }
                            )
                    )
                    if (displayingSound) {
                        musicMenu(
                            menuPromptViewModel,
                            menuItemHeight,
                            images,
                            left = position.x > xmax/2.0f
                        )
                    }
                    else {
                        Image(
                            painter = painterResource(id = images["music"]!!),
                            contentDescription = "Music menu",
                            modifier = Modifier
                                .size(menuItemHeight.dp)
                                .clickable(
                                    onClick = {
                                        menuPromptViewModel.onDisplayingToyMenuChanged(false)
                                        menuPromptViewModel.onDisplayingMusicChanged(true)
                                        aboutViewModel.onDisplayingAboutChanged(false)
                                    }
                                )
                        )
                    }
                    Image(
                        painter = painterResource(id = images["clear"]!!),
                        contentDescription = "clear all the toys",
                        modifier = Modifier
                            .size(menuItemHeight.dp)
                            .clickable(
                                onClick = {
                                    menuPromptViewModel.onClear(true)
                                }
                            )
                    )
                    if (!paused) {
                        Image(
                            painter = painterResource(id = images["pause"]!!),
                            contentDescription = "pause the particles",
                            modifier = Modifier
                                .size(menuItemHeight.dp)
                                .clickable(
                                    onClick = { menuPromptViewModel.onPause(); }
                                )
                        )
                    } else {
                        Image(
                            painter = painterResource(id = images["play"]!!),
                            contentDescription = "unpause the particles",
                            modifier = Modifier
                                .size(menuItemHeight.dp)
                                .clickable(
                                    onClick = { menuPromptViewModel.onPause(); }
                                )
                        )
                    }
                    Image(
                        painter = painterResource(id = images["about"]!!),
                        contentDescription = "About and news",
                        modifier = Modifier
                            .size(menuItemHeight.dp)
                            .clickable(
                                onClick = {
                                    menuPromptViewModel.onDisplayingToyMenuChanged(false)
                                    menuPromptViewModel.onDisplayingMusicChanged(false)
                                    aboutViewModel.onDisplayingAboutChanged(true)
                                }
                            )
                    )
                    IconButton(onClick = { sppViewModel.onRequestPlayServices() }) {
                        Image(
                            modifier = Modifier
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
        }
        Box(
            modifier = Modifier
                .size(menuItemHeight.dp)
                .graphicsLayer {
                    if (displayingAbout || displayingSliders) {
                        if (abs(position.x-xmax) < abs(position.x)) {
                            translationX = xmax
                        }
                        else {
                            translationX = 0.0f
                        }
                        translationY = ymax
                    } else {
                        translationX = position.x
                        translationY = position.y
                    }
                }
                .conditional(!(displayingAbout || displayingSliders)){ Modifier.pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                            change.consume()
                            var x = max(0.0f, position.x+dragAmount.x)
                            x = min(x, xmax)
                            var y = max(0.0f, position.y+dragAmount.y)
                            y = min(y, ymax)
                            position = Offset(x, y)
                        }
                }}
        ) {
            AnimatedVisibility(
                visible = !displayingMenu,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Image(
                    painter = painterResource(id = images["burger"]!!),
                    contentDescription = "Open the menu",
                    modifier = Modifier
                        .size(menuItemHeight.dp)
                        .clickable(
                            onClick = { menuPromptViewModel.onDisplayingMenuChanged(true); }
                        )
                        .alpha(fadePromptAlpha)
                )
            }
            AnimatedVisibility(
                visible = displayingMenu,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Image(
                    painter = painterResource(id = images["dismiss"]!!),
                    contentDescription = "Close the menu",
                    modifier = Modifier
                        .size(menuItemHeight.dp)
                        .clickable(
                            onClick = {
                                if (!(displayingAbout || displayingSliders)) {
                                    menuPromptViewModel.onDisplayingMenuChanged(false)
                                }
                                aboutViewModel.onDisplayingAboutChanged(false)
                                menuPromptViewModel.onDisplayingToyMenuChanged(false)
                                menuPromptViewModel.onDisplayingMusicChanged(false)
                            }
                        )
                )
            }
        }
    }
}