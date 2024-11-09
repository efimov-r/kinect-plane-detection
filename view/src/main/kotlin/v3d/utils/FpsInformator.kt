package v3d.utils

import v3d.eventlistener.CallbackJkiEventListener


class FpsInformator {
    private var prevTimeNS: Long = -1
    private var step = 0

    fun informate(callback: CallbackJkiEventListener) {
        step++
        val currentTime = System.nanoTime()
        if (prevTimeNS == -1L) {
            prevTimeNS = currentTime
        }
        val diff = currentTime - prevTimeNS
        if (diff > 1e9) {
            val fps = (diff / 1e9) * step
            var t = "JOCL / JOGL interaction sample - "
            t += " mode: " + String.format("%.2f", fps) + " FPS"
            callback.setTitle(t)
            prevTimeNS = currentTime
            step = 0
        }
    }
}
