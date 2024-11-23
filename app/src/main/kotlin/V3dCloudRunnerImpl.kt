package era.app

import cloud.databuffer.CloudForView
import v3d.V3dRunner
import java.nio.FloatBuffer

class V3dCloudRunnerImpl : V3dRunner {
    override fun needSet(data: CloudForView): Boolean {
        return data.pointData.isUpdated()
    }

    override fun setVertexBuffer(buffer: FloatBuffer, data: CloudForView): Int {
        var countVertex = 0
        data.pointData.forEach { glob, i ->
            buffer.put(4 * i, glob.z)
            buffer.put(4 * i + 1, glob.x)
            buffer.put(4 * i + 2, glob.y)
            buffer.put(4 * i + 3, 1f)
            countVertex = countVertex!! + 1
        }
        data.pointData.endRead()
        return countVertex
    }
}