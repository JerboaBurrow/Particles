package app.jerboa.spp.composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.jerboa.spp.R

data class NewsItem (val textResourceId: Int, val imageResourceId: Int?)

@Composable
fun newsItem(
    item: NewsItem,
    width: Double
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .width(width.dp)
            .heightIn(0.dp, width.dp)
            .background(shape = RoundedCornerShape(2.dp), color = Color.White)
    ) {
        if (item.imageResourceId != null) {
            Image(
                modifier = androidx.compose.ui.Modifier
                    .width(width.dp)
                    .weight(1f),
                painter = painterResource(id = item.imageResourceId),
                contentDescription = "News item"
            )
        }
        Text(
            text = stringResource(id = item.textResourceId),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun newsLog(
    newsItems: List<NewsItem>,
    spacing: Dp = 2.dp,
    width: Double
) {
    val scrollState = rememberScrollState()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        Text(
            text = stringResource(id = R.string.newsIntro),
            modifier = Modifier
                .background(shape = RoundedCornerShape(2.dp), color = Color.White)
                .width(width.dp),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.size(spacing))
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .verticalScroll(scrollState)
        ) {

            for (item in newsItems) {
                newsItem(item, width*0.9)
                Spacer(modifier = Modifier.size(spacing))
            }
        }
    }
}