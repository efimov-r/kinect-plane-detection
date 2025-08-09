package era.app.utils.subclouders

import clouds.Cloud
import clouds.CloudI
import era.app.AppConstants
import era.utils.time.Span
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import vectors.Vector3
import vectors.Vector3Childed

object Chanker {

    val span = Span()

    //возвращает центральные точки чанков
    suspend fun CoroutineScope.chancked5(fastCloud: CloudI, halfSize: Short): MutableList<Vector3Childed> {
        val minX = (fastCloud.minX() ?: return mutableListOf()).toInt()
        val maxX = (fastCloud.maxX() ?: return mutableListOf()).toInt()
        val minZ = (fastCloud.minZ() ?: return mutableListOf()).toInt()
        val maxZ = (fastCloud.maxZ() ?: return mutableListOf()).toInt()
        val minY = (fastCloud.minY() ?: return mutableListOf()).toInt()
        val maxY = (fastCloud.maxY() ?: return mutableListOf()).toInt()

        if ((maxX - minX) < AppConstants.MIN_SIZE_CREATE_VERTICAL_VECTORS ||
            (maxZ - minZ) < AppConstants.MIN_SIZE_CREATE_VERTICAL_VECTORS) return mutableListOf()

        val ra = (minX..maxX step AppConstants.STEP_CREATE_VERTICAL_VECTORS).map { curX ->
            async(Dispatchers.Default) {
                val xMin = curX - halfSize
                val xMax = curX + halfSize
                val polosas = Cloud(300, "polosa cloud, $curX")
                fastCloud.forEach{x, y, z ->
                    if (x in xMin.toFloat()..xMax.toFloat()) {
                        polosas.add(x, y, z)
                    }
                }

                val corsIn = (minZ..maxZ step AppConstants.STEP_CREATE_VERTICAL_VECTORS).map { curZ->
                    async(Dispatchers.Default) {
                        val zMin = curZ - halfSize
                        val zMax = curZ + halfSize
                        val chank = Cloud(300, "Stolb cloud, $curX $curZ")

                        polosas.forEach{ x, y, z ->
                            if (z in zMin.toFloat()..zMax.toFloat()) {
                                chank.add(x, y, z)
                            }
                        }

                        var minLength = Int.MAX_VALUE
                        var length: Int
                        var result:Vector3<Float>? = null

                        val srY = maxY - minY
                        chank.forEach{x, y, z ->
                            length = (((x-curX)*(x-curX)) + ((y-srY) * (y-srY)) + ((z-curZ) * (z-curZ))).toInt()
                            if(length < minLength) {
                                minLength = length
                                result = Vector3(x, y, z)
                            }
                        }

                        return@async result?.let {
                            Vector3Childed(it.x, it.y, it.z)
                        }
                    }
                }
                return@async corsIn.awaitAll().filterNotNull()
            }
        }
        return ra.awaitAll().flatten().toMutableList()
    }
}