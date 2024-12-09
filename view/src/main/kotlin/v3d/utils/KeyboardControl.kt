package v3d.utils

import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent

internal class KeyboardControl(val call: (c: Char) ->Unit) : KeyAdapter() {
    override fun keyTyped(e: KeyEvent) {
        call(e.keyChar)
    }
}