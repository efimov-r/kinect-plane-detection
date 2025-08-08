package sensors.cams.kinect.util

import sensors.cams.kinect.KinectConstant
import kotlin.math.tan

class ToCloud private constructor() {
    private val xTanMas: DoubleArray = DoubleArray(KinectConstant.KINECT_DEPTH_COLS)
    private val yTanMas: DoubleArray = DoubleArray(KinectConstant.KINECT_DEPTH_ROWS)

    init {
        for(i in xTanMas.indices) {
            xTanMas[i] = calcTan(i, KinectConstant.KINECT_DEPTH_COLS, KinectConstant.KINECT_DEPTH_DG_HORIZONTAL)
        }
        for(i in yTanMas.indices) {
            yTanMas[i] = calcTan(i, KinectConstant.KINECT_DEPTH_ROWS, KinectConstant.KINECT_DEPTH_DG_VERTICAL)
        }
    }

    fun calcXFast(mm: Double, x: Int): Double {
        return xTanMas[x] * mm
    }

    fun calcYFast(mm: Double, y: Int): Double {
        return xTanMas[y] * mm
    }

    private fun calcTan(xy: Int, hw: Int, ug: Double): Double {
        val hw05 = hw/2
        val calcY = if(xy > hw05 - 1) xy - hw05 else xy - hw05 - 1
        val onDeg = ug / 2 / (hw05 - 1)
        val deg = onDeg * calcY
        return tan(Math.toRadians(deg.toDouble()))
    }

    companion object {
        private var instance: ToCloud? = null

        fun getInstance(): ToCloud {
            if(instance == null) {
                instance = ToCloud()
            }
            return instance!!
        }
    }
}