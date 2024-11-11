package kinect.freenect

import java.nio.ByteBuffer

fun interface VideoHandler {
    fun onFrameReceived(mode: FrameMode?, frame: ByteBuffer?, timestamp: Int)
}