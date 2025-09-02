package era.app.gpu

import clouds.GpuCloud
import org.jocl.CL
import org.jocl.cl_context
import org.jocl.cl_device_id
import org.jocl.cl_program
import vectors.P2D
import vectors.Vector3Float

object JoclProjectPointsIndexed {

    private var program: cl_program? = null
    private var context: cl_context? = null

    fun init(context: cl_context) {
        this.context = context
        val src2 = """
            __kernel void projectPointsIndexed(
                __global const float *xArray,
                __global const float *yArray,
                __global const float *zArray,
                __global const int   *indexes,
                const int nIndexes,
                const float ux, const float uy, const float uz,
                const float vx, const float vy, const float vz,
                __global float *outX,
                __global float *outY,
                __global int   *outIdx)
            {
                int gid = get_global_id(0);
                if (gid >= nIndexes) return;
            
                int idx = indexes[gid];
                float x = xArray[idx];
                float y = yArray[idx];
                float z = zArray[idx];
            
                outX[gid] = x * ux + y * uy + z * uz;
                outY[gid] = x * vx + y * vy + z * vz;
                outIdx[gid] = idx; // сохраняем исходный индекс
            }
        """.trimIndent()

        program = CL.clCreateProgramWithSource(context, 1, arrayOf(src2), null, null)
        CL.clBuildProgram(program, 0, null, null, null, null)
    }

    fun runKernel(device: cl_device_id,
                  cloud: GpuCloud, indexes: IntArray, u: Vector3Float, v: Vector3Float): MutableList<P2D> {
        val kh = KernelHelper(context!!, program!!, device, "projectPointsIndexed")

        kh.setKernelArgMem(cloud.xMem)
        kh.setKernelArgMem(cloud.yMem)
        kh.setKernelArgMem(cloud.zMem)
        kh.setKernelIntarrCopy(indexes)
        kh.setKernelIntArg(indexes.size)
        kh.setKernelFloatArg(u.x)
        kh.setKernelFloatArg(u.y)
        kh.setKernelFloatArg(u.z)
        kh.setKernelFloatArg(v.x)
        kh.setKernelFloatArg(v.y)
        kh.setKernelFloatArg(v.z)
        val outXMem = kh.setKernelFloatarrArgWriteOnly(indexes.size)
        val outYMem = kh.setKernelFloatarrArgWriteOnly(indexes.size)
        val outIdxMem = kh.setKernelIntarrArgWriteOnly(indexes.size)

        kh.runKernel(indexes.size)

        val outX = kh.getFloatArr(outXMem, indexes.size)
        val outY = kh.getFloatArr(outYMem, indexes.size)
        val outIdx = kh.getIntArr(outIdxMem, indexes.size)

        kh.release()

        return MutableList(indexes.size) { i -> P2D(outX[i], outY[i], outIdx[i]) }
    }
}