package v3d.runners

import bufferchanel.databuffer.BufferedChannelForView
import java.nio.FloatBuffer

class V3dTriangleRunnerImpl : V3dRunner {
    override fun needSet(data: BufferedChannelForView): Boolean {
        return data.triangleData.isUpdated()
    }

    override fun getBuffSize(data: BufferedChannelForView): Int {
        return data.triangleData.arraySize
    }

    override fun setVertexBuffer(buffer: FloatBuffer, data: BufferedChannelForView): Int {
        data.triangleData.dataToBuffer(buffer)
        return data.triangleData.size/4
    }
}