package app.jerboa.spp

import android.app.Activity
import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Point
import android.graphics.Rect
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import app.jerboa.spp.composable.NewsItem
import app.jerboa.spp.composable.screen
import app.jerboa.spp.ui.theme.SPPTheme
import app.jerboa.spp.viewmodel.AboutViewModel
import app.jerboa.spp.viewmodel.MUSIC
import app.jerboa.spp.viewmodel.MenuPromptViewModel
import app.jerboa.spp.viewmodel.NULL_MENU_POSITION
import app.jerboa.spp.viewmodel.REVIEW_RATE_LIMIT_MILLIS
import app.jerboa.spp.viewmodel.SOCIAL
import app.jerboa.spp.viewmodel.SPPViewModel
import app.jerboa.spp.viewmodel.ToyMenuViewModel
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.games.AuthenticationResult
import com.google.android.gms.games.PlayGames
import com.google.android.gms.games.PlayGamesSdk
import com.google.android.gms.games.achievement.Achievement
import com.google.android.gms.games.achievement.AchievementBuffer
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.android.gms.tasks.Task
import com.google.android.play.core.review.ReviewException
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.review.model.ReviewErrorCode
import java.lang.Integer.min
import java.util.Date


val news = "news-20-08-24"

val DEBUG = false

data class AppInfo(
    val versionString: String,
    val firstLaunch: Boolean,
    val density: Float,
    val textScaling: Float,
    val heightDp: Float,
    val widthDp: Float
)

val achievementNameToID = mapOf(
    "GetLost" to "CggIk4Pih0AQAhAB",
    "SuperFan" to "CggIk4Pih0AQAhAC",
    "ShowMeWhatYouGot" to "CggIk4Pih0AQAhAD",
    "AverageFan" to "CggIk4Pih0AQAhAE",
    "AverageEnjoyer" to "CggIk4Pih0AQAhAF",
    "StillThere" to "CggIk4Pih0AQAhAG",
    "HipToBeSquare" to "CggIk4Pih0AQAhAH",
    "DoubleTrouble" to "CggIk4Pih0AQAhAI",
    "CirclesTheWay" to "CggIk4Pih0AQAhAJ"
)

val achievementsIDToName = achievementNameToID.entries.associate{(k,v)-> v to k}

val incrementable = listOf(
    "SuperFan",
    "AverageFan",
    "AverageEnjoyer"
)

val achievementStates = mutableMapOf(
    "GetLost" to Pair(0,1),
    "SuperFan" to Pair(0,1),
    "ShowMeWhatYouGot" to Pair(0,1),
    "AverageFan" to Pair(0,1),
    "AverageEnjoyer" to Pair(0,1),
    "StillThere" to Pair(0,1),
    "HipToBeSquare" to Pair(0,1),
    "DoubleTrouble" to Pair(0,1),
    "CirclesTheWay" to Pair(0,1)
)

class MainActivity : AppCompatActivity() {

    private val sppViewModel by viewModels<SPPViewModel>()
    private val aboutViewModel by viewModels<AboutViewModel>()
    private val menuPromptViewModel by viewModels<MenuPromptViewModel>()
    private val toyMenuViewModel by viewModels<ToyMenuViewModel>()

    private var mediaPlayer = MediaPlayer()

    private lateinit var reviewManager: ReviewManager

    private var lastPlayTime: Long = 0L
    private var totalTime: Long = 0L
    private var seenReview: Boolean = false
    private var lastReviewTries: Long = 0L

