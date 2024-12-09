package v3d.vbos.other

import com.jogamp.opengl.GL
import com.jogamp.opengl.GL3
import org.jocl.Sizeof
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer

class VBOPoints: VBO {
    private var vertexArrayObject = 0
    private var vertexBufferObject = 0

    override fun init(gl: GL3, size: Int) {
        if (vertexBufferObject != 0) {
            gl.glDeleteBuffers(1, intArrayOf(vertexBufferObject), 0)
            vertexBufferObject = 0
        }
        if (vertexArrayObject != 0) {
            gl.glDeleteVertexArrays(1, intArrayOf(vertexArrayObject), 0)
            vertexArrayObject = 0
        }
        val tempArray = IntArray(1)

        // Create the vertex array object
        gl.glGenVertexArrays(1, IntBuffer.wrap(tempArray))
        vertexArrayObject = tempArray[0]
        gl.glBindVertexArray(vertexArrayObject)

        // Create the vertex buffer object
        gl.glGenBuffers(1, IntBuffer.wrap(tempArray))
        vertexBufferObject = tempArray[0]

        // Initialize the vertex buffer object
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vertexBufferObject)
        gl.glBufferData(GL.GL_ARRAY_BUFFER, (size.toLong()) * 4 * Sizeof.cl_float, null, GL.GL_DYNAMIC_DRAW)
    }

    override fun render(gl: GL3, countOfVboPoint: Int) {
        gl.glBindVertexArray(vertexArrayObject)
        gl.glDrawArrays(GL.GL_POINTS, 0, countOfVboPoint)
    }

    override fun mapBuffer(gl: GL3): FloatBuffer {
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vertexBufferObject)
        val byteBuffer = gl.glMapBuffer(GL.GL_ARRAY_BUFFER, GL.GL_WRITE_ONLY)
        return byteBuffer.order(ByteOrder.nativeOrder()).asFloatBuffer()
    }

    override fun unmapBuffer(gl: GL3) {
        gl.glUnmapBuffer(GL.GL_ARRAY_BUFFER)
    }
}
