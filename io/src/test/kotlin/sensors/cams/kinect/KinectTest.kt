package sensors.cams.kinect

import org.junit.jupiter.api.Test
import org.opencv.core.Core
import org.opencv.highgui.HighGui
import sensors.cams.kinect.freenect.DepthFormat
import sensors.cams.kinect.freenect.VideoFormat
import sensors.cams.kinect.handlers.VideoHandlerImpl
import sensors.cams.kinect.handlers.VideoHandlerImpl.Companion.createRgbMat
import sensors.cams.kinect.testUtils.DepthHandlerTestImpl
import sensors.cams.kinect.testUtils.KinectDepthTestMatCreator
import sensors.cams.kinect.testUtils.KinectDepthTestMatCreator.createDepthMat
import kotlin.test.assertEquals

internal class KinectTest {
    @Test
    fun testDepthCam() {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME)

        val depthMat = createDepthMat()
        val dh = DepthHandlerTestImpl(DepthHandlerTestImpl.KinnectDepthTestCallback { depthMmData ->
            KinectDepthTestMatCreator.createMap(depthMat, depthMmData!!)
            HighGui.imshow("Depth Image", depthMat)
            HighGui.waitKey(3)
        })

        val tmpContext = _root_ide_package_.sensors.cams.kinect.freenect.Freenect.createContext()
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

        val tmpContext = _root_ide_package_.sensors.cams.kinect.freenect.Freenect.createContext()
        tmpContext.selectSubdev()
        val device = tmpContext.openDevice(0)
        device.setVideoFormat(VideoFormat.RGB)
        device.startVideo(vh);

        Thread.sleep(1000*60*60)
        assertEquals(1, 1)
    }
}