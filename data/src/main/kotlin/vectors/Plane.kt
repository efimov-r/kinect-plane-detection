package vectors

import clouds.Cloud
import jdk.incubator.vector.FloatVector
import kotlin.math.abs
import kotlin.math.sqrt

data class Plane(
    private val a: Float,
    private val b: Float,
    private val c: Float,
    private val d: Float,
    private val p1i: Int,
    private val p2i: Int,
    private val p3i: Int,
    private var norm: Float? = null,
    private var cloud: Cloud
) {

    var points: MutableCollection<Int> = mutableSetOf<Int>() as HashSet<Int>
    var pointsWithSecondOrder: MutableCollection<Int> = mutableSetOf<Int>() as HashSet<Int>

    private fun getNorm() : Float {
        if(norm == null) {
            norm = sqrt(a * a + b * b + c * c)
        }
        return norm!!
    }

    fun getNormalize() = Vector3Float(a,b,c).normalize()

    fun getP1() = cloud.getVec(p1i)
    fun getP2() = cloud.getVec(p2i)
    fun getP3() = cloud.getVec(p3i)
    fun getPofCloud(p: Int) = cloud.getVec(p)

    fun recloud(cloud: Cloud, eps: Float) {
        this.cloud = cloud
        points = mutableSetOf()
        pointsWithSecondOrder = mutableSetOf()

        val epsNorm = eps * getNorm()
        val epsNorm3 = epsNorm * 3

        this.cloud.forEachIndexed { x, y, z, i ->
            val len = abs(a * x + b * y + c * z + d)
            if(len < epsNorm) {
                points.add(i)
                pointsWithSecondOrder.add(i)
            } else if(len < epsNorm3) {
                pointsWithSecondOrder.add(i)
            }
        }
    }

    fun filterPointsByDistanceCircle(
        maxGap: Float, pointCenter: Int
    ) {
        if (points.isEmpty()) return

        var changed: Boolean
        do {
            changed = false

            for (coord in 0..2) {
                val sorted = points.sortedBy { idx ->
                    when (coord) {
                        0 -> cloud.getX(idx)
                        1 -> cloud.getY(idx)
                        else -> cloud.getZ(idx)
                    }
                }

                val centerVal = when (coord) {
                    0 -> cloud.getX(pointCenter)
                    1 -> cloud.getY(pointCenter)
                    else -> cloud.getZ(pointCenter)
                }

                val centerPos = sorted.indexOf(pointCenter)
                if (centerPos == -1) continue // центр не в points

                var cutPosLeft = -1
                var cutPosRight = -1

                // идем влево
                var prev = centerVal
                for (i in centerPos - 1 downTo 0) {
                    val cur = when (coord) {
                        0 -> cloud.getX(sorted[i])
                        1 -> cloud.getY(sorted[i])
                        else -> cloud.getZ(sorted[i])
                    }
                    if (prev - cur > maxGap) {
                        cutPosLeft = i
                        break
                    }
                    prev = cur
                }

                // идем вправо
                prev = centerVal
                for (i in centerPos + 1 until sorted.size) {
                    val cur = when (coord) {
                        0 -> cloud.getX(sorted[i])
                        1 -> cloud.getY(sorted[i])
                        else -> cloud.getZ(sorted[i])
                    }
                    if (cur - prev > maxGap) {
                        cutPosRight = i
                        break
                    }
                    prev = cur
                }

                if (cutPosLeft != -1) {
                    val toRemove = sorted.subList(0, cutPosLeft + 1)
                    points.removeAll(toRemove.toSet())
                    changed = true
                }

                if (cutPosRight != -1) {
                    val toRemove = sorted.subList(cutPosRight, sorted.size)
                    points.removeAll(toRemove.toSet())
                    changed = true
                }
            }
        } while (changed)
    }

    fun filterPointsByDistance(
        maxGap: Float, pointCenter: Int
    ) {
        // 1. Создаем список пар (индекс, расстояние до reference)
        val distances = points.map { idx ->
            val dx = cloud.getX(idx) - cloud.getX(pointCenter)
            val dy = cloud.getY(idx) - cloud.getY(pointCenter)
            val dz = cloud.getZ(idx) - cloud.getZ(pointCenter)
            idx to sqrt(dx*dx + dy*dy + dz*dz)
        }

        // 2. Сортируем по расстоянию
        val sorted = distances.sortedBy { it.second }

        // 3. Находим первый разрыв, превышающий maxGap
        var cutoffIndex = sorted.size
        for (i in 0 until sorted.size - 1) {
            val gap = sorted[i + 1].second - sorted[i].second
            if (gap > maxGap) {
                cutoffIndex = i + 1
                break
            }
        }

        // 4. Удаляем из points все после разрыва
        val toRemove = sorted.subList(cutoffIndex, sorted.size).map { it.first }
        points.removeAll(toRemove)
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

}
