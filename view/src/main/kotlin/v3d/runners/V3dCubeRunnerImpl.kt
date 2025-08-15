package v3d.runners

import bufferchanel.databuffer.BufferedChannelForView
import java.nio.FloatBuffer

class V3dCubeRunnerImpl : V3dRunner {
    override fun needSet(data: BufferedChannelForView): Boolean {
        return data.cubeData.isUpdated()
    }

    override fun getBuffSize(data: BufferedChannelForView): Int {
        return data.cubeData.arraySize
    }

    override fun setVertexBuffer(buffer: FloatBuffer, data: BufferedChannelForView): Int {
        data.cubeData.dataToBuffer(buffer)
        return data.cubeData.size/4
    }
}