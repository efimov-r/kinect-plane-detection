package kinect

import cloud.BufferedChannel
import cloud.databuffer.CloudOfSensor
import era.utils.files.KiLogger
import kinect.freenect.DepthFormat
import kinect.freenect.Freenect
import kinect.handlers.DepthHandlerImpl

class Kinect {
    companion object {
        fun init(channel: BufferedChannel<CloudOfSensor>) {
            try {
                val tmpContext = Freenect.createContext()
                tmpContext.selectSubdev()
                val device = tmpContext.openDevice(0)
                device.setDepthFormat(DepthFormat.D11BIT)
                device.startDepth(DepthHandlerImpl(channel))
            } catch (ex: Exception) {
                KiLogger.err(ex)
            }
        }
    }
}