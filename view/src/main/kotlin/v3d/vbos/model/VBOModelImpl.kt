package v3d.vbos.model

import com.jogamp.opengl.GL
import com.jogamp.opengl.GL3
import org.jocl.Sizeof
import v3d.models.V3dModel
import java.nio.ByteOrder
import java.nio.IntBuffer

class VBOModelImpl(private val model: V3dModel) : VBOModel {
    private var vertexArrayObject = 0
    private var vertexBufferObject = 0
    private var countOfVboPoint: Int = 0

    override fun init(gl: GL3) {
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
        gl.glBufferData(GL.GL_ARRAY_BUFFER, (model.getSizeBuf().toLong()) * Sizeof.cl_float, null, GL.GL_STATIC_DRAW)

        val byteBuffer = gl.glMapBuffer(GL.GL_ARRAY_BUFFER, GL.GL_WRITE_ONLY)
        val asFloatBuffer = byteBuffer.order(ByteOrder.nativeOrder()).asFloatBuffer()
        this.countOfVboPoint = model.buildModel(asFloatBuffer)
        gl.glUnmapBuffer(GL.GL_ARRAY_BUFFER)
    }

    override fun render(gl: GL3, countOfVboPoint: Int) {
        gl.glBindVertexArray(vertexArrayObject)
        gl.glDrawArrays(GL.GL_TRIANGLES, 0, this.countOfVboPoint)
    }
}
