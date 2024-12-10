package v3d.runners

import bufferchanel.databuffer.BufferedChannelForView
import java.nio.FloatBuffer

class V3dTriangleRunnerImpl : V3dRunner {
    override fun needSet(data: BufferedChannelForView): Boolean {
        return data.triangleData.isUpdated()
    }

    override fun getBuffSize(data: BufferedChannelForView): Int {
        return data.triangleData.size()
    }

    override fun setVertexBuffer(buffer: FloatBuffer, data: BufferedChannelForView): Int {
        var countVertex = 0
        data.triangleData.forEach { triangle, i ->
            buffer.put(12 * i,  triangle.v1.z)
            buffer.put(12 * i + 1,  triangle.v1.x)
            buffer.put(12 * i + 2,  triangle.v1.y)
            buffer.put(12 * i + 3,  triangle.v1.color)

            buffer.put(12 * i + 4,  triangle.v2.z)
            buffer.put(12 * i + 5,  triangle.v2.x)
            buffer.put(12 * i + 6,  triangle.v2.y)
            buffer.put(12 * i + 7,  triangle.v2.color)

            buffer.put(12 * i + 8,  triangle.v3.z)
            buffer.put(12 * i + 9,  triangle.v3.x)
            buffer.put(12 * i + 10,  triangle.v3.y)
            buffer.put(12 * i + 11,  triangle.v3.color)

            countVertex+=3
        }
        data.triangleData.endRead()
        return countVertex
    }
}