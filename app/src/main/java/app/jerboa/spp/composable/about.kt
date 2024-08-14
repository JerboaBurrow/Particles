package app.jerboa.spp.composable

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.jerboa.spp.AppInfo
import app.jerboa.spp.R
import app.jerboa.spp.viewmodel.AboutViewModel
import app.jerboa.spp.viewmodel.SOCIAL

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun about(
    aboutViewModel: AboutViewModel,
    width75Percent: Double,
    images: Map<String,Int>,
    info: AppInfo,
){
    val displayingAbout: Boolean by aboutViewModel.displayingAbout.observeAsState(initial = false)

    AnimatedVisibility(
        visible = displayingAbout,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.size(8.dp))
            Box(
                androidx.compose.ui.Modifier
                    .width(width75Percent.dp)
                    .height(width75Percent.dp)
                    .background(
                        color = MaterialTheme.colors.secondary,
                        shape = RoundedCornerShape(5)
                    )
                    .padding(horizontal = 4.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Image(
                        modifier = androidx.compose.ui.Modifier
                            .size((width75Percent * 0.33).dp)
                            .weight(1f),
                        painter = painterResource(id = images["logo"]!!),
                        contentDescription = "Logo"
                    )
                    adaptiveTextBox(
                        stringResource(id = R.string.tagline) + stringResource(id = R.string.description),
                        textAlign = TextAlign.Center,
                        maxLines = 4,
                        fontSize = MaterialTheme.typography.body1.fontSize * info.density,
                        colour = Color.Black
                    )
                    TextButton(onClick = { aboutViewModel.onRequestingLicenses() }) {
                        Text(
                            stringResource(id = R.string.OSSprompt),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f),
                            fontSize = MaterialTheme.typography.body1.fontSize * info.density,
                            color = MaterialTheme.colors.primary
                        )
                    }
                    adaptiveTextBox(
                        stringResource(R.string.attrib),
                        maxLines = 3,
                        fontSize = MaterialTheme.typography.overline.fontSize * info.density,
                        textAlign = TextAlign.Center,
                        colour = Color.Black
                    )
                    Text(
                        "version: " + info.versionString, modifier = Modifier.weight(1f),
                        fontSize = MaterialTheme.typography.overline.fontSize * info.density,
                        textAlign = TextAlign.Center,
                        color = Color.Black
                    )
                    TextButton(onClick = { aboutViewModel.onResetTutorial() }) {
                        Text(
                            stringResource(id = R.string.resetTutorial),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f),
                            fontSize = MaterialTheme.typography.body1.fontSize * info.density,
                            color = MaterialTheme.colors.primary
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.size(8.dp))
            socials(images, info) { aboutViewModel.onRequestingSocial(it) }
        }
    }

}