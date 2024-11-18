package v3d

import com.jogamp.opengl.GLCapabilities
import com.jogamp.opengl.awt.GLCanvas
import com.jogamp.opengl.util.AnimatorBase
import com.jogamp.opengl.util.FPSAnimator
import v3d.eventlistener.CallbackJkiEventListener
import v3d.eventlistener.V3dGlMultiEventListener
import v3d.eventlistener.V3dGlMultiEventListener.V3dComplect
import v3d.utils.FpsInformator
import v3d.utils.V3dMouseControl
import v3d.utils.V3dMouseControl.SetMatrixCallback
import v3d.utils.V3dUtils
import v3d.utils.V3dUtils.runExit
import java.awt.Dimension

class GlComponentForView(
    glCapabilities: GLCapabilities?,
    callback: GlComponentCallback,
    v3dComplects: MutableList<V3dComplect>
) {
    private var animator: AnimatorBase? = null
    val glComponent: GLCanvas

    fun interface GlComponentCallback {
        fun callbackFpsInfo(text: String?)
    }

    init {
        val eventListener = V3dGlMultiEventListener(object : CallbackJkiEventListener {
            override fun animatorStop() {
                runExit(animator)
            }

            override fun setTitle(text: String?) {
                callback.callbackFpsInfo(text)
            }
        }, v3dComplects, FpsInformator())


        glComponent = GLCanvas(glCapabilities)
        glComponent.setFocusable(true)
        glComponent.addGLEventListener(eventListener)

        val mouseControl = V3dMouseControl(SetMatrixCallback { modelviewMatrix: FloatArray? ->
            eventListener.setModelviewMatrix(
                modelviewMatrix!!
            )
        })
        glComponent.addMouseMotionListener(mouseControl)
        glComponent.addMouseWheelListener(mouseControl)
        eventListener.setModelviewMatrix(
            V3dUtils.multiply(
                V3dUtils.multiply(V3dUtils.rotationX(40f), V3dUtils.rotationY(30f)),
                V3dUtils.translation(0f, 0f, -4f)
            )!!
        )

        animator = FPSAnimator(glComponent, 40)
        animator!!.start()

        glComponent.setPreferredSize(Dimension(800, 800))
    }


    fun runExitAnimator() {
        runExit(animator)
    }

    fun requestFocus() {
        glComponent.requestFocus()
    }
}
