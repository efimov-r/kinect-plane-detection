package v3d.runners

import bufferchanel.databuffer.BufferedChannelForView
import java.nio.FloatBuffer

class V3dPointRunnerImpl : V3dRunner {
    override fun needSet(data: BufferedChannelForView): Boolean {
        return data.pointData.isUpdated()
    }

    override fun getBuffSize(data: BufferedChannelForView): Int {
        return data.pointData.arraySize
    }

    override fun setVertexBuffer(buffer: FloatBuffer, data: BufferedChannelForView): Int {
        data.pointData.dataToBuffer(buffer)
        return data.pointData.size/4
    }
}