package viewdata.buffers

import vectors.Plane
import vectors.Vector3Float
import java.lang.Integer.max

class PlanesBuffer(initialCap: Int) : ViewBuffer(initialCap) {

    companion object {
        private const val SIZE_SQUARE_PLANE = 2000f
    }

    fun ofPlains(planes: List<Plane>, color: Float, multFor3D: Float) {
        clear()
        resizeForPoint(planes.size)

        planes.forEach { plane ->
            val center = plane.getP1()
            val n = plane.getNormalize()

            // Проверим, что точка лежит на плоскости (опционально)
//        val eq = plane.a*center.x + plane.b*center.y + plane.c*center.z + plane.d
//        require(kotlin.math.abs(eq) < 1e-3) { "Центр не лежит на плоскости!" }

            // базис в плоскости
            val tmp = if (kotlin.math.abs(n.x) < 0.9f) Vector3Float(1f,0f,0f) else Vector3Float(0f,1f,0f)
            val u = tmp.cross(n).normalize()
            val v = n.cross(u).normalize()

            val h = SIZE_SQUARE_PLANE / 2f

            val p1 = center + u*h + v*h
            val p2 = center - u*h + v*h
            val p3 = center - u*h - v*h
            val p4 = center + u*h - v*h

            writePoint(p1, color, multFor3D)
            writePoint(p2, color, multFor3D)
            writePoint(p3, color, multFor3D)
            writePoint(p1, color, multFor3D)
            writePoint(p3, color, multFor3D)
            writePoint(p4, color, multFor3D)
        }

        updated()
    }

    private inline fun writePoint(p: Vector3Float, color: Float, multFor3D: Float) {
        addDat(p.z * multFor3D)
        addDat(p.x * multFor3D)
        addDat(p.y * multFor3D)
        addDat(color)
    }

    private fun resizeForPoint(count: Int) {
        if ((sizeI + (4*6*count)) > data.size) {
            resize(max(sizeI * 2, sizeI+(4*6*count)))
        }
    }
}