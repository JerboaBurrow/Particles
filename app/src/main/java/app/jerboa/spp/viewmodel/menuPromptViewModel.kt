package app.jerboa.spp.viewmodel

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

enum class MUSIC {FORREST, RAIN, NOTHING}

val NULL_MENU_POSITION = Offset(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY)

class MenuPromptViewModel : ViewModel() {

    private val _displayingMenu = MutableLiveData(false)
    val displayingMenu : MutableLiveData<Boolean> = _displayingMenu

    fun onDisplayingMenuChanged(newVal: Boolean) {
        _displayingMenu.value = newVal
    }

    private val _displayingToyMenu = MutableLiveData(false)
    val displayingToyMenu: MutableLiveData<Boolean> = _displayingToyMenu

    fun onDisplayingToyMenuChanged(newVal: Boolean) {
        _displayingToyMenu.value = newVal
    }

    private val _displayingSound = MutableLiveData(false)
    val displayingSound: MutableLiveData<Boolean> = _displayingSound
    fun onDisplayingMusicChanged(newVal: Boolean) {
        _displayingSound.value = newVal
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

    private val _position = MutableLiveData(Offset(0.0f, 0.0f))
    val position: MutableLiveData<Offset> = _position

    fun onPositionChanged(p: Offset) {
        _position.value = p
    }

}