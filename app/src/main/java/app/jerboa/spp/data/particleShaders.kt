package app.jerboa.spp.data

/*
    Draws each particle as a circle with basic anti aliasing

    Particles are colour by a combination of speed (intensity) and angle (colour map)

    ShaderData expects instanced rending
 */
data class ParticleDrawShaderData(
    override val vertexShader: String = "#version 300 es\n"+
        "#define PI 3.14159265359\n"+
        "#define TWO_PI 6.283185307179586\n"+
        "#define INV_TWO_PI 0.15915494309189535\n"+
        "#define CMAP cmapR1\n"+
        "precision lowp float;\n"+
//        "vec3 rgb2hsv(vec3 c){ vec4 K = vec4(0.0, -1.0 / 3.0, 2.0 / 3.0, -1.0);"+
//        "    vec4 p = mix(vec4(c.bg, K.wz), vec4(c.gb, K.xy), step(c.b, c.g));"+
//        "    vec4 q = mix(vec4(p.xyw, c.r), vec4(c.r, p.yzx), step(p.x, c.r));"+
//        "    float d = q.x - min(q.w, q.y); float e = 1.0e-10;"+
//        "    return vec3(abs(q.z + (q.w - q.y) / (6.0 * d + e)), d / (q.x + e), q.x);"+
//        "}"+
//        "vec3 hsv2rgb(vec3 c){ vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);"+
//        "    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);"+
//        "    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);"+
//        "}"+
        // BEGIN CMAPS
        "float poly(float x, float p0, float p1, float p2, float p3, float p4){\n" +
        "   float x2 = x*x; float x4 = x2*x2; float x3 = x2*x;\n" +
        "   return clamp(p0+p1*x+p2*x2+p3*x3+p4*x4,0.0,1.0);\n" +
        "}"+
        "vec3 cmapR1(float t){\n" +
        "    return vec3( poly(t,0.91, 3.74, -32.33, 57.57, -28.99), poly(t,0.2, 5.6, -18.89, 25.55, -12.25), poly(t,0.22, -4.89, 22.31, -23.58, 5.97) );\n" +
        "}"+
        "vec3 cmapR2(float t){\n" +
        "    return vec3( poly(t,1.09, -3.29, 15.43, -30.28, 18.29), poly(t,0.72, -8.03, 35.95, -53.96, 26.02), poly(t,1.06, -5.37, 4.06, 11.65, -10.58) );\n" +
        "}"+
        "vec3 cmapace(float t){\n" +
        "    return vec3( poly(t,-0.09, 5.25, -7.53, -0.16, 2.61), poly(t,-0.17, 7.08, -14.89, 6.12, 2.03), poly(t,-0.09, 5.25, -7.53, -0.16, 2.61) );\n" +
        "}"+
        "vec3 cmapC3(float t){\n" +
        "    return vec3( poly(t,0.78, 4.31, -24.48, 34.15, -13.83), poly(t,0.91, -3.18, 2.06, 3.97, -2.9), poly(t,0.9, -3.54, -1.17, 17.6, -13.05) );\n" +
        "}"+
        "vec3 cmapcb1(float t){\n" +
        "    return vec3( poly(t,0.15, 6.27, -16.92, 14.0, -3.21), poly(t,0.43, 4.35, -13.07, 10.17, -1.28), poly(t,0.79, 4.62, -25.63, 34.86, -13.71) );\n" +
        "}"+
        "vec3 cmapcb2(float t){\n" +
        "    return vec3( poly(t,1.09, -5.45, 26.01, -44.69, 24.22), poly(t,1.08, -6.04, 25.67, -39.2, 19.57), poly(t,1.21, -11.34, 41.35, -50.99, 20.76) );\n" +
        "}"+
        "vec3 cmaptrans(float t){\n" +
        "    return vec3( poly(t,0.36, 1.94, 0.44, -4.35, 1.96), poly(t,0.86, -1.72, 4.98, -3.29, -0.08), poly(t,1.03, -1.9, 4.14, -1.83, -0.5) );\n" +
        "}"+
        "vec3 cmappride(float t){\n" +
        "    return vec3( poly(t,0.89, 4.73, -34.69, 56.42, -26.51), poly(t,-0.17, 11.48, -40.96, 49.28, -19.65), poly(t,0.27, -2.31, 9.27, -7.07, -0.22) );\n" +
        "}"+
        "vec3 cmap(float t){return CMAP(t);}"+
        // END CMAPS
        "vec2 particleNumberToTex(int p, int n){\n"+
        "float i = floor(float(p)/float(n)); float j = mod(float(p),float(n));\n"+
        "return vec2( (j+0.5)/float(n), (i+0.5)/float(n) );\n"+
        "}\n"+
        "uniform int transitionStep; uniform int transitionSteps;\n"+
        "in vec3 a_position; uniform int n; uniform float scale;\n"+
        "uniform mat4 proj; uniform vec2 res;\n"+
        "uniform lowp sampler2D pTex; uniform lowp sampler2D qTex;\n"+
        "out vec4 o_colour;\n"+
        "flat out float transitionAlpha;\n"+
        "void main(void){" +
        "vec2 coords = particleNumberToTex(gl_InstanceID,n);\n"+
        "vec4 P = texture(pTex,coords);"+
        "vec4 Q  = texture(qTex,coords);\n"+
        //"float s = clamp(P.w,0.3,1.0);\n"+
        "float thetav = atan(P.y-Q.y,P.x-Q.x);\n"+
        "if (thetav < 0.0) {thetav += TWO_PI;}\n"+
        "vec3 rgbColour = cmap(thetav*INV_TWO_PI);\n"+
        //"vec3 rgbColour = texture(cmapTex,vec2(thetav/(2.0*PI),0.0)).rgb;\n"+
//        "vec3 hsvColour = rgb2hsv(texture(cmapTex,vec2(thetav/(2.0*PI),0.0)).rgb);\n"+
//        "hsvColour.b = s;\n"+
        "o_colour = vec4(rgbColour,1.0);\n"+
        "vec4 pos = proj*vec4(P.x,P.y,0.0,1.0);\n"+
        "gl_Position = vec4(a_position.xy+pos.xy,0.0,1.0);\n"+
        "gl_PointSize = scale;\n"+
        "float x = clamp(float(transitionStep)/float(transitionSteps),0.0,1.0);\n"+
        "x = 1.0-x;\n"+
        "transitionAlpha = x*x;\n"+
        "}",
    override val fragmentShader: String = "#version 300 es\n"+
        "precision lowp float;\n"+
        "in vec4 o_colour;\n"+
        "flat in float transitionAlpha;\n"+
        "out vec4 colour;\n"+
        "void main(void){\n"+
        "vec2 circCoord = 2.0 * gl_PointCoord - 1.0;"+
        //"float dd = length(circCoord);\n"+
        //"float alpha = 1.0-smoothstep(0.9,1.1,dd);\n"+
        "colour = vec4(o_colour.rgb,transitionAlpha);\n"+
        "if (colour.a == 0.0){discard;}}")
        : ShaderData(vertexShader,fragmentShader)

