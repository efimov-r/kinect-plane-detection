package v3d

import cloud.databuffer.CloudForView
import java.nio.FloatBuffer

interface V3dRunner {
    fun needSet(data: CloudForView): Boolean
    fun setVertexBuffer(buffer: FloatBuffer, data: CloudForView): Int //вернуть сколько отрисовывать вершин сейчас
}