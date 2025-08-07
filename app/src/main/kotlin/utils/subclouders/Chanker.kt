package era.app.utils.subclouders

import clouds.ChankCloudJ
import clouds.Cloud
import era.app.AppConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import vectors.Vector3
import vectors.Vector3ChildedShort

object Chanker {

    //возвращает центральные точки чанков
    suspend fun CoroutineScope.chancked5(fastCloud: Cloud, halfSize: Short): MutableList<Vector3ChildedShort> {
        val minX = fastCloud.minX ?: return mutableListOf()
        val maxX = fastCloud.maxX ?: return mutableListOf()
        val minZ = fastCloud.minZ ?: return mutableListOf()
        val maxZ = fastCloud.maxZ ?: return mutableListOf()

        if ((maxX.toInt() - minX.toInt()) < AppConstants.MIN_SIZE_CREATE_VERTICAL_VECTORS ||
            (maxZ.toInt() - minZ.toInt()) < AppConstants.MIN_SIZE_CREATE_VERTICAL_VECTORS) return mutableListOf()

        val ra = (minX..maxX step AppConstants.STEP_CREATE_VERTICAL_VECTORS.toInt()).map { curX ->
            async(Dispatchers.Default) {
                val xMin = curX - halfSize
                val xMax = curX + halfSize
                var minZ = Short.MAX_VALUE
                var maxZ = Short.MIN_VALUE
                val polosas = ChankCloudJ(300, "polosa cloud, $curX")
                fastCloud.forEach{x, y, z ->
                    if (x in xMin..xMax) {
                        polosas.addGlob(x, y, z)
                        if(maxZ < z) maxZ = z
                        if(minZ > z) minZ = z
                    }
                }

                val corsIn = (minZ..maxZ step AppConstants.STEP_CREATE_VERTICAL_VECTORS.toInt()).map { curZ->
                    async(Dispatchers.Default) {
                        val zMin = curZ - halfSize
                        val zMax = curZ + halfSize
                        val chank = ChankCloudJ(300, "Stolb cloud, $curX $curZ")
                        var maxY: Short = Short.MIN_VALUE
                        var minY: Short = Short.MAX_VALUE
                        polosas.forEach{ x, y, z ->
                            if (z in zMin..zMax) {
                                chank.addGlob(x, y, z)
                                if(maxY < y) maxY = y
                                if(minY > y) minY = y
                            }
                        }

                        var minLength = Int.MAX_VALUE
                        var length: Int
                        var result:Vector3<Short>? = null

                        val srY = maxY - minY
                        chank.forEach{x, y, z ->
                            length = ((x-curX)*(x-curX)) + ((y-srY) * (y-srY)) + ((z-curZ) * (z-curZ))
                            if(length < minLength) {
                                minLength = length
                                result = Vector3(x, y, z)
                            }
                        }

                        return@async result?.let {
                            Vector3ChildedShort(it.x, it.y, it.z)
                        }
                    }
                }
                return@async corsIn.awaitAll().filterNotNull()
            }
        }
        return ra.awaitAll().flatten().toMutableList()
    }
}