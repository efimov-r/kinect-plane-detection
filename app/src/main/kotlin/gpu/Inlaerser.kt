package era.app.gpu

import clouds.Cloud
import org.jocl.*
import vectors.P2D
import vectors.Vector3Float

class Inlaerser {
    private var context: cl_context? = null
    private var program: cl_program? = null
    private var program2: cl_program? = null
    private var devices: Array<cl_device_id?>? = null
    private var deviceIndex: Int? = null

    // Словарь облаков: по имени -> GPU-буферы
    private val clouds = mutableMapOf<String, CloudBuffers>()

    data class CloudBuffers(
        val size: Int,
        val xMem: cl_mem,
        val yMem: cl_mem,
        val zMem: cl_mem
    )

    /** ---------------- Инициализация ---------------- */
    fun init(platformIndex: Int = 0, deviceIndex: Int = 0) {
        this.deviceIndex = deviceIndex
        CL.setExceptionsEnabled(true)

        // Выбор платформы и устройства
        val numPlatformsArray = IntArray(1)
        CL.clGetPlatformIDs(0, null, numPlatformsArray)
        val platforms = arrayOfNulls<cl_platform_id>(numPlatformsArray[0])
        CL.clGetPlatformIDs(platforms.size, platforms, null)
        val platform = platforms[platformIndex]!!

        val numDevicesArray = IntArray(1)
        CL.clGetDeviceIDs(platform, CL.CL_DEVICE_TYPE_ALL, 0, null, numDevicesArray)
        devices = arrayOfNulls<cl_device_id>(numDevicesArray[0])
        CL.clGetDeviceIDs(platform, CL.CL_DEVICE_TYPE_ALL, devices!!.size, devices, null)
        val device = devices?.get(deviceIndex)!!

        context = CL.clCreateContext(null, 1, arrayOf(device), null, null, null)

        // Компиляция ядра
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

        program2 = CL.clCreateProgramWithSource(context, 1, arrayOf(src2), null, null)
        CL.clBuildProgram(program2, 0, null, null, null, null)
    }

    /** ---------------- Загрузка или обновление облака ---------------- */
    fun uploadCloud(cloud: Cloud) {
        clouds[cloud.name]?.let { releaseCloud(it) }
        clouds[cloud.name] = CloudBuffers(cloud.size,
            cloud.getXMem(context),
            cloud.getYMem(context),
            cloud.getZMem(context))
    }

    private fun getDevice() = devices?.let { it[deviceIndex!!]!! }
    private fun getCloud(name: String) = clouds[name] ?: error("Cloud $name not found")

    /** ---------------- Запуск ядра ---------------- */
    fun runKernel(name: String,
                  a: Float, b: Float, c: Float, d: Float,
                  epsNorm: Float, epsNormSec: Float): Pair<IntArray, IntArray> {
        val kh = KernelHelper(context!!, program!!, getDevice()!!, "addPointsToPlane")

        kh.setKernelArgMem(getCloud(name).xMem)
        kh.setKernelArgMem(getCloud(name).yMem)
        kh.setKernelArgMem(getCloud(name).zMem)
        kh.setKernelFloatArg(a)
        kh.setKernelFloatArg(b)
        kh.setKernelFloatArg(c)
        kh.setKernelFloatArg(d)
        kh.setKernelFloatArg(epsNorm)
        kh.setKernelFloatArg(epsNormSec)
        val pointsMem = kh.setKernelIntarrArgWriteOnly(getCloud(name).size)
        val pointsSecondMem = kh.setKernelIntarrArgWriteOnly(getCloud(name).size)
        val counterPointsMem = kh.setKernelIntArgReadWrite(0)
        val counterSecondMem = kh.setKernelIntArgReadWrite(0)
        kh.setKernelIntArg(getCloud(name).size)

        kh.runKernel(getCloud(name).size)

        val counterPoints = kh.getInt(counterPointsMem)
        val counterSecond = kh.getInt(counterSecondMem)
        val points = kh.getIntArr(pointsMem, counterPoints)
        val pointsSecond = kh.getIntArr(pointsSecondMem, counterSecond)

        kh.release()

        return points to pointsSecond
    }

    fun runKernel2(name: String,
                   indexes: IntArray,
                   u: Vector3Float,
                   v: Vector3Float): MutableList<P2D> {
        val kh = KernelHelper(context!!, program2!!, getDevice()!!, "projectPointsIndexed")

        kh.setKernelArgMem(getCloud(name).xMem)
        kh.setKernelArgMem(getCloud(name).yMem)
        kh.setKernelArgMem(getCloud(name).zMem)
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


    /** ---------------- Освобождение памяти облака ---------------- */
    private fun releaseCloud(cloud: CloudBuffers) {
        CL.clReleaseMemObject(cloud.xMem)
        CL.clReleaseMemObject(cloud.yMem)
        CL.clReleaseMemObject(cloud.zMem)
    }

    fun releaseCloud(name: String) {
        clouds[name]?.let { releaseCloud(it) }
    }

    /** ---------------- Освобождение всех ресурсов ---------------- */
    fun release() {
        clouds.values.forEach { releaseCloud(it) }
        clouds.clear()
        program?.let { CL.clReleaseProgram(it) }
        context?.let { CL.clReleaseContext(it) }
    }
}