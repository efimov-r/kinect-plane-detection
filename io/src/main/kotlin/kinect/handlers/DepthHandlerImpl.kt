package kinect.handlers

import cloud.BufferedChannel
import cloud.databuffer.CloudOfSensor
import kinect.KinectConstant.KINECT_DEPTH_COLS
import kinect.KinectConstant.KINECT_DEPTH_ROWS
import kinect.freenect.DepthHandler
import kinect.freenect.FrameMode
import kinect.util.Tari.Companion.getTari
import kinect.util.ToCloud
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.nio.ByteBuffer

class DepthHandlerImpl(val channel: BufferedChannel<CloudOfSensor>) : DepthHandler {
    private val depthDataBytes: ByteArray = ByteArray(KINECT_DEPTH_COLS * KINECT_DEPTH_ROWS * 2)

    override fun onFrameReceived(mode: FrameMode?, frame: ByteBuffer?, timestamp: Int) {
        frame!!.position(0).get(depthDataBytes)
        runBlocking(Dispatchers.IO){
            channel.getDataForWrite()?.let {
                var mmRes = 0
                it.editData{ glob, i ->
                    mmRes =
                        getTari(((depthDataBytes[i * 2].toInt() and 0xFF)) or ((depthDataBytes[i * 2 + 1].toInt() and 7) shl 8))

                    glob.x = ToCloud.getInstance().calcXFast(mmRes, i % KINECT_DEPTH_COLS).toShort()
                    glob.y = ToCloud.getInstance().calcYFast(mmRes, i / KINECT_DEPTH_COLS).toShort()
                    glob.z = mmRes.toShort()
                }
                channel.endWrite(it)
            }
        }
    }
}
