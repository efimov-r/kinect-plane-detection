package era.app

import cloud.BufferedChannel
import cloud.databuffer.CloudForView
import cloud.entities.ToViewText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import v2d.ViewFrame
import v3d.eventlistener.V3dGlMultiEventListener
import v3d.models.KinectModel
import v3d.utils.ComplectFactory.createAllComplects
import v3d.utils.ComplectFactory.createAllModelComplects
import v3d.utils.GLCapabilitiesCreator
import javax.swing.SwingUtilities

class ViewInitor {
    companion object {
        fun init(scope: CoroutineScope, channelToView: BufferedChannel<CloudForView>, channelText: Channel<ToViewText>) {
            var viewFrame: ViewFrame? = null
            val creator = GLCapabilitiesCreator()
            val eventListener = V3dGlMultiEventListener(
                createAllComplects(),
                createAllModelComplects(listOf(KinectModel(1f/AppConstants.MAX_MM_ON_V3D))),
                channelToView)
            SwingUtilities.invokeLater(object : Runnable {
                override fun run() {
                    viewFrame = ViewFrame(creator, eventListener)
                }
            })

            scope.launch(Dispatchers.IO){
                for(text in channelText) {
                    viewFrame?.setText(text.text)
                }
            }
        }
    }
}