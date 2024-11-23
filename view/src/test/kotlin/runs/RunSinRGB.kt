package runs

import cloud.databuffer.CloudForView
import v3d.V3dRunner
import java.nio.FloatBuffer
import java.util.concurrent.TimeUnit
import kotlin.math.cos
import kotlin.math.sin

class RunSinRGB(private var animationState: Float, private val mesh_width: Int, private val mesh_height: Int) :
    V3dRunner {

    override fun needSet(data: CloudForView): Boolean {
        return true
    }

    override fun setVertexBuffer(vertices: FloatBuffer, data: CloudForView): Int {
        for (x in 0..<this.mesh_width) {
            for (y in 0..<this.mesh_height) {
                var u = x / this.mesh_width.toFloat()
                var v = y / this.mesh_height.toFloat()

                u = u * 2.0f - 1.0f
                v = v * 2.0f - 1.0f

                val freq = 4.0f
                val w =
                    sin((u * freq + animationState).toDouble()).toFloat() * cos((v * freq + animationState).toDouble()).toFloat() * 0.5f

                val index = 4 * (y * this.mesh_width + x)

                vertices.put(index, u)
                vertices.put(index + 1, w)
                vertices.put(index + 2, v)
                vertices.put(index + 3, w)
            }
        }
        if (this.mesh_width < 100) {
            TimeUnit.MILLISECONDS.sleep(20)
        }
        animationState += 0.01f
        return this.mesh_width * this.mesh_height
    }
}
