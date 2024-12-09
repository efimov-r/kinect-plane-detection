package sensors.cams.kinect.handlers

import cloud.BufferedChannel
import cloud.databuffer.CloudOfSensor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import sensors.cams.kinect.KinectConstant
import sensors.cams.kinect.KinectConstant.KINECT_DEPTH_COLS
import sensors.cams.kinect.KinectConstant.KINECT_DEPTH_ROWS
import sensors.cams.kinect.freenect.DepthHandler
import sensors.cams.kinect.freenect.FrameMode
import sensors.cams.kinect.util.Tari.Companion.getTari
import sensors.cams.kinect.util.ToCloud
import java.nio.ByteBuffer

class DepthHandlerImpl(val channel: BufferedChannel<CloudOfSensor>) : DepthHandler {
    private val depthDataBytes: ByteArray = ByteArray(KINECT_DEPTH_COLS * KINECT_DEPTH_ROWS * 2)

    override fun onFrameReceived(mode: FrameMode?, frame: ByteBuffer?, timestamp: Int) {
        frame!!.position(0).get(depthDataBytes)
        runBlocking(Dispatchers.IO){
            channel.getDataForWrite()?.let {
                var mmRes: Int
                it.minX = Short.MAX_VALUE
                it.minY = Short.MAX_VALUE
                it.minZ = Short.MAX_VALUE
                it.maxX = Short.MIN_VALUE
                it.maxY = Short.MIN_VALUE
                it.maxZ = Short.MIN_VALUE
                it.editData{ glob, i ->
                    mmRes =
                        getTari(((depthDataBytes[i * 2].toInt() and 0xFF)) or ((depthDataBytes[i * 2 + 1].toInt() and 7) shl 8))

                    glob.x = ToCloud.getInstance().calcXFast(mmRes, i % KINECT_DEPTH_COLS).toShort()
                    glob.y = ToCloud.getInstance().calcYFast(mmRes, i / KINECT_DEPTH_COLS).toShort()
                    glob.z = mmRes.toShort()

                    if(mmRes != KinectConstant.KINECT_DEPTH_MM_NULL) {
                        if(glob.x < it.minX) {
                            it.minX = glob.x
                        } else if(glob.x > it.maxX) {
                            it.maxX = glob.x
                        }

                        if(glob.y < it.minY) {
                            it.minY = glob.y
                        } else if(glob.y > it.maxY) {
                            it.maxY = glob.y
                        }

                        if(glob.z < it.minZ) {
                            it.minZ = glob.z
                        } else if(glob.z > it.maxZ) {
                            it.maxZ = glob.z
                        }
                    }
                }
                channel.endWrite(it)
            }
        }
    }
}
