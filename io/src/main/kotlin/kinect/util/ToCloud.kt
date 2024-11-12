package kinect.util

import kinect.KinectConstant
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

    fun calcX(mm: Int, x: Int): Int {
        return (calcTan(x, KinectConstant.KINECT_DEPTH_COLS, 57.0) * mm).toInt()
    }

    fun calcY(mm: Int, y: Int): Int {
        return (calcTan(y, KinectConstant.KINECT_DEPTH_ROWS, 43.0) * mm).toInt()
    }

    fun calcXFast(mm: Int, x: Int): Int {
        return (xTanMas[x] * mm).toInt()
    }

    fun calcYFast(mm: Int, y: Int): Int {
        return (xTanMas[y] * mm).toInt()
    }

    private fun calcTan(xy: Int, hw: Int, ug: Double): Double {
        val hw05 = hw/2
        val calcY = if(xy > hw05 - 1) xy - hw05 else xy - hw05 - 1
        val onDeg = ug / 2 / (hw05 - 1)
        val deg = onDeg * calcY
        return tan(Math.toRadians(deg))
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