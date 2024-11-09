package v3d.runs

import java.nio.FloatBuffer

interface V3dRunner {
    fun setVertexBuffer(buffer: FloatBuffer): Int //вернуть сколько отрисовывать вершин сейчас
}
