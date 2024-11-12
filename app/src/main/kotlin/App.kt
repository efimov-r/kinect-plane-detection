package era.app

import ToViewText
import cloud.realization.InCloud
import cloud.realization.ToViewCloud
import era.utils.files.KiLogger
import kinect.Kinect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext


fun main() {
    KiLogger.inf("main start")
    runBlocking {
        val channelSensor = Channel<InCloud>(Channel.CONFLATED)
        val channelToView = Channel<ToViewCloud>(Channel.CONFLATED)
        val channelText = Channel<ToViewText>(Channel.CONFLATED)

        Kinect.init(channelSensor)
        launch{
            mainJob(channelSensor, channelToView, channelText)
        }
        ViewInitor.init(channelToView, channelText)
    }
}

private suspend fun mainJob(channelSensor: Channel<InCloud>, channelToView: Channel<ToViewCloud>, channelText: Channel<ToViewText>) {
//    withContext(Dispatchers.Default){
//        launch{
            var toViewCloud: ToViewCloud? = null
            for(sens in channelSensor) {
                toViewCloud = ToViewCloud(sens.size)
                for (i in 0..<sens.globs.size) {
                    toViewCloud.setGlob(i, sens.globs.get(i)!!)
                }
                channelToView.trySend(toViewCloud)
                channelText.trySend(ToViewText(sens.globs.get(2000)!!.z.toString()))
            }
//        }
//    }
}