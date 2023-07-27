package app.jerboa.spp.data

import kotlin.math.floor

data class Colour(
    var r:Float,
    var g: Float,
    var b: Float
){
    init {
        if (r < 0f){r=0f}
        if (r > 1f){r=1f}

        if (g < 0f){g=0f}
        if (g > 1f){g=1f}

        if (b < 0f){b=0f}
        if (b > 1f){b=1f}
    }
}

data class ColourMap(
    private val colours: Map<UInt,Colour>
) {
    operator fun invoke(t: Float): Colour {

        if (t < 0f){ return colours[0u]!! }
        if (t > 1f){ return colours[255u]!! }

        return colours[floor(255f*t).toUInt()]!!
    }
}