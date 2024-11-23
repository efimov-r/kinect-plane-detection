package v3d.eventlistener

import cloud.databuffer.CloudForView
import com.jogamp.opengl.GL3
import com.jogamp.opengl.util.AnimatorBase
import v3d.V3dRunner
import v3d.shaders.Shader
import v3d.utils.V3dUtils
import v3d.vbos.other.VBO

data class V3dComplect (var runner: V3dRunner,
                        var vbo: VBO,
                        var shaders: MutableList<Shader> = ArrayList(),
                        var countPointsRender: Int = 0) {
    companion object {
        fun initComplect(gl: GL3, aspect: Float, complect: V3dComplect, animator: AnimatorBase?) {
            for (shader in complect.shaders) {
                shader.initShader(gl)
                shader.setupView(aspect)
            }

            V3dUtils.checkGlEx(gl, animator)

            complect.vbo.init(gl)

            for (shader in complect.shaders) {
                shader.initAttributes(gl)
            }
        }

        fun displayComplect(gl: GL3, v3dComplect: V3dComplect, cloud: CloudForView, modelviewMatrix: FloatArray) {
            if(v3dComplect.runner.needSet(cloud)) {
                val floatBuffer = v3dComplect.vbo.mapBuffer(gl)
                val countPointsRenderLocal = v3dComplect.runner.setVertexBuffer(floatBuffer, cloud)
                v3dComplect.vbo.unmapBuffer(gl)
                v3dComplect.countPointsRender = countPointsRenderLocal
            }

            v3dComplect.shaders.forEach { shader ->
                shader.setShader(gl, modelviewMatrix)
                v3dComplect.vbo.render(gl, v3dComplect.countPointsRender)
            }
        }

        fun reshapeComplect(v3dComplect: V3dComplect, aspect: Float) {
            v3dComplect.shaders.forEach { it.setupView(aspect) }
        }
    }
}