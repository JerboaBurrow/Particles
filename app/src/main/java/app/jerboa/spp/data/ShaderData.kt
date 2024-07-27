package app.jerboa.spp.data

abstract class ShaderData(open val vertexShader: String, open val fragmentShader: String)
class Mat4(
    val elements: FloatArray = FloatArray(16){0f}
)

data class Vec2 (
    val x: Float,
    val y: Float
)

data class glUniform<T>(
    var value: T,
    val name: String,
    var location: Int
)

val UNIFORM_INT_REGEX = "uniform int (\\S+);".toRegex()
val UNIFORM_FLOAT_REGEX = "uniform float (\\S+);".toRegex()
val UNIFORM_VEC2_REGEX = "uniform vec2 (\\S+);".toRegex()
val UNIFORM_MAT4_REGEX = "uniform mat4 (\\S+);".toRegex()
val UNIFORM_SAMPLER2D_REGEX = "uniform (\\S+) sampler2D (\\S+);".toRegex()