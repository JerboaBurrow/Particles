package app.jerboa.spp.ui

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.core.view.GestureDetectorCompat
import app.jerboa.spp.ViewModel.COLOUR_MAP
import app.jerboa.spp.ViewModel.PARTICLES_SLIDER_DEFAULT
import app.jerboa.spp.ViewModel.TOY
import app.jerboa.spp.ui.view.SPPRenderer

class SPPView (
    context: Context,
    attr: AttributeSet? = null,
    private val resolution: Pair<Int,Int>,
    private val onDisplayingMenuChanged: (Boolean) -> Unit,
    private val onAchievementStateChanged: (Pair<String,Int>) -> Unit,
    private val onAdapt: (Float) -> Unit,
    var placingToy: TOY = TOY.ATTRACTOR,
    var particleNumber: Float = PARTICLES_SLIDER_DEFAULT,
    var allowAdapt: Boolean = true,
    var colourMap: COLOUR_MAP = COLOUR_MAP.R1
    ) : GLSurfaceView(context,attr), GestureDetector.OnGestureListener {

    private val renderer = SPPRenderer(resolution,onAchievementStateChanged,onAdapt,particleNumber,allowAdapt,colourMap)
    private val gestures: GestureDetectorCompat = GestureDetectorCompat(context,this)
    var isDisplayingMenuChanged: Boolean = false

    init {
        setEGLContextClientVersion(3)
        preserveEGLContextOnPause = true
        setRenderer(renderer)
    }

    @JvmName("setParticleNumber1")
    fun setParticleNumber(v: Float){
        renderer.setParticleNumber(v)
    }

    @JvmName("setAllowAdapt1")
    fun setAllowAdapt(v: Boolean){
        renderer.setAllowAdapt(v)
    }

    @JvmName("setColourMap1")
    fun setColourMap(v: COLOUR_MAP){
        renderer.setColourMap(v)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (gestures.onTouchEvent(event)) {
            true
        } else {
            super.onTouchEvent(event)
        }
    }

    override fun onDown(p0: MotionEvent): Boolean {
        return true
    }

    override fun onShowPress(p0: MotionEvent) {
        return
    }

    override fun onSingleTapUp(p0: MotionEvent): Boolean {
        val x: Float = p0!!.x
        val y: Float = p0!!.y

        when (placingToy) {
            TOY.ATTRACTOR -> {
                //Log.d("tap", "attractor")
                renderer.tap(x, y, SPPRenderer.TapType.ATTRACTOR)
            }
            TOY.REPELLOR -> {
                renderer.tap(x, y, SPPRenderer.TapType.REPELLOR)
                //Log.d("tap", "repeller")
            }
            TOY.SPINNER -> {
                renderer.tap(x, y, SPPRenderer.TapType.SPINNER)
                //Log.d("tap", "spinner")
            }
        }

        return true
    }

    override fun onScroll(p0: MotionEvent, p1: MotionEvent, p2: Float, p3: Float): Boolean {
        return true
    }

    override fun onLongPress(p0: MotionEvent) {
        return
    }

    override fun onFling(p0: MotionEvent, p1: MotionEvent, p2: Float, p3: Float): Boolean {
        if (p0 != null) {
            if (resolution.second-p0.y < resolution.second*0.25f){
                Log.d("fling", p0.y.toString())
                isDisplayingMenuChanged = !isDisplayingMenuChanged
                onDisplayingMenuChanged(true)
            }
        }
        return true
    }

}