package vectors

import kotlin.math.PI
import kotlin.math.acos

class Vector3ChildedShort(x: Short,
                        y: Short,
                        z: Short,
                        val neighbours: ArrayList<Vector3Short> = ArrayList(),
                        val neighboursPerpendicularVectors: ArrayList<Vector3Short> = ArrayList()) : Vector3Short(x, y, z) {

    fun createPerpendicularVectors() {
//        println("neighbours.size: ${neighbours.size}")
        for(i in 0..<neighbours.size) {
            for(j in (i+1)..<neighbours.size) {

                val n1 = neighbours.get(i)
                val n2 = neighbours.get(j)

                val n10 = n1 minus Vector3Short(x, y, z)
                val n20 = n2 minus Vector3Short(x, y, z)

                var p = n20 cross n10
                if(p.y > 0) {
                    p = Vector3Short((p.x * -1).toShort(), (p.y*-1).toShort(), (p.z*-1).toShort())
                }

                neighboursPerpendicularVectors.add(p)
            }
        }
    }

    fun filterPerpendicularVectors() {
        neighboursPerpendicularVectors.removeIf { v1->
            val v2 = Vector3Short(0 ,-1, 0)
            // Вычисляем скалярное произведение
            val dotProduct = v1.dot(v2)

            // Вычисляем произведение длин векторов
            val lengthProduct = v1.length() * v2.length()

            // Вычисляем косинус угла
            val cosAngle = dotProduct / lengthProduct

            // Проверяем, что угол меньше 45 градусов
            // 45 градусов в радианах = PI / 4
            return@removeIf acos(cosAngle) > PI / 4
        }
    }

    fun groupPerpendicularVectors(cosAngleMin: Double) {
        val res = mutableListOf<MutableList<Vector3Short>>()
        for(i in 0..<neighboursPerpendicularVectors.size) {
            for(j in i+1..<neighboursPerpendicularVectors.size) {
                if(calckCosAngle(neighboursPerpendicularVectors[i], neighboursPerpendicularVectors[j]) > cosAngleMin) {
                    val r1 = mutableListOf(neighboursPerpendicularVectors[i], neighboursPerpendicularVectors[j])
                    for(k in j+1..<neighboursPerpendicularVectors.size) {
                        if(calckCosAngle(neighboursPerpendicularVectors[i], neighboursPerpendicularVectors[k])> cosAngleMin) {
                            r1.add(neighboursPerpendicularVectors[k])
                        }
                    }
                    res.add(r1)
                }
            }
        }

        var maxNum: Int? = null
        var max = 0
        for(i in 0..<res.size) {
            val r1 = res[i]
            if(r1.size > max) {
                max = r1.size
                maxNum = i
            }
        }

        if(maxNum != null && max > neighboursPerpendicularVectors.size/2) {
            neighboursPerpendicularVectors.removeIf { true }
            val rr = res[maxNum]
            var rx = 0
            var ry = 0
            var rz = 0
            rr.forEach { v->
                rx += v.x
                ry += v.y
                rz += v.z
            }
            neighboursPerpendicularVectors.add(Vector3Short((rx/rr.size).toShort(), (ry/rr.size).toShort(), (rz/rr.size).toShort()))
        } else {
            neighboursPerpendicularVectors.removeIf { true }
        }
    }

    private fun calckCosAngle(v1: Vector3Short, v2: Vector3Short): Double {
        val dotProduct = v1.dot(v2)
        val lengthProduct = v1.length() * v2.length()
        return dotProduct / lengthProduct
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Vector3ChildedShort

        return (x == other.x) && (y == other.y) && (z == other.z)
    }
}