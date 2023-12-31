package app.jerboa.spp.data

data class FadeShaderData(
    override val vertexShader: String = "#version 300 es\n"+
            "in vec3 a_position;\n"+
            "void main(void){" +
            "    gl_Position = vec4(a_position.xy,0.0,1.0);\n"+
            "}",
    override val fragmentShader: String = "#version 300 es\n"+
            "precision lowp float;\n"+
            "uniform float fadeRate;\n"+
            "out vec4 colour;\n"+
            "void main(void){\n"+
            "    colour = vec4(0.0,0.0,0.0,fadeRate);\n"+
            "}")
    : ShaderData(vertexShader,fragmentShader)