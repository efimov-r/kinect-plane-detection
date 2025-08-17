package era.app.utils.subclouders

import clouds.Cloud
import clouds.Hash
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import utils.Threadripper.threadripp

object Voxeler {

    suspend fun createVoxelsFromHash(cloud: Cloud, hash: Hash, countOfThread: Int) : Cloud =
        coroutineScope {
        val mapHash = hash.getVoxelMap(50, countOfThread)

            val entries = mapHash.entries.toList()
            val indexs = ArrayList<Int>(mapHash.size)

            threadripp(mapHash.size, countOfThread) { start, end ->
                for (idx in start until end) {
                    val (v, l) = entries[idx]

                    var minLength = Float.MAX_VALUE
                    var indexMinL = Integer.MAX_VALUE

                    val vx = Hash.getX(v, hash.voxelSize)
                    val vy = Hash.getY(v, hash.voxelSize)
                    val vz = Hash.getZ(v, hash.voxelSize)

                    for (i in l) {
                        val ax = cloud.getX(i) - vx
                        val ay = cloud.getY(i) - vy
                        val az = cloud.getZ(i) - vz
                        val length = ax*ax + ay*ay + az*az
                        if (length < minLength) {
                            minLength = length
                            indexMinL = i
                        }
                    }

                    indexs.add(indexMinL)
                }
            }
            val res = Cloud(indexs.size, "res")
            res.add(cloud, indexs)
            return@coroutineScope res
        }

//        val indexs = ArrayList<Int>(mapHash.size)
//        mapHash.forEach { (v, l) ->
//            var minLength = Float.MAX_VALUE
//            var indexMinL: Int = Integer.MAX_VALUE
//            l.forEach { i ->
//                val ax = cloud.getX(i) - Hash.getX(v, hash.voxelSize)
//                val ay = cloud.getY(i) - Hash.getY(v, hash.voxelSize)
//                val az = cloud.getZ(i) - Hash.getZ(v, hash.voxelSize)
//                val length = (ax * ax) + (ay * ay) + (az * az)
//                if (length < minLength) {
//                    minLength = length
//                    indexMinL = i
//                }
//            }
//            indexs.add(indexMinL)
//        }
//        val res = Cloud(indexs.size, "res")
//        res.add(cloud, indexs)
//        return res
//    }



    suspend fun CoroutineScope.createVoxelsCloudOfIndex(cloud: Cloud, voxelSizeMm: Int) : Cloud {
        val mutex = Mutex()
        val voxelSizeMmd2 = voxelSizeMm/2
        val minX = (cloud.minX() ?: return Cloud()).toInt()
        val maxX = (cloud.maxX() ?: return Cloud()).toInt()
        val minZ = (cloud.minZ() ?: return Cloud()).toInt()
        val maxZ = (cloud.maxZ() ?: return Cloud()).toInt()
        val minY = (cloud.minY() ?: return Cloud()).toInt()
        val maxY = (cloud.maxY() ?: return Cloud()).toInt()

        if ((maxX - minX) < voxelSizeMm*3 ||
            (maxY - minY) < voxelSizeMm*3 ||
            (maxZ - minZ) < voxelSizeMm*3) return Cloud()

        val res = Cloud(initialCapacity = 1_000, name="res")
        (minX + voxelSizeMmd2..maxX step voxelSizeMm).map { centerX ->
            launch(Dispatchers.Default) {
                val xMin = (centerX - voxelSizeMmd2).toFloat()
                val xMax = (centerX + voxelSizeMmd2).toFloat()
                val plos = ArrayList<Int>(70_000)
                cloud.forEachIndexed { x, i ->
                    if(x in xMin .. xMax) {
                        plos.add(i)
                    }
                }

                val t = (minY + voxelSizeMmd2 .. maxY step voxelSizeMm).map { centerY ->
                    async(Dispatchers.Default) {
                        val yMin = (centerY - voxelSizeMmd2).toFloat()
                        val yMax = (centerY + voxelSizeMmd2).toFloat()
                        val polos = ArrayList<Int>(40_000)
                        plos.forEach { i->
                            if(cloud.getY(i) in yMin .. yMax) {
                                polos.add(i)
                            }
                        }
                        return@async (minZ + voxelSizeMmd2 .. maxZ step voxelSizeMm).map { centerZ ->
                            val zMin = (centerZ - voxelSizeMmd2).toFloat()
                            val zMax = (centerZ + voxelSizeMmd2).toFloat()
                            var minLength = Float.MAX_VALUE
                            var indexMinL: Int = Integer.MAX_VALUE
                            polos.forEach { i->
                                val z = cloud.getZ(i)
                                if(z in zMin .. zMax) {
                                    val ax = cloud.getX(i)-centerX
                                    val ay = cloud.getY(i)-centerY
                                    val az = z-centerZ
                                    val length = (ax*ax) + (ay * ay) + (az * az)
                                    if(length < minLength) {
                                        minLength = length
                                        indexMinL = i
                                    }
                                }
                            }
                            return@map indexMinL
                        }
                    }
                }.awaitAll().flatten()

                mutex.withLock {
                    res.add(cloud, t)
                }
            }
        }.joinAll()
        return res
    }

}