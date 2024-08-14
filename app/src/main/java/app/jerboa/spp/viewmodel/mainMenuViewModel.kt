package app.jerboa.spp.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.Calendar

const val MAX_PARTICLES = 500000f
const val PARTICLES_SLIDER_DEFAULT = 100000f/ MAX_PARTICLES
const val MAX_LOG_SPEED = 0.30103f
const val MAX_LOG_FADE = 0.0f
const val MIN_LOG_FADE = -2.0f
const val MIN_LOG_MASS = -3f
const val MAX_LOG_MASS = 0.5f
const val MIN_LOG_AR = 1f
const val MAX_LOG_AR = 6f
const val MAX_LOG_ORBIT = 0.30103f
const val MIN_LOG_ORBIT = -1f
const val MAX_LOG_SPIN = 4.0f
const val MIN_LOG_SPIN = 2f

enum class COLOUR_MAP {
    R1,
    R2,
    ACE,
    C3,
    CB1,
    CB2,
    TRANS,
    PRIDE
}

enum class TOY {ATTRACTOR,REPELLOR,SPINNER,FREEZER,ORBITER, NOTHING}

enum class PARAM {MASS, SPEED, ATTRACTION, REPULSION, ORBIT, SPIN, PARTICLES, FADE}


class MainMenuViewModel: ViewModel() {
    private val _colourMap = MutableLiveData(COLOUR_MAP.R1)
    val colourMap: MutableLiveData<COLOUR_MAP> = _colourMap

    fun onSelectColourMap(v: COLOUR_MAP){
        _colourMap.value = v
    }

    private val _selectedDefaultColourMap = MutableLiveData(false)

    fun selectDefaultColourMap(){

        if (_selectedDefaultColourMap.value == false){return}

        val date = Calendar.getInstance()
        val month = date.get(Calendar.MONTH)
        val day = date.get(Calendar.DAY_OF_MONTH)

        if (month == Calendar.MARCH && day == 31){
            _colourMap.value = COLOUR_MAP.TRANS
        }
        else if (month == Calendar.JUNE && day == 28){
            _colourMap.value = COLOUR_MAP.PRIDE
        }
        else if (month == Calendar.APRIL && day == 6){
            _colourMap.value = COLOUR_MAP.ACE
        }

        _selectedDefaultColourMap.value = true
    }

    private val _toy = MutableLiveData(TOY.ATTRACTOR) // true attractor, false repeller
    val toy: MutableLiveData<TOY> = _toy
    fun onToyChanged(newVal: TOY){
        //Log.d("on attractor changed", newVal.toString())
        _toy.value = newVal
    }

    // sliders

    private val _particleNumber = MutableLiveData(PARTICLES_SLIDER_DEFAULT)
    val particleNumber: MutableLiveData<Float> = _particleNumber

    private val _speed = MutableLiveData(1.0f)
    val speed: MutableLiveData<Float> = _speed

    private val _fade = MutableLiveData(1.0f)
    val fade: MutableLiveData<Float> = _fade

    private val _attractorStrength = MutableLiveData(50000.0f)
    val attractorStrength: MutableLiveData<Float> = _attractorStrength

    private val _repellorStrength = MutableLiveData(50000.0f)
    val repellorStrength: MutableLiveData<Float> = _repellorStrength

    private val _mass = MutableLiveData(0.1f)
    val mass: MutableLiveData<Float> = _mass

    private val _orbit = MutableLiveData(0.5f)
    val orbitStrength: MutableLiveData<Float> = _orbit

    private val _spin = MutableLiveData(1500f)
    val spinStrength: MutableLiveData<Float> = _spin

    fun onParameterChanged(v: Pair<Float, PARAM>){
        when (v.second) {
            PARAM.PARTICLES -> {
                _particleNumber.value = v.first
            }
            PARAM.SPEED -> {
                _speed.value = v.first
            }
            PARAM.FADE -> {
                _fade.value = v.first
            }
            PARAM.ATTRACTION -> {
                _attractorStrength.value = v.first
            }
            PARAM.REPULSION -> {
                _repellorStrength.value = v.first
            }
            PARAM.MASS -> {
                _mass.value = v.first
            }
            PARAM.ORBIT -> {
                _orbit.value = v.first
            }
            PARAM.SPIN -> {
                _spin.value = v.first
            }
        }
    }

    private val _showToys = MutableLiveData(false)
    val showToys: MutableLiveData<Boolean> = _showToys

    fun onShowToysChanged(v: Boolean){
        _showToys.value = v
    }

    // ADAPT

    private val _allowAdapt = MutableLiveData(true)
    private val _autoAdaptMessage = MutableLiveData(false)
    val allowAdapt: MutableLiveData<Boolean> = _allowAdapt
    val autoAdaptMessage: MutableLiveData<Boolean> = _autoAdaptMessage

    fun onAdapt(v: Float) {
        _particleNumber.postValue(v)
        _autoAdaptMessage.postValue(true)
    }

    fun onAllowAdaptChanged(){
        _allowAdapt.value = !_allowAdapt.value!!
    }

    fun onAdaptMessageShown(){
        _autoAdaptMessage.value = false
    }
}