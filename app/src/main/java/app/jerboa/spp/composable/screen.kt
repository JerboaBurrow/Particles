package app.jerboa.spp.composable

import android.annotation.SuppressLint
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import app.jerboa.spp.AppInfo
import app.jerboa.spp.viewmodel.COLOUR_MAP
import app.jerboa.spp.viewmodel.MUSIC
import app.jerboa.spp.viewmodel.PARAM
import app.jerboa.spp.viewmodel.SOCIAL
import app.jerboa.spp.viewmodel.TOY
import app.jerboa.spp.ui.view.SPPView
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class)
// see scaffold below
@SuppressLint("CoroutineCreationDuringComposition", "UnusedMaterialScaffoldPaddingParameter")
@Composable
fun screen(
    displayingMenu: Boolean,
    displayingSound: Boolean,
    displayingAbout: Boolean,
    playSuccess: Boolean,
    toy: TOY,
    particleNumber: Float,
    allowAdapt: Boolean,
    colourMap: COLOUR_MAP,
    showToys: Boolean,
    playingMusic: MUSIC,
    paused: Boolean,
    speed: Float,
    attraction: Float,
    repulsion: Float,
    orbit: Float,
    spin: Float,
    mass: Float,
    fade: Float,
    adaptMsg: Boolean,
    promptPGS: Boolean,
    resolution: Pair<Int,Int>,
    images: Map<String,Int>,
    info: AppInfo,
    onDisplayingMenuChanged: (Boolean) -> Unit,
    onDisplayingMusicChanged: () -> Unit,
    onDisplayingAboutChanged: (Boolean) -> Unit,
    onAttractorChanged: (TOY) -> Unit,
    onRequestPlayServices: () -> Unit,
    onAchievementStateChanged: (Pair<String,Int>) -> Unit,
    onAdapt: (Float) -> Unit,
    onAllowAdaptChanged: () -> Unit,
    onAdaptMessageShown: () -> Unit,
    onRequestingLicenses: () -> Unit,
    onParameterChanged: (Pair<Float, PARAM>) -> Unit,
    onSelectColourMap: (COLOUR_MAP) -> Unit,
    selectDefaultColourMap: () -> Unit,
    onMusicSelected: (MUSIC) -> Unit,
    onRequestingSocial: (SOCIAL) -> Unit,
    onResetTutorial: () -> Unit,
    onPromptPGS: (Boolean) -> Unit,
    onToyChanged: (TOY) -> Unit,
    onRequestReview: () -> Unit,
    onUpdateClock: () -> Unit,
    onPause: () -> Unit,
    onShowToysChanged: (Boolean) -> Unit
) {

    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()

    val seenHelp = remember { mutableStateOf(!info.firstLaunch) }

    val width75Percent = info.widthDp * 0.75
    val height25Percent = info.heightDp * 0.25
    val height10Percent = info.heightDp * 0.1
    val menuItemHeight = height10Percent * 0.66

    selectDefaultColourMap()

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // padding is unused is we are using the scaffold as
        //  a hacky overlay on top of opengl
        Scaffold(
            scaffoldState = scaffoldState,
            topBar = {
            },
            bottomBar = {
                menu(
                    displayingMenu && !displayingAbout,
                    playSuccess,
                    particleNumber,
                    speed,
                    attraction,
                    repulsion,
                    orbit,
                    spin,
                    mass,
                    fade,
                    showToys,
                    width75Percent,
                    height10Percent,
                    menuItemHeight,
                    images,
                    info,
                    onDisplayingAboutChanged,
                    onAttractorChanged,
                    onRequestPlayServices,
                    onParameterChanged,
                    onSelectColourMap,
                    onShowToysChanged
                )
            }
        ) {
            if (adaptMsg) {
                onAdaptMessageShown()
                coroutineScope.launch {
                    val snackbarResult = scaffoldState.snackbarHostState.showSnackbar(
                        message = "FPS lower than 30 adapting...",
                        actionLabel = "STOP!",
                        duration = SnackbarDuration.Short
                    )
                    when (snackbarResult) {
                        //SnackbarResult.Dismissed -> Log.d("screen", "Dismissed")
                        SnackbarResult.ActionPerformed -> {
                            onAllowAdaptChanged()
                        }
                        else -> {}
                    }
                }
            }
            if (promptPGS) {
                coroutineScope.launch {
                    val snackbarResult = scaffoldState.snackbarHostState.showSnackbar(
                        message = "Achievements require Play Games Services",
                        actionLabel = "Install",
                        duration = SnackbarDuration.Short
                    )
                    when (snackbarResult) {
                        //SnackbarResult.Dismissed -> Log.d("screen", "Dismissed")
                        SnackbarResult.ActionPerformed -> {
                            onPromptPGS(true)
                        }
                        else -> {}
                    }
                }
                onPromptPGS(false)
            }
            AndroidView(
                factory = {
                    SPPView(
                        it, null,
                        resolution,
                        onDisplayingMenuChanged,
                        onAchievementStateChanged,
                        onToyChanged,
                        onRequestReview,
                        onAdapt,
                        onUpdateClock,
                        toy,
                        particleNumber,
                        allowAdapt,
                        colourMap
                    )
                },
                update = { view ->
                    view.placingToy = toy
                    view.particleNumber = particleNumber
                    view.setParticleNumber(particleNumber)
                    view.setAllowAdapt(allowAdapt)
                    view.setColourMap(colourMap)
                    view.pause(paused)
                    view.setSpeed(speed)
                    view.setMass(mass)
                    view.setRepulsion(repulsion)
                    view.setAttraction(attraction)
                    view.setOrbit(orbit)
                    view.setSpin(spin)
                    view.setFade(fade)
                    view.showToys(showToys)
                }
            )
            about(
                displayingAbout,
                width75Percent,
                images,
                info,
                onRequestingLicenses,
                onRequestingSocial,
                onResetTutorial
            )
            menuPrompt(images,displayingMenu,displayingSound,menuItemHeight,paused,onDisplayingMenuChanged,onDisplayingMusicChanged,onMusicSelected, onPause)
        }
    }
}