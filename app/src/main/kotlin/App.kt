package era.app

import cloud.entities.ToViewText
import cloud.BufferedChannel
import cloud.databuffer.CloudForView
import cloud.databuffer.CloudOfSensor
import era.utils.files.KiLogger
import keyboard.Keys
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import sensors.cams.file.CloudSerializer
import sensors.cams.kinect.Kinect
import java.security.Key

fun main() {
    KiLogger.inf("main start")

    val channelText = Channel<ToViewText>(Channel.CONFLATED)
    val channelKeyboard = Channel<Char>(Channel.CONFLATED)

    val formSensorChannel = BufferedChannel {
        return@BufferedChannel CloudOfSensor(640*480, it)
    }

    val toViewChannel = BufferedChannel {
        return@BufferedChannel CloudForView(it)
    }

    val kinect = Kinect(formSensorChannel)
    val cloudSerializer = CloudSerializer(formSensorChannel)

    var state = State.DO_JOB

    runBlocking {
        launch(Dispatchers.IO){
            while(true) {
                if(state == State.DO_JOB) {
                    JobBuf.execute(formSensorChannel, toViewChannel, channelText)
                } else if(state == State.SAVE) {
                    formSensorChannel.getFirstDataForRead()?.let {
                        cloudSerializer.writeCloud(it)
                        formSensorChannel.endWrite(it)
                        KiLogger.inf("End save cloud to file")
                    }
                    state = State.DO_JOB
                } else {
                    delay(100)
                }
            }
        }

        ViewInitor.init(this, toViewChannel, channelText, channelKeyboard)

        launch(Dispatchers.IO) {
            for (c in channelKeyboard) {
                if(Keys.K_KEY.isThis(c)) {
                    cloudSerializer.stopDepth()
                    kinect.startDepth()
                    KiLogger.inf("Kinect mode")
                }
                if(Keys.F_KEY.isThis(c)) {
                    kinect.stopDepth()
                    cloudSerializer.startDepth()
                    KiLogger.inf("from file mode")
                }
                if(Keys.P_KEY.isThis(c)) {
                    if(state == State.PAUSE) {
                        state = State.DO_JOB
                        KiLogger.inf("To job state")
                    } else {
                        state = State.PAUSE
                        KiLogger.inf("To pause state")
                    }
                }
                if(Keys.S_KEY.isThis(c)) {
                    state = State.SAVE
                    KiLogger.inf("To save state")
                }
            }
        }
    }

    KiLogger.inf("End init procedure")
}