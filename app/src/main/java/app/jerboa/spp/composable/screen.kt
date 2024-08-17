package app.jerboa.spp.composable

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarResult
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.viewinterop.AndroidView
import app.jerboa.spp.AppInfo
import app.jerboa.spp.ui.view.SPPView
import app.jerboa.spp.viewmodel.*
import kotlinx.coroutines.launch

@SuppressLint("CoroutineCreationDuringComposition", "UnusedMaterialScaffoldPaddingParameter")
@Composable
fun screen(
    sppViewModel: SPPViewModel,
    aboutViewModel: AboutViewModel,
    menuPromptViewModel: MenuPromptViewModel,
    toyMenuViewModel: ToyMenuViewModel,
    resolution: Pair<Int,Int>,
    images: Map<String,Int>,
    info: AppInfo,
    showNews: Boolean = false
){

    val resetTutorial: Boolean by aboutViewModel.resetTutorial.observeAsState(initial = false)
    val dismissedTutorial: Boolean by aboutViewModel.dismissedTutorial.observeAsState(initial = false)
    val dismissedNews: Boolean by sppViewModel.dismissedNews.observeAsState(initial = false)

    val width75Percent = info.widthDp * 0.75
    val height10Percent = info.heightDp * 0.1
    val menuItemHeight = height10Percent * 0.66

    val displayingToyMenu: Boolean by menuPromptViewModel.displayingToyMenu.observeAsState(initial = false)
    val paused: Boolean by menuPromptViewModel.paused.observeAsState(initial = false)
    val clear: Boolean by menuPromptViewModel.clear.observeAsState(initial = false)

    val toy: TOY by toyMenuViewModel.toy.observeAsState(initial = TOY.ATTRACTOR)
    val particleNumber: Float by toyMenuViewModel.particleNumber.observeAsState(initial = PARTICLES_SLIDER_DEFAULT)
    val allowAdapt: Boolean by toyMenuViewModel.allowAdapt.observeAsState(initial = true)
    val adaptMsg: Boolean by toyMenuViewModel.autoAdaptMessage.observeAsState(initial = false)
    val colourMap: COLOUR_MAP by toyMenuViewModel.colourMap.observeAsState(COLOUR_MAP.R1)
    val speed: Float by toyMenuViewModel.speed.observeAsState(initial = 1.0f)
    val attraction: Float by toyMenuViewModel.attractorStrength.observeAsState(50000f)
    val repulsion: Float by toyMenuViewModel.repellorStrength.observeAsState(50000f)
    val orbit: Float by toyMenuViewModel.orbitStrength.observeAsState(0.5f)
    val spin: Float by toyMenuViewModel.spinStrength.observeAsState(1500f)
    val mass: Float by toyMenuViewModel.mass.observeAsState(0.1f)
    val fade: Float by toyMenuViewModel.fade.observeAsState(initial = 1.0f)
    val scale: Float by toyMenuViewModel.scale.observeAsState(initial = 3f)
    val showToys: Boolean by toyMenuViewModel.showToys.observeAsState(initial = false)

    val promptPGS: Boolean by sppViewModel.promptInstallPGS.observeAsState(initial = false)


    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()

    toyMenuViewModel.selectDefaultColourMap()

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
                toyMenu(
                    toyMenuViewModel,
                    displayingToyMenu,
                    width75Percent,
                    height10Percent,
                    menuItemHeight,
                    images
                )
            }
        ) {
            if (adaptMsg) {
                toyMenuViewModel.onAdaptMessageShown()
                coroutineScope.launch {
                    val snackbarResult = scaffoldState.snackbarHostState.showSnackbar(
                        message = "FPS lower than 30 adapting...",
                        actionLabel = "STOP!",
                        duration = SnackbarDuration.Short
                    )
                    when (snackbarResult) {
                        //SnackbarResult.Dismissed -> Log.d("screen", "Dismissed")
                        SnackbarResult.ActionPerformed -> {
                            toyMenuViewModel.onAllowAdaptChanged()
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
                            sppViewModel.onPromptInstallPGS(true)
                        }
                        else -> {}
                    }
                }
                sppViewModel.onPromptInstallPGS(false)
            }
            AndroidView(
                factory = {
                    SPPView(
                        it, null,
                        resolution,
                        sppViewModel,
                        toyMenuViewModel,
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
                    view.setScale(scale)
                    view.showToys(showToys)
                    if (clear) { view.clearToys(); menuPromptViewModel.onClear(false); }
                }
            )
        }
    }

    about(
        aboutViewModel,
        width75Percent,
        images,
        info
    )

    menuPrompt(
        menuPromptViewModel,
        aboutViewModel,
        sppViewModel,
        images,
        menuItemHeight
    )

    if (!dismissedTutorial && (info.firstLaunch || resetTutorial) ){
        Image(
            painter = painterResource(id = images["tutorial"]!!),
            contentDescription = "",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    aboutViewModel.onDismissTutorial()
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
                    sppViewModel.onDismissNews()
                }
        )
    }
}