package v3d.vbos

import com.jogamp.opengl.GL3
import java.nio.FloatBuffer

interface VBOHelper {
    fun init(gl: GL3)
    fun render(gl: GL3, countOfVboPoint: Int)
    fun mapBuffer(gl: GL3): FloatBuffer?
    fun unmapBuffer(gl: GL3)
}
