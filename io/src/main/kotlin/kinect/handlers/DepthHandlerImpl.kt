package kinect.handlers

import cloud.realization.InCloud
import era.kinect.cloud.Glob
import kinect.util.ToCloud
import kinect.KinectConstant.KINECT_DEPTH_COLS
import kinect.KinectConstant.KINECT_DEPTH_ROWS
import kinect.freenect.DepthHandler
import kinect.freenect.FrameMode
import kinect.util.Tari.Companion.getTari
import java.nio.ByteBuffer
import kotlinx.coroutines.channels.Channel

class DepthHandlerImpl(val channel: Channel<InCloud>) : DepthHandler {
    private val depthDataBytes: ByteArray = ByteArray(KINECT_DEPTH_COLS * KINECT_DEPTH_ROWS * 2)
    private val cloud: InCloud = InCloud(KINECT_DEPTH_ROWS * KINECT_DEPTH_COLS)

    override fun onFrameReceived(mode: FrameMode?, frame: ByteBuffer?, timestamp: Int) {
        frame!!.position(0).get(depthDataBytes)

        var mmRes: Int
        for (i in 0..<KINECT_DEPTH_COLS * KINECT_DEPTH_ROWS) {
            mmRes =
                getTari(((depthDataBytes[i * 2].toInt() and 0xFF)) or ((depthDataBytes[i * 2 + 1].toInt() and 7) shl 8))

            val y = i / KINECT_DEPTH_COLS
            val x = i % KINECT_DEPTH_COLS

            cloud.setGlob(i, Glob(x, y, mmRes,
                ToCloud.getInstance().calcYFast(mmRes, y),
                ToCloud.getInstance().calcXFast(mmRes, x)))
        }
        channel.trySend(cloud)
    }
}
