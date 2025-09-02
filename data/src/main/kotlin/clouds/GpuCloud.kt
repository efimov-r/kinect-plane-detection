package clouds

import org.jocl.CL
import org.jocl.cl_context
import org.jocl.cl_mem

class GpuCloud {
    val size: Int
    val xMem: cl_mem
    val yMem: cl_mem
    val zMem: cl_mem

    constructor(context: cl_context, cloud: Cloud) {
        this.size = cloud.size
        this.xMem = cloud.getXMem(context) //todo грузить многопоточно
        this.yMem = cloud.getYMem(context)
        this.zMem = cloud.getZMem(context)
    }

    fun releaseCloud() {
        CL.clReleaseMemObject(xMem)
        CL.clReleaseMemObject(yMem)
        CL.clReleaseMemObject(zMem)
    }
}