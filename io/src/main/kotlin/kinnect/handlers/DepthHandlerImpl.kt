package kinnect.handlers

import kinnect.Constant.KINNECT_DEPTH_COLS
import kinnect.Constant.KINNECT_DEPTH_ROWS
import kinnect.freenect.DepthHandler
import kinnect.freenect.FrameMode
import kinnect.tarir.Tari.Companion.getTari
import java.nio.ByteBuffer
import java.util.*

class DepthHandlerImpl(private val callback: KinnectDepthCallback) : DepthHandler {
    var depthDataBytes: ByteArray
    var depthMmData: IntArray

    fun interface KinnectDepthCallback {
        fun callback(depthMmData: IntArray?)
    }

    init {
        this.depthDataBytes = ByteArray(KINNECT_DEPTH_COLS * KINNECT_DEPTH_ROWS * 2)
        this.depthMmData = IntArray(KINNECT_DEPTH_COLS * KINNECT_DEPTH_ROWS)
    }

    override fun onFrameReceived(mode: FrameMode?, frame: ByteBuffer?, timestamp: Int) {
        frame!!.position(0).get(depthDataBytes)
        for (i in 0..<KINNECT_DEPTH_COLS * KINNECT_DEPTH_ROWS) {
            depthMmData[i] =
                getTari(((depthDataBytes[i * 2].toInt() and 0xFF)) or ((depthDataBytes[i * 2 + 1].toInt() and 7) shl 8))
        }
        callback.callback(depthMmData)
    }
}
