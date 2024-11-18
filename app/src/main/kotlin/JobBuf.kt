package era.app

import ToViewText
import cloud.realization.BufferedChannel
import cloud.realization.CloudOfSensor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class JobBuf {
    companion object{
        fun execute(scope: CoroutineScope,
                    channelSensor: BufferedChannel<CloudOfSensor>,
                    channelToView: BufferedChannel<CloudOfSensor>,
                    channelText: Channel<ToViewText>) {
            scope.launch(Dispatchers.IO){
                while(true) {
                    scope.launch(Dispatchers.IO){
                        channelSensor.getFirstDataForRead()?.let{ readCloud ->
                            channelToView.getDataForWriteOrRewriteOld()?.let { writeCloud ->

                                doJobInternal(readCloud, writeCloud)

                                channelToView.endWrite(writeCloud)
                            }
                            channelSensor.endRead(readCloud)
                        }
                    }
                    delay(10)
                }
            }
        }

        private fun doJobInternal(inp: CloudOfSensor, outp: CloudOfSensor) {
            inp.editData { glob, id ->
                outp.setDataGlob(id, glob.x, glob.y, glob.z)
            }
        }
    }
}