package v3d.runners

import cloud.databuffer.CloudForView
import java.nio.FloatBuffer

class V3dPointRunnerImpl : V3dRunner {
    override fun needSet(data: CloudForView): Boolean {
        return data.pointData.isUpdated()
    }

    override fun getBuffSize(data: CloudForView): Int {
        return data.pointData.size()
    }

    override fun setVertexBuffer(buffer: FloatBuffer, data: CloudForView): Int {
        var countVertex = 0
        data.pointData.forEach { glob, i ->
            buffer.put(4 * i, glob.z)
            buffer.put(4 * i + 1, glob.x)
            buffer.put(4 * i + 2, glob.y)
            buffer.put(4 * i + 3, glob.color)
            countVertex++
        }
        data.pointData.endRead()
        return countVertex
    }
}