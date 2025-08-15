package v3d.runners

import bufferchanel.databuffer.BufferedChannelForView
import java.nio.FloatBuffer

class V3dLineRunnerImpl : V3dRunner {
    override fun needSet(data: BufferedChannelForView): Boolean {
        return data.lineData.isUpdated()
    }

    override fun getBuffSize(data: BufferedChannelForView): Int {
        return data.lineData.arraySize
    }

    override fun setVertexBuffer(buffer: FloatBuffer, data: BufferedChannelForView): Int {
        data.lineData.dataToBuffer(buffer)
        return data.lineData.size/4
    }
}