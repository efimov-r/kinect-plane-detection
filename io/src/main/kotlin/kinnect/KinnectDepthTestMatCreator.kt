package kinnect

import kinnect.Constant.KINNECT_DEPTH_COLS
import kinnect.Constant.KINNECT_DEPTH_MM_NULL
import kinnect.Constant.KINNECT_DEPTH_ROWS
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc

object KinnectDepthTestMatCreator {
    @JvmStatic
    fun createDepthMat(): Mat {
        return Mat(KINNECT_DEPTH_ROWS, KINNECT_DEPTH_COLS, CvType.CV_8UC3)
    }

    @JvmStatic
    fun createMap(depthImage: Mat, depthMmData: IntArray) {
        val rgbData = ByteArray(KINNECT_DEPTH_COLS * KINNECT_DEPTH_ROWS * 3)
        for (i in 0..<640 * 480) {
            if (depthMmData[i] == KINNECT_DEPTH_MM_NULL) {
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

        //        addPoitn4x(rgbData, 0, 239);
//        addPoitn4x(rgbData, 628, 239);
//        addPoitn4x(rgbData, 319, 0);
//        addPoitn4x(rgbData, 319, 478);
        depthImage.put(0, 0, rgbData)

        addText(depthImage, depthMmData, 319, 239)
        //        addText(depthImage, depthMmData, 0, 239);
//        addText(depthImage, depthMmData, 628, 239);
//        addText(depthImage, depthMmData, 319, 0);
//        addText(depthImage, depthMmData, 319, 478);
    }

    private fun addPoitn4x(rgbData: ByteArray, x: Int, y: Int) {
        var pos = 640 * y + x
        rgbData[pos * 3] = (0).toByte()
        rgbData[pos * 3 + 1] = (0).toByte()
        rgbData[pos * 3 + 2] = (255).toByte()

        pos = 640 * y + (x + 1)
        rgbData[pos * 3] = (0).toByte()
        rgbData[pos * 3 + 1] = (0).toByte()
        rgbData[pos * 3 + 2] = (255).toByte()

        pos = 640 * (y + 1) + x
        rgbData[pos * 3] = (0).toByte()
        rgbData[pos * 3 + 1] = (0).toByte()
        rgbData[pos * 3 + 2] = (255).toByte()

        pos = 640 * (y + 1) + (x + 1)
        rgbData[pos * 3] = (0).toByte()
        rgbData[pos * 3 + 1] = (0).toByte()
        rgbData[pos * 3 + 2] = (255).toByte()
    }

    private fun addText(depthImage: Mat, depthMmData: IntArray, x: Int, y: Int) {
        var avr = 0
        var pos = 640 * y + x
        avr += depthMmData[pos]

        pos = 640 * y + (x + 1)
        avr += depthMmData[pos]

        pos = 640 * (y + 1) + x
        avr += depthMmData[pos]

        pos = 640 * (y + 1) + (x + 1)
        avr += depthMmData[pos]


        val textColor = Scalar(0.0, 0.0, 255.0)

        val textX: Int
        val textY: Int
        if (x > 600) {
            textX = x - 60
        } else {
            textX = x + 10
        }

        if (y < 20) {
            textY = y + 20
        } else if (y > 460) {
            textY = y - 20
        } else {
            textY = y
        }

        val text = (avr / 4).toString()

        Imgproc.putText(
            depthImage, text,
            Point(textX.toDouble(), textY.toDouble()), Imgproc.FONT_HERSHEY_SIMPLEX,
            2.0, textColor, 2
        )
    }
}
