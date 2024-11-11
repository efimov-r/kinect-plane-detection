package kinect

import kinect.freenect.DepthFormat
import kinect.freenect.Freenect
import kinect.freenect.VideoFormat
import kinect.handlers.VideoHandlerImpl
import kinect.handlers.VideoHandlerImpl.Companion.createRgbMat
import kinect.testUtils.DepthHandlerTestImpl
import kinect.testUtils.DepthHandlerTestImpl.KinnectDepthTestCallback
import kinect.testUtils.KinectDepthTestMatCreator
import kinect.testUtils.KinectDepthTestMatCreator.createDepthMat
import org.junit.jupiter.api.Test
import org.opencv.core.Core
import org.opencv.highgui.HighGui
import kotlin.test.assertEquals

internal class KinectTest {
    @Test
    fun testDepthCam() {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        val depthMat = createDepthMat()
        val dh = DepthHandlerTestImpl(KinnectDepthTestCallback { depthMmData ->
            KinectDepthTestMatCreator.createMap(depthMat, depthMmData!!)
            HighGui.imshow("Depth Image", depthMat)
            HighGui.waitKey(3)
        })

        val tmpContext = Freenect.createContext()
        tmpContext.selectSubdev()
        val device = tmpContext.openDevice(0)
        device.setDepthFormat(DepthFormat.D11BIT)
        device.startDepth(dh)

        Thread.sleep(1000*60*60)
        assertEquals(1, 1)
    }

    @Test
    fun testVideoCam() {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME)

        val vh = VideoHandlerImpl(createRgbMat()) {
                img -> HighGui.imshow("Video Image", img)
            HighGui.waitKey(3)
        }

        val tmpContext = Freenect.createContext()
        tmpContext.selectSubdev()
        val device = tmpContext.openDevice(0)
        device.setVideoFormat(VideoFormat.RGB)
        device.startVideo(vh);

        Thread.sleep(1000*60*60)
        assertEquals(1, 1)
    }
}