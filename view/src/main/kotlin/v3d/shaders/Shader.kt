package v3d.shaders

import com.jogamp.opengl.GL3

interface Shader {
    fun initShader(gl: GL3)
    fun setShader(gl: GL3, modelviewMatrix: FloatArray)
    fun initAttributes(gl: GL3)
    fun setupView(aspect: Float)
}
