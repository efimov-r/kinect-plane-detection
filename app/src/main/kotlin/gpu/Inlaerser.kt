package era.app.gpu

import clouds.Cloud
import era.utils.time.Span
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
        val size = cloud.size

        // Если уже существует -> освободить старое
        clouds[cloud.name]?.let { releaseCloud(it) }

        // Создаём новые буферы
        val xMem = cloud.getXMem(context)
        val yMem = cloud.getYMem(context)
        val zMem = cloud.getZMem(context)

        clouds[cloud.name] = CloudBuffers(size, xMem, yMem, zMem,)
    }

    /** ---------------- Запуск ядра ---------------- */
    fun runKernel(name: String,
                  a: Float, b: Float, c: Float, d: Float,
                  epsNorm: Float, epsNormSec: Float): Pair<IntArray, IntArray> {
        val device = devices?.let { it[deviceIndex!!]!! }
        val cloud = clouds[name] ?: error("Cloud $name not found")
        // Для каждого потока создаём СВОЙ queue
        val queue = CL.clCreateCommandQueue(context, device, 0, null)

        // И свой kernel (чтобы не гоняться за setKernelArg)
        val kernel = CL.clCreateKernel(program, "addPointsToPlane", null)


        val pointsMem = CL.clCreateBuffer(context,
            CL.CL_MEM_WRITE_ONLY,
            Sizeof.cl_int * cloud.size.toLong(), null, null)

        val pointsSecondMem = CL.clCreateBuffer(context,
            CL.CL_MEM_WRITE_ONLY,
            Sizeof.cl_int * cloud.size.toLong(), null, null)

        val counterPointsMem = CL.clCreateBuffer(context,
            CL.CL_MEM_READ_WRITE,
            Sizeof.cl_int.toLong(), null, null)

        val counterSecondMem = CL.clCreateBuffer(context,
            CL.CL_MEM_READ_WRITE,
            Sizeof.cl_int.toLong(), null, null)

        // Обнулить счётчики
        val zero = intArrayOf(0)
        CL.clEnqueueWriteBuffer(queue, counterPointsMem, true, 0, Sizeof.cl_int.toLong(), Pointer.to(zero), 0, null, null)
        CL.clEnqueueWriteBuffer(queue, counterSecondMem, true, 0, Sizeof.cl_int.toLong(), Pointer.to(zero), 0, null, null)

        // Аргументы ядра
        var arg = 0
        CL.clSetKernelArg(kernel, arg++, Sizeof.cl_mem.toLong(), Pointer.to(cloud.xMem))
        CL.clSetKernelArg(kernel, arg++, Sizeof.cl_mem.toLong(), Pointer.to(cloud.yMem))
        CL.clSetKernelArg(kernel, arg++, Sizeof.cl_mem.toLong(), Pointer.to(cloud.zMem))
        CL.clSetKernelArg(kernel, arg++, Sizeof.cl_float.toLong(), Pointer.to(floatArrayOf(a)))
        CL.clSetKernelArg(kernel, arg++, Sizeof.cl_float.toLong(), Pointer.to(floatArrayOf(b)))
        CL.clSetKernelArg(kernel, arg++, Sizeof.cl_float.toLong(), Pointer.to(floatArrayOf(c)))
        CL.clSetKernelArg(kernel, arg++, Sizeof.cl_float.toLong(), Pointer.to(floatArrayOf(d)))
        CL.clSetKernelArg(kernel, arg++, Sizeof.cl_float.toLong(), Pointer.to(floatArrayOf(epsNorm)))
        CL.clSetKernelArg(kernel, arg++, Sizeof.cl_float.toLong(), Pointer.to(floatArrayOf(epsNormSec)))
        CL.clSetKernelArg(kernel, arg++, Sizeof.cl_mem.toLong(), Pointer.to(pointsMem))
        CL.clSetKernelArg(kernel, arg++, Sizeof.cl_mem.toLong(), Pointer.to(pointsSecondMem))
        CL.clSetKernelArg(kernel, arg++, Sizeof.cl_mem.toLong(), Pointer.to(counterPointsMem))
        CL.clSetKernelArg(kernel, arg++, Sizeof.cl_mem.toLong(), Pointer.to(counterSecondMem))
        CL.clSetKernelArg(kernel, arg++, Sizeof.cl_int.toLong(), Pointer.to(intArrayOf(cloud.size)))

        // Запуск (каждый поток гоняет свой NDRange)
        val globalWorkSize = longArrayOf(cloud.size.toLong())
        CL.clEnqueueNDRangeKernel(queue, kernel, 1, null, globalWorkSize, null, 0, null, null)
        CL.clFinish(queue)

        // Считывание счётчиков
        val counterPoints = IntArray(1)
        val counterSecond = IntArray(1)
        CL.clEnqueueReadBuffer(queue, counterPointsMem, true, 0, Sizeof.cl_int.toLong(), Pointer.to(counterPoints), 0, null, null)
        CL.clEnqueueReadBuffer(queue, counterSecondMem, true, 0, Sizeof.cl_int.toLong(), Pointer.to(counterSecond), 0, null, null)

        // Считывание результатов
        val points = IntArray(counterPoints[0])
        val pointsSecond = IntArray(counterSecond[0])
        CL.clEnqueueReadBuffer(queue, pointsMem, true, 0, Sizeof.cl_int * points.size.toLong(), Pointer.to(points), 0, null, null)
        CL.clEnqueueReadBuffer(queue, pointsSecondMem, true, 0, Sizeof.cl_int * pointsSecond.size.toLong(), Pointer.to(pointsSecond), 0, null, null)

        // Чистим ресурсы этого потока
        CL.clReleaseKernel(kernel)
        CL.clReleaseCommandQueue(queue)

        CL.clReleaseMemObject(pointsMem)
        CL.clReleaseMemObject(pointsSecondMem)
        CL.clReleaseMemObject(counterPointsMem)
        CL.clReleaseMemObject(counterSecondMem)

        return points to pointsSecond
    }

    fun runKernel2(name: String,
                   indexes: IntArray,
                   u: Vector3Float,
                   v: Vector3Float): MutableList<P2D> {
        val n = indexes.size
        val device = devices?.let { it[deviceIndex!!]!! }
        val cloud = clouds[name] ?: error("Cloud $name not found")
        // Для каждого потока создаём СВОЙ queue
        val queue = CL.clCreateCommandQueue(context, device, 0, null)

        // И свой kernel (чтобы не гоняться за setKernelArg)
        val kernel = CL.clCreateKernel(program2, "projectPointsIndexed", null)

        // Буфер индексов
        val idxMem = CL.clCreateBuffer(context, CL.CL_MEM_READ_ONLY or CL.CL_MEM_COPY_HOST_PTR,
            Sizeof.cl_int * n.toLong(), Pointer.to(indexes), null)

        val outXMem = CL.clCreateBuffer(context, CL.CL_MEM_WRITE_ONLY,
            Sizeof.cl_float * indexes.size.toLong(), null, null)
        val outYMem = CL.clCreateBuffer(context, CL.CL_MEM_WRITE_ONLY,
            Sizeof.cl_float * indexes.size.toLong(), null, null)
        val outIdxMem = CL.clCreateBuffer(context, CL.CL_MEM_WRITE_ONLY,
            Sizeof.cl_int * indexes.size.toLong(), null, null)

        // Аргументы ядра
        var arg = 0
        CL.clSetKernelArg(kernel, arg++, Sizeof.cl_mem.toLong(), Pointer.to(cloud.xMem))
        CL.clSetKernelArg(kernel, arg++, Sizeof.cl_mem.toLong(), Pointer.to(cloud.yMem))
        CL.clSetKernelArg(kernel, arg++, Sizeof.cl_mem.toLong(), Pointer.to(cloud.zMem))
        CL.clSetKernelArg(kernel, arg++, Sizeof.cl_mem.toLong(), Pointer.to(idxMem))
        CL.clSetKernelArg(kernel, arg++, Sizeof.cl_int.toLong(), Pointer.to(intArrayOf(n)))
        CL.clSetKernelArg(kernel, arg++, Sizeof.cl_float.toLong(), Pointer.to(floatArrayOf(u.x)))
        CL.clSetKernelArg(kernel, arg++, Sizeof.cl_float.toLong(), Pointer.to(floatArrayOf(u.y)))
        CL.clSetKernelArg(kernel, arg++, Sizeof.cl_float.toLong(), Pointer.to(floatArrayOf(u.z)))
        CL.clSetKernelArg(kernel, arg++, Sizeof.cl_float.toLong(), Pointer.to(floatArrayOf(v.x)))
        CL.clSetKernelArg(kernel, arg++, Sizeof.cl_float.toLong(), Pointer.to(floatArrayOf(v.y)))
        CL.clSetKernelArg(kernel, arg++, Sizeof.cl_float.toLong(), Pointer.to(floatArrayOf(v.z)))
        CL.clSetKernelArg(kernel, arg++, Sizeof.cl_mem.toLong(), Pointer.to(outXMem))
        CL.clSetKernelArg(kernel, arg++, Sizeof.cl_mem.toLong(), Pointer.to(outYMem))
        CL.clSetKernelArg(kernel, arg++, Sizeof.cl_mem.toLong(), Pointer.to(outIdxMem))

        // Запуск (каждый поток гоняет свой NDRange)
        val globalWorkSize = longArrayOf(indexes.size.toLong())
        CL.clEnqueueNDRangeKernel(queue, kernel, 1, null, globalWorkSize, null, 0, null, null)
        CL.clFinish(queue)

        // Читаем результат
        val outX = FloatArray(indexes.size)
        val outY = FloatArray(indexes.size)
        val outIdx = IntArray(indexes.size)
        CL.clEnqueueReadBuffer(queue, outXMem, true, 0, Sizeof.cl_float * indexes.size.toLong(), Pointer.to(outX), 0, null, null)
        CL.clEnqueueReadBuffer(queue, outYMem, true, 0, Sizeof.cl_float * indexes.size.toLong(), Pointer.to(outY), 0, null, null)
        CL.clEnqueueReadBuffer(queue, outIdxMem, true, 0, Sizeof.cl_int * indexes.size.toLong(), Pointer.to(outIdx), 0, null, null)

        // Освобождаем ресурсы
        CL.clReleaseMemObject(idxMem)
        CL.clReleaseMemObject(outXMem)
        CL.clReleaseMemObject(outYMem)
        CL.clReleaseMemObject(outIdxMem)
        CL.clReleaseKernel(kernel)
        CL.clReleaseCommandQueue(queue)


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