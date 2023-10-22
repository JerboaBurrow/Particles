package app.jerboa.spp.ui

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.util.Log
import android.view.DragEvent
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.MotionEventCompat
import app.jerboa.spp.ViewModel.COLOUR_MAP
import app.jerboa.spp.ViewModel.PARTICLES_SLIDER_DEFAULT
import app.jerboa.spp.ViewModel.TOY
import app.jerboa.spp.ui.view.DRAG_ACTION
import app.jerboa.spp.ui.view.SPPRenderer

class SPPView (
    context: Context,
    attr: AttributeSet? = null,
    private val resolution: Pair<Int,Int>,
    private val onDisplayingMenuChanged: (Boolean) -> Unit,
    private val onAchievementStateChanged: (Pair<String,Int>) -> Unit,
    private val onToyChanged: (TOY) -> Unit,
    private val onRequestReview: () -> Unit,
    private val onAdapt: (Float) -> Unit,
    var placingToy: TOY = TOY.ATTRACTOR,
    var particleNumber: Float = PARTICLES_SLIDER_DEFAULT,
    var allowAdapt: Boolean = true,
    var colourMap: COLOUR_MAP = COLOUR_MAP.R1
    ) : GLSurfaceView(context,attr), GestureDetector.OnGestureListener {

    private val renderer = SPPRenderer(
        resolution,
        onAchievementStateChanged,
        onToyChanged,
        onRequestReview,
        onAdapt,
        particleNumber,
        allowAdapt,
        colourMap
    )
    private val gestures: GestureDetectorCompat = GestureDetectorCompat(context,this)
    var isDisplayingMenuChanged: Boolean = false

    private var lastTouchX: Float = 0f
    private var lastTouchY: Float = 0f
    private var posX: Float = 0f
    private var posY: Float = 0f
    private var pointerId: Int = 0

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
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                event.actionIndex.also { pointer ->
                    lastTouchX = event.getX(pointer)
                    lastTouchY = event.getY(pointer)
                }
                renderer.drag(lastTouchX,lastTouchY,DRAG_ACTION.START, placingToy)
                pointerId = event.getPointerId(0)
            }
            MotionEvent.ACTION_MOVE -> {
                val pointer = event.findPointerIndex(pointerId)
                if (pointer in 0..event.pointerCount) {

                    val (x: Float, y: Float) = pointer.let {
                        event.getX(pointer) to event.getY(pointer)
                    }

                    posX += x - lastTouchX
                    posY += y - lastTouchY

                    invalidate()

                    lastTouchX = x
                    lastTouchY = y

                    renderer.drag(x, y, DRAG_ACTION.CONTINUE, placingToy)
                }
            }
            MotionEvent.ACTION_UP -> {
                pointerId = MotionEvent.INVALID_POINTER_ID
                renderer.drag(lastTouchX,lastTouchY,DRAG_ACTION.STOP, placingToy)
            }
            MotionEvent.ACTION_POINTER_UP -> {
                event.actionIndex.also { pointer ->
                    event.getPointerId(pointer).takeIf { it == pointerId }
                        ?.run {
                            val newPointerIndex = if (pointer == 0) 1 else 0
                            lastTouchX = event.getX(newPointerIndex)
                            lastTouchY = event.getY(newPointerIndex)
                            renderer.drag(lastTouchX,lastTouchY,DRAG_ACTION.STOP, placingToy)
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

    override fun onScroll(p0: MotionEvent, p1: MotionEvent, p2: Float, p3: Float): Boolean {
        return true
    }

    override fun onLongPress(p0: MotionEvent) {
        return
    }

    override fun onFling(p0: MotionEvent, p1: MotionEvent, p2: Float, p3: Float): Boolean {
        return true
    }

}