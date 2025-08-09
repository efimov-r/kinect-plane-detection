package v3d.utils

import com.jogamp.opengl.GL
import com.jogamp.opengl.GL3
import com.jogamp.opengl.GLAutoDrawable
import com.jogamp.opengl.util.Animator
import com.jogamp.opengl.util.AnimatorBase
import java.io.BufferedReader
import java.io.FileReader
import java.io.IOException
import java.util.*
import javax.swing.JOptionPane
import kotlin.math.cos
import kotlin.math.sin
import kotlin.system.exitProcess

object V3dUtils {
    fun glInit(drawable: GLAutoDrawable): GL3 {
        val gl = drawable.getGL().getGL3()
        gl.setSwapInterval(0)
        gl.glEnable(GL.GL_DEPTH_TEST)
        gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)

        (drawable.getGL() as GL3).glViewport(0, 0,
            drawable.getSurfaceWidth(),
            drawable.getSurfaceHeight()
        )
        return gl
    }

    fun glViewport(drawable: GLAutoDrawable) {
        val gl = drawable.getGL() as GL3
        gl.glViewport(
            0, 0,
            drawable.getSurfaceWidth(),
            drawable.getSurfaceHeight()
        )
    }

    fun checkGlEx(gl: GL3, animator: AnimatorBase?) {
        if (!gl.isExtensionAvailable("GL_ARB_vertex_buffer_object")) {
            showErrorMess(
                "Unavailable extension",
                "GL_ARB_vertex_buffer_object extension not available",
                animator
            )
        }
    }

    fun showErrorMess(title: String?, text: String?, animator: AnimatorBase?) {
        Thread {
            JOptionPane.showMessageDialog(
                null,
                text,
                title, JOptionPane.ERROR_MESSAGE
            )
            Thread {
                animator?.stop()
                exitProcess(0)
            }.start()
        }.start()
    }

    /**
     * Helper method that creates a perspective matrix
     * @param fovy The fov in y-direction, in degrees
     *
     * @param aspect The aspect ratio
     * @param zNear The near clipping plane
     * @param zFar The far clipping plane
     * @return A perspective matrix
     */
    fun perspective(
        fovy: Float, aspect: Float, zNear: Float, zFar: Float
    ): FloatArray? {
        val radians = Math.toRadians((fovy / 2).toDouble())
        val deltaZ = zFar - zNear
        val sine = sin(radians)
        if ((deltaZ == 0f) || (sine == 0.0) || (aspect == 0f)) {
            return identity()
        }
        val cotangent = (cos(radians) / sine).toFloat()
        val m = identity()
        m!![0 * 4 + 0] = cotangent / aspect
        m[1 * 4 + 1] = cotangent
        m[2 * 4 + 2] = -(zFar + zNear) / deltaZ
        m[2 * 4 + 3] = -1f
        m[3 * 4 + 2] = -2 * zNear * zFar / deltaZ
        m[3 * 4 + 3] = 0f
        return m
    }

    /**
     * Calls System.exit() in a new Thread. It may not be called
     * synchronously inside one of the JOGL callbacks.
     *
     * am Animator the animator to stop
     */
    fun runExit(animatorEx: AnimatorBase?) {
        Thread(object : Runnable {
            override fun run() {
                if (animatorEx != null) {
                    animatorEx.stop()
                }
                System.exit(0)
            }
        }).start()
    }

    /**
     * Creates an identity matrix
     *
     * @return An identity matrix
     */
    fun identity(): FloatArray? {
        val m: FloatArray? = FloatArray(16)
        Arrays.fill(m, 0f)
        m!![15] = 1.0f
        m[10] = m[15]
        m[5] = m[10]
        m[0] = m[5]
        return m
    }

    /**
     * Multiplies the given matrices and returns the result
     *
     * @param m0 The first matrix
     * @param m1 The second matrix
     * @return The product m0*m1
     */
    fun multiply(m0: FloatArray?, m1: FloatArray?): FloatArray {
        val m = FloatArray(16)
        for (x in 0..3) {
            for (y in 0..3) {
                m!![x * 4 + y] =
                    m0!![x * 4 + 0] * m1!![y + 0] + m0[x * 4 + 1] * m1[y + 4] + m0[x * 4 + 2] * m1[y + 8] + m0[x * 4 + 3] * m1[y + 12]
            }
        }
        return m
    }

    /**
     * Creates a translation matrix
     *
     * @param x The x translation
     * @param y The y translation
     * @param z The z translation
     * @return A translation matrix
     */
    fun translation(x: Float, y: Float, z: Float): FloatArray? {
        val m = identity()
        m!![12] = x
        m[13] = y
        m[14] = z
        return m
    }

    /**
     * Creates a matrix describing a rotation around the x-axis
     *
     * @param angleDeg The rotation angle, in degrees
     * @return The rotation matrix
     */
    fun rotationX(angleDeg: Float): FloatArray? {
        val m = identity()
        val angleRad = Math.toRadians(angleDeg.toDouble())
        val ca = cos(angleRad).toFloat()
        val sa = sin(angleRad).toFloat()
        m!![5] = ca
        m[6] = sa
        m[9] = -sa
        m[10] = ca
        return m
    }

    /**
     * Creates a matrix describing a rotation around the y-axis
     *
     * @param angleDeg The rotation angle, in degrees
     * @return The rotation matrix
     */
    fun rotationY(angleDeg: Float): FloatArray? {
        val m = identity()
        val angleRad = Math.toRadians(angleDeg.toDouble())
        val ca = cos(angleRad).toFloat()
        val sa = sin(angleRad).toFloat()
        m!![0] = ca
        m[2] = -sa
        m[8] = sa
        m[10] = ca
        return m
    }
}
