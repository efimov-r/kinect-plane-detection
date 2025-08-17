package vectors

import clouds.Cloud
import jdk.incubator.vector.FloatVector
import kotlin.math.abs
import kotlin.math.sqrt

data class Plane(
    val a: Float,
    val b: Float,
    val c: Float,
    val d: Float,
    val p1i: Int,
    val p2i: Int,
    val p3i: Int,
    var norm: Float? = null
) {

    val points: HashSet<Int> = mutableSetOf<Int>() as HashSet<Int>

    fun getNorm() : Float {
        if(norm == null) {
            norm = sqrt(a * a + b * b + c * c)
        }
        return norm!!
    }

    override fun toString(): String {
        return "Плоскость A=${a}, B=${b}, C=${c}, D=${d}"
    }

    //можно на gpu
    fun setInliersSIMD(cloud: Cloud, eps: Float) {
        val species = FloatVector.SPECIES_PREFERRED
        val len = species.length()

        val aVec = FloatVector.broadcast(species, a)
        val bVec = FloatVector.broadcast(species, b)
        val cVec = FloatVector.broadcast(species, c)
        val dVec = FloatVector.broadcast(species, d)

        val epsNorm = eps * getNorm()

        var i = 0
        while (i < cloud.size) {
            if(i <= cloud.size - len) {
                // dot = ax + by + cz + d
                val dot = aVec.mul(cloud.getXFV(species, i))
                    .add(bVec.mul(cloud.getYFV(species, i)))
                    .add(cVec.mul(cloud.getZFV(species, i)))
                    .add(dVec).abs()

                // сравнение с epsNorm
                val mask = dot.lt(FloatVector.broadcast(species, epsNorm))

                // собрать индексы
                for (j in 0 until len) {
                    if (mask.laneIsSet(j)) {
                        points.add(i + j)
                    }
                }

                i += len
            } else {
                val dot = abs(a * cloud.getX(i) + b * cloud.getY(i) + c * cloud.getZ(i) + d)
                if (dot < epsNorm) points.add(i)
                i++
            }
        }
    }


}