    private val imageResources: Map<String, Int> = mapOf(
        "logo" to R.drawable.ic_logo,
        "about" to R.drawable.about_,
        "attractor" to R.drawable.attractor_,
        "repeller" to R.drawable.repeller_,
        "spinner" to R.drawable.spinner_,
        "orbiter" to R.drawable.orbiter_,
        "play-controller" to R.drawable.games_controller_grey,
        "play-logo" to R.drawable.play_,
        "music" to R.drawable.ic_music,
        "burger" to R.drawable.ic_burger,
        "dismiss" to R.drawable.ic_dismiss,
        "rainbow1" to R.drawable.rainbow,
        "rainbow2" to R.drawable.c1,
        "ace" to R.drawable.ace,
        "c3" to R.drawable.c3,
        "cb1" to R.drawable.cblind1,
        "cb2" to R.drawable.cblind2,
        "trans" to R.drawable.trans,
        "pride" to R.drawable.pride,
        "music-forrest" to R.drawable.music_forrest_,
        "music-rain" to R.drawable.music_rain_,
        "music-none" to R.drawable.music_none_,
        "yt" to R.drawable.ic_yt,
        "web" to R.drawable.ic_logo,
        "github" to R.drawable.github_mark_white,
        "tutorial" to R.drawable.tutorial,
        "news" to R.drawable.news,
        "pause" to R.drawable.pause,
        "play" to R.drawable.play_button,
        "freezer" to R.drawable.freezer,
        "clear" to R.drawable.clear,
        "toyMenu" to R.drawable.toymenu
    )

    private val rcAchievementUI = 9003

    private fun isGooglePlayGamesServicesInstalled(activity: Activity): Boolean {
        val v =
            activity.packageManager.getLaunchIntentForPackage("com.google.android.play.games") != null
        if (DEBUG) {
            Log.d("isGooglePlayGamesServicesInstalled", v.toString())
        }
        return v
    }

    private fun showAchievements() {
        if (!isGooglePlayGamesServicesInstalled(this)) {
            sppViewModel.onRequestPGSAndNotInstalled()
            return
        }
        PlayGames.getAchievementsClient(this)
            .achievementsIntent
            .addOnSuccessListener { intent ->
                startActivityForResult(
                    intent,
                    rcAchievementUI
                );
                if (DEBUG) {
                    Log.d("showAchievements", "success")
                }
            }
            .addOnFailureListener {
                if (DEBUG) {
                    Log.d("showAchievements failure", "${it.toString()}")
                }
            }
    }

    private fun playGamesServicesLogin() {
        if (!isGooglePlayGamesServicesInstalled(this)) {
            return
        }
        val gamesSignInClient = PlayGames.getGamesSignInClient(this)

        gamesSignInClient.isAuthenticated.addOnCompleteListener { isAuthenticatedTask: Task<AuthenticationResult> ->
            val isAuthenticated =
                isAuthenticatedTask.isSuccessful && isAuthenticatedTask.result.isAuthenticated
            if (isAuthenticated) {
                // Continue with Play Games Services
                sppViewModel.onPlaySuccess(true)
                if (DEBUG) {
                    Log.d("playGames", "success")
                }
            } else {
                // Disable your integration with Play Games Services or show a
                // login button to ask  players to sign-in. Clicking it should
                // call GamesSignInClient.signIn().
                // calling that will trigger sign in, and update the play store etc.
                if (DEBUG) {
                    Log.d("playGames", "failure ${isAuthenticatedTask.result.toString()}")
                }
                sppViewModel.onPlaySuccess(false)
            }
        }
    }

    private fun updatePlayGamesAchievements(states: Map<String, Pair<Int, Int>>) {
        if (!isGooglePlayGamesServicesInstalled(this)) {
            return
        }
        if (DEBUG) {
            Log.d("achievements", "called update play services")
        }
        for (s in states) {
            val id: String = achievementNameToID[s.key]!!

            if (s.value.first == s.value.second) {
                // unlocked
                PlayGames.getAchievementsClient(this).unlock(id)
            }

            if (s.key in incrementable) {
                PlayGames.getAchievementsClient(this)
                    .setSteps(id, min(s.value.first, s.value.second))
            }

        }
    }

