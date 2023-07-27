package app.jerboa.glskeleton.utils
import android.util.Log
import java.nio.FloatBuffer
import android.opengl.GLES30 as gl3

fun glBufferStatus(): Int {
    val e = gl3.glCheckFramebufferStatus(gl3.GL_FRAMEBUFFER)
    when(e){
        gl3.GL_FRAMEBUFFER_UNDEFINED -> {
            Log.e("GL","GL_FRAMEBUFFER_UNDEFINED")}
        gl3.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT -> {Log.e("GL","GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT")}
        gl3.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT -> {Log.e("GL","GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT")}
        gl3.GL_FRAMEBUFFER_UNSUPPORTED -> {Log.e("GL","GL_FRAMEBUFFER_UNSUPPORTED")}
        gl3.GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE -> {Log.e("GL","GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE")}
    }
    return e
}

fun glError(msg:String=""): Int {
    val e = gl3.glGetError()
    when (e){
        gl3.GL_NO_ERROR -> {}
        gl3.GL_INVALID_ENUM -> {throw RuntimeException("$msg: GL_INVALID_ENUM")}
        gl3.GL_INVALID_VALUE -> {throw RuntimeException("$msg: GL_INVALID_VALUE")}
        gl3.GL_INVALID_OPERATION -> {throw RuntimeException("$msg: GL_INVALID_OPERATION")}
        gl3.GL_OUT_OF_MEMORY -> {throw RuntimeException("$msg: GL_OUT_OF_MEMORY")}
        gl3.GL_INVALID_FRAMEBUFFER_OPERATION -> {throw RuntimeException("$msg: INVALID_FRAMEBUFFER_OPERATION")}
    }
    return e
}

fun compileGLSLProgram(id: Int, vert: String, frag: String){

    val vertexShader = gl3.glCreateShader(gl3.GL_VERTEX_SHADER)
    gl3.glShaderSource(vertexShader,vert)
    gl3.glCompileShader(vertexShader)
    glError("Compiling vertex shader: $vert")
    gl3.glAttachShader(id,vertexShader)
    gl3.glLinkProgram(id)
    glError("Linking vertex shader: $vert")

    val fragmentShader = gl3.glCreateShader(gl3.GL_FRAGMENT_SHADER)
    gl3.glShaderSource(fragmentShader,frag)
    gl3.glCompileShader(fragmentShader)
    glError("Compiling fragment shader: $frag")
    gl3.glAttachShader(id,fragmentShader)
    gl3.glLinkProgram(id)
    glError("Linking fragment shader: $frag")
    gl3.glValidateProgram(id)
    glError("Validating shaders: $vert\n $frag")
}

fun initTexture2DRGBA32F(id: Int, n: Int): Int {
    gl3.glBindTexture(gl3.GL_TEXTURE_2D,id)
    gl3.glTexParameteri(
        gl3.GL_TEXTURE_2D,
        gl3.GL_TEXTURE_MIN_FILTER,
        gl3.GL_NEAREST
    )
    gl3.glTexParameteri(
        gl3.GL_TEXTURE_2D,
        gl3.GL_TEXTURE_MAG_FILTER,
        gl3.GL_NEAREST
    )
    gl3.glTexParameteri(
        gl3.GL_TEXTURE_2D,
        gl3.GL_TEXTURE_WRAP_S,
        gl3.GL_CLAMP_TO_EDGE
    )
    gl3.glTexParameteri(
        gl3.GL_TEXTURE_2D,
        gl3.GL_TEXTURE_WRAP_T,
        gl3.GL_CLAMP_TO_EDGE
    )
    gl3.glTexImage2D(
        gl3.GL_TEXTURE_2D,
        0,
        gl3.GL_RGBA32F,
        n,
        n,
        0,
        gl3.GL_RGBA,
        gl3.GL_FLOAT,
        null
    )
    return glError()
}

fun transferToTexture2DRGBA32F(id: Int, data: FloatBuffer, n: Int): Int{
    data.flip()
    data.limit(n*n)
    gl3.glBindTexture(gl3.GL_TEXTURE_2D,id)

    gl3.glTexImage2D(
        gl3.GL_TEXTURE_2D,
        0,
        gl3.GL_RGBA32F,
        n,
        n,
        0,
        gl3.GL_RGBA,
        gl3.GL_FLOAT,
        data
    )
    return glError()
}

fun initTexture1DRGBA32F(id: Int, n: Int): Int {
    gl3.glBindTexture(gl3.GL_TEXTURE_2D,id)
    gl3.glTexParameteri(
        gl3.GL_TEXTURE_2D,
        gl3.GL_TEXTURE_MIN_FILTER,
        gl3.GL_NEAREST
    )
    gl3.glTexParameteri(
        gl3.GL_TEXTURE_2D,
        gl3.GL_TEXTURE_MAG_FILTER,
        gl3.GL_NEAREST
    )
    gl3.glTexParameteri(
        gl3.GL_TEXTURE_2D,
        gl3.GL_TEXTURE_WRAP_S,
        gl3.GL_CLAMP_TO_EDGE
    )
    gl3.glTexParameteri(
        gl3.GL_TEXTURE_2D,
        gl3.GL_TEXTURE_WRAP_T,
        gl3.GL_CLAMP_TO_EDGE
    )
    gl3.glTexImage2D(
        gl3.GL_TEXTURE_2D,
        0,
        gl3.GL_RGBA32F,
        n,
        1,
        0,
        gl3.GL_RGBA,
        gl3.GL_FLOAT,
        null
    )
    return glError()
}

fun transferToTexture1DRGBA32F(id: Int, data: FloatBuffer, n: Int): Int{
    data.flip()
    data.limit(n)
    gl3.glBindTexture(gl3.GL_TEXTURE_2D,id)

    gl3.glTexImage2D(
        gl3.GL_TEXTURE_2D,
        0,
        gl3.GL_RGBA32F,
        n,
        1,
        0,
        gl3.GL_RGBA,
        gl3.GL_FLOAT,
        data
    )
    return glError()
}
