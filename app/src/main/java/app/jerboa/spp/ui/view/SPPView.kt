package app.jerboa.spp.ui.view

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import app.jerboa.spp.viewmodel.COLOUR_MAP
import app.jerboa.spp.viewmodel.ToyMenuViewModel
import app.jerboa.spp.viewmodel.PARTICLES_SLIDER_DEFAULT
import app.jerboa.spp.viewmodel.SPPViewModel
import app.jerboa.spp.viewmodel.TOY

class SPPView (
    context: Context,
    attr: AttributeSet? = null,
    private val resolution: Pair<Int,Int>,
    private val sppViewModel: SPPViewModel,
    private val toyMenuViewModel: ToyMenuViewModel,
    var placingToy: TOY = TOY.ATTRACTOR,
    var particleNumber: Float = PARTICLES_SLIDER_DEFAULT,
    private var allowAdapt: Boolean = true,
    private var colourMap: COLOUR_MAP = COLOUR_MAP.R1
    ) : GLSurfaceView(context,attr), GestureDetector.OnGestureListener {

    private val renderer = SPPRenderer(
        resolution,
        sppViewModel,
        toyMenuViewModel,
        particleNumber,
        allowAdapt,
        colourMap
    )
    private val gestures: GestureDetector = GestureDetector(context,this)
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

    fun pause(v: Boolean) { renderer.pause(v) }
    fun setSpeed(v: Float) { renderer.setSpeed(v) }
    fun setMass(v: Float) { renderer.setMass(v) }
    fun setAttraction(v: Float) { renderer.setAttraction(v) }
    fun setRepulsion(v: Float) { renderer.setRepulsion(v) }
    fun setFade(v: Float) { renderer.setFade(v) }
    fun setSpin(v: Float) { renderer.setSpin(v) }
    fun setOrbit(v: Float) { renderer.setOrbit(v) }
    fun showToys(v: Boolean){ renderer.setShowToys(v) }
    fun clearToys() { renderer.clearToys() }

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
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                event.actionIndex.also { pointer ->
                    renderer.handleTouchEvent(
                        event.getX(pointer),
                        event.getY(pointer),
                        DRAG.START,
                        placingToy,
                        0U
                    )
                }

            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                event.actionIndex.also { pointer ->
                    renderer.handleTouchEvent(
                        event.getX(pointer),
                        event.getY(pointer),
                        DRAG.START,
                        placingToy,
                        event.getPointerId(event.actionIndex).toUInt()
                    )
                }
            }
            MotionEvent.ACTION_MOVE -> {
                for (i in 0 until event.pointerCount) {
                    renderer.handleTouchEvent(
                        event.getX(i),
                        event.getY(i),
                        DRAG.CONTINUE,
                        placingToy,
                        event.getPointerId(i).toUInt()
                    )
                }
            }
            MotionEvent.ACTION_UP -> {
                renderer.handleTouchEvent(event.getX(0),event.getY(0),DRAG.STOP, placingToy, event.getPointerId(event.actionIndex).toUInt())
            }
            MotionEvent.ACTION_POINTER_UP -> {
                event.actionIndex.also { pointer ->
                    event.getPointerId(pointer)
                    .run {
                        renderer.handleTouchEvent(event.getX(pointer),event.getY(pointer),DRAG.STOP, placingToy, event.getPointerId(event.actionIndex).toUInt())
                    }
                }
            }
        }
        return gestures.onTouchEvent(event)
    }

    override fun onDown(p0: MotionEvent): Boolean {
        return true
    }

    override fun onShowPress(p0: MotionEvent) {
        return
    }

    override fun onSingleTapUp(p0: MotionEvent): Boolean {
        return true
    }

    override fun onScroll(e1: MotionEvent?, p0: MotionEvent, p2: Float, p3: Float): Boolean {
        return true
    }

    override fun onLongPress(p0: MotionEvent) {
        return
    }

    override fun onFling(e1: MotionEvent?, p0: MotionEvent, p2: Float, p3: Float): Boolean {
        return true
    }

}