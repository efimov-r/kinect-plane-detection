package v3d.runners

import cloud.databuffer.CloudForView
import java.nio.FloatBuffer

class V3dLineRunnerImpl : V3dRunner {
    override fun needSet(data: CloudForView): Boolean {
        return data.lineData.isUpdated()
    }

    override fun getBuffSize(data: CloudForView): Int {
        return data.lineData.size()
    }

    override fun setVertexBuffer(buffer: FloatBuffer, data: CloudForView): Int {
        var countVertex = 0
        data.lineData.forEach { triangle, i ->
            buffer.put(8 * i,  triangle.v1.z)
            buffer.put(8 * i + 1,  triangle.v1.x)
            buffer.put(8 * i + 2,  triangle.v1.y)
            buffer.put(8 * i + 3,  triangle.v1.color)

            buffer.put(8 * i + 4,  triangle.v2.z)
            buffer.put(8 * i + 5,  triangle.v2.x)
            buffer.put(8 * i + 6,  triangle.v2.y)
            buffer.put(8 * i + 7,  triangle.v2.color)
            countVertex+=2
        }
        data.lineData.endRead()
        return countVertex
    }
}