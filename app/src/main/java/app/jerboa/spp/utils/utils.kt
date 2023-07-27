package app.jerboa.spp.utils

import android.content.res.AssetManager
import androidx.compose.ui.graphics.Color
import app.jerboa.spp.data.Colour
import app.jerboa.spp.data.ColourMap
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.math.ceil
import kotlin.math.sqrt

fun printMatrix(A:FloatArray){
    val n = ceil(sqrt(A.size.toDouble())).toInt()
    for (i in 0 until n){
        for (j in 0 until n){
            print(A[i*n+j].toString()+", ")
        }
        println()
    }

}

fun loadColourMap(fileName: String, assets: AssetManager): ColourMap {
    val filestream = assets.open(fileName)
    val reader = BufferedReader(InputStreamReader(filestream))
    var line = reader.readLine()
    val colours: MutableMap<UInt, Colour> = mutableMapOf<UInt, Colour>()
    var i = 0u
    while (line != null){
        val rgb = line.split(",")
        val c = Colour(
            rgb[0].toFloat(),
            rgb[1].toFloat(),
            rgb[2].toFloat()
        )
        colours[i] = c
        i++
        line = reader.readLine()
    }
    return ColourMap(colours.toMap())
}

fun norm2(x: Float, y: Float, u: Float, v: Float): Float{
    val a = x-u
    val b = y-v
    return a*a+b*b
}