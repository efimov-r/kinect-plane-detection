package vectors

import clouds.Cloud
import jdk.incubator.vector.FloatVector
import kotlin.math.abs
import kotlin.math.sqrt

data class Plane(
    val a: Float, //todo to private
    val b: Float,
    val c: Float,
    val d: Float,
    private val p1i: Int,
    private val p2i: Int,
    private val p3i: Int,
    private var norm: Float? = null,
    private var cloud: Cloud
) {

    fun getName() = cloud.name
    var points: IntArray = IntArray(10)
    var pointsWithSecondOrder: IntArray = IntArray(10)

    private var pointsSet: Set<Int>? = null

    fun getPointsSet() : Set<Int> {
        if(pointsSet == null) {
            pointsSet = points.toSet()
        }
        return pointsSet!!
    }

    fun getNorm() : Float { //todo to private
        if(norm == null) {
            norm = sqrt(a * a + b * b + c * c)
        }
        return norm!!
    }

    fun getNormalize() = Vector3Float(a,b,c).normalize()

    fun getVecCloud(p: Int) = cloud.getVec(p)

    fun recloud(cloud: Cloud, eps: Float, epsSec: Float) {
        this.cloud = cloud
//        points = mutableSetOf()
//        pointsWithSecondOrder = mutableSetOf()

//        val epsNorm = eps * getNorm()
//        val epsNormSec = epsSec * getNorm()

//        this.cloud.forEachAddPointToPlaneSimd(a, b, c, d, this, epsNorm, epsNormSec)
    }

    fun isPointOnPlane(index: Int, eps: Float): Boolean {
        val value = a * cloud.getX(index) + b * cloud.getY(index) + c * cloud.getZ(index) + d
        return abs(value) < eps
    }

    fun isPointOnPlane(x: Float, y: Float, z: Float, eps: Float): Boolean {
        val epsNorm = eps * getNorm()
        val value = a * x + b * y + c * z + d
        return abs(value) < epsNorm
    }

    fun isPointOnPlaneFast(index: Int): Boolean {
        return points.contains(index)
    }

    //можно на gpu
    fun setInliersSIMD(cloud: Cloud, eps: Float) {
        val species = FloatVector.SPECIES_PREFERRED
        val len = species.length()
        val res = ArrayList<Int>(200)

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
                        res.add(i + j)
                    }
                }

                i += len
            } else {
                val dot = abs(a * cloud.getX(i) + b * cloud.getY(i) + c * cloud.getZ(i) + d)
                if (dot < epsNorm) res.add(i)
                i++
            }
        }
        points = res.toIntArray()
    }

    override fun toString(): String {
        return "Plane(a=$a, b=$b, c=$c, d=$d)"
    }

    fun area(): Float {
        require(points.isNotEmpty()) { "Нет точек" }

        // нормаль плоскости
        val n = Vector3Float(a, b, c).normalize()
        val tmp = if (abs(n.x) < 0.9f) Vector3Float(1f, 0f, 0f) else Vector3Float(0f, 1f, 0f)
        val u = tmp.cross(n).normalize()
        val v = n.cross(u).normalize()

        // первая точка плоскости
        val firstIdx = points.first()
        val p0 = cloud.getVec(firstIdx)

        var minU = Float.POSITIVE_INFINITY
        var maxU = Float.NEGATIVE_INFINITY
        var minV = Float.POSITIVE_INFINITY
        var maxV = Float.NEGATIVE_INFINITY

        for (i in points) {
            val p = cloud.getVec(i)
            val rel = p - p0
            val pu = rel.dot(u)
            val pv = rel.dot(v)
            if (pu < minU) minU = pu
            if (pu > maxU) maxU = pu
            if (pv < minV) minV = pv
            if (pv > maxV) maxV = pv
        }

        return (maxU - minU)*(maxV - minV)
    }

    private val V1Y = Vector3Float(0f, 1f, 0f)
    private val V1X = Vector3Float(1f, 0f, 0f)

    fun get2DPoints() : List<P2D> {
        val normal = getNormalize()
        val tmp = if (abs(normal.x) > 0.9f) V1Y else V1X // базис u,v в плоскости
        val u = (tmp cross normal).normalized()
        val v = (normal cross u).normalized()

        return cloud.create2DPoints(u, v, points)
    }

    fun getUandV() : Pair<Vector3Float, Vector3Float> {
        val normal = getNormalize()
        val tmp = if (abs(normal.x) > 0.9f) V1Y else V1X // базис u,v в плоскости
        val u = (tmp cross normal).normalized()
        return u to (normal cross u).normalized()
    }

}
