package era.app.gpu

import clouds.Cloud2D
import clouds.GpuCloud
import org.jocl.CL
import org.jocl.cl_context
import org.jocl.cl_device_id
import org.jocl.cl_program
import vectors.Vector3Float

object JoclProjectPointsIndexed {

    private var program: cl_program? = null
    private var programSort: cl_program? = null
    private var context: cl_context? = null

    fun init(context: cl_context) {
        this.context = context
        val src1 = """
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
        program = CL.clCreateProgramWithSource(context, 1, arrayOf(src1), null, null)
        CL.clBuildProgram(program, 0, null, null, null, null)

        val src2 = """
            __kernel void bitonic_sort_xy(
            __global float *x,
            __global float *y,
            __global int   *idx,
            uint j,
            uint k
        ) {
            uint i = get_global_id(0);
            uint ixj = i ^ j;
            if (ixj > i) {
                int ascending = ((i & k) == 0);
                float xi = x[i], yi = y[i];
                float xj = x[ixj], yj = y[ixj];
                int cmp = 0;
                if (xi < xj) cmp = -1;
                else if (xi > xj) cmp = 1;
                else {
                    if (yi < yj) cmp = -1;
                    else if (yi > yj) cmp = 1;
                }
                if ((ascending && cmp > 0) || (!ascending && cmp < 0)) {
                    float tx = x[i]; x[i] = x[ixj]; x[ixj] = tx;
                    float ty = y[i]; y[i] = y[ixj]; y[ixj] = ty;
                    int ti = idx[i]; idx[i] = idx[ixj]; idx[ixj] = ti;
                }
            }
        }
        """.trimIndent()
        programSort = CL.clCreateProgramWithSource(context, 1, arrayOf(src2), null, null)
        CL.clBuildProgram(programSort, 0, null, null, null, null)

    }

    fun runKernel(device: cl_device_id,
                  cloud: GpuCloud, indexes: IntArray, u: Vector3Float, v: Vector3Float
    ): Cloud2D {

        val n = indexes.size
        val paddedN = nextPowerOfTwo(n)

        val kh = KernelHelper(context!!, program!!, device, "projectPointsIndexed")
        val khSort = KernelHelper(context!!, programSort!!, device, "bitonic_sort_xy")

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
        val outXMem = kh.setKernelFloatarrArgWriteOnly(paddedN)
        val outYMem = kh.setKernelFloatarrArgWriteOnly(paddedN)
        val outIdxMem = kh.setKernelIntarrArgWriteOnly(paddedN)

        kh.runKernel(indexes.size)

        if (paddedN != n) {
            // добавляем мусорные данные (они уйдут в конец после сортировки)
            val padCount = paddedN - n
            val padX = FloatArray(padCount) { Float.POSITIVE_INFINITY }
            val padY = FloatArray(padCount) { Float.POSITIVE_INFINITY }
            val padIdx = IntArray(padCount) { Int.MAX_VALUE }

            khSort.offsetFloatMem(outXMem, n, padCount, padX)
            khSort.offsetFloatMem(outYMem, n, padCount, padY)
            khSort.offsetIntMem(outIdxMem, n, padCount, padIdx)
        }
//        span.resetTimer()
        // стандартный bitonic sort по paddedN
        var k = 2
        khSort.setArgNum(0)
        khSort.setKernelArgMem(outXMem)
        khSort.setKernelArgMem(outYMem)
        khSort.setKernelArgMem(outIdxMem)
        while (k <= paddedN) {
            var j = k / 2
            khSort.setArgNum(4)
            khSort.setKernelIntArg(k)
            while (j > 0) {
                khSort.setArgNum(3)
                khSort.setKernelIntArg(j)

                khSort.runKernel(paddedN)
                j /= 2
            }
            k *= 2
        }


        val outX = khSort.getFloatArr(outXMem, indexes.size,)
        val outY = khSort.getFloatArr(outYMem, indexes.size,)
        val outIdx = khSort.getIntArr(outIdxMem, indexes.size,)


        kh.release()
        khSort.release()

//        span.middleCircle(100, "kernel")
        return Cloud2D.create(outX, outY, outIdx, indexes.size)
    }

    private fun nextPowerOfTwo(n: Int): Int {
        var v = 1
        while (v < n) v = v shl 1
        return v
    }
}