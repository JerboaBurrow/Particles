package app.jerboa.spp.composable

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import app.jerboa.spp.ViewModel.MUSIC

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun menuPrompt(
    images: Map<String,Int>,
    displayingMenu: Boolean,
    displayingSound: Boolean,
    menuItemHeight: Double,
    onDisplayingMenuChanged: (Boolean) -> Unit,
    onDisplayingMusicChanged: () -> Unit,
    onMusicSelected: (MUSIC) -> Unit
){

    val alphaM1: Float by animateFloatAsState(
        targetValue = if (!displayingMenu) 0.66f else 1.0f,
        animationSpec = tween(
            durationMillis = 500,
            easing = LinearEasing,
        )
    )

    val alphaM2: Float by animateFloatAsState(
        targetValue = if (displayingMenu) 0.66f else 1.0f,
        animationSpec = tween(
            durationMillis = 500,
            easing = LinearEasing,
        )
    )

    val alphaS1: Float by animateFloatAsState(
        targetValue = if (!displayingSound) 0.66f else 1.0f,
        animationSpec = tween(
            durationMillis = 500,
            easing = LinearEasing,
        )
    )

    val alphaS2: Float by animateFloatAsState(
        targetValue = if (displayingSound) 0.66f else 1.0f,
        animationSpec = tween(
            durationMillis = 500,
            easing = LinearEasing,
        )
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
                            onClick = { onDisplayingMenuChanged(true); }
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
                            onClick = { onDisplayingMenuChanged(true); }
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
                                onClick = { onDisplayingMusicChanged(); onMusicSelected(MUSIC.RAIN) }
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
                                onClick = { onDisplayingMusicChanged(); onMusicSelected(MUSIC.FORREST) }
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
                                onClick = { onDisplayingMusicChanged(); onMusicSelected(MUSIC.NOTHING) }
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
                                onClick = { onDisplayingMusicChanged();}
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
                            onClick = { onDisplayingMusicChanged(); }
                        )
                        .alpha(alphaS1)
                )
            }
        }
    }
}