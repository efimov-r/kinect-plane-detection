package v3d.eventlistener

import com.jogamp.opengl.GL3
import com.jogamp.opengl.util.AnimatorBase
import v3d.shaders.Shader
import v3d.utils.V3dUtils
import v3d.vbos.VBOHelper

data class V3dModelComplect(var vboHelper: VBOHelper,
                            var shader: Shader
) {
    companion object {
        fun initModelComplect(gl: GL3, aspect: Float, modelComplect: V3dModelComplect, animator: AnimatorBase?) {
            modelComplect.shader.initShader(gl)
            modelComplect.shader.setupView(aspect)
            V3dUtils.checkGlEx(gl, animator)
            modelComplect.vboHelper.init(gl)
            modelComplect.shader.initAttributes(gl)
        }

        fun displayModelComplect(gl: GL3, v3dModelComplect: V3dModelComplect, modelviewMatrix: FloatArray) {
            v3dModelComplect.shader.setShader(gl, modelviewMatrix)
            v3dModelComplect.vboHelper.render(gl, 0)
        }

        fun reshapeModelComplect(v3dModelComplect: V3dModelComplect, aspect: Float) {
            v3dModelComplect.shader.setupView(aspect)
        }
    }
}