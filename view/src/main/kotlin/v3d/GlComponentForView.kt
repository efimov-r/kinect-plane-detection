package v3d

import com.jogamp.opengl.GLCapabilities
import com.jogamp.opengl.awt.GLCanvas
import com.jogamp.opengl.util.AnimatorBase
import com.jogamp.opengl.util.FPSAnimator
import v3d.eventlistener.V3dGlMultiEventListener
import v3d.utils.V3dMouseControl
import v3d.utils.V3dMouseControl.SetMatrixCallback
import v3d.utils.V3dUtils
import v3d.utils.V3dUtils.runExit
import java.awt.Dimension

class GlComponentForView(
    glCapabilities: GLCapabilities?,
    eventListener: V3dGlMultiEventListener
) {
    private var animator: AnimatorBase? = null
    val glComponent: GLCanvas

    init {
        glComponent = GLCanvas(glCapabilities)
        glComponent.setFocusable(true)
        glComponent.addGLEventListener(eventListener)

        val mouseControl = V3dMouseControl { modelviewMatrix: FloatArray ->
            eventListener.modelviewMatrix = modelviewMatrix
        }
        glComponent.addMouseMotionListener(mouseControl)
        glComponent.addMouseWheelListener(mouseControl)
        eventListener.modelviewMatrix = V3dUtils.multiply(
                V3dUtils.multiply(V3dUtils.rotationX(40f), V3dUtils.rotationY(30f)),
                V3dUtils.translation(0f, 0f, -4f)
            )

        animator = FPSAnimator(glComponent, 120)
        eventListener.animator = animator
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
