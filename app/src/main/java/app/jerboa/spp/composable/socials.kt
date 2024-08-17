package app.jerboa.spp.composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.jerboa.spp.AppInfo
import app.jerboa.spp.R
import app.jerboa.spp.viewmodel.SOCIAL

@Composable
fun socials(
    images: Map<String,Int>,
    info: AppInfo,
    onRequestingSocial: (SOCIAL) -> Unit
){
    val width75Percent = info.widthDp*0.75
    val height25Percent = info.heightDp*0.25
    val height20Percent = info.heightDp*0.2
    val menuItemHeight = height20Percent*0.75

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            Modifier
                .width(width75Percent.dp)
                .height(menuItemHeight.dp)
                .background(
                    color = Color(255, 255, 255, 0),
                    shape = RoundedCornerShape(5)
                )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.height((0.5 * menuItemHeight).dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { onRequestingSocial(SOCIAL.PLAY) }) {
                        Image(
                            painter = painterResource(id = images["play-logo"]!!),
                            contentDescription = "Play Logo",
                            modifier = Modifier.size((0.5 * menuItemHeight).dp)
                        )
                    }
                    IconButton(onClick = { onRequestingSocial(SOCIAL.YOUTUBE) }) {
                        Image(
                            painter = painterResource(id = images["yt"]!!),
                            contentDescription = "youtube",
                            modifier = Modifier.size((0.5 * menuItemHeight).dp)
                        )
                    }
                    IconButton(onClick = { onRequestingSocial(SOCIAL.WEB) }) {
                        Image(
                            painter = painterResource(id = images["web"]!!),
                            contentDescription = "jerboa.app",
                            modifier = Modifier.size((0.5 * menuItemHeight).dp)
                        )
                    }
                    IconButton(onClick = { onRequestingSocial(SOCIAL.GITHUB) }) {
                        Image(
                            painter = painterResource(id = images["github"]!!),
                            contentDescription = "https://github.com/Jerboa-app",
                            modifier = Modifier.size((0.5 * menuItemHeight).dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = stringResource(id = R.string.rate),
                    color = Color(255,255,255,255),
                    fontSize = MaterialTheme.typography.body1.fontSize*info.density,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}