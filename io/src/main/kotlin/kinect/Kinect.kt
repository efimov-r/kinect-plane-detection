package kinect

import cloud.realization.InCloud
import era.utils.files.KiLogger
import kinect.freenect.DepthFormat
import kinect.freenect.Freenect
import kinect.handlers.DepthHandlerImpl
import kotlinx.coroutines.channels.Channel

class Kinect {
    companion object {
        fun init(channel: Channel<InCloud>) {
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