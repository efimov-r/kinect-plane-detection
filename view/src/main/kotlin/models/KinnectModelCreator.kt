package models

import v3d.runs.V3dRunner
import java.nio.FloatBuffer
import java.util.concurrent.TimeUnit


class KinnectModelCreator(private val multi: Float) : V3dRunner {
    private var index = 0

    private var isFirst = true

    override fun setVertexBuffer(buffer: FloatBuffer): Int { //768
        if (isFirst) {
            index = 0
            createBodyModel(buffer)

            createCircle(buffer, 0.1f, -30f, 10f, COLOR_2)
            createCircle(buffer, 0.1f, 20f, 10f, COLOR_2)
            createCircle(buffer, 0.1f, 45f, 10f, COLOR_2)
            createCircle(buffer, 0.1f, -5f, 1f, COLOR_3)

            isFirst = false
        }

        try {
            TimeUnit.MILLISECONDS.sleep(20)
        } catch (e: InterruptedException) {
            //ignore
        }

        return index / 4
    }

    private fun createCircle(vertices: FloatBuffer, dx: Float, dy: Float, ray: Float, color: Float) { //96
        addSquare(
            vertices,
            Vertex(dx, -1f * ray + dy, 0.42f * ray),
            Vertex(dx, 1f * ray + dy, 0.42f * ray),
            Vertex(dx, 1f * ray + dy, -0.42f * ray),
            Vertex(dx, -1f * ray + dy, -0.42f * ray),
            color
        )
        addSquare(
            vertices,
            Vertex(dx, 0.42f * ray + dy, -1f * ray),
            Vertex(dx, 0.42f * ray + dy, 1f * ray),
            Vertex(dx, -0.42f * ray + dy, 1f * ray),
            Vertex(dx, -0.42f * ray + dy, -1f * ray),
            color
        )
        addSquare(
            vertices,
            Vertex(dx, -0.42f * ray + dy, 1f * ray),
            Vertex(dx, 1f * ray + dy, -0.42f * ray),
            Vertex(dx, 0.42f * ray + dy, -1f * ray),
            Vertex(dx, -1f * ray + dy, 0.42f * ray),
            color
        )
        addSquare(
            vertices,
            Vertex(dx, -1f * ray + dy, -0.42f * ray),
            Vertex(dx, 0.42f * ray + dy, 1f * ray),
            Vertex(dx, 1f * ray + dy, 0.42f * ray),
            Vertex(dx, -0.42f * ray + dy, -1f * ray),
            color
        )
    }

    private fun createBodyModel(vertices: FloatBuffer) { //384
        createBodySideModel(vertices, -75f) //96
        createBodySideModel(vertices, 75f) //96
        addSquare(
            vertices,
            Vertex(0f, -75f, 12.5f),
            Vertex(0f, 75f, 12.5f),
            Vertex(0f, 75f, -12.5f),
            Vertex(0f, -75f, -12.5f),
            COLOR_1
        )
        addSquare(
            vertices,
            Vertex(-5f, -75f, 17.5f),
            Vertex(-5f, 75f, 17.5f),
            Vertex(0f, 75f, 12.5f),
            Vertex(0f, -75f, 12.5f),
            COLOR_1
        )
        addSquare(
            vertices,
            Vertex(0f, -75f, -12.5f),
            Vertex(0f, 75f, -12.5f),
            Vertex(-5f, 75f, -17.5f),
            Vertex(-5f, -75f, -17.5f),
            COLOR_1
        )
        addSquare(
            vertices,
            Vertex(-55f, -75f, 17.5f),
            Vertex(-55f, 75f, 17.5f),
            Vertex(-5f, 75f, 17.5f),
            Vertex(-5f, -75f, 17.5f),
            COLOR_1
        )
        addSquare(
            vertices,
            Vertex(-55f, -75f, -17.5f),
            Vertex(-55f, 75f, -17.5f),
            Vertex(-5f, 75f, -17.5f),
            Vertex(-5f, -75f, -17.5f),
            COLOR_1
        )
        addSquare(
            vertices,
            Vertex(-60f, -75f, 12.5f),
            Vertex(-60f, 75f, 12.5f),
            Vertex(-60f, 75f, -12.5f),
            Vertex(-60f, -75f, -12.5f),
            COLOR_1
        )
        addSquare(
            vertices,
            Vertex(-55f, -75f, 17.5f),
            Vertex(-55f, 75f, 17.5f),
            Vertex(-60f, 75f, 12.5f),
            Vertex(-60f, -75f, 12.5f),
            COLOR_1
        )
        addSquare(
            vertices,
            Vertex(-60f, -75f, -12.5f),
            Vertex(-60f, 75f, -12.5f),
            Vertex(-55f, 75f, -17.5f),
            Vertex(-55f, -75f, -17.5f),
            COLOR_1
        )
    }

    private fun createBodySideModel(vertices: FloatBuffer, dy: Float) { //96
        addSquare(
            vertices,
            Vertex(0f, dy, 12.5f),
            Vertex(-60f, dy, 12.5f),
            Vertex(-60f, dy, -12.5f),
            Vertex(0f, dy, -12.5f),
            COLOR_1
        )
        addSquare(
            vertices,
            Vertex(-5f, dy, 17.5f),
            Vertex(-55f, dy, 17.5f),
            Vertex(-55f, dy, -17.5f),
            Vertex(-5f, dy, -17.5f),
            COLOR_1
        )
        addSquare(
            vertices,
            Vertex(-5f, dy, 17.5f),
            Vertex(-60f, dy, -12.5f),
            Vertex(-55f, dy, -17.5f),
            Vertex(0f, dy, 12.5f),
            COLOR_1
        )
        addSquare(
            vertices,
            Vertex(0f, dy, -12.5f),
            Vertex(-55f, dy, 17.5f),
            Vertex(-60f, dy, 12.5f),
            Vertex(-5f, dy, -17.5f),
            COLOR_1
        )
    }

    private fun addSquare(vertices: FloatBuffer, a: Vertex, b: Vertex, c: Vertex, d: Vertex, color: Float) { //24
        addTriangle(vertices, a, b, c, color)
        addTriangle(vertices, a, c, d, color)
    }

    private fun addTriangle(vertices: FloatBuffer, a: Vertex, b: Vertex, c: Vertex, color: Float) { //12
        vertices.put(index, a.x * multi)
        vertices.put(index + 1, a.y * multi)
        vertices.put(index + 2, a.z * multi)
        vertices.put(index + 3, color)

        vertices.put(index + 4, b.x * multi)
        vertices.put(index + 5, b.y * multi)
        vertices.put(index + 6, b.z * multi)
        vertices.put(index + 7, color)

        vertices.put(index + 8, c.x * multi)
        vertices.put(index + 9, c.y * multi)
        vertices.put(index + 10, c.z * multi)
        vertices.put(index + 11, color)
        index += 12
    }

    companion object {
        private const val COLOR_1 = 0.5f
        private const val COLOR_2 = 1f
        private const val COLOR_3 = 0f

        val sizeBuf: Int
            get() = 192
    }
}
