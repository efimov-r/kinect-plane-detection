package era.app

import ToViewText
import cloud.realization.ToViewCloud
import kinect.KinectConstant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import models.KinectModel
import v2d.ViewFrame
import v3d.eventlistener.V3dGlMultiEventListener.V3dComplect
import v3d.utils.ComplectFactory
import v3d.utils.ComplectFactory.createPointComplect
import v3d.utils.GLCapabilitiesCreator
import javax.swing.SwingUtilities

class ViewInitor {
    companion object {
        suspend fun init(channelToView: Channel<ToViewCloud>, channelText: Channel<ToViewText>) {
            var viewFrame: ViewFrame? = null

            val creator = GLCapabilitiesCreator()

            val v3dComplects: MutableList<V3dComplect> = ArrayList<V3dComplect>()
            v3dComplects.add(createPointComplect(V3dCloudRunnerImpl(channelToView), KinectConstant.KINECT_DEPTH_COLS * KinectConstant.KINECT_DEPTH_ROWS))
            v3dComplects.add(ComplectFactory.createModelComplect(KinectModel(1f/AppConstants.MAX_MM_ON_V3D)))
            SwingUtilities.invokeLater(object : Runnable {
                override fun run() {
                    viewFrame = ViewFrame(creator, v3dComplects)
                }
            })

            withContext(Dispatchers.IO){ //todo
                launch{
                    for(text in channelText) {
                        viewFrame?.setText(text.text)
                    }
                }
            }
        }
    }
}