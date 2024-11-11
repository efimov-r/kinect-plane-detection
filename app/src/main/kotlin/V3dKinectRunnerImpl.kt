package era.app

import era.kinect.cloud.Cloud
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import v3d.V3dRunner
import java.nio.FloatBuffer

class V3dKinectRunnerImpl(val channel: Channel<Cloud>) : V3dRunner {
    override fun setVertexBuffer(vertices: FloatBuffer): Int {
        var countVertex = 0
        runBlocking{
            val receive = channel.receive()
            for (i in 0..<receive.globs.size) {
                val glob = receive.globs.get(i)
                val index = 4 * i

                if(glob != null) {
                    vertices.put(index, glob.x.toFloat() / 7000)
                    vertices.put(index + 1, -glob.y.toFloat() / 7000)
                    vertices.put(index + 2, glob.z.toFloat() / 7000)
                    vertices.put(index + 3, 1f)
                    countVertex++
                }
            }
        }
        return countVertex
    }
}