data class ToyDrawShaderData(
    override val vertexShader: String = "#version 300 es\n"+
            "precision lowp float; precision highp int;\n"+
            "in vec3 a_position;\n"+
            "out vec2 centre; out vec4 o_colour; flat out float time; flat out int spins;\n"+
            "uniform int na; uniform int nr; uniform mat4 attr; uniform mat4 rep;\n"+
            "uniform int ns; uniform mat4 spin; uniform int nf; uniform mat4 freeze;\n"+
            "uniform float scale; uniform vec2 res; uniform mat4 proj;\n"+
            "uniform float t; uniform float contTime; uniform float T; uniform float alpha;\n"+
            "void main(void){\n"+
            "   float a_offset = float(gl_InstanceID);\n"+
            "   int a = int(floor(a_offset)); float x = 0.0; float y = 0.0; float drawa = 0.0; float drawr = 0.0; float draws = 0.0; float drawf = 0.0;\n"+
            "   if (a < na){ int col = int(floor(float(a)/2.0)); int o = int(2.0*mod(float(a),2.0));\n"+
            "        x = attr[col][0+o]; y = attr[col][1+o]; drawa = 0.33;}\n"+
            "   if (a >= 8 && a < 8+nr){ a = a-8; int col = int(floor(float(a)/2.0)); int o = int(2.0*mod(float(a),2.0));\n"+
            "        x = rep[col][0+o]; y = rep[col][1+o]; drawr = 0.33;}\n"+
            "   if (a >= 16 && a < 16+ns){ a = a-16; int col = int(floor(float(a)/2.0)); int o = int(2.0*mod(float(a),2.0));\n"+
            "        x = spin[col][0+o]; y = spin[col][1+o]; draws = 0.33;}\n"+
            "   if (a >= 24 && a < 24+nf){ a = a-24; int col = int(floor(float(a)/2.0)); int o = int(2.0*mod(float(a),2.0));\n"+
            "        x = freeze[col][0+o]; y = freeze[col][1+o]; drawf = 0.33;}\n"+
            "   centre = vec2(x,y);\n"+
            "   vec4 pos = proj*vec4(centre.xy,0.0,1.0);\n"+
            "   gl_Position = vec4(a_position.x+pos.x,a_position.y+pos.y,0.0,1.0);\n"+
            "   o_colour = vec4(1.0,1.0,1.0,0.0); time = 1.0;\n"+
            "   spins = 0;"+
            "   if (drawr > 0.0 ){ o_colour = vec4(1.0,0.0,0.0,alpha); time = t/T; gl_PointSize = time*scale;}"+
            "   else if (drawa > 0.0){ o_colour = vec4(0.0,1.0,0.0,alpha); time = 1.0-t/T; gl_PointSize = time*scale;}\n"+
            "   else if (draws > 0.0){ o_colour = vec4(199.0/255.0,203.0/255.0,1.0,alpha); spins=1; time = contTime/T; gl_PointSize = scale;}\n"+
            "   else if (drawf > 0.0){ o_colour = vec4(204.0/255.0,224.0/255.0,1.0,1.0); time = contTime/T; gl_PointSize = scale;}\n"+
            "}",
    override val fragmentShader: String = "#version 300 es\n"+
            "precision lowp float;\n"+
            "in vec4 o_colour; flat in float time; flat in int spins;\n"+
            "out vec4 colour;\n"+
            "void main(void){\n"+
            "if (o_colour.a == 0.0){discard;}\n"+
            "vec2 circCoord = 2.0 * gl_PointCoord - 1.0;"+
            "float dd = length(circCoord);\n"+
            "float alpha = o_colour.a*(1.0-smoothstep(0.9,1.1,dd));\n"+
            "vec2 spiral = vec2( 0.5*cos(time*30.0), 0.5*sin(time*30.0) );"+
            "alpha = (1.0-float(spins))*alpha+alpha*float(spins)*( smoothstep(length(circCoord-spiral),0.1,0.2) );"+
            "colour = vec4(o_colour.rgb,alpha);\n"+
            "if (colour.a == 0.0){discard;}}")
    : ShaderData(vertexShader,fragmentShader)

