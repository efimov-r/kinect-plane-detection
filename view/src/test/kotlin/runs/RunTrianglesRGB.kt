package runs

import cloud.databuffer.CloudForView
import v3d.V3dRunner
import java.nio.FloatBuffer
import java.util.concurrent.TimeUnit

class RunTrianglesRGB : V3dRunner {
    override fun needSet(data: CloudForView): Boolean {
        return true
    }

    override fun setVertexBuffer(buffer: FloatBuffer, data: CloudForView): Int {
        buffer.put(0, -1f)
        buffer.put(1, 1f)
        buffer.put(2, 0f)
        buffer.put(3, 1f)
        buffer.put(4, -1f)
        buffer.put(5, -1f)
        buffer.put(6, 0f)
        buffer.put(7, 1f)
        buffer.put(8, 1f)
        buffer.put(9, -1f)
        buffer.put(10, 0f)
        buffer.put(11, 1f)

        buffer.put(12, -1f)
        buffer.put(13, 1f)
        buffer.put(14, 0f)
        buffer.put(15, 1f)
        buffer.put(16, 1f)
        buffer.put(17, -1f)
        buffer.put(18, 0f)
        buffer.put(19, 1f)
        buffer.put(20, 1f)
        buffer.put(21, 1f)
        buffer.put(22, 0f)
        buffer.put(23, 1f)

        TimeUnit.MILLISECONDS.sleep(20)

        return 6
    }
}
