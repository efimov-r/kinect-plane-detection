package era.app

import cloud.BufferedChannel
import cloud.databuffer.CloudForView
import cloud.entities.ToViewText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import v2d.ViewFrame
import v3d.eventlistener.V3dGlMultiEventListener
import v3d.models.KinectModel
import v3d.utils.ComplectFactory.createAllComplects
import v3d.utils.ComplectFactory.createAllModelComplects
import v3d.utils.GLCapabilitiesCreator
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.SwingUtilities

class ViewInitor {
    companion object {
        fun init(scope: CoroutineScope,
                 channelToView: BufferedChannel<CloudForView>,
                 channelText: Channel<ToViewText>,
                 channelKeyboard: Channel<Char>) {
            var viewFrame: ViewFrame? = null
            val creator = GLCapabilitiesCreator()
            val eventListener = V3dGlMultiEventListener(
                createAllComplects(),
                createAllModelComplects(listOf(KinectModel(1f/AppConstants.MAX_MM_ON_V3D))),
                channelToView)
            SwingUtilities.invokeLater(object : Runnable {
                override fun run() {
                    viewFrame = ViewFrame(creator, eventListener, KeyboardControl(channelKeyboard))
                }
            })

            scope.launch(Dispatchers.IO){
                delay(1000)
                viewFrame?.setText(createFinalText(""))
                for(text in channelText) {
                    viewFrame?.setText(createFinalText(text.text))
                }
            }
        }

        private fun createFinalText(text: String) : String {
            return "K - cloud fromKinect\n" +
                    "F - cloud from file\n" +
                    "P - pause\n" +
                    "S - save to file\n" +
                    "-------------------------------\n" +
                    text
        }
    }

    class KeyboardControl(val channelKeyboard: Channel<Char>) : KeyAdapter() {
        override fun keyTyped(e: KeyEvent) {
            channelKeyboard.trySend(e.keyChar)
        }
    }
}

