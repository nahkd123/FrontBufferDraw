package io.github.naharaoss.frontbufferdraw

import android.opengl.GLES20
import android.util.Log
import androidx.compose.ui.graphics.Matrix
import androidx.graphics.lowlatency.BufferInfo
import androidx.graphics.lowlatency.GLFrontBufferedRenderer
import androidx.graphics.opengl.egl.EGLManager

class DrawAreaRenderer : GLFrontBufferedRenderer.Callback<Pair<PenInput, PenInput>> {
    private var initialized = false
    private var program = 0

    private fun initialize() {
        if (initialized) return
        initialized = true
        val vertex = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER)
        GLES20.glShaderSource(vertex, """
            #version 320 es
            precision mediump float;
            
            layout(location = 0) uniform mat4 modelView;
            
            const vec4 a[4] = vec4[](
                vec4(-0.5, -0.5, 0, 1),
                vec4( 0.5, -0.5, 0, 1),
                vec4(-0.5,  0.5, 0, 1),
                vec4( 0.5,  0.5, 0, 1)
            );
            
            void main() {
                gl_Position = modelView * a[gl_VertexID];
            }
        """.trimIndent())
        GLES20.glCompileShader(vertex)
        Log.e("DrawAreaRenderer", GLES20.glGetShaderInfoLog(vertex))

        val fragment = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER)
        GLES20.glShaderSource(fragment, """
            #version 320 es
            precision mediump float;
            
            out vec4 color;
            
            void main() {
                color = vec4(0, 0, 0, 1);
            }
        """.trimIndent())
        GLES20.glCompileShader(fragment)
        Log.e("DrawAreaRenderer", GLES20.glGetShaderInfoLog(fragment))

        program = GLES20.glCreateProgram()
        GLES20.glAttachShader(program, vertex)
        GLES20.glAttachShader(program, fragment)
        GLES20.glLinkProgram(program)
    }

    override fun onDrawFrontBufferedLayer(
        eglManager: EGLManager,
        width: Int,
        height: Int,
        bufferInfo: BufferInfo,
        transform: FloatArray,
        param: Pair<PenInput, PenInput>
    ) {
        param.each {
            GLES20.glUseProgram(program)
            GLES20.glUniformMatrix4fv(0, 1, false, Matrix().apply {
                scale(2f / width, 2f / height)
                translate(it.x - width / 2, it.y - height / 2)
                scale(10f * it.pressure, 10f * it.pressure)
            }.values, 0)
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        }
    }

    override fun onDrawMultiBufferedLayer(
        eglManager: EGLManager,
        width: Int,
        height: Int,
        bufferInfo: BufferInfo,
        transform: FloatArray,
        params: Collection<Pair<PenInput, PenInput>>
    ) {
        initialize()
        GLES20.glViewport(0, 0, width, height)
        GLES20.glClearColor(1f, 1f, 1f, 1f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        GLES20.glUseProgram(program)
        params.forEach { param ->
            param.each {
                GLES20.glUniformMatrix4fv(0, 1, false, Matrix().apply {
                    scale(2f / width, 2f / height)
                    translate(it.x - width / 2, it.y - height / 2)
                    scale(10f * it.pressure, 10f * it.pressure)
                }.values, 0)
                GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
            }
        }

        Log.d("DrawAreaRenderer", "GL error: ${GLES20.glGetError()}")
    }
}