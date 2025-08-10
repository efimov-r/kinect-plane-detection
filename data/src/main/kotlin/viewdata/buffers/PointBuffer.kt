package viewdata.buffers

import clouds.Cloud
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import java.lang.Integer.max
import kotlin.math.min

class PointBuffer(initialCapacity: Int = 10) : ViewBuffer(initialCapacity) {

    suspend fun ofCloud(cloud: Cloud, color: Float, multFor3D: Float, countOfThread: Int) = coroutineScope {
        clear()

        if (bufRealSize < cloud.size * 4) {
            resize(cloud.size * 4)
        }

        val chunk = (cloud.size + countOfThread - 1) / countOfThread

        val jobs = ArrayList<Job>(countOfThread)

        for (t in 0 until countOfThread) {
            val start = t * chunk
            val end = min(cloud.size, start + chunk)

            if (start >= end) break

            jobs += launch(Dispatchers.Default) {
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
        }

        jobs.joinAll()

        sizeI = cloud.size * 4
        updated()
    }

    fun ofCloud(cloud: Cloud, color: Float, multFor3D: Float) {
        clear()
        if(bufRealSize < (cloud.size*4)) {
            resize(cloud.size*4)
        }
        for(i in 0 until cloud.size) {
            data[sizeI] = cloud.getZ(i) * multFor3D
            sizeI++
            data[sizeI] = cloud.getX(i) * multFor3D
            sizeI++
            data[sizeI] = cloud.getY(i) * multFor3D
            sizeI++
            data[sizeI] = color
            sizeI++
        }
        updated()
    }

    fun addPoint(x: Float, y: Float, z: Float, color: Float) {
        if ((sizeI + 4) > data.size) {
            resize(max(sizeI * 2, sizeI+4))
        }
        data[sizeI] = z
        sizeI++
        data[sizeI] = x
        sizeI++
        data[sizeI] = y
        sizeI++
        data[sizeI] = color
        sizeI++
        updated()
    }

    fun addPoint(x: Float, y: Float, z: Float, color: Float, multFor3D: Float) {
        if ((sizeI + 4) > data.size) {
            resize(max(sizeI * 2, sizeI+4))
        }
        data[sizeI] = z * multFor3D
        sizeI++
        data[sizeI] = x * multFor3D
        sizeI++
        data[sizeI] = y * multFor3D
        sizeI++
        data[sizeI] = color
        sizeI++
        updated()
    }
}