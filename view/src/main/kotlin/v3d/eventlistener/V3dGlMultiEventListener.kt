package v3d.eventlistener

import com.jogamp.opengl.GL
import com.jogamp.opengl.GL3
import com.jogamp.opengl.GLAutoDrawable
import com.jogamp.opengl.GLEventListener
import com.jogamp.opengl.util.AnimatorBase
import v3d.V3dRunner
import v3d.shaders.Shader
import v3d.utils.FpsInformator
import v3d.utils.V3dUtils.showErrorMess
import v3d.vbos.VBOHelper

class V3dGlMultiEventListener(
    private val v3dComplects: MutableList<V3dComplect>
) : GLEventListener {
    class V3dComplect {
        var runner: V3dRunner? = null
        var vboHelper: VBOHelper? = null
        var shaders: MutableList<Shader>? = null
        var countPointsRender: Int = 0
    }

    private var initialized = false

    private var modelviewMatrix = FloatArray(16)

    var animator: AnimatorBase? = null
    var fpsInformator: FpsInformator? = null

    fun setModelviewMatrix(modelviewMatrix: FloatArray) {
        this.modelviewMatrix = modelviewMatrix
    }

    override fun init(drawable: GLAutoDrawable) {
        val gl = drawable.getGL().getGL3()
        gl.setSwapInterval(0)
        gl.glEnable(GL.GL_DEPTH_TEST)
        gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)

        val glv = drawable.getGL() as GL3
        glv.glViewport(
            0, 0,
            drawable.getSurfaceWidth(),
            drawable.getSurfaceHeight()
        )

        for (v3dComplect in v3dComplects) {
            for (shader in v3dComplect.shaders!!) {
                shader.initShader(gl)
                shader.setupView(drawable.getSurfaceWidth().toFloat() / drawable.getSurfaceHeight())
            }

            if (!gl.isExtensionAvailable("GL_ARB_vertex_buffer_object")) {
                showErrorMess(
                    "Unavailable extension",
                    "GL_ARB_vertex_buffer_object extension not available",
                    animator
                )
            }

            v3dComplect.vboHelper!!.init(gl)

            for (shader in v3dComplect.shaders!!) {
                shader.initAttributes(gl)
            }
        }

        initialized = true
    }

    override fun dispose(glAutoDrawable: GLAutoDrawable?) {
    }

    override fun display(drawable: GLAutoDrawable) {
        if (!initialized) return

        val gl = drawable.getGL() as GL3
        gl.glClear(GL.GL_COLOR_BUFFER_BIT or GL.GL_DEPTH_BUFFER_BIT)
        for (v3dComplect in v3dComplects) {
            val floatBuffer = v3dComplect.vboHelper!!.mapBuffer(gl)
            if(floatBuffer != null) {
                val countPointsRenderLocal = v3dComplect.runner!!.setVertexBuffer(floatBuffer)
                v3dComplect.vboHelper!!.unmapBuffer(gl)
                if(countPointsRenderLocal != null) {
                    v3dComplect.countPointsRender = countPointsRenderLocal
                }
            }


            for (shader in v3dComplect.shaders!!) {
                shader.setShader(gl, modelviewMatrix)
                v3dComplect.vboHelper!!.render(gl, v3dComplect.countPointsRender)
            }
        }

        fpsInformator?.informate()
    }

    override fun reshape(drawable: GLAutoDrawable, x: Int, y: Int, width: Int, height: Int) {
        val gl = drawable.getGL() as GL3
        gl.glViewport(
            0, 0,
            drawable.getSurfaceWidth(),
            drawable.getSurfaceHeight()
        )

        for (v3dComplect in v3dComplects) {
            for (shader in v3dComplect.shaders!!) {
                shader.setupView(drawable.getSurfaceWidth().toFloat() / drawable.getSurfaceHeight())
            }
        }
    }
}