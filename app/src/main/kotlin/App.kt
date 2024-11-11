package era.app

import era.kinect.cloud.Cloud
import kinect.Kinect
import kinect.handlers.DepthHandlerImpl
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import v2d.ViewFrame
import v3d.eventlistener.V3dGlMultiEventListener.V3dComplect
import v3d.utils.ComplectFactory.createPointComplect
import v3d.utils.GLCapabilitiesCreator
import javax.swing.SwingUtilities


fun main() {
    runBlocking {
        val channel = Channel<Cloud>(Channel.CONFLATED)

        Kinect.init(DepthHandlerImpl(channel))

        start(createPointComplect(V3dKinectRunnerImpl(channel), 640*480))
    }
    println("done")
}

private fun start(complect: V3dComplect) {
    val creator = GLCapabilitiesCreator()

    val v3dComplects: MutableList<V3dComplect> = ArrayList<V3dComplect>()
    v3dComplects.add(complect)
    SwingUtilities.invokeLater(object : Runnable {
        override fun run() {
            ViewFrame(creator, v3dComplects)
        }
    })
}