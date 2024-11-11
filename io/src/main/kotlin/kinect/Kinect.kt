package kinect

import kinect.freenect.DepthFormat
import kinect.freenect.DepthHandler
import kinect.freenect.Freenect

class Kinect {
    companion object {
        fun init(depthHandler: DepthHandler) {
            val tmpContext = Freenect.createContext()
            tmpContext.selectSubdev()
            val device = tmpContext.openDevice(0)
            device.setDepthFormat(DepthFormat.D11BIT)
            device.startDepth(depthHandler)
        }
    }
}