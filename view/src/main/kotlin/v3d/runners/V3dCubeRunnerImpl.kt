package v3d.runners

import bufferchanel.databuffer.BufferedChannelForView
import java.nio.FloatBuffer

class V3dCubeRunnerImpl : V3dRunner {
    override fun needSet(data: BufferedChannelForView): Boolean {
        return data.cubeData.isUpdated()
    }

    override fun getBuffSize(data: BufferedChannelForView): Int {
        return data.cubeData.size()
    }

    override fun setVertexBuffer(buffer: FloatBuffer, data: BufferedChannelForView): Int {
        var countVertex = 0
        data.cubeData.forEach { glob, i ->
            buffer.put(4 * i, glob.z)
            buffer.put(4 * i + 1, glob.x)
            buffer.put(4 * i + 2, glob.y)
            buffer.put(4 * i + 3, glob.color)
            countVertex++
        }
        data.cubeData.endRead()
        return countVertex
    }
}