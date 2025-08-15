package era.app.jobs

import bufferchanel.databuffer.BufferedChannelForView
import clouds.Cloud
import era.app.AppConstants
import era.app.utils.neighbour.NeighbourHelper
import era.app.utils.subclouders.Chanker.chancked5
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import vectors.Vector3Childed
import vectors.Vector3Float
import kotlin.collections.forEach
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin

object JobChank {
    fun CoroutineScope.doJobChank(
        fastCloud: Cloud, writeCloud: BufferedChannelForView
    ): Deferred<Vector3Float?> = async(Dispatchers.IO) {
        val v3List = chancked5(fastCloud, (AppConstants.STEP_CREATE_VERTICAL_VECTORS / 3).toShort())

        NeighbourHelper.findAndFilter(v3List)

        v3List.map { v->
            launch(Dispatchers.Default) {
                v.createPerpendicularVectors()
                v.filterPerpendicularVectors()
                v.groupPerpendicularVectors(AppConstants.COS_GROUP_PERPENDIC)
            }
        }.joinAll()

        v3List.forEach {
            writeCloud.lineData.addPerpendicular(it, AppConstants.AD_MAX_MM_ON_V3D)
        }

        val vectorP = groupPerpendicularVectors(v3List)
        return@async vectorP
    }

    fun rotateVector(v: Vector3Float, matrix: Array<FloatArray>): Vector3Float {
        val x = matrix[0][0] * v.x + matrix[0][1] * v.y + matrix[0][2] * v.z
        val y = matrix[1][0] * v.x + matrix[1][1] * v.y + matrix[1][2] * v.z
        val z = matrix[2][0] * v.x + matrix[2][1] * v.y + matrix[2][2] * v.z
        return Vector3Float(x.toFloat(), y.toFloat(), z.toFloat())
    }

    fun rotationMatrixToY(v: Vector3Float): Array<FloatArray> {
        val from = v.normalize()
        val to = Vector3Float(0f, -1f, 0f)

        val axis = from.cross(to).normalize()
        val angle = acos(from.dot(to).coerceIn(-1.0, 1.0))  // важно не выйти за пределы [-1;1]

        if (angle == 0.0 || axis.length() == 0.0) {
            return identityMatrix()
        }

        val x = axis.x
        val y = axis.y
        val z = axis.z
        val c = cos(angle)
        val s = sin(angle)
        val t = 1 - c

        return arrayOf(
            floatArrayOf((t*x*x + c).toFloat(),     (t*x*y - z*s).toFloat(),   (t*x*z + y*s).toFloat()),
            floatArrayOf((t*y*x + z*s).toFloat(),   (t*y*y + c).toFloat(),     (t*y*z - x*s).toFloat()),
            floatArrayOf((t*z*x - y*s).toFloat(),   (t*z*y + x*s).toFloat(),   (t*z*z + c).toFloat())
        )
    }

    fun identityMatrix(): Array<FloatArray> = arrayOf(
        floatArrayOf(1f, 0f, 0f),
        floatArrayOf(0f, 1f, 0f),
        floatArrayOf(0f, 0f, 1f)
    )

    private fun groupPerpendicularVectors(v3List: MutableList<Vector3Childed>): Vector3Float? {
        val vectors = v3List.mapNotNull { v ->
            if (v.vecs.size == 1) v.vecs[0] else null
        }

        if (vectors.size < 2) return null

        val cosThreshold = AppConstants.COS_GROUP_PERPENDIC
        val res = mutableListOf<List<Vector3Float>>()

        for (i in vectors.indices) {
            val vi = vectors[i]
            for (j in i + 1 until vectors.size) {
                val vj = vectors[j]
                if (calckCosAngle(vi, vj) > cosThreshold) {
                    val group = mutableListOf(vi, vj)
                    for (k in j + 1 until vectors.size) {
                        val vk = vectors[k]
                        if (calckCosAngle(vi, vk) > cosThreshold) {
                            group.add(vk)
                        }
                    }
                    res.add(group)
                }
            }
        }

        // Найдём самую большую группу
        val maxGroup = res.maxByOrNull { it.size }

        // Если она релевантна — вернём её средний вектор
        return if (maxGroup != null && maxGroup.size > vectors.size / 2) {
            var rx = 0f
            var ry = 0f
            var rz = 0f
            maxGroup.forEach { v->
                rx += v.x
                ry += v.y
                rz += v.z
            }
            Vector3Float(rx / maxGroup.size, ry / maxGroup.size, rz / maxGroup.size)
        } else {
            null
        }
    }

    private fun calckCosAngle(v1: Vector3Float, v2: Vector3Float): Double {
        val dotProduct = v1.dot(v2)
        val lengthProduct = v1.length() * v2.length()
        return dotProduct / lengthProduct
    }
}