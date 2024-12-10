package v3d.runners

import bufferchanel.databuffer.BufferedChannelForView
import java.nio.FloatBuffer

interface V3dRunner {
    fun needSet(data: BufferedChannelForView): Boolean
    fun getBuffSize(data: BufferedChannelForView): Int
    fun setVertexBuffer(buffer: FloatBuffer, data: BufferedChannelForView): Int //вернуть сколько отрисовывать вершин сейчас
}