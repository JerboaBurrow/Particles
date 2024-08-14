package app.jerboa.spp.composable

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import app.jerboa.spp.viewmodel.MUSIC
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

    val fadeAlpha = 0.33f

    val alphaM1: Float by animateFloatAsState(
        targetValue = if (!displayingMenu) fadeAlpha else 1.0f,
        animationSpec = tween(
            durationMillis = 500,
            easing = LinearEasing,
        ), label = "m1"
    )

    val alphaM2: Float by animateFloatAsState(
        targetValue = if (displayingMenu) fadeAlpha else 1.0f,
        animationSpec = tween(
            durationMillis = 500,
            easing = LinearEasing,
        ), label = "m2"
    )

    val alphaS1: Float by animateFloatAsState(
        targetValue = if (!displayingSound) fadeAlpha else 1.0f,
        animationSpec = tween(
            durationMillis = 500,
            easing = LinearEasing,
        ), label = "s1"
    )

    val alphaS2: Float by animateFloatAsState(
        targetValue = if (displayingSound) fadeAlpha else 1.0f,
        animationSpec = tween(
            durationMillis = 500,
            easing = LinearEasing,
        ), label = "s2"
    )


    Box(modifier = Modifier
        .fillMaxHeight()
        .fillMaxWidth()) {
        Box(
            modifier = Modifier
                .width(menuItemHeight.dp)
                .height((menuItemHeight * 2.0).dp)
                .padding((menuItemHeight * 0.1).dp)
                .align(alignment = Alignment.BottomStart)
        ) {
            AnimatedVisibility(
                visible = !displayingMenu,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Image(
                    painter = painterResource(id = images["burger"]!!),
                    contentDescription = "menu",
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(
                            interactionSource = MutableInteractionSource(),
                            indication = null,
                            onClick = { menuPromptViewModel.onDisplayingMenuChanged(true); }
                        )
                        .alpha(alphaM1)
                )
            }
            AnimatedVisibility(
                visible = displayingMenu,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Image(
                    painter = painterResource(id = images["dismiss"]!!),
                    contentDescription = "menu",
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(
                            interactionSource = MutableInteractionSource(),
                            indication = null,
                            onClick = {
                                menuPromptViewModel.onDisplayingMenuChanged(false)
                                aboutViewModel.onDisplayingAboutChanged(false)
                            }
                        )
                        .alpha(alphaM2)
                )
            }
        }
        Box(
            modifier = Modifier
                .width(menuItemHeight.dp)
                .height((menuItemHeight * 8.0).dp)
                .padding((menuItemHeight * 0.1).dp)
                .align(alignment = Alignment.BottomEnd)
        ) {
            AnimatedVisibility(
                visible = displayingSound,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy((0.01*menuItemHeight).dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = images["music-rain"]!!),
                        contentDescription = "rain",
                        modifier = Modifier
                            .fillMaxWidth().height((menuItemHeight*2.0).dp)
                            .clickable(
                                interactionSource = MutableInteractionSource(),
                                indication = null,
                                onClick = {
                                    menuPromptViewModel.onDisplayingMusicChanged(false)
                                    aboutViewModel.onDisplayingAboutChanged(false)
                                    menuPromptViewModel.onMusicSelected(MUSIC.RAIN)
                                }
                            )
                            .alpha(alphaS2)
                    )
                    Image(
                        painter = painterResource(id = images["music-forrest"]!!),
                        contentDescription = "forrest",
                        modifier = Modifier
                            .fillMaxWidth().height((menuItemHeight*2.0).dp)
                            .clickable(
                                interactionSource = MutableInteractionSource(),
                                indication = null,
                                onClick = {
                                    menuPromptViewModel.onDisplayingMusicChanged(false)
                                    aboutViewModel.onDisplayingAboutChanged(false)
                                    menuPromptViewModel.onMusicSelected(MUSIC.FORREST)
                                }
                            )
                            .alpha(alphaS2)
                    )
                    Image(
                        painter = painterResource(id = images["music-none"]!!),
                        contentDescription = "no sound",
                        modifier = Modifier
                            .fillMaxWidth().height((menuItemHeight*2.0).dp)
                            .clickable(
                                interactionSource = MutableInteractionSource(),
                                indication = null,
                                onClick = {
                                    menuPromptViewModel.onDisplayingMusicChanged(false)
                                    aboutViewModel.onDisplayingAboutChanged(false)
                                    menuPromptViewModel.onMusicSelected(MUSIC.NOTHING)
                                }
                            )
                            .alpha(alphaS2)
                    )
                    Image(
                        painter = painterResource(id = images["dismiss"]!!),
                        contentDescription = "menu",
                        modifier = Modifier
                            .fillMaxWidth().height((menuItemHeight*2.0).dp)
                            .clickable(
                                interactionSource = MutableInteractionSource(),
                                indication = null,
                                onClick = {
                                    menuPromptViewModel.onDisplayingMusicChanged(false)
                                    aboutViewModel.onDisplayingAboutChanged(false)
                                }
                            )
                            .alpha(alphaS2)
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .width(menuItemHeight.dp)
                .height((menuItemHeight * 2.0).dp)
                .padding((menuItemHeight * 0.1).dp)
                .align(alignment = Alignment.BottomCenter)
        ) {
            AnimatedVisibility(
                visible = !paused && !displayingMenu,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Image(
                    painter = painterResource(id = images["pause"]!!),
                    contentDescription = "pause",
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(
                            interactionSource = MutableInteractionSource(),
                            indication = null,
                            onClick = { menuPromptViewModel.onPause(); }
                        )
                        .alpha(0.66f)
                )
            }
            AnimatedVisibility(
                visible = paused && !displayingMenu,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Image(
                    painter = painterResource(id = images["play"]!!),
                    contentDescription = "unpause",
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(
                            interactionSource = MutableInteractionSource(),
                            indication = null,
                            onClick = { menuPromptViewModel.onPause(); }
                        )
                        .alpha(0.66f)
                )
            }
        }
        Box(
            modifier = Modifier
                .width(menuItemHeight.dp)
                .height((menuItemHeight * 2.0).dp)
                .padding((menuItemHeight * 0.1).dp)
                .align(alignment = Alignment.BottomEnd)
        ) {
            AnimatedVisibility(
                visible = !displayingSound,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Image(
                    painter = painterResource(id = images["music"]!!),
                    contentDescription = "menu",
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(
                            interactionSource = MutableInteractionSource(),
                            indication = null,
                            onClick =
                            {
                                menuPromptViewModel.onDisplayingMusicChanged(true)
                            }
                        )
                        .alpha(alphaS1)
                )
            }
        }
    }
}