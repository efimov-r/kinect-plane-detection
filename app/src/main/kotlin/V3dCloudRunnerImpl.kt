package era.app

import cloud.realization.ToViewCloud
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import v3d.V3dRunner
import java.nio.FloatBuffer

class V3dCloudRunnerImpl(val channel: Channel<ToViewCloud>) : V3dRunner {
    override fun setVertexBuffer(vertices: FloatBuffer): Int {
        var countVertex = 0
        runBlocking{
            try {
                withTimeout(100){
                    val receive = channel.receive()
                    for (i in 0..<receive.globs.size) {
                        val glob = receive.globs.get(i)
                        val index = 4 * i

                        if(glob != null) {
                            vertices.put(index, glob.z.toFloat() / AppConstants.MAX_MM_ON_V3D)
                            vertices.put(index + 1, glob.x.toFloat() / AppConstants.MAX_MM_ON_V3D)
                            vertices.put(index + 2, glob.y.toFloat() / AppConstants.MAX_MM_ON_V3D)
                            vertices.put(index + 3, 1f)
                            countVertex++
                        }
                    }
                }
            } catch (e: TimeoutCancellationException) {
//                println("Время ожидания данных истекло, завершаем работу.")
            } catch (e: ClosedReceiveChannelException) {
//                println("Канал закрыт, завершаем работу.")
            }
        }
        return countVertex
    }
}