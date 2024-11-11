package kinect.handlers

import era.kinect.cloud.Cloud
import era.kinect.cloud.Glob
import era.kinect.cloud.ToCloud
import kinect.Constant.KINECT_DEPTH_COLS
import kinect.Constant.KINECT_DEPTH_ROWS
import kinect.freenect.DepthHandler
import kinect.freenect.FrameMode
import kinect.tarir.Tari.Companion.getTari
import java.nio.ByteBuffer
import java.util.*

class DepthHandlerImpl(private val callback: KinnectDepthCallback) : DepthHandler {
    val depthDataBytes: ByteArray = ByteArray(KINECT_DEPTH_COLS * KINECT_DEPTH_ROWS * 2)
    val depthMmData: IntArray = IntArray(KINECT_DEPTH_COLS * KINECT_DEPTH_ROWS)
    val cloud: Cloud = Cloud()


    fun interface KinnectDepthCallback {
        fun callback(depthMmData: IntArray?)
    }

    override fun onFrameReceived(mode: FrameMode?, frame: ByteBuffer?, timestamp: Int) {
        frame!!.position(0).get(depthDataBytes)
        val start: Long = Date().time
        for (i in 0..<KINECT_DEPTH_COLS * KINECT_DEPTH_ROWS) {
            depthMmData[i] =
                getTari(((depthDataBytes[i * 2].toInt() and 0xFF)) or ((depthDataBytes[i * 2 + 1].toInt() and 7) shl 8))

            val y = i / KINECT_DEPTH_COLS
            val x = i % KINECT_DEPTH_COLS

            cloud.setGlob(x, y, Glob(x, y, depthMmData[i],
                ToCloud.getInstance().calcYFast(depthMmData[i], y),
                ToCloud.getInstance().calcXFast(depthMmData[i], x)))
        }

        println("time: ${Date().time - start}")

        callback.callback(depthMmData)
    }
}
