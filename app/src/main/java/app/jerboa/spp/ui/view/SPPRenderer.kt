package app.jerboa.spp.ui.view

import android.opengl.GLES31.*
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import app.jerboa.glskeleton.utils.*
import app.jerboa.spp.ViewModel.*
import app.jerboa.spp.data.*
import app.jerboa.spp.utils.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.util.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.*
import kotlin.random.Random
import android.opengl.GLES31 as gl3

const val DEBUG: Boolean = false
const val PERFORMANCE: Boolean = false

enum class DRAG_ACTION {START, STOP, CONTINUE}

class SPPRenderer(
    private val resolution: Pair<Int,Int>,
    private val onAchievementStateChanged: (Pair<String,Int>) -> Unit,
    private val onToyChanged: (TOY) -> Unit,
    private val onRequestReview: () -> Unit,
    private val onAdapt: (Float) -> Unit,
    private val onUpdateClock: () -> Unit,
    private var particleNumber: Float = PARTICLES_SLIDER_DEFAULT,
    private var allowAdapt: Boolean = true,
    private var colourMap: COLOUR_MAP = COLOUR_MAP.R1
    ) : GLSurfaceView.Renderer {

    private val frameIndependent = true

    private var lastReviewRequest: Long = System.currentTimeMillis()

    private var begun: Long = System.currentTimeMillis()

    private val RNG = Random(System.nanoTime())
    private var debugString = ""

    private var clock: Float = 0f
    private var timeSinceLastTap: Float = 0f

    private var transitionStep: Int = 0
    private val transitionSteps: Int = 120

    private var stillThereAchieved: Boolean = false

    private var allParticlesOfScreen: Boolean = false
    private var allParticlesOfScreenAchieved: Boolean = false

    private var toysInSquareFormation: Boolean = false
    private var squareAchieved: Boolean = false

    private var toyInsideAnother: Boolean = false
    private var insideAchieved: Boolean = false

    private var toysInCircleFormation: Boolean = false
    private var circleAchieved: Boolean = false

    private var timeSinceLastAdapt = 0f
    private val MAXN = MAX_PARTICLES.toInt()
    private val MAX_TEX_DIM = ceil(sqrt(MAXN.toDouble())).toInt()

    private val particleBuffer = ByteBuffer.allocateDirect((4*MAX_TEX_DIM*MAX_TEX_DIM) * 4).order(ByteOrder.nativeOrder()).asFloatBuffer()
    private val pBuffer = ByteBuffer.allocateDirect((4*MAX_TEX_DIM*MAX_TEX_DIM) * 4).order(ByteOrder.nativeOrder()).asFloatBuffer()
    private val qBuffer = ByteBuffer.allocateDirect((4*MAX_TEX_DIM*MAX_TEX_DIM) * 4).order(ByteOrder.nativeOrder()).asFloatBuffer()

    private var p = MAXN
    private var newP = MAXN
    private var generatedParticles = 0
    private var generating: Boolean = false
    // particle scale, gl_PointSize
    private val scale = 3f
    private val projection: FloatArray = FloatArray(16){0f}
    private val invProjection: FloatArray = FloatArray(16){0f}

    private var reset: Boolean = false
    private var recompileDrawShader: Boolean = false

    private val nCells = 10
    private val dcx = resolution.first / nCells.toFloat()
    private val dcy = resolution.second / nCells.toFloat()

    // keep track of frame rate
    private var delta: Long = 0
    private var last: Long = System.nanoTime()
    // id for a frame (will be kep mod 60)
    private var frameNumber: Int = 0
    // rate-limit touch events
    private val touchRateLimit = 10 // frames
    private var lastTapped: Int = 0
    // smoothed framerate states
    private val deltas: FloatArray = FloatArray(60){0f}

    private val DEBUG_GL: Boolean = false
    private val DEBUG_TOYS: Boolean = false
    private var DEMO_REAL: Boolean = true
    private val queryFrequency: Int = 59
    private val queryFormationFrequency: Int = 30

    private var demoId = 0
    private val demoFrames = 30
    // kept mod demoFrames
    private var demoFrameId = 0

    // model parameters for underdamped Langevin equations
    // using Niels-Oded 2nd order integrator https://arxiv.org/abs/1212.1244
    private val M = 0.001
    private val J = 0.01
    private var dt = 1.0/60.0f
    // DR,cr,br,ar,ct,bt,at,alpha,beta,gamma
    // can be computed once if DR, M, J do not vary
    private var DR = sqrt(2.0*0.01*dt)

    private var cr = (1.0*dt)/(2.0*J)
    private var br = 1.0 / (1.0+cr)
    private var ar = (1.0-cr)*br

    private var ct = (1.0*dt)/(2.0*M)
    private var bt = 1.0 / (1.0+ct)
    private var at = (1.0-ct)*bt

    private var alpha = bt*dt*dt/M
    private var beta = br*dt*dt/J
    private var gamma = br*dt/(2.0*J)

    private val attr = FloatArray(16){0f}
    private val rep = FloatArray(16){0f}
    private val spin = FloatArray(16){0f}
    private val freezer = FloatArray(16){0f}

    private var bounds: Pair<Pair<Float,Float>,Pair<Float,Float>>

    private val fbos = ByteBuffer.allocateDirect(1 * 4).order(ByteOrder.nativeOrder()).asIntBuffer()
    private val drawBuffers = ByteBuffer.allocateDirect(2 * 4).order(ByteOrder.nativeOrder()).asIntBuffer()

    // query buffer
    private val queryBuffer = ByteBuffer.allocateDirect(1 * 4).order(ByteOrder.nativeOrder()).asIntBuffer()

    private val anySamples = ByteBuffer.allocateDirect((1) * 4).order(ByteOrder.nativeOrder()).asIntBuffer()

    // texture ids
    private val texBuffer = ByteBuffer.allocateDirect(4 * 4).order(ByteOrder.nativeOrder()).asIntBuffer()
    // texture book keeping
    enum class TextureId {
        X,Y,PARAM,CMAP
    }

    private val Textures: EnumMap<TextureId, Int> = EnumMap(
        mutableMapOf(
            TextureId.X to 0,
            TextureId.Y to 1,
            TextureId.PARAM to 2,
            TextureId.CMAP to 3
        )
    )

    // 8 so attractors and 8 repeller positions (x,y) can be
    // stored in a GL::mat4 (4x4 matrix)
    private val toysAlpha = 0.5f
    private val maxAorR = 8
    private val attractors = mutableListOf<Pair<Float,Float>>()
    private val repellors = mutableListOf<Pair<Float,Float>>()
    private val spinners = mutableListOf<Pair<Float,Float>>()
    private val freezers = mutableListOf<Pair<Float,Float>>()
    // drawing parameters
    private val arScale = scale * 30f
    private val atrPeriod = 60*3
    private var arTime = 0
    private var contTime = 0

    private var draggedToy: Pair<TOY, Int>? = null
    private var dragDelta: Vec2 = Vec2(0f,0f)
    private var dragStartTime: Long = 0L
    private var dragPlacedToy: Boolean = false

    data class TapDelta (val distance: Float, val timeMillis: Long)
    private val tapDelta = TapDelta(arScale*0.5f, 100L)

    // shader for particle drawing (and vertices)

    private val particleDrawShader = Shader(
        ParticleDrawShaderData().vertexShader,
        ParticleDrawShaderData().fragmentShader,
        name = "ParticleDrawShader"
    )

    private val drawVertices: FloatBuffer

    // now for the toys

    private val toyDrawShader = Shader(
        ToyDrawShaderData().vertexShader,
        ToyDrawShaderData().fragmentShader,
        name = "ToyDrawShader"
    )

    // compute shader and vertices (note gl es 300 not 310 so not actually a "gl compute shader")
    //    we just "draw" to a quad that happens to also compute wat we need!

    private val computeShader = Shader(
        NielsOdedIntegratorShaderData().vertexShader,
        NielsOdedIntegratorShaderData().fragmentShader,
        name = "ComputeDrawShader"
    )

    private val computeVerts: FloatBuffer
    private val computeTexCoords: FloatBuffer

    private var paused: Boolean = false

    init {

        Matrix.orthoM(projection,0,0f,resolution.first.toFloat(),0f,resolution.second.toFloat(),0.0f,100f)
        Matrix.invertM(invProjection,0,projection,0)

        val minWorld = screenToWorld(0f,0f)
        val maxWorld = screenToWorld(resolution.first.toFloat(),resolution.second.toFloat())

        val l = Pair(minWorld[0],minWorld[1])
        val L = Pair(maxWorld[0],maxWorld[1])
        bounds = Pair(l,L)

        // allocate vertex for particle
        // we will use instanced rendering, and our simulation texture data, so only a dummy vertex is needed!
        drawVertices = ByteBuffer.allocateDirect(3 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer()
        drawVertices.put(
            listOf<Float>(
                0f, 0f, 0f
            ).toFloatArray()
        )
        drawVertices.flip()
        drawVertices.limit(3)

        // a single quad (square) for GPGPU computations (want a 1-1 mapping from texture pixels to texture coords)
        computeVerts = ByteBuffer.allocateDirect(6*3 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer()
        //FloatBuffer.allocate(6 * 3)
        computeVerts.put(
            listOf<Float>(
                -1f, -1f, 0f,
                1f, -1f, 0f,
                1f, 1f, 0f,
                -1f, -1f, 0f,
                -1f, 1f, 0f,
                1f, 1f, 0f
            ).toFloatArray()
        )
        computeVerts.flip()
        computeVerts.limit(6 * 3)

        // map each corner in the square (verts) to a corner in the acutal texture (want a 1-1 mapping)
        computeTexCoords = ByteBuffer.allocateDirect(6*2 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer()
        computeTexCoords.put(
            listOf<Float>(
                0f, 0f,
                1f, 0f,
                1f, 1f,
                0f, 0f,
                0f, 1f,
                1f, 1f
            ).toFloatArray()
        )
        computeTexCoords.flip()
        computeTexCoords.limit(6 * 2)
    }

    fun pause(v: Boolean)
    {
        paused = v
    }

    private fun achievements(){
        if (DEMO_REAL){return}
        val dt = delta.toFloat()*1e-9f
        clock += dt
        timeSinceLastTap += dt

        if (clock > 30f){
            onAchievementStateChanged(
                Pair(
                    "AverageFan",
                    clock.toInt()
                )
            )

            onAchievementStateChanged(
                Pair(
                    "AverageEnjoyer",
                    clock.toInt()
                )
            )

            onAchievementStateChanged(
                Pair(
                    "SuperFan",
                    clock.toInt()
                )
            )

            clock = 0f
        }

        if (attractors.size == maxAorR && repellors.size == maxAorR){
            onAchievementStateChanged(
                Pair(
                    "ShowMeWhatYouGot",
                    1
                )
            )
        }
        if (!stillThereAchieved && timeSinceLastTap >= 60*10){
            onAchievementStateChanged(
                Pair(
                    "StillThere",
                    1
                )
            )
            stillThereAchieved = true
        }

        if (!allParticlesOfScreenAchieved && allParticlesOfScreen){
            onAchievementStateChanged(
                Pair(
                    "GetLost",
                    1
                )
            )
            allParticlesOfScreenAchieved = true
        }

        if (!insideAchieved && toyInsideAnother){
            onAchievementStateChanged(
                Pair(
                    "DoubleTrouble",
                    1
                )
            )
            insideAchieved = true
        }

        if (!squareAchieved && toysInSquareFormation){
            onAchievementStateChanged(
                Pair(
                    "HipToBeSquare",
                    1
                )
            )
            squareAchieved = true
        }

        if (!circleAchieved && toysInCircleFormation){
            onAchievementStateChanged(
                Pair(
                    "CirclesTheWay",
                    1
                )
            )
            circleAchieved = true
        }
    }

    fun screenToWorld(x: Float, y: Float): FloatArray {
        val s = floatArrayOf(2f*x/resolution.first.toFloat()-1f,2f*y/resolution.second.toFloat()-1f,0f,1f)
        val r = floatArrayOf(0f,0f,0f,0f)
        Matrix.multiplyMV(r,0,invProjection,0,s,0)
        return r
    }

    fun worldToScreen(x: Float, y: Float): FloatArray{
        val s = floatArrayOf(x,y,0f,1f)
        val r = floatArrayOf(0f,0f,0f,0f)
        Matrix.multiplyMV(r,0,projection,0,s,0)
        r[0] = (r[0]+1f) * resolution.first.toFloat()/2f
        r[1] = (r[1]+1f) * resolution.second.toFloat()/2f
        return r
    }

    private fun toySelected(x: Float, y: Float, eps: Float = 2.0f*arScale): Pair<TOY, Int>? {

        for (i in 0 until attractors.size){
            val a = attractors[i]
            if (norm2(x,y,a.first,a.second) < eps*eps){

                return Pair(TOY.ATTRACTOR, i)
            }
        }

        for (i in 0 until repellors.size){
            val a = repellors[i]
            if (norm2(x,y,a.first,a.second) < eps*eps){
                return Pair(TOY.REPELLOR, i)
            }
        }

        for (i in 0 until spinners.size){
            val a = spinners[i]
            if (norm2(x,y,a.first,a.second) < eps*eps){
                return Pair(TOY.SPINNER, i)
            }
        }

        for (i in 0 until freezers.size){
            val a = freezers[i]
            if (norm2(x,y,a.first,a.second) < eps*eps){
                return Pair(TOY.FREEZER, i)
            }
        }

        return null
    }

    // drag event
    fun drag(x: Float, y: Float, state: DRAG_ACTION, toyType: TOY){

        val w = screenToWorld(x,resolution.second-y)
        val wx = w[0]
        val wy = w[1]

        when (state){
            DRAG_ACTION.START -> {
                dragStartTime = System.currentTimeMillis()
                val toy = toySelected(wx, wy)
                if (toy != null){
                    draggedToy = toy
                    onToyChanged(toy.first)
                    dragPlacedToy = false
                }
                else {
                    when (toyType)
                    {
                        TOY.ATTRACTOR -> {
                            if (attractors.size < maxAorR){
                                attractors.add(Pair(wx, wy))
                            }
                            draggedToy = Pair(TOY.ATTRACTOR, attractors.size-1)
                            onToyChanged(TOY.ATTRACTOR)
                            dragPlacedToy = true
                        }
                        TOY.REPELLOR -> {
                            if (repellors.size < maxAorR) {
                                repellors.add(Pair(wx, wy))
                            }
                            draggedToy = Pair(TOY.REPELLOR, repellors.size-1)
                            onToyChanged(TOY.REPELLOR)
                            dragPlacedToy = true
                        }
                        TOY.SPINNER -> {
                            if (spinners.size < maxAorR){
                                spinners.add(Pair(wx, wy))
                            }
                            draggedToy = Pair(TOY.SPINNER, spinners.size-1)
                            onToyChanged(TOY.SPINNER)
                            dragPlacedToy = true
                        }
                        TOY.FREEZER -> {
                            if (spinners.size < maxAorR){
                                freezers.add(Pair(wx, wy))
                            }
                            draggedToy = Pair(TOY.FREEZER, freezers.size-1)
                            onToyChanged(TOY.FREEZER)
                            dragPlacedToy = true
                        }
                        TOY.NOTHING -> {dragPlacedToy = false}
                    }
                }
                dragDelta = Vec2(0f,0f)
            }
            DRAG_ACTION.STOP -> {
                draggedToy = null

                if (dragPlacedToy){dragPlacedToy = false; return}

                val dragDistance2 = dragDelta.x*dragDelta.x + dragDelta.y*dragDelta.y
                val dragTime = System.currentTimeMillis() - dragStartTime
                dragStartTime = 0L
                if (dragDistance2 < tapDelta.distance*tapDelta.distance || dragTime < tapDelta.timeMillis)
                {
                    tap(x, y, toyType)
                }

            }
            DRAG_ACTION.CONTINUE -> {
                var dx: Float = 0f
                var dy: Float = 0f
                val toy = draggedToy
                if (toy != null) {
                    when (toy.first) {
                        TOY.ATTRACTOR -> {
                            if (toy.second in attractors.indices) {
                                dx = wx - attractors[toy.second].first
                                dy = wy - attractors[toy.second].second
                                attractors[toy.second] = Pair(wx, wy)
                            }
                        }
                        TOY.REPELLOR -> {
                            if (toy.second in repellors.indices) {
                                dx = wx - repellors[toy.second].first
                                dy = wy - repellors[toy.second].second
                                repellors[toy.second] = Pair(wx, wy)
                            }
                        }
                        TOY.SPINNER -> {
                            if (toy.second in spinners.indices) {
                                dx = wx - spinners[toy.second].first
                                dy = wy - spinners[toy.second].second
                                spinners[toy.second] = Pair(wx, wy)
                            }
                        }
                        TOY.FREEZER -> {
                            if (toy.second in freezers.indices) {
                                dx = wx - freezers[toy.second].first
                                dy = wy - freezers[toy.second].second
                                freezers[toy.second] = Pair(wx, wy)
                            }
                        }
                        TOY.NOTHING -> {}
                    }
                }
                dragDelta = Vec2(dragDelta.x + sqrt(dx*dx), dragDelta.y + sqrt(dy*dy))
            }
        }
    }

    // propagate a tap event
    fun tap(x: Float,y: Float, type: TOY = TOY.ATTRACTOR){
        if (DEMO_REAL){return}
        val w = screenToWorld(x,resolution.second-y)
        val wx = w[0]
        val wy = w[1]
        if (lastTapped>touchRateLimit) {
            force(wx, wy, type, 2.0f*arScale)
            lastTapped = 0
        }
        timeSinceLastTap = 0f
    }
    
    // add an attractor or repeller toy
    private fun force(x: Float, y: Float, mode: TOY = TOY.NOTHING, eps: Float = scale){

        if (mode == TOY.ATTRACTOR){
            var removed = false
            for (i in 0 until attractors.size){
                val a = attractors[i]
                if (norm2(x,y,a.first,a.second) < eps*eps){
                    attractors.removeAt(i)
                    removed=true
                    break
                }
            }
            if (!removed && attractors.size < maxAorR){
                attractors.add(Pair(x,y))
            }
        }
        else if (mode == TOY.REPELLOR){
            var removed = false
            for (i in 0 until repellors.size){
                val a = repellors[i]
                if (norm2(x,y,a.first,a.second) < eps*eps){
                    repellors.removeAt(i)
                    removed = true
                    break
                }
            }
            if (!removed && repellors.size < maxAorR){
                repellors.add(Pair(x,y))
            }
        }
        else if (mode == TOY.SPINNER){
            var removed = false
            for (i in 0 until spinners.size){
                val a = spinners[i]
                if (norm2(x,y,a.first,a.second) < eps*eps){
                    spinners.removeAt(i)
                    removed = true
                    break
                }
            }
            if (!removed && spinners.size < maxAorR){
                spinners.add(Pair(x,y))
            }
        }
        else if (mode == TOY.FREEZER){
            var removed = false
            for (i in 0 until freezers.size){
                val a = freezers[i]
                if (norm2(x,y,a.first,a.second) < eps*eps){
                    freezers.removeAt(i)
                    removed = true
                    break
                }
            }
            if (!removed && freezers.size < maxAorR){
                freezers.add(Pair(x,y))
            }
        }

    }

    private fun pointsRoughlyCircular(points: List<Pair<Float,Float>>, tolerance: Float = 0.33f): Boolean{

        if (points.size<6){ return false }

        var mux = 0f
        var muy = 0f

        for (i in points.iterator()){
            mux += i.first
            muy += i.second
        }

        mux /= points.size
        muy /= points.size

        var sxx = 0f
        var syy = 0f
        var sxy = 0f

        for (i in points.iterator()){
            val rx = i.first-mux
            val ry = i.second-muy

            sxx += rx*rx
            syy += ry*ry
            sxy += rx*ry
        }

        sxx /= points.size
        syy /= points.size
        sxy /= points.size

        val b = sxx+syy
        val c = 4*(sxx*syy-sxy*sxy)

        val det = sqrt(b*b-c)

        val m = (b+det)/(b-det)

        if(DEBUG){Log.d("circle","$sxx, $syy, $sxy, $det, $b, $m")}

        if ( abs(m-1) < tolerance){
            return true
        }

        return false

    }

    private fun pointsRoughlySquare(points: List<Pair<Float,Float>>, tolerance: Float = 160f*160f): Boolean{
        if (points.size != 4){ return false }

        val dists = mutableListOf<Float>()

        //Log.d("square",points.toString())

        for (i in points.indices){
            for (j in points.indices){
                if (i != j){
                    val rx = points[i].first-points[j].first
                    val ry = points[i].second-points[j].second
                    val d2 = rx*rx+ry*ry

                    if (dists.size == 0) { dists.add(d2) }
                    if (dists.size == 1){
                        if (abs(dists[0] - d2 ) > tolerance){
                            dists.add(d2)
                        }
                    }

                    if (dists.size == 2){
                        if (abs(dists[0] - d2 ) > tolerance && abs(dists[1] - d2 ) > tolerance){
                            //Log.d("square",dists.toString() + " $d2")
                            return false
                        }
                    }
                }
            }
        }

        return true
    }

    private fun isSquareFormation(){
        val lAttractors = attractors.toList()
        val lRepellors = repellors.toList()
        val lSpinners = spinners.toList()
        if (lAttractors.size == 4 && lRepellors.isEmpty() && lSpinners.isEmpty()){
            val points = listOf(
                lAttractors[0],
                lAttractors[1],
                lAttractors[2],
                lAttractors[3]
            )
            toysInSquareFormation = pointsRoughlySquare(points)
        }
        else if (lRepellors.size == 4 && lAttractors.isEmpty() && lSpinners.isEmpty()){
            val points = listOf(
                lRepellors[0],
                lRepellors[1],
                lRepellors[2],
                lRepellors[3]
            )
            toysInSquareFormation = pointsRoughlySquare(points)
        }
        else if (lRepellors.isEmpty() && lAttractors.isEmpty() && lSpinners.size == 4) {
            val points = listOf(
                lSpinners[0],
                lSpinners[1],
                lSpinners[2],
                lSpinners[3]
            )
            toysInSquareFormation = pointsRoughlySquare(points)
        }
        else if (lRepellors.size+lAttractors.size+lSpinners.size == 4){
            val points = mutableListOf<Pair<Float,Float>>()
            for (element in lAttractors){
                points.add(element)
            }
            for (element in lRepellors){
                points.add(element)
            }
            for (element in lSpinners){
                points.add(element)
            }
            toysInSquareFormation = pointsRoughlySquare(points.toList())
        }
        if(DEBUG){Log.d("square", toysInSquareFormation.toString())}
    }

    private fun isCircleFormation(){
        val lAttractors = attractors.toList()
        val lRepellors = repellors.toList()
        val lSpinners = spinners.toList()
        if (lRepellors.size+lAttractors.size+lSpinners.size >= 6){
            val points = mutableListOf<Pair<Float,Float>>()
            for (element in lAttractors){
                points.add(element)
            }
            for (element in lRepellors){
                points.add(element)
            }
            for (element in lSpinners){
                points.add(element)
            }
            toysInCircleFormation = pointsRoughlyCircular(points.toList())
        }
        if(DEBUG){Log.d("circle", toysInCircleFormation.toString())}
    }

    private fun isToyInsideAnother(){
        if (toyInsideAnother){return}
        val lAttractors = attractors.toList()
        val lRepellors = repellors.toList()
        val lSpinners = spinners.toList()
        for (i in lAttractors.indices){
            for (j in lRepellors.indices){
                val rx = lAttractors[i].first-lRepellors[j].first
                val ry = lAttractors[i].second-lRepellors[j].second

                val d = rx*rx+ry*ry

                if (d < arScale*arScale){
                    toyInsideAnother = true
                    return
                }
            }
        }
        for (i in lAttractors.indices){
            for (j in lSpinners.indices){
                val rx = lAttractors[i].first-lSpinners[j].first
                val ry = lAttractors[i].second-lSpinners[j].second

                val d = rx*rx+ry*ry

                if (d < arScale*arScale){
                    toyInsideAnother = true
                    return
                }
            }
        }
        for (i in lSpinners.indices){
            for (j in lRepellors.indices){
                val rx = lSpinners[i].first-lRepellors[j].first
                val ry = lSpinners[i].second-lRepellors[j].second

                val d = rx*rx+ry*ry

                if (d < arScale*arScale){
                    toyInsideAnother = true
                    return
                }
            }
        }
    }

    fun setParticleNumber(v: Float){
        if (particleNumber == v){return}
        particleNumber = v
        newP = ceil(MAXN*particleNumber).toInt()
        // recall this function comes down via a compose THREAD
        // use a switch so the main GL THREAD handles the change
        // Overwise the wrong GL THREAD will be used!
        reset = true
    }

    fun setAllowAdapt(b: Boolean){
        allowAdapt = b
    }

    fun setColourMap(v: COLOUR_MAP){
        if (v == colourMap){return}
        colourMap = v
        recompileDrawShader = true
    }

    private fun adapt(){
        if (!allowAdapt){return}
        val oldP = p
        p = max(ceil(oldP*0.5f).toInt(),10000)
        particleNumber = p.toFloat()/ MAX_PARTICLES
        Log.w("adapt","$p, $oldP, ${ceil(oldP*0.5f).toInt()}")

        if (oldP == p){return}

        newP = p
        reset = true
        onAdapt(particleNumber)
    }

    private fun compileDrawShader(){

        particleDrawShader.release()
        particleDrawShader.create()

        // sneaky, bake in the colourmap
        val cmap = when(colourMap){
            COLOUR_MAP.R1 -> {
                "cmapR1"
            }
            COLOUR_MAP.R2 -> {
                "cmapR2"
            }
            COLOUR_MAP.ACE -> {
                "cmapace"
            }
            COLOUR_MAP.C3 -> {
                "cmapC3"
            }
            COLOUR_MAP.CB1 -> {
                "cmapcb1"
            }
            COLOUR_MAP.CB2 -> {
                "cmapcb2"
            }
            COLOUR_MAP.TRANS -> {
                "cmaptrans"
            }
            COLOUR_MAP.PRIDE -> {
                "cmappride"
            }
        }

        particleDrawShader.vertexShader = ParticleDrawShaderData().vertexShader.replace(
            "#define CMAP cmapR1",
            "#define CMAP $cmap"
        )

        particleDrawShader.fragmentShader = ParticleDrawShaderData().fragmentShader

        particleDrawShader.compile()

        particleDrawShader.use()

        // now we add a few uniforms that don't need to be updated

        particleDrawShader.setUniform("proj",Mat4(projection))

        particleDrawShader.setUniform("res",
            Vec2(resolution.first.toFloat(),resolution.second.toFloat())
        )

        particleDrawShader.setUniform("qTex",Textures[TextureId.X]!!)

        particleDrawShader.setUniform("scale",scale)
        particleDrawShader.setUniform("transitionSteps",transitionSteps)

        if (DEBUG_GL){glError()}

    }

    /*
        Generate the particle data once in a couroutine to avoid blocking the main thread so much
            only occurs once per app launch
    */
    @OptIn(DelicateCoroutinesApi::class)
    fun generate()
    {
        if (generating) {return}
        if (generatedParticles < MAXN) {
            CoroutineScope(newSingleThreadContext("generate")).launch {
                val t0 = System.nanoTime()
                generating = true
                particleBuffer.flip()
                particleBuffer.limit(4 * MAX_TEX_DIM * MAX_TEX_DIM)
                particleBuffer.position(generatedParticles * 4)

                qBuffer.flip()
                qBuffer.limit(4 * MAX_TEX_DIM * MAX_TEX_DIM)
                qBuffer.position(generatedParticles * 4)

                pBuffer.flip()
                pBuffer.limit(4 * MAX_TEX_DIM * MAX_TEX_DIM)
                pBuffer.position(generatedParticles * 4)

                for (i in generatedParticles + 1 until min(MAXN,MAX_TEX_DIM*MAX_TEX_DIM)) {

                    if (particleBuffer.remaining() < 4 || qBuffer.remaining() < 4 || pBuffer.remaining() < 4)
                    {
                        // this should not happen but it seems to on some devices...
                        break
                    }

                    val x =
                        Random.nextFloat() * (bounds.second.first - 2.0f * (bounds.first.first - scale)) + bounds.first.first + scale
                    val y =
                        Random.nextFloat() * (bounds.second.second - 2.0f * (bounds.first.second - scale)) + bounds.first.second + scale
                    val theta = Random.nextFloat() * 2.0f * PI.toFloat()
                    particleBuffer.put(x)
                    particleBuffer.put(y)
                    particleBuffer.put(theta)
                    particleBuffer.put(nCells * floor(x / dcx) + floor(y / dcy))

                    qBuffer.put(x)
                    qBuffer.put(y)
                    qBuffer.put(theta)
                    qBuffer.put(Random.nextFloat() * DR.toFloat())

                    pBuffer.put(i.toFloat())
                    pBuffer.put(0f)
                    pBuffer.put(0f)
                    pBuffer.put(0f)
                }

                pBuffer.flip()
                pBuffer.limit(4 * MAX_TEX_DIM * MAX_TEX_DIM)
                pBuffer.position(0)

                qBuffer.flip()
                qBuffer.limit(4 * MAX_TEX_DIM * MAX_TEX_DIM)
                qBuffer.position(0)

                particleBuffer.flip()
                particleBuffer.limit(4 * MAX_TEX_DIM * MAX_TEX_DIM)
                particleBuffer.position(0)

                generatedParticles = MAXN

                generating = false

                if (PERFORMANCE) {
                    val t = System.nanoTime()-t0
                    Log.i("Generated", "$generatedParticles in $t ns")
                }

            }
        }
    }

    fun initGPUData() {

        if (generating) {
            // call blocked, keep reset set to true and return to drawing
            return
        }

        if (reset) {
            p = newP
            transitionStep = transitionSteps
            reset = false
        }

        val m = ceil(sqrt(p.toDouble())).toInt()

        // randomly initialise particles (within bounds)

        if (generatedParticles < p) {

            particleBuffer.flip()
            particleBuffer.limit(4*MAX_TEX_DIM*MAX_TEX_DIM)
            particleBuffer.position(generatedParticles*4)

            qBuffer.flip()
            qBuffer.limit(4*MAX_TEX_DIM*MAX_TEX_DIM)
            qBuffer.position(generatedParticles*4)

            pBuffer.flip()
            pBuffer.limit(4*MAX_TEX_DIM*MAX_TEX_DIM)
            pBuffer.position(generatedParticles*4)

            for (i in generatedParticles+1 until p) {
                val x =
                    Random.nextFloat() * (bounds.second.first - 2.0f * (bounds.first.first - scale)) + bounds.first.first + scale
                val y =
                    Random.nextFloat() * (bounds.second.second - 2.0f * (bounds.first.second - scale)) + bounds.first.second + scale
                val theta = Random.nextFloat() * 2.0f * PI.toFloat()
                particleBuffer.put(x)
                particleBuffer.put(y)
                particleBuffer.put(theta)
                particleBuffer.put(nCells * floor(x / dcx) + floor(y / dcy))

                qBuffer.put(x)
                qBuffer.put(y)
                qBuffer.put(theta)
                qBuffer.put(Random.nextFloat() * DR.toFloat())

                pBuffer.put(i.toFloat())
                pBuffer.put(0f)
                pBuffer.put(0f)
                pBuffer.put(0f)
            }

            pBuffer.flip()
            pBuffer.limit(4*MAX_TEX_DIM*MAX_TEX_DIM)
            pBuffer.position(0)

            qBuffer.flip()
            qBuffer.limit(4*MAX_TEX_DIM*MAX_TEX_DIM)
            qBuffer.position(0)

            particleBuffer.flip()
            particleBuffer.limit(4*MAX_TEX_DIM*MAX_TEX_DIM)
            particleBuffer.position(0)

            generatedParticles = p
        }

        // delete and recreate texture
        texBuffer.flip()
        texBuffer.limit(4)
        gl3.glDeleteTextures(4,texBuffer)
        texBuffer.flip()
        texBuffer.limit(4)
        gl3.glGenTextures(4, texBuffer)

        val pTex = texBuffer[Textures[TextureId.X]!!]
        val qTex = texBuffer[Textures[TextureId.Y]!!]
        val paramTex = texBuffer[Textures[TextureId.PARAM]!!]

        if (DEBUG_GL){glError()}

        // instance and pack each texture

        val t1 = System.nanoTime()

        // create textures
        initTexture2DRGBA32F(pTex, m)
        transferToTexture2DRGBA32F(pTex, particleBuffer, m)

        if (DEBUG_GL){glError()}

        initTexture2DRGBA32F(qTex, m)
        transferToTexture2DRGBA32F(qTex, qBuffer, m)

        if (DEBUG_GL){glError()}

        initTexture2DRGBA32F(paramTex,m)
        transferToTexture2DRGBA32F(paramTex,pBuffer,m)

        // generate rest of particles on new thread
        generate()

        glError("texture setup")

        compileDrawShader()

        if (DEBUG_GL){glError()}

        computeShader.release()
        computeShader.compile()
        computeShader.use()

        // the compute shader has quite a few parameters, largely for the integrator

        // some textures to store the particle data
        computeShader.setUniform("paramTex",2)
        computeShader.setUniform("qTex",1)
        computeShader.setUniform("pTex",0)

        // boundaries, for folding particles back in etc.
        computeShader.setUniform("Lx",bounds.second.first)
        computeShader.setUniform("lx",bounds.first.first)
        computeShader.setUniform("Ly",bounds.second.second)
        computeShader.setUniform("ly",bounds.first.second)

        // paramters for the integration algorithm
        computeShader.setUniform("ar",ar.toFloat())
        computeShader.setUniform("at",at.toFloat())
        computeShader.setUniform("br",br.toFloat())
        computeShader.setUniform("bt",bt.toFloat())
        computeShader.setUniform("alpha",alpha.toFloat())
        computeShader.setUniform("beta",beta.toFloat())
        computeShader.setUniform("gamma",gamma.toFloat())
        computeShader.setUniform("DR",DR.toFloat())
        computeShader.setUniform("rad",0.5f*scale)

        computeShader.setUniform("res",
            Vec2(resolution.first.toFloat(),resolution.second.toFloat())
        )

        computeShader.setUniform("softMaxRadialDistance",
            max(bounds.second.first,bounds.second.second)
        )

        toyDrawShader.release()
        toyDrawShader.compile()
        toyDrawShader.use()

        toyDrawShader.setUniform("proj",Mat4(projection))

        toyDrawShader.setUniform("res",
            Vec2(resolution.first.toFloat(),resolution.second.toFloat())
        )

        toyDrawShader.setUniform("scale",arScale)
        toyDrawShader.setUniform("T",atrPeriod.toFloat())

        if (DEBUG_GL){glError()}

        gl3.glGenQueries(1,queryBuffer)
    }

    private fun updateMotionParams(delta: Float){

        var mu = 0f
        var c = 1

        for (i in 0 until deltas.size){
            if (deltas[i] == 0f){
                if (i == 0){mu = delta}
                break
            }
            mu += 1.0f / deltas[i]
            c += 1
        }

        dt = (mu/c).toDouble()

        DR = sqrt(2.0*0.01*dt)

        cr = (1.0*dt)/(2.0*J)
        br = 1.0 / (1.0+cr)
        ar = (1.0-cr)*br

        ct = (1.0*dt)/(2.0*M)
        bt = 1.0 / (1.0+ct)
        at = (1.0-ct)*bt

        alpha = bt*dt*dt/M
        beta = br*dt*dt/J
        gamma = br*dt/(2.0*J)

        computeShader.use()

        computeShader.setUniform("ar",ar.toFloat())
        computeShader.setUniform("at",at.toFloat())
        computeShader.setUniform("br",br.toFloat())
        computeShader.setUniform("bt",bt.toFloat())
        computeShader.setUniform("alpha",alpha.toFloat())
        computeShader.setUniform("beta",beta.toFloat())
        computeShader.setUniform("gamma",gamma.toFloat())
        computeShader.setUniform("DR",DR.toFloat())
    }

    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {

        gl3.glClearColor(0f,0f,0f, 1f)
        p = ceil(MAXN*particleNumber).toInt()
        initGPUData()

    }

    override fun onSurfaceChanged(p0: GL10?, p1: Int, p2: Int) {
        gl3.glViewport(0,0,p1,p2)
    }

    override fun onDrawFrame(p0: GL10?) {

        if (System.currentTimeMillis() - lastReviewRequest > REVIEW_RATE_LIMIT_MILLIS){onRequestReview(); lastReviewRequest = System.currentTimeMillis()}
        debugString = ""
        if (reset){
            initGPUData()
        }
        if (recompileDrawShader){compileDrawShader(); recompileDrawShader=false}

        val t1 = System.nanoTime()
        if (frameNumber == queryFormationFrequency && !DEMO_REAL){
            isSquareFormation()
            isToyInsideAnother()
            isCircleFormation()
        }
        if(DEBUG){debugString += "check formation time "+"${System.nanoTime()-t1}"}

        val t2 = System.nanoTime()
        lastTapped++
        gl3.glViewport(0,0,resolution.first,resolution.second)
        gl3.glEnable(gl3.GL_BLEND);
        gl3.glBlendFunc(gl3.GL_SRC_ALPHA, gl3.GL_ONE_MINUS_SRC_ALPHA);
        gl3.glClear(gl3.GL_COLOR_BUFFER_BIT or gl3.GL_DEPTH_BUFFER_BIT)

        if (DEMO_REAL) {
            if (demoFrameId > demoFrames) {
                demoId += 1
                demoFrameId = 0

                if (demoId > 4){
                    DEMO_REAL = false
                    attractors.clear()
                }
            }
            if (demoId == 0){
                demoToys(CONFIGURATION.CIRCLE)
            }
            else if (demoId == 1){
                demoToys(CONFIGURATION.SQUARE)
            }
            else if (demoId == 2){
                demoToys((CONFIGURATION.TRIANGLE))
            }
            else if (demoId == 3){
                demoToys(CONFIGURATION.WIGGLE)
            }
            demoFrameId += 1
        }

        if (DEBUG_GL){glError()}

        val m = ceil(sqrt(p.toFloat())).toInt()

        particleDrawShader.use()

        transitionStep = if (transitionStep > 0){
            transitionStep - 1
        }
        else {
            0
        }

        particleDrawShader.setUniform("transitionStep",transitionStep)

        if (DEBUG_GL){glError()}

        val pTex = texBuffer[Textures[TextureId.X]!!]
        val qTex = texBuffer[Textures[TextureId.Y]!!]

        // we bind the screen frame buffer (default)

        gl3.glBindFramebuffer(gl3.GL_DRAW_FRAMEBUFFER, 0)

        gl3.glActiveTexture(gl3.GL_TEXTURE1)
        gl3.glBindTexture(gl3.GL_TEXTURE_2D,qTex)

        gl3.glActiveTexture(gl3.GL_TEXTURE0)
        gl3.glBindTexture(gl3.GL_TEXTURE_2D, pTex)

        gl3.glEnableVertexAttribArray(0)
        gl3.glVertexAttribPointer(0, 3, gl3.GL_FLOAT, false, 0, drawVertices)

        particleDrawShader.setUniform("qTex",1)
        particleDrawShader.setUniform("pTex",0)

        particleDrawShader.setUniform("n",m)

        if (DEBUG_GL){glError()}

        if(DEBUG){debugString += "setup time "+"${System.nanoTime()-t2}"}

        val t3 = System.nanoTime()

        // instanced drawing of p particles

        anySamples.clear()

        if (frameNumber == queryFrequency) {
            gl3.glBeginQuery(GL_ANY_SAMPLES_PASSED_CONSERVATIVE, queryBuffer[0])
        }

        gl3.glDrawArraysInstanced(gl3.GL_POINTS, 0, 1, p)

        if (frameNumber == queryFrequency) {
            gl3.glEndQuery(GL_ANY_SAMPLES_PASSED_CONSERVATIVE)
            gl3.glGetQueryObjectuiv(queryBuffer[0], GL_QUERY_RESULT, anySamples)
            if (anySamples[0] == 0){
                allParticlesOfScreen = true
            }
        }
        if (DEBUG_GL){glError()}

        for (i in attr.indices){
            attr[i] = 0.0f
            rep[i] = 0.0f
            spin[i] = 0.0f
            freezer[i] = 0.0f
        }
        // rare concurrent write errors on some devices?
        val lAttractors = attractors.toList()
        for (i in lAttractors.indices){
            attr[i * 2] = lAttractors[i].first
            attr[i * 2 + 1] = lAttractors[i].second
        }

        val lRepellors = repellors.toList()
        for (i in lRepellors.indices){
            rep[i * 2] = lRepellors[i].first
            rep[i * 2 + 1] = lRepellors[i].second
        }

        val lSpinners = spinners.toList()
        for (i in lSpinners.indices) {
            spin[i * 2] = lSpinners[i].first
            spin[i * 2 + 1] = lSpinners[i].second
        }

        val lFreezers = freezers.toList()
        for (i in lFreezers.indices) {
            freezer[i * 2] = lFreezers[i].first
            freezer[i * 2 + 1] = lFreezers[i].second
        }

        toyDrawShader.use()

        // upload the positions

        toyDrawShader.setUniform("attr",Mat4(attr))
        toyDrawShader.setUniform("rep",Mat4(rep))
        toyDrawShader.setUniform("spin",Mat4(spin))
        toyDrawShader.setUniform("freeze",Mat4(freezer))

        toyDrawShader.setUniform("na",attractors.size)
        toyDrawShader.setUniform("nr",repellors.size)
        toyDrawShader.setUniform("ns",spinners.size)
        toyDrawShader.setUniform("nf",freezers.size)

        toyDrawShader.setUniform("t",arTime.toFloat())
        toyDrawShader.setUniform("contTime",contTime.toFloat())

        arTime++
        contTime++
        if (contTime > 100000){contTime = 0}
        if (arTime == atrPeriod){arTime = 0}

        if (DEBUG_TOYS) {
            toyDrawShader.setUniform("alpha",1f)
        }
        else{
            toyDrawShader.setUniform("alpha",toysAlpha)
        }

        if (DEBUG_GL){glError()}

        gl3.glEnableVertexAttribArray(0)
        gl3.glVertexAttribPointer(0, 3, gl3.GL_FLOAT, false, 0, drawVertices)
        if (DEBUG_GL){glError()}

        // draw the toys
        gl3.glDrawArraysInstanced(gl3.GL_POINTS, 0, 1, 32)
        if (DEBUG_GL){glError()}

        if(DEBUG){debugString += "draw and query time "+"${System.nanoTime()-t3}"}

        // compute step
        val t0 = System.nanoTime()
        step(delta.toFloat()*1e-9f)
        if(DEBUG){debugString += "step time "+"${System.nanoTime()-t0}"}
        // measure time
        val t = System.nanoTime()
        delta = t-last
        last = t

        timeSinceLastAdapt += delta*1e-9f

        deltas[frameNumber] = 1.0f / (delta.toFloat()*1e-9f)
        frameNumber += 1
        if (frameNumber >= 60){
            onUpdateClock()
            frameNumber = 0
            val mu = deltas.sum()/deltas.size
            if (PERFORMANCE){
                Log.i("FPS", "$mu")
            }
            if (mu < 30.0f){
                adapt()
            }
        }

        val t4 = System.nanoTime()
        achievements()
        if(DEBUG){debugString += "achievements time "+"${System.nanoTime()-t4}"}
        if(DEBUG){debugString += "achievements time "+"${System.nanoTime()-t4}"}
        if(DEBUG){debugString += "full time "+ "${System.nanoTime()-t1}"; Log.d("Times",debugString)}

    }

    private fun step(delta: Float){

        if (paused)
        {
            return
        }

        if (frameIndependent){
            updateMotionParams(delta)
        }

        val pTex = texBuffer[Textures[TextureId.X]!!]
        val qTex = texBuffer[Textures[TextureId.Y]!!]
        val paramTex = texBuffer[Textures[TextureId.PARAM]!!]

        val m = ceil(sqrt(p.toFloat())).toInt()

        if (DEBUG_GL){glError()}

        // gen frame buffer which we will "draw" particle positions to
        fbos.clear()
        gl3.glGenFramebuffers(1,fbos)
        val fbo = fbos[0]
        gl3.glBindFramebuffer(gl3.GL_FRAMEBUFFER, fbo)
        if (DEBUG_GL){glError()}

        // frame buffer textures
        // position now
        gl3.glFramebufferTexture2D(
            gl3.GL_FRAMEBUFFER,
            gl3.GL_COLOR_ATTACHMENT0,
            gl3.GL_TEXTURE_2D,
            pTex,
            0
        )
        // last position
        gl3.glFramebufferTexture2D(
            gl3.GL_FRAMEBUFFER,
            gl3.GL_COLOR_ATTACHMENT1,
            gl3.GL_TEXTURE_2D,
            qTex,
            0
        )
        // parameters
        gl3.glFramebufferTexture2D(
            gl3.GL_FRAMEBUFFER,
            gl3.GL_COLOR_ATTACHMENT2,
            gl3.GL_TEXTURE_2D,
            paramTex,
            0
        )

        if (DEBUG_GL){glError()}
        glBufferStatus()

        // pTex and qTex need to be dranw too, paramTex is constant for now
        drawBuffers.clear()
        drawBuffers.put(gl3.GL_COLOR_ATTACHMENT0)
        drawBuffers.put(gl3.GL_COLOR_ATTACHMENT1)
        drawBuffers.flip()
        drawBuffers.limit(2)
        gl3.glDrawBuffers(2,drawBuffers)
        if (DEBUG_GL){glError()}
        glBufferStatus()

        gl3.glActiveTexture(gl3.GL_TEXTURE2)
        gl3.glBindTexture(gl3.GL_TEXTURE_2D,paramTex)

        gl3.glActiveTexture(gl3.GL_TEXTURE1)
        gl3.glBindTexture(gl3.GL_TEXTURE_2D,qTex)

        gl3.glActiveTexture(gl3.GL_TEXTURE0)
        gl3.glBindTexture(gl3.GL_TEXTURE_2D, pTex)

        computeShader.use()

        computeShader.setUniform("n",m)
        computeShader.setUniform("np",MAXN)
        computeShader.setUniform("dt",dt.toFloat())

        val seed = RNG.nextFloat()
        // for wiener processes, we use one time based seed
        // and a psuedo random generator varying on particle index
        computeShader.setUniform("seed",seed)

        for (i in attr.indices){
            attr[i] = 0.0f
            rep[i] = 0.0f
            spin[i] = 0.0f
        }
        // rare concurrent write errors on some devices? yes but gone now
        //   for various reasons
        val lAttractors = attractors.toList()
        for (i in lAttractors.indices){
            attr[i * 2] = lAttractors[i].first
            attr[i * 2 + 1] = lAttractors[i].second
        }

        val lRepellors = repellors.toList()
        for (i in lRepellors.indices){
            rep[i * 2] = lRepellors[i].first
            rep[i * 2 + 1] = lRepellors[i].second
        }

        val lSpinners = spinners.toList()
        for (i in lSpinners.indices) {
            spin[i * 2] = lSpinners[i].first
            spin[i * 2 + 1] = lSpinners[i].second
        }

        val lFreezers = freezers.toList()
        for (i in lFreezers.indices) {
            freezer[i * 2] = lFreezers[i].first
            freezer[i * 2 + 1] = lFreezers[i].second
        }

        computeShader.setUniform("attr",Mat4(attr))
        computeShader.setUniform("rep",Mat4(rep))
        computeShader.setUniform("spin",Mat4(spin))
        computeShader.setUniform("freeze",Mat4(freezer))

        computeShader.setUniform("na",attractors.size)
        computeShader.setUniform("nr",repellors.size)
        computeShader.setUniform("ns",spinners.size)
        computeShader.setUniform("nf",freezers.size)

        gl3.glDepthMask(false)
        gl3.glDisable(gl3.GL_BLEND)

        gl3.glViewport(0, 0, m, m)

        if (DEBUG_GL){glError()}

        // upload the vertices into attribute 0 (see shaders)
        gl3.glEnableVertexAttribArray(0)
        gl3.glVertexAttribPointer(0, 3, gl3.GL_FLOAT, false, 0, computeVerts)
        if (DEBUG_GL){glError()}

        // textures go ini attribute 1
        gl3.glEnableVertexAttribArray(1)
        gl3.glVertexAttribPointer(1, 2, gl3.GL_FLOAT, false, 0, computeTexCoords)
        if (DEBUG_GL){glError()}

        // draw the quad === compute the next positions
        gl3.glDrawArrays(gl3.GL_TRIANGLES, 0, 6)

        if (DEBUG_GL){glError()}

        // if needed we can retrieve and print particle positions while developing
        // this of course crushes performance

        if (DEBUG_GL){
            // kill performance but get all pixel === particle data onto cpu
            val returnBuffer = ByteBuffer.allocateDirect((4*m*m) * 4).order(ByteOrder.nativeOrder()).asFloatBuffer()
            gl3.glReadBuffer(gl3.GL_COLOR_ATTACHMENT0)
            returnBuffer.flip()
            returnBuffer.limit(4 * m * m)

            glBufferStatus()
            if (DEBUG_GL){glError()}
            // get all pixels in the return buffer
            gl3.glReadPixels(
                0,
                0,
                m,
                m,
                gl3.GL_RGBA,
                gl3.GL_FLOAT,
                returnBuffer
            )
            returnBuffer.flip()
            returnBuffer.limit(4 * m * m)
            if (DEBUG_GL){glError()}
            glBufferStatus()

            // get result as float array
            var ret: FloatArray = FloatArray(4*m*m) { 0f }
            for (i in 0 until 4*m*m) {
                ret[i] = returnBuffer[i]
            }

            for (i in 0 until 10) {
                Log.d("GL","x: "+ret[4*i]+" y: "+ret[4*i+1]+ " theta: " +ret[4*i+2]+ " i: "+ret[4*i+3])
            }

            gl3.glReadBuffer(gl3.GL_COLOR_ATTACHMENT1)
            returnBuffer.flip()
            returnBuffer.limit(4 * m * m)

            glBufferStatus()
            if (DEBUG_GL){glError()}
            // get all pixels in the return buffer
            gl3.glReadPixels(
                0,
                0,
                m,
                m,
                gl3.GL_RGBA,
                gl3.GL_FLOAT,
                returnBuffer
            )
            returnBuffer.flip()
            returnBuffer.limit(4 * m * m)
            if (DEBUG_GL){glError()}
            glBufferStatus()

            // get result as float array
            ret = FloatArray(4*m*m) { 0f }
            for (i in 0 until 4*m*m) {
                ret[i] = returnBuffer[i]
            }

            for (i in 0 until 1) {
                Log.d("GL","xp: "+ret[4*i]+" yp: "+ret[4*i+1]+ " thetap: " +ret[4*i+2]+ " i: "+ret[4*i+3])
            }
        }

        // free the buffers!
        gl3.glDeleteFramebuffers(1,fbos)
        drawBuffers.flip()
        drawBuffers.limit(2)
        gl3.glDeleteBuffers(2,drawBuffers)
        if (DEBUG_GL){glError()}
    }

    enum class CONFIGURATION {
        SQUARE,
        TRIANGLE,
        CIRCLE,
        WIGGLE
    }
    fun demoToys(
        configuration: CONFIGURATION
    ) {

        val Mx = bounds.second.first
        val My = bounds.second.second
        val cx = Mx/2f
        val cy = My/2f
        val h = arScale*4f

        if (configuration == CONFIGURATION.SQUARE){
            attractors.clear()
            for (p in listOf(
                Pair<Float,Float>(cx-h,cy-h),
                Pair<Float,Float>(cx+h,cy-h),
                Pair<Float,Float>(cx+h,cy+h),
                Pair<Float,Float>(cx-h,cy+h),
                Pair<Float,Float>(cx,cy)
            )){
                attractors.add(p)
            }
        }
        else if (configuration == CONFIGURATION.TRIANGLE){
            attractors.clear()
            for (p in listOf(
                Pair<Float,Float>(cx,cy+h),
                Pair<Float,Float>(cx-h,cy-h),
                Pair<Float,Float>(cx+h,cy-h),
                Pair<Float,Float>(cx,cy)
            )){
                attractors.add(p)
            }
        }
        else if (configuration == CONFIGURATION.CIRCLE){
            attractors.clear()
            val dtheta = 2f*PI.toFloat()/8f
            var theta = 0f
            val h = arScale*4f
            for (i in 0 until 8){
                attractors.add(
                    Pair<Float,Float>(
                        h*cos(theta)+cx,
                        h*sin(theta)+cy
                    )
                )
                theta += dtheta
            }
        }
        else if (configuration == CONFIGURATION.WIGGLE){
            attractors.clear()
            val dtheta = 2f*PI.toFloat()/8f
            var theta = 0f
            val h = My*0.75f
            val l = arScale*4f
            for (i in 0 until 8){
                attractors.add(
                    Pair<Float,Float>(
                        cx+sin(theta)*l,h*i/8f+cy/3f
                    )
                )
                theta += dtheta
            }
        }
    }
}