package sensors.cams.kinect

import cloud.BufferedChannel
import cloud.databuffer.CloudOfSensor
import era.utils.files.KiLogger
import sensors.cams.Cloudsorce
import sensors.cams.kinect.freenect.DepthFormat
import sensors.cams.kinect.freenect.Device
import sensors.cams.kinect.freenect.Freenect.createContext
import sensors.cams.kinect.handlers.DepthHandlerImpl

class Kinect(channel: BufferedChannel<CloudOfSensor>): Cloudsorce {

    private var device: Device? = null
    private val dhi: DepthHandlerImpl = DepthHandlerImpl(channel)

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
        } catch (ex: Exception) {
            KiLogger.err(ex)
        }
    }

    override fun stopDepth() {
        try {
            device?.stopDepth()
        } catch (ex: Exception) {
            KiLogger.err(ex)
        }
    }
}