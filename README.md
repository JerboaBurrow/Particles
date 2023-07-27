# Particles
### [jerboa.app/particles](https://jerboa.app/particles) Android app, source code

Welcome! This is source code to the Android app Particles, it is licensed under the GPL (v3). So you 
are free to view, modify, use etc the code as long as you also release it under the GPL (v3).

![feature](https://github.com/JerboaBurrow/Particles/assets/84378622/3a004e59-82d4-4f33-a464-1a4728878d48)

The app uses [Android Jetpack Compose](https://developer.android.com/jetpack/compose?gclid=CjwKCAjwq4imBhBQEiwA9Nx1Bng2Y188HWKz4WYfktYXKEIbvSbMU2oG1ElnjMk83p-jP5zKVaOZkxoC2LoQAvD_BwE&gclsrc=aw.ds) to structure the UI backend/frontent content. [Kotlin](https://kotlinlang.org/) is the main language used. Rendering is done using [Opengl ES 3.0](https://registry.khronos.org/OpenGL-Refpages/es3.0/), in particular using the old school "compute shader" technique to simulate the particle system - i.e. just Vertex and Fragment shaders are used with a single quad to integrate the equations of motion. That is GPGPU. An old tutorial is here [WaybackMachine: http://www.mathematik.uni-dortmund.de/~goeddeke/gpgpu/tutorial.html](https://web.archive.org/web/20190410185616/http://www.mathematik.uni-dortmund.de/~goeddeke/gpgpu/tutorial.html)

#### Questions

Why GPL?
- As a "complete" standalone app designed to be free and open source the GPL preserves this, and in particular not
  permitting closed source modifications. The intention is not as a library for other software to be based upon,
  where MIT, BSD, etc. are more widely used.
