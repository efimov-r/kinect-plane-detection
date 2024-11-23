package v3d.vbos.model

import com.jogamp.opengl.GL3

interface VBOModel {
    fun init(gl: GL3)
    fun render(gl: GL3, countOfVboPoint: Int)
}
