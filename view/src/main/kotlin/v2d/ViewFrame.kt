package v2d

import v3d.GlComponentForView
import v3d.GlComponentForView.GlComponentCallback
import v3d.eventlistener.V3dGlMultiEventListener.V3dComplect
import v3d.utils.GLCapabilitiesCreator
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Frame
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextArea

class ViewFrame(glCapabilitiesCreator: GLCapabilitiesCreator, v3dComplects: MutableList<V3dComplect>) {
    private val textArea: JTextArea

    init {
        textArea = JTextArea()

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
        frame.add(createPanel(), BorderLayout.EAST)
        frame.pack()
        frame.setVisible(true)
        frame.setTitle("It is title")
//        frame.setSize(frame.maximumSize.width, frame.maximumSize.height)
        frame.setSize(2000, 1200)
        glComponentForView.requestFocus()
    }

    fun setText(text: String) {
        textArea.text = text
    }

    fun createPanel(): JPanel {
        textArea.setEditable(false)
        textArea.setLineWrap(true)
        textArea.setWrapStyleWord(true)

        val textPanel = JPanel(BorderLayout())
        textPanel.setLayout(BorderLayout())
        textPanel.add(JScrollPane(textArea), BorderLayout.CENTER)
        textPanel.setPreferredSize(Dimension(200, 0)) // Фиксированная ширина
        return textPanel
    }
}
