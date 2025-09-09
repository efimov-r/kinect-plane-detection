package era.app.gpu

import clouds.Cloud
import clouds.GpuCloud
import era.utils.gpu.KernelHelper
import org.jocl.CL
import org.jocl.cl_context
import org.jocl.cl_device_id
import org.jocl.cl_program
import vectors.PlanePoints

object JoclGetCloudOfIndex {
    private var program: cl_program? = null
    private var context: cl_context? = null

    fun init(context: cl_context) {
        this.context = context
        val src = """
            __kernel void getCloudOfIndex(
                __global const float *xArray,
                __global const float *yArray,
                __global const float *zArray,
                __global const int   *indexes,
                const int nIndexes,
                __global float *outX,
                __global float *outY,
                __global float *outZ)
            {
                int gid = get_global_id(0);
                if (gid >= nIndexes) return;
            
                int idx = indexes[gid];                
            
                outX[gid] = xArray[idx];
                outY[gid] = yArray[idx];
                outZ[gid] = zArray[idx];
            }
        """.trimIndent()

        program = CL.clCreateProgramWithSource(context, 1, arrayOf(src), null, null)
        CL.clBuildProgram(program, 0, null, null, null, null)
    }

    fun runKernel(device: cl_device_id, plane: PlanePoints,
                  cloud: GpuCloud, indexes: IntArray) {

        val n = indexes.size
        val kh = KernelHelper(context!!, program!!, device, "getCloudOfIndex")
        kh.setKernelArgMem(cloud.xMem)
        kh.setKernelArgMem(cloud.yMem)
        kh.setKernelArgMem(cloud.zMem)
        kh.setKernelIntarrCopy(indexes)
        kh.setKernelIntArg(n)
        val outXMem = kh.setKernelFloatarrArgWriteOnly(n)
        val outYMem = kh.setKernelFloatarrArgWriteOnly(n)
        val outZMem = kh.setKernelFloatarrArgWriteOnly(n)

        kh.runKernel(n)

        plane.setPoints(
            kh.getFloatArr(outXMem, n),
            kh.getFloatArr(outYMem, n),
            kh.getFloatArr(outZMem, n),
            n
        )

        kh.release()

    }

    fun runKernel(device: cl_device_id,
                  cloud: GpuCloud, indexes: IntArray) : Cloud {

        val n = indexes.size
        val kh = KernelHelper(context!!, program!!, device, "getCloudOfIndex")
        kh.setKernelArgMem(cloud.xMem)
        kh.setKernelArgMem(cloud.yMem)
        kh.setKernelArgMem(cloud.zMem)
        kh.setKernelIntarrCopy(indexes)
        kh.setKernelIntArg(n)
        val outXMem = kh.setKernelFloatarrArgWriteOnly(n)
        val outYMem = kh.setKernelFloatarrArgWriteOnly(n)
        val outZMem = kh.setKernelFloatarrArgWriteOnly(n)

        kh.runKernel(n)

        val res = Cloud(
            kh.getFloatArr(outXMem, n),
            kh.getFloatArr(outYMem, n),
            kh.getFloatArr(outZMem, n),
            n, null ,null
        )

        kh.release()

        return res
    }
}