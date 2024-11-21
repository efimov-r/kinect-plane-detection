package v3d.utils


class FpsInformator(val callback: (text: String) -> Unit) {
    private var prevTimeNS: Long = -1
    private var step = 0

    fun informate() {
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
            callback(t)
            prevTimeNS = currentTime
            step = 0
        }
    }
}
