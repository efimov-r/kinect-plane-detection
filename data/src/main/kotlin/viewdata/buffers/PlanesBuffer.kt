package viewdata.buffers

import vectors.Plane
import vectors.PlanePolygon
import vectors.Vector3Float
import java.lang.Integer.max

class PlanesBuffer(initialCap: Int) : ViewBuffer(initialCap) {

    companion object {
        private const val SIZE_SQUARE_PLANE = 2000f
    }

    fun ofPlainPolygon(planes: List<PlanePolygon>, color: Float, multFor3D: Float) {
        clear()
        planes.forEach { plane->
            for(a in 0 until plane.vertices.size){
                for(b in 0 until plane.vertices.size){
                    for(c in 0 until plane.vertices.size){
                        if(a!=b && b!=c && a!=c) {
                            resizeForTriangle(1)
                            writePoint(plane.vertices[a], color, multFor3D)
                            writePoint(plane.vertices[b], color, multFor3D)
                            writePoint(plane.vertices[c], color, multFor3D)
                        }
                    }
                }
            }
        }
        updated()
    }

    fun ofPlainPolygon2(planes: List<PlanePolygon>, color: Float, multFor3D: Float) {
        clear()
        for(f in 0 until planes.size) {
            val poly = planes[f]
            val cx = poly.vertices.map { it.x }.average().toFloat()
            val cy = poly.vertices.map { it.y }.average().toFloat()
            val cz = poly.vertices.map { it.z }.average().toFloat()
            val center = Vector3Float(cx, cy, cz)

            if (poly.vertices.size < 3) continue

            for (i in poly.vertices.indices) {
                val next = (i + 1) % poly.vertices.size
                resizeForTriangle(1)
                writePoint(center, color, multFor3D)
                writePoint(poly.vertices[i], color, multFor3D)
                writePoint(poly.vertices[next], color, multFor3D)
            }
        }
        updated()
    }

    fun ofPlainPolygon3(planes: List<Plane>, color: Float, multFor3D: Float) {
        clear()
        for (poly in planes) {
            if (poly.points.size < 3) continue

            // достаём координаты всех точек полигона
            val coords = poly.points.map { poly.getPofCloud(it) }

            // вычисляем геометрический центр (усреднение по координатам)
            val cx = coords.map { it.x }.average().toFloat()
            val cy = coords.map { it.y }.average().toFloat()
            val cz = coords.map { it.z }.average().toFloat()
            val center = Vector3Float(cx, cy, cz)

            // триангуляция фаном вокруг центра
            for (i in coords.indices) {
                val next = (i + 1) % coords.size
                resizeForTriangle(1)
                writePoint(center, color, multFor3D)
                writePoint(coords[i], color, multFor3D)
                writePoint(coords[next], color, multFor3D)
            }
        }
        updated()
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

    private fun resizeForTriangle(count: Int) {
        if ((sizeI + (4*3*count)) > data.size) {
            resize(max(sizeI * 2, sizeI+(4*3*count)))
        }
    }

    private fun resizeForPoint(count: Int) {
        if ((sizeI + (4*6*count)) > data.size) {
            resize(max(sizeI * 2, sizeI+(4*6*count)))
        }
    }
}