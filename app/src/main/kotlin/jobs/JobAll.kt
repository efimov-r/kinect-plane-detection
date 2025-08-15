package era.app.jobs

import bufferchanel.databuffer.BufferedChannelForView
import clouds.Cloud
import clouds.Hash
import clouds.KDTree
import era.app.AppConstants
import era.app.utils.subclouders.Voxeler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import vectors.Vector3Float

object JobAll {
    fun CoroutineScope.doJobAll(
        fastCloud: Cloud, writeCloud: BufferedChannelForView
    ) = async(Dispatchers.IO) {
        var count = 0
        val hashFastCloud = Hash(300)
        hashFastCloud.create(fastCloud)
        val voxelCloud = Voxeler.createVoxelsFromHash(fastCloud, hashFastCloud)
        val tree = KDTree.instanceSingle(voxelCloud)
        voxelCloud.forEachIndexed { x, y, z, i ->
            val nei = tree.radiusSearch(450f, i)
            nei.forEach { ine->
                val v1 = Vector3Float(x, y, z)
                val v2 = Vector3Float(
                    voxelCloud.getX(ine),
                    voxelCloud.getY(ine),
                    voxelCloud.getZ(ine))
                writeCloud.lineData.addVec(v1, v2, AppConstants.AD_MAX_MM_ON_V3D)
                count++
            }
        }
        voxelCloud.forEach { x, y, z ->
            writeCloud.cubeData.writeCube(Vector3Float(x, y, z), AppConstants.AD_MAX_MM_ON_V3D)
        }

    }
}