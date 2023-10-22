package app.jerboa.spp

import app.jerboa.spp.data.*
import org.junit.Test

import org.junit.Assert.*

class ShaderUniformParsing {

    private val vert: String = "#version 300 es\n"+
            "precision lowp float; precision highp int;\n"+
            "in vec3 a_position;\n"+
            "out vec2 centre; out vec4 o_colour; flat out float time; flat out int spins;\n"+
            "uniform int na; uniform int nr; uniform mat4 attr; uniform mat4 rep;\n"+
            "uniform int ns; uniform mat4 spin;\n"+
            "uniform highp sampler2D test1;"+
            "uniform float scale; uniform vec2 res; uniform mat4 proj;\n"+
            "uniform float t; uniform float contTime; uniform float T; uniform float alpha;\n"+
            "uniform lowp sampler2D test2;"+
            "void main(void){\n"+
            "   float a_offset = float(gl_InstanceID);\n"+
            "   int a = int(floor(a_offset)); float x = 0.0; float y = 0.0; float drawa = 0.0; float drawr = 0.0; float draws = 0.0;\n"+
            "   if (a < na){ int col = int(floor(float(a)/2.0)); int o = int(2.0*mod(float(a),2.0));\n"+
            "        x = attr[col][0+o]; y = attr[col][1+o]; drawa = 0.33;}\n"+
            "   if (a >= 8 && a < 8+nr){ a = a-8; int col = int(floor(float(a)/2.0)); int o = int(2.0*mod(float(a),2.0));\n"+
            "        x = rep[col][0+o]; y = rep[col][1+o]; drawr = 0.33;}\n"+
            "   if (a >= 16 && a < 16+ns){ a = a-16; int col = int(floor(float(a)/2.0)); int o = int(2.0*mod(float(a),2.0));\n"+
            "        x = spin[col][0+o]; y = spin[col][1+o]; draws = 0.33;}\n"+
            "   centre = vec2(x,y);\n"+
            "   vec4 pos = proj*vec4(centre.xy,0.0,1.0);\n"+
            "   gl_Position = vec4(a_position.x+pos.x,a_position.y+pos.y,0.0,1.0);\n"+
            "   o_colour = vec4(1.0,1.0,1.0,0.0); time = 1.0;\n"+
            "   spins = 0;"+
            "   if (drawr > 0.0 ){ o_colour = vec4(1.0,0.0,0.0,alpha); time = t/T; gl_PointSize = time*scale;}"+
            "   else if (drawa > 0.0){ o_colour = vec4(0.0,1.0,0.0,alpha); time = 1.0-t/T; gl_PointSize = time*scale;}\n"+
            "   else if (draws > 0.0){ o_colour = vec4(199.0/255.0,203.0/255.0,1.0,alpha); spins=1; time = contTime/T; gl_PointSize = scale;}\n"+
            "}"

    private val s = Shader(
        vert,
        ""
    )

    @Test
    fun shader_parses(){
        assert(s.parseUniforms())
    }
    @Test
    fun uniformInt_isCorrect() {
        s.parseUniforms()
        assertEquals(15, s.uniforms.size)

        assert(s.uniforms.keys.contains("nr"))

        assertEquals("nr", s.uniforms["nr"]!!.name)
        assertEquals(0, s.uniforms["nr"]!!.value)
        assertEquals(0, s.uniforms["nr"]!!.location)

    }

    @Test
    fun setUniformInt_isCorrect()
    {
        s.parseUniforms()
        s.setUniform("nr", 2)
        assertEquals(2, s.uniforms["nr"]!!.value)
    }

    @Test
    fun uniformVec2_isCorrect(){
        s.parseUniforms()
        assertEquals(15, s.uniforms.size)

        assert("res" in s.uniforms.keys)

        assertEquals("res", s.uniforms["res"]!!.name)
        assertEquals(0.0f, (s.uniforms["res"]!! as glUniform<Vec2>).value.x )
        assertEquals(0.0f, (s.uniforms["res"]!! as glUniform<Vec2>).value.y )
    }

    @Test
    fun setUniformVec2_isCorrect()
    {
        s.parseUniforms()
        s.setUniform("res", Vec2(1.0f,2.0f))
        assertEquals(Vec2(1.0f,2.0f), (s.uniforms["res"]!! as glUniform<Vec2>).value)
    }


    @Test
    fun uniformFloat_isCorrect(){
        s.parseUniforms()
        assertEquals(15, s.uniforms.size)

        assert("scale" in s.uniforms.keys)

        assertEquals("scale", s.uniforms["scale"]!!.name)
        assertEquals(0.0f, s.uniforms["scale"]!!.value)
        assertEquals(0.0f, s.uniforms["scale"]!!.value)
    }

    @Test
    fun setUniformFloat_isCorrect()
    {
        s.parseUniforms()
        s.setUniform("scale", 1.0f)
        assertEquals(1.0f, s.uniforms["scale"]!!.value)
    }


    @Test
    fun uniformMat4_isCorrect(){
        s.parseUniforms()
        assertEquals(15, s.uniforms.size)

        assert("proj" in s.uniforms.keys)

        assertEquals("proj", s.uniforms["proj"]!!.name)
        val v: Mat4 = s.uniforms["proj"]!!.value as Mat4

       assertEquals(0.0f,v.elements.sum())
    }

    @Test
    fun setUniformMat4_isCorrect()
    {
        s.parseUniforms()
        val v: Mat4 = Mat4()
        for (i in 0 until 16)
        {
            v.elements[i] = i.toFloat()
        }
        s.setUniform("proj", v)
        val proj: Mat4 = s.uniforms["proj"]!!.value as Mat4
        assertEquals(v.elements, proj.elements)
    }


    @Test
    fun uniformSampler2D_isCorrect(){
        s.parseUniforms()
        assertEquals(15, s.uniforms.size)

        assert("test1" in s.uniforms.keys)

        assertEquals("test1", s.uniforms["test1"]!!.name)
        val value1 = s.uniforms["test1"]!! as glUniform<Int>
        assertEquals(0,value1.value)

        assertEquals("test2", s.uniforms["test2"]!!.name)
        val value2 = s.uniforms["test2"]!! as glUniform<Int>
        assertEquals(0,value2.value)
    }

    @Test
    fun setUniformSampler2D_isCorrect()
    {
        s.parseUniforms()
        s.setUniform("test1", 1)
        assertEquals(1, s.uniforms["test1"]!!.value)
    }

}