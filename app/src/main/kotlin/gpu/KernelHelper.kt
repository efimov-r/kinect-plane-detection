package era.app.gpu

import org.jocl.*

class KernelHelper {
    val queue: cl_command_queue
    val kernel: cl_kernel
    val context: cl_context
    val memToRelease: ArrayList<cl_mem> = ArrayList()
    var arg = 0

    constructor(context: cl_context, program: cl_program, device: cl_device_id, prName: String) {
        this.queue = CL.clCreateCommandQueue(context, device, 0, null)
        this.kernel = CL.clCreateKernel(program, prName, null)
        this.context = context
    }

    fun runKernel(size: Int) {
        val globalWorkSize = longArrayOf(size.toLong())
        CL.clEnqueueNDRangeKernel(queue, kernel, 1, null, globalWorkSize, null, 0, null, null)
        CL.clFinish(queue)
    }

    fun release() {
        memToRelease.forEach { mem ->
            CL.clReleaseMemObject(mem)
        }
        CL.clReleaseKernel(kernel)
        CL.clReleaseCommandQueue(queue)
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////
    /////запись аргуметов в ядро
    /////////////////////////////////////////////////////////////////////////////////////////////////////
    fun setKernelFloatArg(value: Float) {
        CL.clSetKernelArg(kernel, arg++, Sizeof.cl_float.toLong(), Pointer.to(floatArrayOf(value)))
    }

    fun setKernelIntArg(value: Int) {
        CL.clSetKernelArg(kernel, arg++, Sizeof.cl_int.toLong(), Pointer.to(intArrayOf(value)))
    }

    fun setKernelIntarrArgWriteOnly(size: Int) : cl_mem {
        val mem = CL.clCreateBuffer(context, CL.CL_MEM_WRITE_ONLY, Sizeof.cl_int * size.toLong(), null, null)
        CL.clSetKernelArg(kernel, arg++, Sizeof.cl_mem.toLong(), Pointer.to(mem))
        return mem
    }

    fun setKernelFloatarrArgWriteOnly(size: Int) : cl_mem {
        val mem = CL.clCreateBuffer(context, CL.CL_MEM_WRITE_ONLY, Sizeof.cl_float * size.toLong(), null, null)
        CL.clSetKernelArg(kernel, arg++, Sizeof.cl_mem.toLong(), Pointer.to(mem))
        return mem
    }

    fun setKernelIntArgReadWrite(value: Int) : cl_mem {
        val mem = CL.clCreateBuffer(context,
            CL.CL_MEM_READ_WRITE,
            Sizeof.cl_int.toLong(), null, null)
        CL.clEnqueueWriteBuffer(queue, mem, true, 0, Sizeof.cl_int.toLong(), Pointer.to(intArrayOf(value)), 0, null, null)
        CL.clSetKernelArg(kernel, arg++, Sizeof.cl_mem.toLong(), Pointer.to(mem))
        return mem
    }

    fun setKernelIntarrCopy(data: IntArray) {
        val mem = CL.clCreateBuffer(context, CL.CL_MEM_READ_ONLY or CL.CL_MEM_COPY_HOST_PTR,
            Sizeof.cl_int * data.size.toLong(), Pointer.to(data), null)
        CL.clSetKernelArg(kernel, arg++, Sizeof.cl_mem.toLong(), Pointer.to(mem))
        memToRelease.add(mem)
    }

    fun setKernelArgMem(mem: cl_mem) {
        CL.clSetKernelArg(kernel, arg++, Sizeof.cl_mem.toLong(), Pointer.to(mem))
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///// чтение результата
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////

    fun getFloatArr(mem: cl_mem, size: Int) : FloatArray {
        val outData = FloatArray(size)
        CL.clEnqueueReadBuffer(queue, mem, true, 0, Sizeof.cl_float * size.toLong(), Pointer.to(outData), 0, null, null)
        CL.clReleaseMemObject(mem)
        return outData
    }

    fun getIntArr(mem: cl_mem, size: Int) : IntArray {
        val outData = IntArray(size)
        CL.clEnqueueReadBuffer(queue, mem, true, 0, Sizeof.cl_int * size.toLong(), Pointer.to(outData), 0, null, null)
        CL.clReleaseMemObject(mem)
        return outData
    }

    fun getInt(mem: cl_mem) : Int {
        val outData = IntArray(1)
        CL.clEnqueueReadBuffer(queue, mem, true, 0, Sizeof.cl_int.toLong(), Pointer.to(outData), 0, null, null)
        CL.clReleaseMemObject(mem)
        return outData[0]
    }

    
}