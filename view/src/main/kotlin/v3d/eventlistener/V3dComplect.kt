package v3d.eventlistener

import cloud.databuffer.CloudForView
import com.jogamp.opengl.GL3
import com.jogamp.opengl.util.AnimatorBase
import v3d.runners.V3dRunner
import v3d.shaders.Shader
import v3d.utils.V3dUtils
import v3d.vbos.other.VBO

data class V3dComplect (var runner: V3dRunner,
                        var vbo: VBO,
                        var shaders: MutableList<Shader> = ArrayList(),
                        var countPointsRender: Int = 0,
                        private var currentSizeBuff: Int) {
    fun init(gl: GL3, aspect: Float, animator: AnimatorBase?) {
        for (shader in this.shaders) {
            shader.initShader(gl)
            shader.setupView(aspect)
        }

        V3dUtils.checkGlEx(gl, animator)

        this.vbo.init(gl, this.currentSizeBuff)

        for (shader in this.shaders) {
            shader.initAttributes(gl)
        }
    }

    fun redisplay(gl: GL3, modelviewMatrix: FloatArray) {
        this.shaders.forEach { shader ->
            shader.setShader(gl, modelviewMatrix)
            this.vbo.render(gl, this.countPointsRender)
        }
    }

    fun display(gl: GL3, cloud: CloudForView, modelviewMatrix: FloatArray) {
        if(this.runner.needSet(cloud)) {
            val newSize = this.runner.getBuffSize(cloud)
            if(newSize > this.currentSizeBuff) {
                this.vbo.init(gl, newSize)
                this.currentSizeBuff = newSize

                for (shader in this.shaders) {
                    shader.initAttributes(gl)
                }
            }
            val floatBuffer = this.vbo.mapBuffer(gl)
            this.countPointsRender = this.runner.setVertexBuffer(floatBuffer, cloud)
            this.vbo.unmapBuffer(gl)
        }

        this.shaders.forEach { shader ->
            shader.setShader(gl, modelviewMatrix)
            this.vbo.render(gl, this.countPointsRender)
        }
    }

    fun reshape(aspect: Float) {
        this.shaders.forEach { it.setupView(aspect) }
    }
}