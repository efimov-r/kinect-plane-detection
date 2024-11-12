package kinect.testUtils

import kinect.KinectConstant.KINECT_DEPTH_COLS
import kinect.KinectConstant.KINECT_DEPTH_ROWS
import kinect.freenect.DepthHandler
import kinect.freenect.FrameMode
import kinect.util.Tari.Companion.getTari
import java.nio.ByteBuffer

class DepthHandlerTestImpl(private val callback: KinnectDepthTestCallback) : DepthHandler {
    val depthDataBytes: ByteArray = ByteArray(KINECT_DEPTH_COLS * KINECT_DEPTH_ROWS * 2)
    val depthMmData: IntArray = IntArray(KINECT_DEPTH_COLS * KINECT_DEPTH_ROWS)


    fun interface KinnectDepthTestCallback {
        fun callback(depthMmData: IntArray?)
    }

    override fun onFrameReceived(mode: FrameMode?, frame: ByteBuffer?, timestamp: Int) {
        frame!!.position(0).get(depthDataBytes)

        for (i in 0..<KINECT_DEPTH_COLS * KINECT_DEPTH_ROWS) {
            depthMmData[i] =
                getTari(((depthDataBytes[i * 2].toInt() and 0xFF)) or ((depthDataBytes[i * 2 + 1].toInt() and 7) shl 8))
        }

        callback.callback(depthMmData)
    }
}
