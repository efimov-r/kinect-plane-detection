package era.app

import ToViewText
import cloud.realization.InCloud
import cloud.realization.ToViewCloud
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

class Job {
    companion object{
        fun execute(scope: CoroutineScope,
                            channelSensor: Channel<InCloud>,
                            channelToView: Channel<ToViewCloud>,
                            channelText: Channel<ToViewText>) {
            scope.launch(Dispatchers.IO){
                var toViewCloud: ToViewCloud? = null
                for(sens in channelSensor) {
                    toViewCloud = ToViewCloud(sens.size)
                    for (i in 0..<sens.globs.size) {
                        toViewCloud.setGlob(i, sens.globs.get(i)!!)
                    }
                    channelToView.trySend(toViewCloud)
                    channelText.trySend(ToViewText(sens.globs.get(2000)!!.z.toString()))
                }
            }
        }
    }
}