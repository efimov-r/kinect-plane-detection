package sensors.cams.kinect.util

import clouds.GpuCloud
import era.utils.gpu.KernelHelper
import org.jocl.CL
import org.jocl.Pointer
import org.jocl.Sizeof
import org.jocl.cl_context
import org.jocl.cl_device_id
import org.jocl.cl_mem
import org.jocl.cl_program
import sensors.cams.kinect.KinectConstant
import sensors.cams.kinect.KinectConstant.KINECT_DEPTH_COLS
import sensors.cams.kinect.KinectConstant.KINECT_DEPTH_ROWS
import kotlin.math.tan

object JoclCloudCreator {
    private var program: cl_program? = null
    private var context: cl_context? = null

    var xTanMasMem: cl_mem? = null
    var yTanMasMem: cl_mem? = null
    var tariMasMem: cl_mem? = null

    fun init(context: cl_context) {
        this.context = context

        val xTanMas = FloatArray(KINECT_DEPTH_COLS)
        for(i in xTanMas.indices) {
            xTanMas[i] = calcTan(i, KINECT_DEPTH_COLS, KinectConstant.KINECT_DEPTH_DG_HORIZONTAL)
        }
        xTanMasMem = createMem(context, xTanMas)

        val yTanMas = FloatArray(KINECT_DEPTH_ROWS)
        for(i in yTanMas.indices) {
            yTanMas[i] = calcTan(i, KINECT_DEPTH_ROWS, KinectConstant.KINECT_DEPTH_DG_VERTICAL)
        }
        yTanMasMem = createMem(context, yTanMas)

        tariMasMem = createMem(context, TariInitor.getTariMass())

        val src = """
            __kernel void gatherDepthToXYZ(
                __global const uchar* depthBytes,   // depth bytes 2*size
                __global const float* xTanMas,      // length cols
                __global const float* yTanMas,      // length rows
                __global const float* tariMas,      // length tariLen (>= maxIndex+1)
                const int cols,
                const int rows,
                const int depthMax,                 // KinectConstant.KINECT_DEPTH_MAX
                const float depthNull,              // KinectConstant.KINECT_DEPTH_MM_NULL as float
                __global float* outX,
                __global float* outY,
                __global float* outZ
            ) {
                int gid = get_global_id(0);
                int size = cols * rows;
                if (gid >= size) return;

                // read two bytes per sample
                int b0 = depthBytes[gid * 2];      // uchar promoted to int (0..255)
                int b1 = depthBytes[gid * 2 + 1];  // uchar

                // compute kiRes = (b0 & 0xFF) | ((b1 & 7) << 8)
                // since depthBytes are uchar, masking b0 with 0xFF is redundant but kept for clarity
                int kiRes = (b0 & 0xFF) | ((b1 & 7) << 8);

                if (kiRes > depthMax) {
                    outX[gid] = depthNull;
                    outY[gid] = depthNull;
                    outZ[gid] = depthNull;
                    return;
                }

                float mmRes = tariMas[kiRes];

                int cx = gid % cols;
                int cy = gid / cols;

                // note: original formula uses xTanMas[i % KINECT_DEPTH_COLS] and yTanMas[i / KINECT_DEPTH_COLS]
                float x = xTanMas[cx] * mmRes;
                float y = yTanMas[cy] * mmRes;
                float z = mmRes;

                outX[gid] = x;
                outY[gid] = y;
                outZ[gid] = z;
            }
        """.trimIndent()

        program = CL.clCreateProgramWithSource(context, 1, arrayOf(src), null, null)
        CL.clBuildProgram(program, 0, null, null, null, null)
    }

    fun runKernel(device: cl_device_id, ofSensor: ByteArray) : GpuCloud {
        val kh = KernelHelper(context!!, program!!, device, "gatherDepthToXYZ")

        kh.setKernelBytearrCopy(ofSensor)
        kh.setKernelArgMem(xTanMasMem!!)
        kh.setKernelArgMem(yTanMasMem!!)
        kh.setKernelArgMem(tariMasMem!!)
        kh.setKernelIntArg(KINECT_DEPTH_COLS)
        kh.setKernelIntArg(KINECT_DEPTH_ROWS)
        kh.setKernelIntArg(KinectConstant.KINECT_DEPTH_MAX)
        kh.setKernelFloatArg(KinectConstant.KINECT_DEPTH_MM_NULL)
        val outXMem = kh.setKernelFloatarrArgWriteOnly(KINECT_DEPTH_COLS*KINECT_DEPTH_ROWS)
        val outYMem = kh.setKernelFloatarrArgWriteOnly(KINECT_DEPTH_COLS*KINECT_DEPTH_ROWS)
        val outZMem = kh.setKernelFloatarrArgWriteOnly(KINECT_DEPTH_COLS*KINECT_DEPTH_ROWS)

        kh.runKernel(KINECT_DEPTH_COLS*KINECT_DEPTH_ROWS)

        val res = GpuCloud().loadCloud(outXMem, outYMem, outZMem, KINECT_DEPTH_COLS*KINECT_DEPTH_ROWS)

        kh.release()
        return res
    }

    private fun createMem(context: cl_context?, arr: FloatArray) = CL.clCreateBuffer(context,
        CL.CL_MEM_READ_ONLY or CL.CL_MEM_COPY_HOST_PTR,
        Sizeof.cl_float * arr.size.toLong(), Pointer.to(arr), null)

    private fun calcTan(xy: Int, hw: Int, ug: Double): Float {
        val hw05 = hw/2
        val calcY = if(xy > hw05 - 1) xy - hw05 else xy - hw05 - 1
        val onDeg = ug / 2 / (hw05 - 1)
        val deg = onDeg * calcY
        return tan(Math.toRadians(deg)).toFloat()
    }
}