package sensors.cams.kinect.handlers

import bufferchanel.BufferedChannel
import bufferchanel.databuffer.BufferedCloudOfSensor
import clouds.CloudJ
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import sensors.cams.kinect.KinectConstant.KINECT_DEPTH_COLS
import sensors.cams.kinect.KinectConstant.KINECT_DEPTH_ROWS
import sensors.cams.kinect.freenect.DepthHandler
import sensors.cams.kinect.freenect.FrameMode
import sensors.cams.kinect.util.Tari.Companion.getTari
import sensors.cams.kinect.util.ToCloud
import java.nio.ByteBuffer

class DepthHandlerImpl(val channel: BufferedChannel<BufferedCloudOfSensor>) : DepthHandler {
    private val depthDataBytes: ByteArray = ByteArray(KINECT_DEPTH_COLS * KINECT_DEPTH_ROWS * 2)

    override fun onFrameReceived(mode: FrameMode?, frame: ByteBuffer?, timestamp: Int) {
        frame!!.position(0).get(depthDataBytes)
        runBlocking(Dispatchers.IO){
            channel.getDataForWrite()?.let {
                var mmRes: Int

                it.clear()
                for(i in 0..<KINECT_DEPTH_COLS * KINECT_DEPTH_ROWS){
                    mmRes =
                        getTari(((depthDataBytes[i * 2].toInt() and 0xFF)) or ((depthDataBytes[i * 2 + 1].toInt() and 7) shl 8))

                    val x = ToCloud.getInstance().calcXFast(mmRes, i % KINECT_DEPTH_COLS).toShort()
                    val y = ToCloud.getInstance().calcYFast(mmRes, i / KINECT_DEPTH_COLS).toShort()
                    val z = mmRes.toShort()
                    it.addGlob(x, y, z)
                }
                channel.endWrite(it)
            }
        }
    }
}
