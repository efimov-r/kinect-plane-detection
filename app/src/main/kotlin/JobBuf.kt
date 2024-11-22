package era.app

import cloud.entities.ToViewText
import cloud.BufferedChannel
import cloud.databuffer.CloudForView
import cloud.databuffer.CloudOfSensor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class JobBuf {
    companion object{
        fun execute(scope: CoroutineScope,
                    channelSensor: BufferedChannel<CloudOfSensor>,
                    channelToView: BufferedChannel<CloudForView>,
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

        private fun doJobInternal(inp: CloudOfSensor, outp: CloudForView) {
            outp.pointData.clear()
            inp.editData { glob, id ->
                outp.pointData.add {
                    it.z = glob.z.toFloat()/ AppConstants.MAX_MM_ON_V3D
                    it.x = glob.x.toFloat()/ AppConstants.MAX_MM_ON_V3D
                    it.y = glob.y.toFloat()/ AppConstants.MAX_MM_ON_V3D
                }
            }
        }
    }
}