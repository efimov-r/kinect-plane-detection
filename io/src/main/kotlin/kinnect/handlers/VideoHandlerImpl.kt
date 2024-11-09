package kinnect.handlers

import kinnect.freenect.FrameMode
import kinnect.freenect.VideoHandler
import org.opencv.core.CvType
import org.opencv.core.Mat
import java.nio.ByteBuffer

class VideoHandlerImpl(var rgbImage: Mat) : VideoHandler {
    var rgbData: ByteArray?

    init {
        rgbData = ByteArray(COLS * ROWS * 3)
    }

    override fun onFrameReceived(mode: FrameMode?, frame: ByteBuffer?, timestamp: Int) {
        frame!!.position(0).get(rgbData)
        rgbImage.put(0, 0, rgbData)
    }

    companion object {
        private const val ROWS = 480
        private const val COLS = 640

        fun createRgbMat(): Mat {
            return Mat(ROWS, COLS, CvType.CV_8UC3)
        }
    }
}
