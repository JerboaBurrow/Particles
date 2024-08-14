package app.jerboa.spp.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

enum class SOCIAL {NOTHING, WEB, PLAY, YOUTUBE, GITHUB}

class AboutViewModel : ViewModel() {

    private val _dismissedTutorial = MutableLiveData(false)
    private val _resetTutorial = MutableLiveData(false)

    val dismissedTutorial: MutableLiveData<Boolean> = _dismissedTutorial
    val resetTutorial: MutableLiveData<Boolean> = _resetTutorial

    fun onResetTutorial(){
        _resetTutorial.value = true; _dismissedTutorial.value = false
        //onDisplayingMenuChanged(false)
    }
    fun onDismissTutorial(){_dismissedTutorial.value = true; _resetTutorial.value = false}


    private val _requestingLicenses = MutableLiveData(false)
    val requestingLicenses: MutableLiveData<Boolean> = _requestingLicenses

    fun onRequestingLicenses(){
        _requestingLicenses.value = true
    }

    private val _displayingAbout = MutableLiveData(false)
    val displayingAbout: MutableLiveData<Boolean> = _displayingAbout

    fun onDisplayingAboutChanged(newVal: Boolean){
        _displayingAbout.value = newVal
    }

    private val _requestingSocial = MutableLiveData(SOCIAL.NOTHING)
    val requestingSocial: MutableLiveData<SOCIAL> = _requestingSocial

    fun onRequestingSocial(v: SOCIAL){
        _requestingSocial.value = v
    }
}