    private fun syncAchievementsState() {
        if (!isGooglePlayGamesServicesInstalled(this)) {
            return
        }
        PlayGames.getAchievementsClient(this).load(true).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val achievements: AchievementBuffer? = task.result.get()
                if (achievements != null) {
                    for (i in 0 until achievements.count) {
                        val ach: Achievement = achievements.get(i)
                        val id = ach.achievementId
                        val state = ach.state

                        val unlocked = if (state == Achievement.STATE_UNLOCKED) {
                            1
                        } else {
                            0
                        }

                        if (state == Achievement.TYPE_INCREMENTAL) {
                            achievementStates[achievementsIDToName[id].toString()] = Pair(
                                ach.currentSteps,
                                ach.totalSteps
                            )
                        } else {
                            achievementStates[achievementsIDToName[id].toString()] = Pair(
                                unlocked,
                                1
                            )
                        }
                        if (DEBUG) {
                            Log.d(
                                "loadAchievements",
                                id + " " + achievementsIDToName[id].toString() + ": " + achievementStates[achievementsIDToName[id].toString()]
                            )
                        }
                    }
                }
            }
        }
    }

    private fun tryStartActivity(intent: Intent, failInfoToast: String) {
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Log.e("tryStartActivity", failInfoToast)
            val toast = Toast.makeText(this, failInfoToast, Toast.LENGTH_SHORT) // in Activity
            toast.show()
        }
    }
    private fun playRate() {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(
                "https://play.google.com/store/apps/details?id=app.jerboa.spp"
            )
            setPackage("com.android.vending")
        }
        tryStartActivity(intent, "Could not open Play Store")
    }

    private fun youtube() {
        val uri = Uri.parse("https://www.youtube.com/channel/UCP3KhLhmG3Z1CMWyLkn7pbQ")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        tryStartActivity(intent, "Could not open Youtube")
    }

    private fun web() {
        val uri = Uri.parse("https://jerboa.app")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        tryStartActivity(intent, "Could not open https://jerboa.app")
    }

    private fun github() {
        val uri = Uri.parse("https://github.com/JerboaBurrow/Particles")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        tryStartActivity(intent, "Could not open Github")
    }

    private fun showLicenses() {
        val intent = Intent(this.applicationContext, OssLicensesMenuActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        //on opening OSS sometimes there is a crash..
        //https://github.com/google/play-services-plugins/issues/100
        //com.google.android.gms.internal.oss_licenses.zzf.dummy_placeholder = getResources().getIdentifier("third_party_license_metadata", "raw", getPackageName());
        tryStartActivity(intent, "Could not show licenses")
    }

    private fun installPGS() {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(
                "https://play.google.com/store/apps/details?id=com.google.android.play.games"
            )
            setPackage("com.android.vending")
        }
        tryStartActivity(intent, "Could not open Play Store")
    }

    private fun requestUserReviewPrompt() {

        if (lastReviewTries < 2) {

            val request = reviewManager.requestReviewFlow()
            request.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // We got the ReviewInfo object
                    val reviewInfo = task.result
                    val flow = reviewManager.launchReviewFlow(this, reviewInfo)
                    //Log.d("reviewManager", "flow")
                    flow.addOnCompleteListener {
                        sppViewModel.onInAppReviewShown()
                        //Log.d("reviewManager", "complete: $reviewInfo")
                    }
                } else {
                    // There was some problem, log or handle the error code.
                    @ReviewErrorCode val reviewErrorCode =
                        (task.exception as ReviewException).errorCode
                    Log.e("requestUserReviewPrompt", "$reviewErrorCode")
                }
            }
        }

        sppViewModel.onInAppReviewShown()
        seenReview = true

        if (lastReviewTries < 5) {
            val prefs = getSharedPreferences("jerboa.app.spp.prefs", MODE_PRIVATE)
            val prefsEdit = prefs.edit()
            prefsEdit.putLong("reviewTries", lastReviewTries + 1L)
            prefsEdit.apply()
        } else {
            val prefs = getSharedPreferences("jerboa.app.spp.prefs", MODE_PRIVATE)
            val prefsEdit = prefs.edit()
            prefsEdit.putLong("reviewTries", 0L)
            prefsEdit.apply()
        }

    }

    private fun isAppForeground(): Boolean {
        val activityManager = this.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val appProcesses = activityManager.runningAppProcesses ?: return false
        for (appProcess in appProcesses) {
            if (appProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName == "app.jerboa.spp") {
                return true
            }
        }
        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        reviewManager = ReviewManagerFactory.create(this)

        sppViewModel.requestingInAppReview.observe(
            this, androidx.lifecycle.Observer {
                if (it) {
                    requestUserReviewPrompt()
                }
            }
        )

        val status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)
        if (DEBUG) {
            Log.d("playServices", "$status ${status == ConnectionResult.SUCCESS}")
        }

        sppViewModel.requestingPlayServices.observe(
            this, androidx.lifecycle.Observer { request ->
                if (request) {
                    if (DEBUG) {
                        Log.d("playGames", "${sppViewModel.playSuccess.value!!}")
                    }
                    if (!sppViewModel.playSuccess.value!! && isGooglePlayGamesServicesInstalled(this)) {
                        if (DEBUG) {
                            Log.d("playGames", "login")
                        }
                        val gamesSignInClient = PlayGames.getGamesSignInClient(this)
                        gamesSignInClient.signIn()
                            .addOnFailureListener {
                                if (DEBUG) {
                                    Log.d("playGames", "Login failed")
                                }
                            }
                            .addOnSuccessListener {
                                if (DEBUG) {
                                    Log.d("playGames", "Login Success")
                                }
                            }
                    }
                    showAchievements()
                }
            }
        )

        sppViewModel.requestingInstallPGS.observe(
            this, androidx.lifecycle.Observer { request ->
                if (request) {
                    installPGS()
                    sppViewModel.onInstallPGSInitiated()
                }
            }
        )

        aboutViewModel.requestingLicenses.observe(
            this, androidx.lifecycle.Observer { request ->
                if (request) {
                    showLicenses()
                }
            }
        )

        aboutViewModel.requestingSocial.observe(
            this, androidx.lifecycle.Observer { request ->
                when (request) {
                    SOCIAL.WEB -> web()
                    SOCIAL.PLAY -> playRate()
                    SOCIAL.YOUTUBE -> youtube()
                    SOCIAL.GITHUB -> github()
                    else -> {}
                }
            }
        )

        toyMenuViewModel.showToys.observe(
            this, androidx.lifecycle.Observer { show ->
                val prefs = getSharedPreferences("jerboa.app.spp.prefs", MODE_PRIVATE)
                val prefsEdit = prefs.edit()
                prefsEdit.putBoolean("showToys", show)
                prefsEdit.apply()
            }
        )

        menuPromptViewModel.playingMusic.observe(
            this, androidx.lifecycle.Observer { playingMusic ->
                when (playingMusic) {
                    MUSIC.FORREST -> {
                        mediaPlayer.release()
                        mediaPlayer = MediaPlayer.create(this, R.raw.forrest)
                        mediaPlayer.isLooping = true
                        mediaPlayer.start()
                    }
                    MUSIC.RAIN -> {
                        mediaPlayer.release()
                        mediaPlayer = MediaPlayer.create(this, R.raw.rain)
                        mediaPlayer.isLooping = true
                        mediaPlayer.start()
                    }
                    MUSIC.NOTHING -> {
                        mediaPlayer.release()
                    }
                }
            }
        )

        sppViewModel.playTime.observe(
            this, androidx.lifecycle.Observer { time ->
                run {
                    totalTime = if (seenReview) {
                        0L
                    } else {
                        lastPlayTime + time
                    }
                }
            }
        )

        val prefs = getSharedPreferences("jerboa.app.spp.prefs", MODE_PRIVATE)

        if (!prefs.contains("firstLaunch")) {
            val prefsEdit = prefs.edit()
            prefsEdit.putBoolean("firstLaunch", true)
            prefsEdit.apply()
        }

        val firstLaunch: Boolean = prefs.getBoolean("firstLaunch", false)

        val prefsEdit = prefs.edit()
        prefsEdit.putBoolean("firstLaunch", false)
        prefsEdit.apply()

        lastReviewTries = if (!prefs.contains("reviewTries")) {
            prefsEdit.putLong("reviewTries", 0L)
            prefsEdit.apply()
            0L
        } else {
            prefs.getLong("reviewTries", 0L)
        }

        Log.d("review", "$lastReviewTries")

        if (!prefs.contains("playTime")) {
            prefsEdit.putLong("playTime", 0L)
            prefsEdit.apply()
            lastPlayTime = 0L
        } else {
            lastPlayTime = prefs.getLong("playTime", 0L)
            if (lastPlayTime > REVIEW_RATE_LIMIT_MILLIS) {
                requestUserReviewPrompt()
            }
        }
        Log.d("play time", "$lastPlayTime")

        val versionString =
            BuildConfig.VERSION_NAME + " (vc" + BuildConfig.VERSION_CODE + ") : " + Date(BuildConfig.TIMESTAMP)

        var showNews = false
        if (!firstLaunch) {
            Log.d("launch", "not first")
            if (!prefs.contains(news)) {
                Log.d("launch", "show news")
                val prefsEdit = prefs.edit()
                prefsEdit.putBoolean(news, true)
                prefsEdit.apply()
                showNews = true
            }
        }

        if (!prefs.contains("showToys")) {
            val prefsEdit = prefs.edit()
            prefsEdit.putBoolean("showToys", false)
            prefsEdit.apply()
        }

        menuPromptViewModel.onPositionChanged(
            Offset(
                prefs.getFloat("menuX", NULL_MENU_POSITION.x),
                prefs.getFloat("menuY", NULL_MENU_POSITION.y)
            )
        )

        toyMenuViewModel.onShowToysChanged(prefs.getBoolean("showToys", false))

