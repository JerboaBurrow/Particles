package app.jerboa.spp.viewmodel

import android.media.MediaPlayer
import android.util.Log
import androidx.lifecycle.*
import app.jerboa.spp.incrementable
import java.util.*

const val REVIEW_RATE_LIMIT_MILLIS = 1000*60*30

class SPPViewModel : ViewModel() {
    // clock
    private val _clockTicking = MutableLiveData(true)
    private val _lastClock = MutableLiveData(System.currentTimeMillis())
    private val _playTime = MutableLiveData(0L)
    val playTime: MutableLiveData<Long> = _playTime

    fun stopClock() {
        _clockTicking.postValue(false)
    }

    fun startClock() {
        if (!_clockTicking.value!!) {
            _clockTicking.postValue(true)
            _lastClock.postValue(System.currentTimeMillis())
        }
    }

    fun onUpdateClock()
    {
        if (_clockTicking.value!!) {
            _playTime.postValue(_playTime.value!! + (System.currentTimeMillis() - _lastClock.value!!))
        }
        _lastClock.postValue(System.currentTimeMillis())
    }

    // review api
    private val _canReview = MutableLiveData(true)
    private val _requestingInAppReview = MutableLiveData(false)
    val requestingInAppReview: MutableLiveData<Boolean> = _requestingInAppReview
    private val _lastReviewRequestTime = MutableLiveData<Long>(System.currentTimeMillis())

    fun onRequestingInAppReview(){
        Log.d("onRequestingInAppReview","called")
        if (System.currentTimeMillis() - _lastReviewRequestTime.value!! > REVIEW_RATE_LIMIT_MILLIS && _canReview.value!!) {
            Log.d("onRequestingInAppReview","calling API")
            _requestingInAppReview.postValue(true)
            _lastReviewRequestTime.postValue(System.currentTimeMillis())
        }
    }

    fun onInAppReviewShown(){
        _requestingInAppReview.value = false
        _canReview.value = false
    }

    private val _achievementStates = MutableLiveData(
        mapOf(
            "GetLost" to Pair(0,0),
            "SuperFan" to Pair(0,0),
            "ShowMeWhatYouGot" to Pair(0,0),
            "AverageFan" to Pair(0,0),
            "AverageEnjoyer" to Pair(0,0),
            "StillThere" to Pair(0,0),
            "HipToBeSquare" to Pair(0,0),
            "CirclesTheWay" to Pair(0,0),
            "DoubleTrouble" to Pair(0,0)
        )
    )

    val achievementStates: MutableLiveData<Map<String, Pair<Int, Int>>> = _achievementStates

    // TUTORIAL

    private val _dismissedNews = MutableLiveData(false)
    val dismissedNews: MutableLiveData<Boolean> = _dismissedNews
    fun onDismissNews(){_dismissedNews.value = true}

    // PGS

    private val _requestingPlayServices = MutableLiveData(false)
    val requestingPlayServices: MutableLiveData<Boolean> = _requestingPlayServices

    fun onRequestPlayServices(){
        _requestingPlayServices.value = true
    }

    fun onRequestPGSAndNotInstalled(){
        _promptInstallPGS.value = true
    }

    fun onPromptInstallPGS(v: Boolean = false){
        _promptInstallPGS.value = false
        if (v) {
            _requestingInstallPGS.value = true
        }
    }

    fun onInstallPGSInitiated(){
        _requestingInstallPGS.value = false
    }

    private val _promptInstallPGS = MutableLiveData(false)
    val promptInstallPGS: MutableLiveData<Boolean> = _promptInstallPGS

    private val _requestingInstallPGS = MutableLiveData(false)
    val requestingInstallPGS: MutableLiveData<Boolean> = _requestingInstallPGS

    private val _playSuccess = MutableLiveData(false)
    val playSuccess: MutableLiveData<Boolean> = _playSuccess

    fun onPlaySuccess(v: Boolean)
    {
        _playSuccess.postValue(v)
    }

    fun onAchievementStateChanged(data: Pair<String,Int>){
        //Log.d("achievements", data.toString())
        val ach = data.first
        val value = data.second

        val state = _achievementStates.value!!.toMutableMap()

        if (!state.containsKey(ach)){
            Log.e("achievement state does not contain key",ach)
            return
        }

        if (state[ach]!!.first == state[ach]!!.second){
            return
        }

        if (ach in incrementable) {
            state[ach] = Pair(
                achievementStates.value!![ach]!!.first + value,
                achievementStates.value!![ach]!!.second
            )
        }
        else{
            state[ach] = Pair(1,1)
        }

        _achievementStates.postValue(state.toMap())
    }

    fun setAchievementState(states: MutableMap<String, Pair<Int, Int>>){
        _achievementStates.value = states
        achievementStates.value = states
    }

}