package era.app

import cloud.realization.BufferedChannel
import cloud.realization.CloudOfSensor
import kotlinx.coroutines.runBlocking
import v3d.V3dRunner
import java.nio.FloatBuffer

class V3dCloudRunnerImpl(val channel: BufferedChannel<CloudOfSensor>) : V3dRunner {
    override fun setVertexBuffer(buffer: FloatBuffer): Int? {
        var countVertex: Int? = null
        runBlocking {
            channel.getLastDataForRead()?.let {
                countVertex = 0
                it.editData { glob, i ->
                    buffer.put(4 * i, glob.z.toFloat() / AppConstants.MAX_MM_ON_V3D)
                    buffer.put(4 * i + 1, glob.x.toFloat() / AppConstants.MAX_MM_ON_V3D)
                    buffer.put(4 * i + 2, glob.y.toFloat() / AppConstants.MAX_MM_ON_V3D)
                    buffer.put(4 * i + 3, 1f)
                    countVertex = countVertex!! + 1
                }
                channel.endRead(it)
            }
        }
        return countVertex
    }
}