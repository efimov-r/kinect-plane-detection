package v3d.utils

import v3d.utils.V3dUtils.multiply
import v3d.utils.V3dUtils.rotationX
import v3d.utils.V3dUtils.rotationY
import v3d.utils.V3dUtils.translation
import java.awt.Point
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionListener
import java.awt.event.MouseWheelEvent
import java.awt.event.MouseWheelListener

class V3dMouseControl(private val callback: SetMatrixCallback) : MouseMotionListener, MouseWheelListener {
    private var translationX = 0f
    private var translationY = 0f
    private var translationZ = -4f
    private var rotationX = 40f
    private var rotationY = 30f

    fun interface SetMatrixCallback {
        fun setMtx(mtx: FloatArray?)
    }

    private var previousMousePosition = Point()

    override fun mouseDragged(e: MouseEvent) {
        val dx = e.getX() - previousMousePosition.x
        val dy = e.getY() - previousMousePosition.y

        // If the left button is held down, move the object
        if ((e.getModifiersEx() and MouseEvent.BUTTON1_DOWN_MASK) ==
            MouseEvent.BUTTON1_DOWN_MASK
        ) {
            translationX += dx / 100.0f
            translationY -= dy / 100.0f
        } else if ((e.getModifiersEx() and MouseEvent.BUTTON3_DOWN_MASK) ==
            MouseEvent.BUTTON3_DOWN_MASK
        ) {
            rotationX += dy.toFloat()
            rotationY += dx.toFloat()
        }
        previousMousePosition = e.getPoint()
        updateModelviewMatrix()
    }

    override fun mouseMoved(e: MouseEvent) {
        previousMousePosition = e.getPoint()
    }

    override fun mouseWheelMoved(e: MouseWheelEvent) {
        // Translate along the Z-axis
        translationZ += e.getWheelRotation() * 0.25f
        previousMousePosition = e.getPoint()
        updateModelviewMatrix()
    }

    private fun updateModelviewMatrix() {
        val m0 = translation(translationX, translationY, translationZ)
        val m1 = rotationX(rotationX)
        val m2 = rotationY(rotationY)
        callback.setMtx(multiply(multiply(m1, m2), m0))
    }
}
