package sensors.cams.kinect

import bufferchanel.BufferedChannel
import bufferchanel.databuffer.BufferedCloudOfSensor
import era.utils.files.KiLogger
import sensors.cams.Cloudsorce
import sensors.cams.kinect.freenect.DepthFormat
import sensors.cams.kinect.freenect.Device
import sensors.cams.kinect.freenect.Freenect.createContext
import sensors.cams.kinect.handlers.DepthHandlerImpl

class Kinect(channel: BufferedChannel<BufferedCloudOfSensor>): Cloudsorce {

    private var device: Device? = null
    private val dhi: DepthHandlerImpl = DepthHandlerImpl(channel)
    private var isRun = false

    init {
        try {
            val tmpContext = createContext()
            tmpContext.selectSubdev()
            device = tmpContext.openDevice(0)
            device?.setDepthFormat(DepthFormat.D11BIT)
        } catch (ex: Exception) {
            KiLogger.err(ex)
        }
    }

    override fun startDepth() {
        try {
            device?.startDepth(dhi)
            isRun = true
        } catch (ex: Exception) {
            KiLogger.err(ex)
        }
    }

    override fun stopDepth() {
        try {
            isRun = false
            device?.stopDepth()
        } catch (ex: Exception) {
            KiLogger.err(ex)
        }
    }

    override fun isRun(): Boolean {
        return isRun
    }
}