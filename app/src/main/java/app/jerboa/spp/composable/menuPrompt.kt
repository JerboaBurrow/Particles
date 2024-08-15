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
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import app.jerboa.spp.viewmodel.AboutViewModel
import app.jerboa.spp.viewmodel.MenuPromptViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun menuPrompt(
    menuPromptViewModel: MenuPromptViewModel,
    aboutViewModel: AboutViewModel,
    images: Map<String,Int>,
    menuItemHeight: Double
){

    val displayingMenu: Boolean by menuPromptViewModel.displayingMenu.observeAsState(initial = false)
    val displayingSound: Boolean by menuPromptViewModel.displayingSound.observeAsState(initial = false)
    val paused: Boolean by menuPromptViewModel.paused.observeAsState(initial = false)

    val fadePromptAlpha: Float by animateFloatAsState(
        targetValue = if (!displayingMenu) 0.33f else 1.0f,
        animationSpec = tween(
            durationMillis = 500,
            easing = LinearEasing,
        ), label = "alpha for fading the prompt"
    )

    Box(modifier = Modifier
        .fillMaxHeight()
        .fillMaxWidth()
        .padding(16.dp)) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .align(alignment = Alignment.BottomStart)
        ) {
            AnimatedVisibility(
                visible = displayingMenu,
                enter = fadeIn(tween(0)),
                exit = fadeOut(tween(0))
            ) {
                verticalMenu(modifier = Modifier, offset = Pair(0.dp, menuItemHeight.dp), contentPadding = 16.dp) {
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
                            images
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
                }
            }
        }
        Box(
            modifier = Modifier
                .size(menuItemHeight.dp)
                .align(alignment = Alignment.BottomStart)
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
                                menuPromptViewModel.onDisplayingMenuChanged(false)
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