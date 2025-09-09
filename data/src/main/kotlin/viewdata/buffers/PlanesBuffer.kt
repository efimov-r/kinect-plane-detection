package viewdata.buffers

import vectors.PlaneCloud
import vectors.PlanePoints
import vectors.Vector3Float
import java.lang.Integer.max

class PlanesBuffer(initialCap: Int) : ViewBuffer(initialCap) {

    companion object {
        private const val SIZE_SQUARE_PLANE = 2000f
    }

    fun addPlainPolygon(poly: PlaneCloud, color: Float, multFor3D: Float) {
        if (poly.points.size < 3) return

        // достаём координаты всех точек полигона
        val coords = poly.points.map { poly.getVecCloud(it) }

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

//    fun ofPlainsPolygon(planes: List<PlaneCloud>, color: Float, multFor3D: Float) {
//        clear()
//        for (poly in planes) {
//            if (poly.points.size < 3) continue
//
//            // достаём координаты всех точек полигона
//            val coords = poly.points.map { poly.getVecCloud(it) }
//
//            // вычисляем геометрический центр (усреднение по координатам)
//            val cx = coords.map { it.x }.average().toFloat()
//            val cy = coords.map { it.y }.average().toFloat()
//            val cz = coords.map { it.z }.average().toFloat()
//            val center = Vector3Float(cx, cy, cz)
//
//            // триангуляция фаном вокруг центра
//            for (i in coords.indices) {
//                val next = (i + 1) % coords.size
//                resizeForTriangle(1)
//                writePoint(center, color, multFor3D)
//                writePoint(coords[i], color, multFor3D)
//                writePoint(coords[next], color, multFor3D)
//            }
//        }
//        updated()
//    }

    fun ofPlainsPolygon(planes: List<PlanePoints>, color: Float, multFor3D: Float) {
        clear()
        for (plane in planes) {
            if (plane.size < 3) continue

            // достаём координаты всех точек полигона
            val coords = plane.getPointsAsVector()

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