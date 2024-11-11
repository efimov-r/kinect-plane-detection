package v3d

import java.nio.FloatBuffer

fun interface V3dRunner {
    fun setVertexBuffer(buffer: FloatBuffer): Int //вернуть сколько отрисовывать вершин сейчас
}