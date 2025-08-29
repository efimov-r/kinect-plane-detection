package viewdata.buffers

import clouds.Cloud
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import utils.Threadripper.threadripp
import vectors.Plane
import java.lang.Integer.max
import kotlin.math.min

class PointBuffer(initialCapacity: Int = 10) : ViewBuffer(initialCapacity) {

    suspend fun ofCloud(cloud: Cloud, color: Float, multFor3D: Float, countOfThread: Int) = coroutineScope {
        clear()

        if (arraySize < cloud.size * 4) {
            resize(cloud.size * 4)
        }

        threadripp(cloud.size, countOfThread) { start, end ->
            var i = start
            var j = start * 4
            while (i < end) {
                data[j]     = cloud.getZ(i) * multFor3D
                data[j + 1] = cloud.getX(i) * multFor3D
                data[j + 2] = cloud.getY(i) * multFor3D
                data[j + 3] = color
                i++
                j += 4
            }
        }

        sizeI = cloud.size * 4
        updated()
    }

    fun ofCloud(cloud: Cloud, color: Float, multFor3D: Float) {
        clear()
        if(arraySize < (cloud.size*4)) {
            resize(cloud.size*4)
        }
        for(i in 0 until cloud.size) {
            addDat(cloud.getZ(i) * multFor3D)
            addDat(cloud.getX(i) * multFor3D)
            addDat(cloud.getY(i) * multFor3D)
            addDat(color)
        }
        updated()
    }

    fun addPointOn3DKord(x: Float, y: Float, z: Float, color: Float) {
        resizeForPoint(1)

        addDat(z)
        addDat(x)
        addDat(y)
        addDat(color)
        updated()
    }

    fun addPoint(x: Float, y: Float, z: Float, color: Float, multFor3D: Float) {
        resizeForPoint(1)

        addDat(z * multFor3D)
        addDat(x * multFor3D)
        addDat(y * multFor3D)
        addDat(color)
        updated()
    }

    private fun resizeForPoint(count: Int) {
        if ((sizeI + (4*count)) > data.size) {
            resize(max(sizeI * 2, sizeI+(4*count)))
        }
    }
}