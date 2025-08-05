package era.app

import bufferchanel.BufferedChannel
import bufferchanel.databuffer.BufferedChannelForView
import bufferchanel.databuffer.BufferedCloudOfSensor
import era.utils.files.KiLogger
import keyboard.Keys
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import sensors.cams.file.CloudSerializer
import sensors.cams.kinect.Kinect
import viewdata.transferdata.ToViewText
import sensors.cams.kinect.KinectConstant.KINECT_DEPTH_COLS
import sensors.cams.kinect.KinectConstant.KINECT_DEPTH_ROWS

fun main() {
    KiLogger.inf("main start")

    val channelText = Channel<ToViewText>(Channel.CONFLATED)
    val channelKeyboard = Channel<Char>(Channel.CONFLATED)

    val formSensorChannel = BufferedChannel {
        return@BufferedChannel BufferedCloudOfSensor(KINECT_DEPTH_ROWS*KINECT_DEPTH_COLS, "cloud of sensor", it)
    }

    val toViewChannel = BufferedChannel {
        return@BufferedChannel BufferedChannelForView(it)
    }

    val kinect = Kinect(formSensorChannel)
    val cloudSerializer = CloudSerializer(formSensorChannel)

    var state = State.DO_JOB

    runBlocking {
        launch(Dispatchers.IO){
            while(true) {
                when(state) {
                    State.DO_JOB -> {
                        JobBuf.execute(formSensorChannel, toViewChannel, channelText)
                    }
                    State.SAVE -> {
                        cloudSerializer.writeCloud()
                        KiLogger.inf("End save cloud to file")
                        state = State.DO_JOB
                    }
                    else -> {
                        delay(100)
                    }
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
                    if(kinect.isRun()) {
                        state = State.SAVE
                        KiLogger.inf("To save state")
                    } else {
                        KiLogger.inf("Error save: kinect is not run")
                    }
                }
            }
        }
    }

    KiLogger.inf("End init procedure")
}