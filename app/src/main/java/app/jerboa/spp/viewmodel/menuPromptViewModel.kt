package app.jerboa.spp.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

enum class MUSIC {FORREST, RAIN, NOTHING}

class MenuPromptViewModel : ViewModel() {

    private val _displayingMenu = MutableLiveData(false)
    val displayingMenu : MutableLiveData<Boolean> = _displayingMenu

    //private val _lastClick = MutableLiveData(0L)
    fun onDisplayingMenuChanged(newVal: Boolean){
        _displayingMenu.value = newVal

        if (_displayingMenu.value == true){
            _displayingSound.value = false
        }

        //_lastClick.value = System.currentTimeMillis()

    }

    private val _displayingSound = MutableLiveData(false)
    val displayingSound: MutableLiveData<Boolean> = _displayingSound
    fun onDisplayingMusicChanged(newVal: Boolean){
        _displayingSound.value = newVal
        if (_displayingSound.value == true){
            _displayingMenu.value = false
        }
    }

    private val _playingMusic = MutableLiveData(MUSIC.NOTHING)
    val playingMusic: MutableLiveData<MUSIC> = _playingMusic

    fun onMusicSelected(v: MUSIC) {
        _playingMusic.value = v
    }

    private val _paused = MutableLiveData(false)
    val paused: MutableLiveData<Boolean> = _paused

    fun onPause() {
        _paused.postValue(!_paused.value!!)
    }

    private val _clear = MutableLiveData(false)
    val clear: MutableLiveData<Boolean> = _clear

    fun onClear(v: Boolean) {
        _clear.postValue(v)
    }

}