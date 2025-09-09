package sensors.cams.kinect.handlers

import bufferchanel.BufferedChannel
import bufferchanel.databuffer.BufferedCloudOfSensor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import sensors.cams.kinect.KinectConstant
import sensors.cams.kinect.KinectConstant.KINECT_DEPTH_COLS
import sensors.cams.kinect.KinectConstant.KINECT_DEPTH_ROWS
import sensors.cams.kinect.freenect.DepthHandler
import sensors.cams.kinect.freenect.FrameMode
import sensors.cams.kinect.util.TariInitor
import java.nio.ByteBuffer
import kotlin.math.tan

class DepthHandlerImpl(val channel: BufferedChannel<BufferedCloudOfSensor>) : DepthHandler {
    private val depthDataBytes: ByteArray = ByteArray(KINECT_DEPTH_COLS * KINECT_DEPTH_ROWS * 2)

    private val xTanMas: DoubleArray = DoubleArray(KINECT_DEPTH_COLS)
    private val yTanMas: DoubleArray = DoubleArray(KINECT_DEPTH_ROWS)

    private val tariMas: DoubleArray = TariInitor.getTariMass()

    init {
        for(i in xTanMas.indices) {
            xTanMas[i] = calcTan(i, KINECT_DEPTH_COLS, KinectConstant.KINECT_DEPTH_DG_HORIZONTAL)
        }
        for(i in yTanMas.indices) {
            yTanMas[i] = calcTan(i, KINECT_DEPTH_ROWS, KinectConstant.KINECT_DEPTH_DG_VERTICAL)
        }
    }

    override fun onFrameReceived(mode: FrameMode?, frame: ByteBuffer?, timestamp: Int) {
        frame!!.position(0).get(depthDataBytes)
        runBlocking(Dispatchers.IO){
            channel.getDataForWrite()?.let {
                var mmRes: Double

                it.clear()
                it.height = KINECT_DEPTH_ROWS
                it.width = KINECT_DEPTH_COLS
                for(i in 0..<KINECT_DEPTH_COLS * KINECT_DEPTH_ROWS){
                    val kiRes = ((depthDataBytes[i * 2].toInt() and 0xFF)) or ((depthDataBytes[i * 2 + 1].toInt() and 7) shl 8)

                    if(kiRes > KinectConstant.KINECT_DEPTH_MAX) {
                        it.add(KinectConstant.KINECT_DEPTH_MM_NULL.toFloat(), KinectConstant.KINECT_DEPTH_MM_NULL.toFloat(), KinectConstant.KINECT_DEPTH_MM_NULL.toFloat())
                    } else {
                        mmRes = tariMas[kiRes]
                        val x = xTanMas[i % KINECT_DEPTH_COLS] * mmRes
                        val y = xTanMas[i / KINECT_DEPTH_COLS] * mmRes
                        it.add(x.toFloat(), y.toFloat(), mmRes.toFloat())
                    }
                }
                channel.endWrite(it)
            }
        }
    }

    private fun calcTan(xy: Int, hw: Int, ug: Double): Double {
        val hw05 = hw/2
        val calcY = if(xy > hw05 - 1) xy - hw05 else xy - hw05 - 1
        val onDeg = ug / 2 / (hw05 - 1)
        val deg = onDeg * calcY
        return tan(Math.toRadians(deg))
    }
}
