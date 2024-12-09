package sensors.cams.kinect.handlers

import org.opencv.core.CvType
import org.opencv.core.Mat
import sensors.cams.kinect.freenect.FrameMode
import sensors.cams.kinect.freenect.VideoHandler
import java.nio.ByteBuffer

class VideoHandlerImpl(var rgbImage: Mat, val callback: KinnectVideoCallback) : VideoHandler {
    var rgbData: ByteArray?

    init {
        rgbData = ByteArray(COLS * ROWS * 3)
    }

    fun interface KinnectVideoCallback {
        fun callback(depthMmData: Mat?)
    }

    override fun onFrameReceived(mode: FrameMode?, frame: ByteBuffer?, timestamp: Int) {
        frame!!.position(0).get(rgbData)
        rgbImage.put(0, 0, rgbData)
        callback.callback(rgbImage)
    }

    companion object {
        private const val ROWS = 480
        private const val COLS = 640

        fun createRgbMat(): Mat {
            return Mat(ROWS, COLS, CvType.CV_8UC3)
        }
    }
}
