package clouds

import era.utils.gpu.KernelHelper
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import org.jocl.CL
import org.jocl.cl_context
import org.jocl.cl_mem

class GpuCloud {
    private var sizeI: Int? = null
    val size: Int
        get() = sizeI!!

    private var xMemI: cl_mem? = null
    val xMem: cl_mem
        get() = xMemI!!

    private var yMemI: cl_mem? = null
    val yMem: cl_mem
        get() = yMemI!!

    private var zMemI: cl_mem? = null
    val zMem: cl_mem
        get() = zMemI!!

    suspend fun loadCloud(context: cl_context, cloud: Cloud) : GpuCloud{
        coroutineScope {
            sizeI = cloud.size
            listOf(
                launch {
                    xMemI = cloud.getXMem(context)
                },
                launch {
                    yMemI = cloud.getYMem(context)
                },
                launch {
                    zMemI = cloud.getZMem(context)
                }
            ).joinAll()
        }

        return this
    }

    fun loadCloud(xMem: cl_mem, yMem: cl_mem, zMem: cl_mem, size: Int) : GpuCloud{
        xMemI = xMem
        yMemI = yMem
        zMemI = zMem
        sizeI = size
        return this
    }

    fun releaseCloud() {
        CL.clReleaseMemObject(xMemI)
        CL.clReleaseMemObject(yMemI)
        CL.clReleaseMemObject(zMemI)
    }
}