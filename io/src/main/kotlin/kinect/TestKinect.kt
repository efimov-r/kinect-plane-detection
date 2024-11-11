package kinect

import kinect.KinectDepthTestMatCreator.createDepthMat
import kinect.freenect.DepthFormat
import kinect.freenect.Freenect
import kinect.freenect.VideoFormat
import kinect.handlers.DepthHandlerImpl
import kinect.handlers.DepthHandlerImpl.KinnectDepthCallback
import kinect.handlers.VideoHandlerImpl.Companion.createRgbMat
import org.opencv.core.Core
import org.opencv.highgui.HighGui


object TestKinect {
    fun execute() {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        
        val tmpContext = Freenect.createContext()
        tmpContext.selectSubdev()
        val device = tmpContext.openDevice(0)

        // Установка форматов
        device.setVideoFormat(VideoFormat.RGB)
        device.setDepthFormat(DepthFormat.D11BIT)

        // Буферы для хранения изображений
//        Mat rgbImage = VideoHandlerImpl.createRgbMat();
//        Mat depthImage = DepthHandlerImplTest.createDepthMat();
//        VideoHandler vh = new VideoHandlerImpl(rgbImage);
//        DepthHandlerImplTest dh = new DepthHandlerImplTest(depthImage);
        val depthMat = createDepthMat()
        val rgbMap = createRgbMat()

        val dh = DepthHandlerImpl(KinnectDepthCallback { depthMmData: IntArray? ->
            KinectDepthTestMatCreator.createMap(depthMat, depthMmData!!)
            HighGui.imshow("Depth Image", depthMat)
            HighGui.waitKey(3)
        })
//        val vh = VideoHandlerImpl(rgbMap) {
//            img -> HighGui.imshow("Video Image", img)
//            HighGui.waitKey(3)
//        }

//        device.startVideo(vh);
        device.startDepth(dh)

        var i = 0
        while (true) {
            i++
            i--
        }

        //        // Основной цикл
//        while (true) {
        /**/            // Получаем RGB изображение
        //            rgbImage.put(0, 0, device.getVideo());
        //            HighGui.imshow("RGB Image", rgbImage); */
//            HighGui.imshow("Depth Image", depthImage);
        /**/
        //            // Получаем карту глубины
        //            depthImage.put(0, 0, device.getDepth());
        //            HighGui.imshow("Depth Image", depthImage); */
//
//            // Обработка событий окна
//            if (HighGui.waitKey(30) >= 0) {
//                break; // Прерываем цикл при нажатии клавиши
//            }
//        }
//
//        // Закрываем устройство и освобождаем ресурсы
//        device.close();
//        tmpContext.shutdown();
//        HighGui.destroyAllWindows();
    }
}
