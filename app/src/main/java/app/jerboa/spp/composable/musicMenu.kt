package app.jerboa.spp.composable

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import app.jerboa.spp.viewmodel.AboutViewModel
import app.jerboa.spp.viewmodel.MUSIC
import app.jerboa.spp.viewmodel.MenuPromptViewModel

@Composable
fun musicMenu(
    menuPromptViewModel: MenuPromptViewModel,
    menuItemHeight: Double,
    images: Map<String,Int>,
) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(menuItemHeight.dp),
            horizontalArrangement = Arrangement.spacedBy((0.01*menuItemHeight).dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = images["music-rain"]!!),
                contentDescription = "rain",
                modifier = Modifier
                    .size(menuItemHeight.dp)
                    .clickable(
                        onClick = {
                            menuPromptViewModel.onDisplayingMusicChanged(false)
                            menuPromptViewModel.onMusicSelected(MUSIC.RAIN)
                        }
                    )
            )
            Image(
                painter = painterResource(id = images["music-forrest"]!!),
                contentDescription = "forrest",
                modifier = Modifier
                    .size(menuItemHeight.dp)
                    .clickable(
                        onClick = {
                            menuPromptViewModel.onDisplayingMusicChanged(false)
                            menuPromptViewModel.onMusicSelected(MUSIC.FORREST)
                        }
                    )
            )
            Image(
                painter = painterResource(id = images["music-none"]!!),
                contentDescription = "no sound",
                modifier = Modifier
                    .size(menuItemHeight.dp)
                    .clickable(
                        onClick = {
                            menuPromptViewModel.onDisplayingMusicChanged(false)
                            menuPromptViewModel.onMusicSelected(MUSIC.NOTHING)
                        }
                    )
            )
        }
    }
}