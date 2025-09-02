package era.app.gpu

import org.jocl.CL
import org.jocl.cl_context
import org.jocl.cl_device_id
import org.jocl.cl_platform_id

object Gpu {
    private var devices: Array<cl_device_id?>? = null
    private var deviceIndex: Int? = null
    private var context: cl_context? = null

    fun init(platformIndex: Int = 0, deviceIndex: Int = 0) : cl_context {
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
        return context!!
    }

    fun getContext() = context!!

    fun getDevice() = (devices?.let { it[deviceIndex!!]!! })!!
}