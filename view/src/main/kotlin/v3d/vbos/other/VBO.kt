package v3d.vbos.other

import com.jogamp.opengl.GL3
import java.nio.FloatBuffer

interface VBO {
    fun init(gl: GL3, size: Int)
    fun render(gl: GL3, countOfVboPoint: Int)
    fun mapBuffer(gl: GL3): FloatBuffer
    fun unmapBuffer(gl: GL3)
}
