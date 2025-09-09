package era.app.gpu

import clouds.Cloud
import clouds.GpuCloud
import org.jocl.CL
import org.jocl.cl_context
import org.jocl.cl_device_id
import org.jocl.cl_program

object JoclGetFastCloudWithHash {
    private var program: cl_program? = null
    private var context: cl_context? = null

    fun init(context: cl_context) {
        this.context = context
        val src = """
            #pragma OPENCL EXTENSION cl_khr_int64_base_atomics : enable

            __kernel void gatherAndHashCompact(
                __global const float* xArray,
                __global const float* yArray,
                __global const float* zArray,
                const int width,
                const int size,
                const float bias,
                const int voxelSize,
                const long mask,

                __global float* outX,
                __global float* outY,
                __global float* outZ,
                __global long* outHash,
                __global volatile int* counter  // единый глобальный счётчик
            ) {
                int i = get_global_id(0);
                if (i >= size) return;

                int gg = width / 4;

                int x = (i % gg) * 4 + 1;
                int y = (i / gg) * 4 + 1;

                int c = y * width + x;
                float v;

                float rx = 0.0f, ry = 0.0f, rz = 0.0f;
                int found = 0;

                // сосед 0
                v = zArray[c];
                if (v != 0.0f) {
                    rx = xArray[c]; ry = yArray[c]; rz = v;
                    found = 1;
                }

                // сосед 1
                if (!found) {
                    c += width;
                    v = zArray[c];
                    if (v != 0.0f) {
                        rx = xArray[c]; ry = yArray[c]; rz = v;
                        found = 1;
                    }
                }

                // сосед 2
                if (!found) {
                    c = c - width + 1;
                    v = zArray[c];
                    if (v != 0.0f) {
                        rx = xArray[c]; ry = yArray[c]; rz = v;
                        found = 1;
                    }
                }

                // сосед 3
                if (!found) {
                    c += width;
                    v = zArray[c];
                    if (v != 0.0f) {
                        rx = xArray[c]; ry = yArray[c]; rz = v;
                        found = 1;
                    }
                }

                if (found) {
                    // атомарно получаем индекс
                    int outIdx = atomic_inc(counter);

                    outX[outIdx] = rx;
                    outY[outIdx] = ry;
                    outZ[outIdx] = rz;

                    long ix = (long)((rx + bias) / (float)voxelSize);
                    long iy = (long)((ry + bias) / (float)voxelSize);
                    long iz = (long)((rz + bias) / (float)voxelSize);

                    outHash[outIdx] = ((ix & mask) << 42) |
                                      ((iy & mask) << 21) |
                                      (iz & mask);
                }
            }

        """.trimIndent()

        program = CL.clCreateProgramWithSource(context, 1, arrayOf(src), null, null)
        CL.clBuildProgram(program, 0, null, null, null, null)
    }

    fun runKernel(device: cl_device_id, cloud: GpuCloud,
                  width: Int, inpSize: Int, bias: Float, hashSize: Int, mask: Long,
                  ) : Cloud {
        val kh = KernelHelper(context!!, program!!, device, "gatherAndHashCompact")
        val outSize = inpSize / 16

        kh.setKernelArgMem(cloud.xMem)
        kh.setKernelArgMem(cloud.yMem)
        kh.setKernelArgMem(cloud.zMem)
        kh.setKernelIntArg(width)
        kh.setKernelIntArg(outSize)
        kh.setKernelFloatArg(bias)
        kh.setKernelIntArg(hashSize)
        kh.setKernelLongArg(mask)
        val outXMem = kh.setKernelFloatarrArgWriteOnly(outSize)
        val outYMem = kh.setKernelFloatarrArgWriteOnly(outSize)
        val outZMem = kh.setKernelFloatarrArgWriteOnly(outSize)
        val outHashMem = kh.setKernelLongarrArgWriteOnly(outSize)
        val counterMem = kh.setKernelIntArgReadWrite(0)

        kh.runKernel(outSize)

        val counter = kh.getInt(counterMem)

        val res = Cloud(
            kh.getFloatArr(outXMem, counter),
            kh.getFloatArr(outYMem, counter),
            kh.getFloatArr(outZMem, counter),
            counter,
            kh.getLongArr(outHashMem, counter), hashSize
        )
        kh.release()
        return res
    }
}