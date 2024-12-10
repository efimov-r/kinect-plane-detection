package v3d.eventlistener

import bufferchanel.BufferedChannel
import bufferchanel.databuffer.BufferedChannelForView
import com.jogamp.opengl.GL
import com.jogamp.opengl.GL3
import com.jogamp.opengl.GLAutoDrawable
import com.jogamp.opengl.GLEventListener
import com.jogamp.opengl.util.AnimatorBase
import kotlinx.coroutines.runBlocking
import v3d.utils.FpsInformator
import v3d.utils.V3dUtils
import java.util.Date

class V3dGlMultiEventListener(
    private val v3dComplects: List<V3dComplect>,
    private val v3dModelComplects: List<V3dModelComplect>,
    private val channelToView: BufferedChannel<BufferedChannelForView>?
) : GLEventListener {

    private var initialized = false
    var modelviewMatrix = FloatArray(16)
    var animator: AnimatorBase? = null
    var fpsInformator: FpsInformator? = null
    var dat: Date = Date()

    override fun init(drawable: GLAutoDrawable) {
        val gl = V3dUtils.glInit(drawable)
        val aspect = drawable.getSurfaceWidth().toFloat() / drawable.getSurfaceHeight()

        v3dComplects.forEach { it.init(gl, aspect, animator) }
        v3dModelComplects.forEach{ it.init(gl, aspect, animator) }

        initialized = true
    }

    override fun dispose(glAutoDrawable: GLAutoDrawable?) {
    }

    override fun display(drawable: GLAutoDrawable) {
        if (!initialized) return
        val gl = drawable.getGL() as GL3

        runBlocking{
            channelToView?.getLastDataForRead()?.let { lastDataForRead ->
                gl.glClear(GL.GL_COLOR_BUFFER_BIT or GL.GL_DEPTH_BUFFER_BIT)
                v3dComplects.forEach { it.display(gl, lastDataForRead, modelviewMatrix) }
                v3dModelComplects.forEach{ it.display(gl, modelviewMatrix) }

                channelToView.endRead(lastDataForRead)

                fpsInformator?.informate()
                dat = Date()
                return@runBlocking
            }
            if(Date().time - dat.time > 40) {
                gl.glClear(GL.GL_COLOR_BUFFER_BIT or GL.GL_DEPTH_BUFFER_BIT)
                v3dComplects.forEach { it.redisplay(gl, modelviewMatrix) }
                v3dModelComplects.forEach{ it.display(gl, modelviewMatrix) }
                fpsInformator?.informate()
                dat = Date()
            }
        }
    }

    override fun reshape(drawable: GLAutoDrawable, x: Int, y: Int, width: Int, height: Int) {
        V3dUtils.glViewport(drawable)

        val aspect = drawable.getSurfaceWidth().toFloat() / drawable.getSurfaceHeight()
        v3dComplects.forEach { it.reshape(aspect) }
        v3dModelComplects.forEach { it.reshape(aspect) }
    }
}