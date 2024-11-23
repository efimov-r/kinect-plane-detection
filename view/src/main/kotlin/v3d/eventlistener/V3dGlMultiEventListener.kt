package v3d.eventlistener

import cloud.BufferedChannel
import cloud.databuffer.CloudForView
import com.jogamp.opengl.GL
import com.jogamp.opengl.GL3
import com.jogamp.opengl.GLAutoDrawable
import com.jogamp.opengl.GLEventListener
import com.jogamp.opengl.util.AnimatorBase
import kotlinx.coroutines.runBlocking
import v3d.utils.FpsInformator
import v3d.utils.V3dUtils

class V3dGlMultiEventListener(
    private val v3dComplects: MutableList<V3dComplect>,
    private val v3dModelComplects: MutableList<V3dModelComplect>,
    private val channelToView: BufferedChannel<CloudForView>
) : GLEventListener {

    private var initialized = false
    var modelviewMatrix = FloatArray(16)
    var animator: AnimatorBase? = null
    var fpsInformator: FpsInformator? = null

    override fun init(drawable: GLAutoDrawable) {
        val gl = V3dUtils.glInit(drawable)
        val aspect = drawable.getSurfaceWidth().toFloat() / drawable.getSurfaceHeight()

        v3dComplects.forEach { V3dComplect.initComplect(gl, aspect, it, animator) }
        v3dModelComplects.forEach{ V3dModelComplect.initModelComplect(gl, aspect, it, animator) }

        initialized = true
    }

    override fun dispose(glAutoDrawable: GLAutoDrawable?) {
    }

    override fun display(drawable: GLAutoDrawable) {
        if (!initialized) return

        runBlocking{
            channelToView.getLastDataForRead()?.let { lastDataForRead ->
                val gl = drawable.getGL() as GL3
                gl.glClear(GL.GL_COLOR_BUFFER_BIT or GL.GL_DEPTH_BUFFER_BIT)
                v3dComplects.forEach { V3dComplect.displayComplect(gl, it, lastDataForRead, modelviewMatrix) }
                v3dModelComplects.forEach{ V3dModelComplect.displayModelComplect(gl, it, modelviewMatrix) }

                channelToView.endRead(lastDataForRead)

                fpsInformator?.informate()
            }
        }
    }

    override fun reshape(drawable: GLAutoDrawable, x: Int, y: Int, width: Int, height: Int) {
        V3dUtils.glViewport(drawable)

        val aspect = drawable.getSurfaceWidth().toFloat() / drawable.getSurfaceHeight()
        v3dComplects.forEach { V3dComplect.reshapeComplect(it, aspect) }
        v3dModelComplects.forEach { V3dModelComplect.reshapeModelComplect(it, aspect) }
    }
}