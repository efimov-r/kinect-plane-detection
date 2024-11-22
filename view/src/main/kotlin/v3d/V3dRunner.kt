package v3d

import cloud.databuffer.CloudForView
import java.nio.FloatBuffer

fun interface V3dRunner {
    fun setVertexBuffer(buffer: FloatBuffer, data: CloudForView): Int? //вернуть сколько отрисовывать вершин сейчас
}