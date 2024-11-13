package era.app

import ToViewText
import cloud.realization.InCloud
import cloud.realization.ToViewCloud
import era.utils.files.KiLogger
import kinect.Kinect
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking


fun main() {
    KiLogger.inf("main start")
    runBlocking {
        val channelSensor = Channel<InCloud>(Channel.CONFLATED)
        val channelToView = Channel<ToViewCloud>(Channel.CONFLATED)
        val channelText = Channel<ToViewText>(Channel.CONFLATED)

        Kinect.init(channelSensor)
        Job.execute(this, channelSensor, channelToView, channelText)
        ViewInitor.init(this, channelToView, channelText)
    }
}