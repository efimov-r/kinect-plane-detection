package v3d.eventlistener

import com.jogamp.opengl.GL3
import com.jogamp.opengl.util.AnimatorBase
import v3d.shaders.Shader
import v3d.utils.V3dUtils
import v3d.vbos.model.VBOModel

data class V3dModelComplect(var vbo: VBOModel,
                            var shader: Shader
) {
    fun init(gl: GL3, aspect: Float, animator: AnimatorBase?) {
        this.shader.initShader(gl)
        this.shader.setupView(aspect)
        V3dUtils.checkGlEx(gl, animator)
        this.vbo.init(gl)
        this.shader.initAttributes(gl)
    }

    fun display(gl: GL3, modelviewMatrix: FloatArray) {
        this.shader.setShader(gl, modelviewMatrix)
        this.vbo.render(gl, 0)
    }

    fun reshape(aspect: Float) {
        this.shader.setupView(aspect)
    }
}