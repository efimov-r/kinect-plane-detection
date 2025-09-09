package era.app.gpu

import clouds.GpuCloud
import era.utils.gpu.KernelHelper
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.jocl.CL
import org.jocl.cl_context
import org.jocl.cl_device_id
import org.jocl.cl_program

object JoclAddPointsToPlane {
    private var program: cl_program? = null
    private var context: cl_context? = null
    
    fun init(context: cl_context) {
        this.context = context
        val src = """
            __kernel void addPointsToPlane(
                __global const float *xArray,
                __global const float *yArray,
                __global const float *zArray,
                const float a, const float b, const float c, const float d,
                const float epsNorm, const float epsNormSec,
                __global int *points,
                __global int *pointsSecond,
                __global volatile int *counterPoints,
                __global volatile int *counterSecond,
                const int size)
            {
                int i = get_global_id(0);
                if (i >= size) return;
            
                float len = fabs(a * xArray[i] + b * yArray[i] + c * zArray[i] + d);
            
                if (len < epsNorm) {
                    // atomic_add возвращает СТАРОЕ значение — идеально как индекс
                    int pos  = atomic_add(counterPoints, 1);
                    points[pos] = i;
            
                    int pos2 = atomic_add(counterSecond, 1);
                    pointsSecond[pos2] = i;
                } else if (len < epsNormSec) {
                    int pos2 = atomic_add(counterSecond, 1);
                    pointsSecond[pos2] = i;
                }
            }
        """.trimIndent()

        program = CL.clCreateProgramWithSource(context, 1, arrayOf(src), null, null)
        CL.clBuildProgram(program, 0, null, null, null, null)
    }

    suspend fun runKernel(device: cl_device_id,
                  cloud: GpuCloud, a: Float, b: Float, c: Float, d: Float, epsNorm: Float, epsNormSec: Float
    ): Pair<IntArray, IntArray> {
        val kh = KernelHelper(context!!, program!!, device, "addPointsToPlane")

        kh.setKernelArgMem(cloud.xMem)
        kh.setKernelArgMem(cloud.yMem)
        kh.setKernelArgMem(cloud.zMem)
        kh.setKernelFloatArg(a)
        kh.setKernelFloatArg(b)
        kh.setKernelFloatArg(c)
        kh.setKernelFloatArg(d)
        kh.setKernelFloatArg(epsNorm)
        kh.setKernelFloatArg(epsNormSec)
        val pointsMem = kh.setKernelIntarrArgWriteOnly(cloud.size)
        val pointsSecondMem = kh.setKernelIntarrArgWriteOnly(cloud.size)
        val counterPointsMem = kh.setKernelIntArgReadWrite(0)
        val counterSecondMem = kh.setKernelIntArgReadWrite(0)
        kh.setKernelIntArg(cloud.size)

        kh.runKernel(cloud.size)

        val res = coroutineScope {
            val pointsJob = async {
                val counterPoints = kh.getInt(counterPointsMem)
                kh.getIntArr(pointsMem, counterPoints)
            }
            val pointsSecondJob = async {
                val counterSecond = kh.getInt(counterSecondMem)
                kh.getIntArr(pointsSecondMem, counterSecond)
            }
            return@coroutineScope pointsJob.await() to pointsSecondJob.await()
        }

        kh.release()

        return res
    }
}