package era.app

import ToViewText
import cloud.realization.BufferedChannel
import cloud.realization.CloudOfSensor
import era.utils.files.KiLogger
import kinect.Kinect
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking


fun main() {
    KiLogger.inf("main start")
    runBlocking {
        val channelText = Channel<ToViewText>(Channel.CONFLATED)

        val formSensorChannel = BufferedChannel {
            return@BufferedChannel CloudOfSensor(640*480, it)
        }

        val toViewChannel = BufferedChannel {
            return@BufferedChannel CloudOfSensor(640*480, it)
        }

        Kinect.init(formSensorChannel)
        JobBuf.execute(this, formSensorChannel, toViewChannel, channelText)
        ViewInitor.init(this, toViewChannel, channelText)

    }
}