//        if (BuildConfig.DEBUG){
//            prefs.edit().clear().apply()
//        }

        // play game services

        if (isGooglePlayGamesServicesInstalled(this)) {

            PlayGamesSdk.initialize(this)
            playGamesServicesLogin()

            syncAchievementsState()

            sppViewModel.setAchievementState(achievementStates)

        }

        sppViewModel.achievementStates.observe(
            this, androidx.lifecycle.Observer { states ->
                updatePlayGamesAchievements(states)
            }
        )

        val news: List<NewsItem> = listOf(
            NewsItem(R.string.news1, R.drawable.__5_0),
            NewsItem(R.string.news2, null),
            NewsItem(R.string.news3, null),
            NewsItem(R.string.news4, R.drawable.__5_3),
            NewsItem(R.string.news5, null),
            NewsItem(R.string.news6, R.drawable.__5_5),
            NewsItem(R.string.news7, null),
            NewsItem(R.string.news8, R.drawable.__6_0),
            NewsItem(R.string.news9, null),
            NewsItem(R.string.news10, R.drawable.news)
        ).reversed()

        val height = resources.displayMetrics.heightPixels
        val width = resources.displayMetrics.widthPixels

        val displayInfo = resources.displayMetrics
        val dpHeight = displayInfo.heightPixels / displayInfo.density
        val dpWidth = displayInfo.widthPixels / displayInfo.density
        val appInfo = AppInfo(
            versionString,
            firstLaunch,
            displayInfo.density,
            if (resources.getBoolean(R.bool.isTablet)) {
                displayInfo.density
            } else {
                1f
            },
            dpHeight,
            dpWidth
        )

        setContent {
            SPPTheme {
                screen(
                    sppViewModel,
                    aboutViewModel,
                    menuPromptViewModel,
                    toyMenuViewModel,
                    news,
                    Pair(width, height),
                    imageResources,
                    appInfo,
                    showNews
                )
            }
        }
    }

    public override fun onStart() {
        super.onStart()
        sppViewModel.startClock()
    }

    public override fun onStop()
    {
        val prefs = getSharedPreferences("jerboa.app.spp.prefs", MODE_PRIVATE)
        val prefsEdit = prefs.edit()
        prefsEdit.putLong("playTime", totalTime)
        prefsEdit.putFloat("menuX", menuPromptViewModel.position.value!!.x)
        prefsEdit.putFloat("menuY", menuPromptViewModel.position.value!!.y)
        prefsEdit.apply()
        super.onStop()
        sppViewModel.stopClock()
    }
}