package v3d.runners

import bufferchanel.databuffer.BufferedChannelForView
import java.nio.FloatBuffer

class V3dPlanesRunnerImpl : V3dRunner {
    override fun needSet(data: BufferedChannelForView): Boolean {
        return data.planesData.isUpdated()
    }

    override fun getBuffSize(data: BufferedChannelForView): Int {
        return data.planesData.arraySize
    }

    override fun setVertexBuffer(buffer: FloatBuffer, data: BufferedChannelForView): Int {
        data.planesData.dataToBuffer(buffer)
        return data.planesData.size/4
    }
}