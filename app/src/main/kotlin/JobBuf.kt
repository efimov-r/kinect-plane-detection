package era.app

import bufferchanel.BufferedChannel
import bufferchanel.databuffer.BufferedChannelForView
import bufferchanel.databuffer.BufferedCloudOfSensor
import clouds.Cloud
import era.app.AppConstants.COS_GROUP_PERPENDIC
import era.app.utils.neighbour.NeighbourHelper
import era.app.utils.subclouders.Chanker.chancked5
import era.app.utils.subclouders.CloudOfSensorCompressor
import era.app.utils.writers.WriterCloud
import era.utils.time.Span
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import vectors.Vector3ChildedShort
import vectors.Vector3Double
import vectors.Vector3Float
import vectors.Vector3Short
import viewdata.transferdata.ToViewText
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin

class JobBuf {
    companion object{

        val span = Span()
        val span2 = Span()

        suspend fun execute(
            formSensorChannel: BufferedChannel<BufferedCloudOfSensor>,
            toViewChannel: BufferedChannel<BufferedChannelForView>,
            channelText: Channel<ToViewText>
        ) {
            coroutineScope {
                formSensorChannel.getFirstDataForRead()?.let { readCloud ->
                    toViewChannel.getDataForWriteOrRewriteOld()?.let { writeCloud ->
                        span.resetTimer()

                        writeCloud.cubeData.clear()
                        writeCloud.lineData.clear()
                        writeCloud.pointData.clear()
                        writeCloud.triangleData.clear()

                        val text = "         min      max \n" +
                                "X:      ${readCloud.minX}    ${readCloud.maxX}\n" +
                                "Y:      ${readCloud.minY}    ${readCloud.maxY}\n" +
                                "Z:      ${readCloud.minZ}    ${readCloud.maxZ}"
                        channelText.send(ToViewText(text))


                        val fastCloud = CloudOfSensorCompressor.compressHexadrupleRare(readCloud)


                        val vectorP = doJobCube(fastCloud, writeCloud).await()
                        if(vectorP == null) {
                            WriterCloud.writePoint(readCloud, writeCloud.pointData)
                        } else {
                            WriterCloud.viewNullVector(vectorP, writeCloud.lineData, 0f)


                            val matrix = rotationMatrixToYf(Vector3Float(vectorP.x.toFloat(), vectorP.y.toFloat(), vectorP.z.toFloat()))
                            val newP = rotateVectorf(vectorP, matrix)
                            WriterCloud.viewNullVector(newP, writeCloud.lineData, 1f)

                            val res = Cloud(readCloud.size(), "cloudR")
                            span2.resetTimer()
                            readCloud.forEach { x, y, z ->
                                res.addGlob(
                                    (matrix[0][0] * x + matrix[0][1] * y + matrix[0][2] * z).toInt().toShort(),
                                    (matrix[1][0] * x + matrix[1][1] * y + matrix[1][2] * z).toInt().toShort(),
                                    (matrix[2][0] * x + matrix[2][1] * y + matrix[2][2] * z).toInt().toShort()
                                )
                            }
                            span2.middleCircle(100, "rotate")
                            WriterCloud.writePoint(res, writeCloud.pointData)


                        }
                        span.middleCircle(100, "All")

                        toViewChannel.endWrite(writeCloud)
                    }
                    formSensorChannel.endRead(readCloud)
                }
            }
        }

        fun CoroutineScope.doJobCube(
            fastCloud: Cloud, writeCloud: BufferedChannelForView
        ): Deferred<Vector3Short?> = async (Dispatchers.IO) {
            val v3List = chancked5(fastCloud, (AppConstants.STEP_CREATE_VERTICAL_VECTORS / 3).toShort())

            NeighbourHelper.findAndFilter(v3List)

            v3List.map { v->
                launch(Dispatchers.Default) {
                    v.createPerpendicularVectors()
                    v.filterPerpendicularVectors()
                    v.groupPerpendicularVectors(COS_GROUP_PERPENDIC)
                }
            }.joinAll()

//            v3List.forEach {
//                WriterCloud.viewPerpendicular(it, writeCloud.lineData)
//            }

            val vectorP = groupPerpendicularVectors(v3List)
            return@async vectorP
//            if(vectorP == null) {
//                WriterCloud.writePoint(fastCloud, writeCloud.pointData)
//            } else {
//                WriterCloud.viewNullVector(vectorP, writeCloud.lineData, 0f)
//
//
//                val matrix = rotationMatrixToY(Vector3Double(vectorP.x.toDouble(), vectorP.y.toDouble(), vectorP.z.toDouble()))
//                val newP = rotateVector(vectorP, matrix)
//                WriterCloud.viewNullVector(newP, writeCloud.lineData, 1f)
//
//                val res = CloudJ(fastCloud.size(), "cloudR")
//                span2.resetTimer()
//                fastCloud.forEach { x, y, z ->
//                    res.addGlob(
//                        (matrix[0][0] * x + matrix[0][1] * y + matrix[0][2] * z).toInt().toShort(),
//                        (matrix[1][0] * x + matrix[1][1] * y + matrix[1][2] * z).toInt().toShort(),
//                        (matrix[2][0] * x + matrix[2][1] * y + matrix[2][2] * z).toInt().toShort()
//                    )
//                }
//                span2.middleCircle(100, "rotate")
//                WriterCloud.writePoint(res, writeCloud.pointData)
//
//
//            }
        }

        fun rotateVector(v: Vector3Short, matrix: Array<DoubleArray>): Vector3Short {
            val x = matrix[0][0] * v.x + matrix[0][1] * v.y + matrix[0][2] * v.z
            val y = matrix[1][0] * v.x + matrix[1][1] * v.y + matrix[1][2] * v.z
            val z = matrix[2][0] * v.x + matrix[2][1] * v.y + matrix[2][2] * v.z
            return Vector3Short(x.toInt().toShort(), y.toInt().toShort(), z.toInt().toShort())
        }

        fun rotateVectorf(v: Vector3Short, matrix: Array<FloatArray>): Vector3Short {
            val x = matrix[0][0] * v.x + matrix[0][1] * v.y + matrix[0][2] * v.z
            val y = matrix[1][0] * v.x + matrix[1][1] * v.y + matrix[1][2] * v.z
            val z = matrix[2][0] * v.x + matrix[2][1] * v.y + matrix[2][2] * v.z
            return Vector3Short(x.toInt().toShort(), y.toInt().toShort(), z.toInt().toShort())
        }

        fun rotationMatrixToY(v: Vector3Double): Array<DoubleArray> {
            val from = v.normalize()
            val to = Vector3Double(0.0, -1.0, 0.0)

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
                doubleArrayOf(t*x*x + c,     t*x*y - z*s,   t*x*z + y*s),
                doubleArrayOf(t*y*x + z*s,   t*y*y + c,     t*y*z - x*s),
                doubleArrayOf(t*z*x - y*s,   t*z*y + x*s,   t*z*z + c)
            )
        }

