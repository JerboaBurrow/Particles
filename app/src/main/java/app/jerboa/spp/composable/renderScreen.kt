package app.jerboa.spp.composable

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import app.jerboa.spp.AppInfo
import app.jerboa.spp.ViewModel.*

@Composable
fun renderScreen(
    renderViewModel: RenderViewModel,
    resolution: Pair<Int,Int>,
    images: Map<String,Int>,
    info: AppInfo,
    showNews: Boolean = false
){
    val displayingMenu: Boolean by renderViewModel.displayingMenu.observeAsState(initial = false)
    val displayingSound: Boolean by renderViewModel.displayingSound.observeAsState(initial = false)
    val displayingAbout: Boolean by renderViewModel.displayingAbout.observeAsState(initial = false)
    val toy: TOY by renderViewModel.toy.observeAsState(initial = TOY.ATTRACTOR)
    val particleNumber: Float by renderViewModel.particleNumber.observeAsState(initial = PARTICLES_SLIDER_DEFAULT)
    val allowAdapt: Boolean by renderViewModel.allowAdapt.observeAsState(initial = true)
    val adaptMsg: Boolean by renderViewModel.autoAdaptMessage.observeAsState(initial = false)
    val colourMap: COLOUR_MAP by renderViewModel.colourMap.observeAsState(COLOUR_MAP.R1)
    val playingMusic: MUSIC by renderViewModel.playingMusic.observeAsState(initial = MUSIC.NOTHING)
    val resetTutorial: Boolean by renderViewModel.resetTutorial.observeAsState(initial = false)
    val dismissedTutorial: Boolean by renderViewModel.dismissedTutorial.observeAsState(initial = false)
    val promptPGS: Boolean by renderViewModel.promptInstallPGS.observeAsState(initial = false)
    val dismissedNews: Boolean by renderViewModel.dismissedNews.observeAsState(initial = false)
    val paused: Boolean by renderViewModel.paused.observeAsState(initial = false)
    val speed: Float by renderViewModel.speed.observeAsState(initial = 1.0f)
    val fade: Float by renderViewModel.fade.observeAsState(initial = 1.0f)
    val showToys: Boolean by renderViewModel.showToys.observeAsState(initial = true)
    val playSuccess: Boolean by renderViewModel.playSuccess.observeAsState(initial = false)

    screen(
        displayingMenu,
        displayingSound,
        displayingAbout,
        playSuccess,
        toy,
        particleNumber,
        allowAdapt,
        colourMap,
        showToys,
        playingMusic,
        paused,
        speed,
        fade,
        adaptMsg,
        promptPGS,
        resolution,
        images,
        info,
        onDisplayingMenuChanged = {renderViewModel.onDisplayingMenuChanged(it)},
        onDisplayingMusicChanged = {renderViewModel.onDisplayingMusicChanged()},
        onDisplayingAboutChanged = {renderViewModel.onDisplayingAboutChanged(it)},
        onAttractorChanged = {renderViewModel.onToyChanged(it)},
        onRequestPlayServices = {renderViewModel.onRequestPlayServices()},
        onAchievementStateChanged = {renderViewModel.onAchievementStateChanged(it)},
        onAdapt = {renderViewModel.onAdapt(it)},
        onAllowAdaptChanged = {renderViewModel.onAllowAdaptChanged()},
        onAdaptMessageShown = {renderViewModel.onAdaptMessageShown()},
        onRequestingLicenses = {renderViewModel.onRequestingLicenses()},
        onParticleNumberChanged = {renderViewModel.onParticleNumberChanged(it)},
        onSelectColourMap = {renderViewModel.onSelectColourMap(it)},
        selectDefaultColourMap = {renderViewModel.selectDefaultColourMap()},
        onMusicSelected = {renderViewModel.onMusicSelected(it)},
        onRequestingSocial = {renderViewModel.onRequestingSocial(it)},
        onResetTutorial = {renderViewModel.onResetTutorial()},
        onPromptPGS = {renderViewModel.onPromptInstallPGS(it)},
        onToyChanged = {renderViewModel.onToyChanged(it)},
        onRequestReview = {renderViewModel.onRequestingInAppReview()},
        onUpdateClock = {renderViewModel.onUpdateClock()},
        onPause = {renderViewModel.onPause()},
        onSpeedChanged = {renderViewModel.onSpeedChanged(it)},
        onFadeChanged = {renderViewModel.onFadeChanged(it)},
        onShowToysChanged = {renderViewModel.onShowToysChanged(it)}
    )

    if (!dismissedTutorial && (info.firstLaunch || resetTutorial) ){
        Image(
            painter = painterResource(id = images["tutorial"]!!),
            contentDescription = "",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    renderViewModel.onDismissTutorial()
                }
        )
    }else if (showNews && !dismissedNews){
        Image(
            painter = painterResource(id = images["news"]!!),
            contentDescription = "",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    renderViewModel.onDismissNews()
                }
        )
    }
}