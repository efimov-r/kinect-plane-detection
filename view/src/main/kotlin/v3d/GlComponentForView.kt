package v3d

import com.jogamp.opengl.GLCapabilities
import com.jogamp.opengl.awt.GLJPanel
import com.jogamp.opengl.util.AnimatorBase
import com.jogamp.opengl.util.FPSAnimator
import v3d.eventlistener.V3dGlMultiEventListener
import v3d.utils.V3dMouseControl
import v3d.utils.V3dUtils
import v3d.utils.V3dUtils.runExit
import java.awt.Dimension
import java.awt.event.KeyAdapter

class GlComponentForView(
    glCapabilities: GLCapabilities?,
    eventListener: V3dGlMultiEventListener,
    keyAdapter: KeyAdapter? = null
) {
    private var animator: AnimatorBase? = null
    // GLJPanel вместо GLCanvas: GLCanvas на Windows с Java 9+ не находит GraphicsConfiguration
    val glComponent: GLJPanel

    init {
        glComponent = GLJPanel(glCapabilities)
        glComponent.setFocusable(true)
        glComponent.addGLEventListener(eventListener)

        val mouseControl = V3dMouseControl { modelviewMatrix: FloatArray ->
            eventListener.modelviewMatrix = modelviewMatrix
        }
        glComponent.addMouseMotionListener(mouseControl)
        glComponent.addMouseWheelListener(mouseControl)
        keyAdapter?.let { glComponent.addKeyListener(it) }
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