        fun rotationMatrixToYf(v: Vector3Float): Array<FloatArray> {
            val from = v.normalize()
            val to = Vector3Float(0f, -1f, 0f)

            val axis = from.cross(to).normalize()
            val angle = acos(from.dot(to).coerceIn(-1f, 1f))  // важно не выйти за пределы [-1;1]

            if (angle == 0f || axis.length() == 0f) {
                return identityMatrixf()
            }

            val x = axis.x
            val y = axis.y
            val z = axis.z
            val c = cos(angle)
            val s = sin(angle)
            val t = 1 - c

            return arrayOf(
                floatArrayOf(t*x*x + c,     t*x*y - z*s,   t*x*z + y*s),
                floatArrayOf(t*y*x + z*s,   t*y*y + c,     t*y*z - x*s),
                floatArrayOf(t*z*x - y*s,   t*z*y + x*s,   t*z*z + c)
            )
        }

        fun identityMatrix(): Array<DoubleArray> = arrayOf(
            doubleArrayOf(1.0, 0.0, 0.0),
            doubleArrayOf(0.0, 1.0, 0.0),
            doubleArrayOf(0.0, 0.0, 1.0)
        )

        fun identityMatrixf(): Array<FloatArray> = arrayOf(
            floatArrayOf(1f, 0f, 0f),
            floatArrayOf(0f, 1f, 0f),
            floatArrayOf(0f, 0f, 1f)
        )

        private fun groupPerpendicularVectors(v3List: MutableList<Vector3ChildedShort>): Vector3Short? {
            val vectors = v3List.mapNotNull { v ->
                if (v.vecs.size == 1) v.vecs[0] else null
            }

            if (vectors.size < 2) return null

            val cosThreshold = COS_GROUP_PERPENDIC
            val res = mutableListOf<List<Vector3Short>>()

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
                var rx = 0
                var ry = 0
                var rz = 0
                maxGroup.forEach { v->
                    rx += v.x
                    ry += v.y
                    rz += v.z
                }
                Vector3Short((rx/maxGroup.size).toShort(), (ry/maxGroup.size).toShort(), (rz/maxGroup.size).toShort())
            } else {
                null
            }
        }

        private fun calckCosAngle(v1: Vector3Short, v2: Vector3Short): Double {
            val dotProduct = v1.dot(v2)
            val lengthProduct = v1.length() * v2.length()
            return dotProduct / lengthProduct
        }
    }
}