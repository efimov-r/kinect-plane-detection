package v3d.runners

import bufferchanel.databuffer.BufferedChannelForView
import java.nio.FloatBuffer

class V3dPointRunnerImpl : V3dRunner {
    override fun needSet(data: BufferedChannelForView): Boolean {
        return data.pointData.isUpdated()
    }

    override fun getBuffSize(data: BufferedChannelForView): Int {
        return data.pointData.size()
    }

    override fun setVertexBuffer(buffer: FloatBuffer, data: BufferedChannelForView): Int {
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