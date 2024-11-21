package era.app

import cloud.BufferedChannel
import cloud.databuffer.CloudForView
import kotlinx.coroutines.runBlocking
import v3d.V3dRunner
import java.nio.FloatBuffer

class V3dCloudRunnerImpl(val channel: BufferedChannel<CloudForView>) : V3dRunner {
    override fun setVertexBuffer(buffer: FloatBuffer): Int? {
        var countVertex: Int? = null
        runBlocking {
            channel.getLastDataForRead()?.let {
                countVertex = 0
                it.getPointData { glob, i ->
                    buffer.put(4 * i, glob.z)
                    buffer.put(4 * i + 1, glob.x)
                    buffer.put(4 * i + 2, glob.y)
                    buffer.put(4 * i + 3, 1f)
                    countVertex = countVertex!! + 1
                }
                channel.endRead(it)
            }
        }
        return countVertex
    }
}