/*

    Applies basic physics (newtons equations) with rotational diffusion (noise)

    The method is a 2nd order accurate (in time) integrator which correctly handles the
    noise.

    This is essentially a Verlet integration with noise.

    Data in textures:

        "uniform highp sampler2D pTex;\n"+ // x,y,theta,cell
        "uniform highp sampler2D qTex;\n"+ // xp,yp,thetap,wp
        "uniform highp sampler2D paramTex;\n"+ // i,.,.,.

    pTex and qTex are written to

    noise takes a time-based seed, basic pseudo randoms for per-particle noise based on that

    Attractors and repellers are handles as mat4's so 16 floats = 8 x-y coordinates max
 */
data class NielsOdedIntegratorShaderData(
    override val vertexShader: String =
        "#version 300 es\n"+
                "precision highp float;\n"+
                "precision highp int;\n"+
                "layout(location = 0) in vec3 a_position;\n"+
                "layout(location = 1) in vec2 a_texCoords;\n"+
                "out vec2 o_texCoords;\n"+
                "void main(void){\n" +
                "   gl_Position = vec4(a_position,1);\n"+
                "   o_texCoords = a_texCoords.st;\n"+
                "}\n",
    override val fragmentShader: String =
        "#version 300 es\n"+
                "precision highp float;\n"+
                "precision highp int;\n"+
                "#define PI 3.14159265359\n"+
                "float prng(float x, float y){\n"+
                "   return clamp(fract(sin(x*12.9898 + y*78.233)*43758.5453),0.001,0.999);\n"+
                "}\n"+
                "float wiener(float t, float seed){\n"+
                "   float u = prng(seed+t,seed-t); return log(u/(1.0-u))*0.6266570686577501;\n"+
                "}\n"+
                "vec2 particleNumberToTex(int p, int n){\n"+
                "float i = floor(float(p)/float(n)); float j = mod(float(p),float(n));\n"+
                "return vec2( (j+0.5)/float(n), (i+0.5)/float(n) );\n"+
                "}\n"+
                "float periodicBound(float x, float l, float L){if (x < l){return L+(x-l);} else if (x > L) {return l+L-x;} else {return x;}}\n"+
                "in vec2 o_texCoords;\n"+
                "layout(location = 0) out vec4 newP; layout(location = 1) out vec4 newQ;\n"+
                "uniform int na; uniform int nr; uniform mat4 attr; uniform mat4 rep;\n"+
                "uniform int ns; uniform mat4 spin; uniform int nf; uniform mat4 freeze;\n"+
                "uniform vec2 res; uniform float softMaxRadialDistance;\n"+
                "uniform highp sampler2D pTex;\n"+ // x,y,theta,cell
                "uniform highp sampler2D qTex;\n"+ // xp,yp,thetap,wp
                "uniform highp sampler2D paramTex;\n"+ // i,.,.,.
                "uniform int n; uniform int np; uniform float dt; uniform float seed;\n"+
                "uniform float rad; uniform float v;\n"+
                "uniform float Lx; uniform float lx; uniform float Ly; uniform float ly;\n"+
                "uniform float ar; uniform float at; uniform float br; uniform float bt;\n"+
                "uniform float alpha; uniform float beta; uniform float gamma; uniform float DR;\n"+
                "uniform int paused;\n"+
                "void main(void) {\n"+
                "    float fn = float(n);\n"+
                "    vec4 p = texture(pTex,o_texCoords); vec4 q = texture(qTex,o_texCoords);\n"+
                "    vec4 param = texture(paramTex,o_texCoords);\n"+
                "    vec2 r = vec2(res.x/2.0-p.x,res.y/2.0-p.y); float d = r.x*r.x+r.y*r.y;\n"+
                "    vec2 f = v*(100.0+clamp(d,0.0,100.0))*vec2(cos(p.z),sin(p.z));\n"+
                "    bool allowRepulsion = d < softMaxRadialDistance*softMaxRadialDistance/3.0;\n"+
                "    bool frozen = false;\n"+
                "    float torque = 0.0;\n"+
                "    for (int j = 0; j < 8; j++){\n"+
                "       int col = int(floor(float(j)/2.0)); int o = int(2.0*mod(float(j),2.0));\n"+
                //      ATTRACTORS
                "       if (j < na){\n"+
                "       vec2 r = vec2(attr[col][0+o]-p.x,attr[col][1+o]-p.y);\n"+
                "       float d = r.x*r.x+r.y*r.y;\n"+
                "       if (d < 3.0) { float t = 2.0*PI*prng(param.x/fn+seed,seed+p.w); r = vec2(cos(t),sin(t)); d = 1.0; }\n"+
                "       else { f.x += 50000.0 * r.x / d; f.y += 50000.0 * r.y / d;}}\n"+
                //      REPELLERS
                "       if (j < nr && allowRepulsion){\n"+
                "       vec2 r = vec2(rep[col][0+o]-p.x,rep[col][1+o]-p.y);\n"+
                "       float d = r.x*r.x+r.y*r.y;\n"+
                "       f.x -= 50000.0*r.x/d; f.y -= 50000.0*r.y/d;}\n"+
                //      SPINNERS
                "       if (j < ns){\n"+
                "       vec2 r = vec2(spin[col][0+o]-p.x,spin[col][1+o]-p.y);\n"+
                "       float d = r.x*r.x+r.y*r.y;\n"+
                "       if (d > 3.0){torque -= 2500.0*min(1.0/sqrt(d),10.0);}}\n"+
                //      FREEZERS
                "       if (j < nf){\n"+
                "       vec2 r = vec2(freeze[col][0+o]-p.x,freeze[col][1+o]-p.y);\n"+
                "       float d = r.x*r.x+r.y*r.y;\n"+
                "       if (d < 30000.0){frozen = true;}}\n"+
                "    \n}"+
                "    float x = p.x; float y = p.y; float theta = p.z;\n"+
                "    float xp = q.x; float yp = q.y; float thetap = q.z;\n"+
                "    float newX = 2.0*bt * x - at*xp + alpha*f.x;\n"+
                "    float newY = 2.0*bt * y - at*yp + alpha*f.y;\n"+
                "    float w = DR*wiener(param.x/fn+seed,seed);\n"+
                "    float newTheta = 2.0*br * theta - ar*thetap + beta*torque + gamma*(w+q.w);\n"+
                "    if (frozen){ newX = x; newY = y; newTheta = theta; }\n"+
                //"    float vx = (newX-x)/dt; float vy = (newY-y)/dt; bool b = false;\n"+
                //"    float s = log((vx*vx+vy*vy)+1.0)/1.0;\n"+
                "    float cross = (cos(newTheta)*r.y-sin(newTheta)*r.x)/sqrt(d);\n"+
                "    if (newX < lx+rad*5.0 || newX > Lx - rad*5.0) { newTheta += cross*dt; }\n"+
                "    if (newY < ly+rad*5.0 || newY > Ly - rad*5.0) { newTheta += cross*dt; }\n"+
                "    if (paused == 0) { newP = vec4(newX,newY,newTheta,1.0);\n"+
                "                       newQ = vec4(x,y,theta,w);}\n"+
                "    else { newP = p; newQ = q; } \n"+
                "}"
) : ShaderData(vertexShader, fragmentShader)
