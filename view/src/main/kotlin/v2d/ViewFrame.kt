package v2d

import v3d.GlComponentForView
import v3d.GlComponentForView.GlComponentCallback
import v3d.eventlistener.V3dGlMultiEventListener.V3dComplect
import v3d.utils.GLCapabilitiesCreator
import java.awt.BorderLayout
import java.awt.Frame
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.JFrame

class ViewFrame(glCapabilitiesCreator: GLCapabilitiesCreator, v3dComplects: MutableList<V3dComplect>) {
    init {
        val frame: Frame = JFrame("View sample")
        val glComponentForView = GlComponentForView(
            glCapabilitiesCreator.capabilities,
            GlComponentCallback { title: String? -> frame.setTitle(title) },
            v3dComplects
        )
        frame.addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                glComponentForView.runExitAnimator()
            }
        })
        frame.setLayout(BorderLayout())
        frame.add(glComponentForView.glComponent, BorderLayout.CENTER)
        frame.pack()
        frame.setVisible(true)
        frame.setTitle("It is title")
        glComponentForView.requestFocus()
    }
}
