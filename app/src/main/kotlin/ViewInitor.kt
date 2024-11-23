package era.app

import cloud.BufferedChannel
import cloud.databuffer.CloudForView
import cloud.entities.ToViewText
import kinect.KinectConstant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import models.KinectModel
import v2d.ViewFrame
import v3d.eventlistener.V3dComplect
import v3d.eventlistener.V3dGlMultiEventListener
import v3d.eventlistener.V3dModelComplect
import v3d.utils.ComplectFactory.createModelComplect
import v3d.utils.ComplectFactory.createPointComplect
import v3d.utils.GLCapabilitiesCreator
import javax.swing.SwingUtilities

class ViewInitor {
    companion object {
        fun init(scope: CoroutineScope, channelToView: BufferedChannel<CloudForView>, channelText: Channel<ToViewText>) {

            val complects: MutableList<V3dComplect> = ArrayList()
            complects.add(createPointComplect(V3dCloudRunnerImpl(), KinectConstant.KINECT_DEPTH_COLS * KinectConstant.KINECT_DEPTH_ROWS))

            val modelComplects: MutableList<V3dModelComplect> = ArrayList()
            modelComplects.add(createModelComplect(KinectModel(1f/AppConstants.MAX_MM_ON_V3D)))

            var viewFrame: ViewFrame? = null
            val creator = GLCapabilitiesCreator()
            val eventListener = V3dGlMultiEventListener(complects, modelComplects, channelToView)
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