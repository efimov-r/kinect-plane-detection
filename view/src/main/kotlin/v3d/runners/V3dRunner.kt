package v3d.runners

import cloud.databuffer.CloudForView
import java.nio.FloatBuffer

interface V3dRunner {
    fun needSet(data: CloudForView): Boolean
    fun getBuffSize(data: CloudForView): Int
    fun setVertexBuffer(buffer: FloatBuffer, data: CloudForView): Int //вернуть сколько отрисовывать вершин сейчас
}