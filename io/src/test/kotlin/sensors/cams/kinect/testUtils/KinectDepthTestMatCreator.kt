package sensors.cams.kinect.testUtils

import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import sensors.cams.kinect.KinectConstant

object KinectDepthTestMatCreator {
    @JvmStatic
    fun createDepthMat(): Mat {
        return Mat(KinectConstant.KINECT_DEPTH_ROWS, KinectConstant.KINECT_DEPTH_COLS, CvType.CV_8UC3)
    }

    @JvmStatic
    fun createMap(depthImage: Mat, depthMmData: IntArray) {
        val rgbData = ByteArray(KinectConstant.KINECT_DEPTH_COLS * KinectConstant.KINECT_DEPTH_ROWS * 3)
        for (i in 0..<640 * 480) {
            if (depthMmData[i] == KinectConstant.KINECT_DEPTH_MM_NULL) {
                rgbData[i * 3] = (0).toByte()
                rgbData[i * 3 + 1] = (255).toByte()
                rgbData[i * 3 + 2] = (0).toByte()
            } else {
                rgbData[i * 3] = ((depthMmData[i] - 452) / 25).toByte()
                rgbData[i * 3 + 1] = ((depthMmData[i] - 452) / 25).toByte()
                rgbData[i * 3 + 2] = ((depthMmData[i] - 452) / 25).toByte()
            }
        }

        addPoitn4x(rgbData, 319, 239)

        addPoitn4x(rgbData, 10, 239);
        addPoitn4x(rgbData, 628, 239);
//        addPoitn4x(rgbData, 319, 0);
//        addPoitn4x(rgbData, 319, 478);
        depthImage.put(0, 0, rgbData)

        addText(depthImage, depthMmData, 319, 239)
        addText(depthImage, depthMmData, 10, 239)
        addText(depthImage, depthMmData, 628, 239)

        val lp = getAvrVal(depthMmData, 10, 239)
        val rp = getAvrVal(depthMmData, 628, 239)
        val cp = getAvrVal(depthMmData, 319, 239)

//        println( (lp+rp)/2 - cp )
//        addText(depthImage, depthMmData, 319, 0);
//        addText(depthImage, depthMmData, 319, 478);
    }

    private fun addPoitn4x(rgbData: ByteArray, x: Int, y: Int) {
        addPoint(rgbData, 640 * y + x)
        addPoint(rgbData, 640 * y + (x + 1))
        addPoint(rgbData, 640 * (y + 1) + x)
        addPoint(rgbData, 640 * (y + 1) + (x + 1))
    }

    private fun addPoint(rgbData: ByteArray, pos: Int) {
        rgbData[pos * 3] = (0).toByte()
        rgbData[pos * 3 + 1] = (0).toByte()
        rgbData[pos * 3 + 2] = (255).toByte()
    }

    private fun getAvrVal(depthMmData: IntArray, x: Int, y: Int) : Int {
        val avr = depthMmData[640 * y + x] +
                depthMmData[640 * y + (x + 1)] +
                depthMmData[640 * (y + 1) + x] +
                depthMmData[640 * (y + 1) + (x + 1)]
        return avr / 4
    }

    private fun addText(depthImage: Mat, depthMmData: IntArray, x: Int, y: Int) {
        val text = (getAvrVal(depthMmData, x, y)).toString()

        val textColor = Scalar(0.0, 0.0, 255.0)

        val textX: Int =
            if(x > 600) x-60 else x+10

        val textY: Int =
            if (y < 20) {
                y + 20
            } else if (y > 460) {
                y - 20
            } else {
                y
            }

        Imgproc.putText(
            depthImage, text,
            Point(textX.toDouble(), textY.toDouble()), Imgproc.FONT_HERSHEY_SIMPLEX,
            0.5, textColor, 2
        )
    }
}